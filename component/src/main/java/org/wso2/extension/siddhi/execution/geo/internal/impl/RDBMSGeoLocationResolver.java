/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.extension.siddhi.execution.geo.internal.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.extension.siddhi.execution.geo.api.Location;
import org.wso2.extension.siddhi.execution.geo.internal.LRUCache;
import org.wso2.extension.siddhi.execution.geo.internal.exception.GeoLocationResolverException;
import org.wso2.extension.siddhi.execution.geo.internal.utils.DatabaseUtils;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the implementation class that provides the RDBMS based approach to get country,city based on the ip.
 */
public class RDBMSGeoLocationResolver {
    private static final Log log = LogFactory.getLog(RDBMSGeoLocationResolver.class);
    private static final RDBMSGeoLocationResolver instance = new RDBMSGeoLocationResolver();

    private static final String CONFIG_KEY_CACHE_SIZE = "cacheSize";
    private static final String CONFIG_KEY_ISPERSIST_IN_DATABASE = "isPersistInDatabase";
    private static final String CONFIG_KEY_DATASOURCE = "datasource";
    private static final String DEFAULT_DATASOURCE_NAME = "GEO_LOCATION_DATA";
    private static final String CITY_NAME = "city_name";
    private static final String COUNTRY_NAME = "country_name";
    private static final String SUBDIVISION_1_NAME = "subdivision_1_name";
    private static final int DEFAULT_CACHE_SIZE = 10000;

    private AtomicBoolean isInitialized = new AtomicBoolean(false);
    private DatabaseUtils dbUtils;
    private boolean isPersistInDatabase;
    private LRUCache<String, Long> ipToLongCache;

    private static final String SQL_SELECT_LOCATION_FROM_IP = "SELECT country_name, city_name FROM " +
            "IP_LOCATION WHERE ip = ?";
    private static final String SQL_INSERT_LOCATION_INTO_TABLE = "INSERT INTO IP_LOCATION (ip,country_name," +
            "city_name) VALUES (?,?,?)";
    private static final String SQL_SELECT_LOCATION_FROM_LONG_VALUE_OF_IP = "SELECT loc.country_name,loc" +
            ".subdivision_1_name FROM BLOCKS block , LOCATION loc WHERE block.network_blocks = ? AND ? BETWEEN block" +
            ".network AND block.broadcast AND block.geoname_id=loc.geoname_id";
    private static final String SQL_SELECT_LOCATION_FROM_CIDR_OF_IP = "SELECT loc.country_name,loc.subdivision_1_name" +
            " FROM BLOCKS block , LOCATION loc WHERE block.network_cidr = ? AND block.geoname_id=loc.geoname_id";


    public static RDBMSGeoLocationResolver getInstance() {
        return instance;
    }

    public void init(ConfigReader configReader) throws GeoLocationResolverException {
        if (isInitialized.get()) {
            return;
        }
        String configCacheValue = configReader.readConfig(CONFIG_KEY_CACHE_SIZE, String.valueOf(DEFAULT_CACHE_SIZE));
        int cacheSize;
        try {
            cacheSize = Integer.parseInt(configCacheValue);
        } catch (NumberFormatException e) {
            cacheSize = DEFAULT_CACHE_SIZE;
            log.warn("The config '" + configCacheValue + "' provided in 'cacheSize' is not a valid integer. Hence " +
                    "using the default cache size '" + DEFAULT_CACHE_SIZE + "'");
        }
        isPersistInDatabase = Boolean.parseBoolean(configReader.readConfig(CONFIG_KEY_ISPERSIST_IN_DATABASE, "true"));
        ipToLongCache = new LRUCache<>(cacheSize);

        dbUtils = DatabaseUtils.getInstance();
        dbUtils.initialize(configReader.readConfig(CONFIG_KEY_DATASOURCE, DEFAULT_DATASOURCE_NAME));
        isInitialized.set(true);
    }

    public Location getLocation(String ipAddress) throws GeoLocationResolverException {
        Location location = null;
        Connection connection = null;
        try {
            connection = dbUtils.getConnection();
            if (isPersistInDatabase) {
                location = loadLocation(ipAddress, connection);
            }
            if (location == null) {
                if (!isCIDR(ipAddress)) {
                    InetAddress address = InetAddress.getByName(ipAddress);

                    if (address instanceof Inet6Address) {
                        // It's ipv6
                        // Any mapped IPv4 address in IPv6 space will also returns as Inet4Address
                        if (log.isDebugEnabled()) {
                            log.debug("Found IPv6 address which can not be resolved to location. IP Address = " +
                                    ipAddress);
                        }
                        location = getLocationFromIPv6((Inet6Address) address, connection);
                    } else if (address instanceof Inet4Address) {
                        // It's ipv4
                        location = getLocationFromLongValueOfIp(address.getHostAddress(), connection);
                    }
                } else {
                    location = getLocationFromCIDR(ipAddress, connection);
                }

                if (location != null) {
                    if (isPersistInDatabase) {
                        //Insert or update in Application Level, Rather than using DB specific query.
                        boolean autoCommitMode = connection.getAutoCommit();
                        try {
                            connection.setAutoCommit(false);
                            Location updatedLocation = loadLocation(location.getIp(), connection);
                            if (updatedLocation != null) {
                                saveLocation(location, connection);
                            }
                        } finally {
                            connection.setAutoCommit(autoCommitMode);
                        }
                    }
                }
            }
        } catch (UnknownHostException e) {
            log.error("Cannot parse the IP address : " + ipAddress, e);
        } catch (SQLException e) {
            log.error("Cannot retrieve the location from database", e);
        } finally {
            dbUtils.closeAllConnections(null, connection, null);
        }
        return location;
    }

    private Location getLocationFromLongValueOfIp(String ipAddress, Connection connection) throws
            GeoLocationResolverException {

        Location location = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(SQL_SELECT_LOCATION_FROM_LONG_VALUE_OF_IP);
            if (ipAddress != null && ipAddress.split("\\.").length >= 4) {
                statement.setString(1, ipAddress.substring(0, ipAddress.substring(0, ipAddress.lastIndexOf("."))
                        .lastIndexOf(".")));
                statement.setLong(2, getIpV4ToLong(ipAddress));
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    location = new Location(resultSet.getString(COUNTRY_NAME), resultSet.getString(SUBDIVISION_1_NAME),
                            ipAddress);
                }
            }
        } catch (SQLException e) {
            throw new GeoLocationResolverException("Cannot get the location from database", e);
        } finally {
            dbUtils.closeAllConnections(statement, null, resultSet);
        }
        return location;
    }

    private Location getLocationFromCIDR(String ipAddress, Connection connection) throws
            GeoLocationResolverException {

        Location location = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(SQL_SELECT_LOCATION_FROM_CIDR_OF_IP);
            statement.setString(1, ipAddress);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                location = new Location(resultSet.getString(COUNTRY_NAME), resultSet.getString(SUBDIVISION_1_NAME),
                        ipAddress);
            }
        } catch (SQLException e) {
            throw new GeoLocationResolverException("Cannot get the location from database", e);
        } finally {
            dbUtils.closeAllConnections(statement, null, resultSet);
        }
        return location;
    }

    /**
     * Calls external system or database database to find the IPv6 adress to location details.
     * Can be used by an extended class.
     *
     * @param address    ipv6 address
     * @param connection the Db connection to be used. Do not close this connection within this method.
     * @return null
     */
    private Location getLocationFromIPv6(Inet6Address address, Connection connection)
            throws SQLException, GeoLocationResolverException {
        return null;
    }


    private Location loadLocation(String ipAddress, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Location location = null;
        try {
            if (isPersistInDatabase) {
                statement = connection.prepareStatement(SQL_SELECT_LOCATION_FROM_IP);
                statement.setString(1, ipAddress);
                resultSet = statement.executeQuery();
            }
            if (resultSet != null && resultSet.next()) {
                location = new Location(resultSet.getString(COUNTRY_NAME), resultSet.getString(CITY_NAME),
                        ipAddress);
            }
        } finally {
            dbUtils.closeAllConnections(statement, null, resultSet);
        }
        return location;
    }

    private void saveLocation(Location location, Connection connection) throws GeoLocationResolverException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(SQL_INSERT_LOCATION_INTO_TABLE);
            statement.setString(1, location.getIp());
            statement.setString(2, location.getCountry());
            statement.setString(3, location.getCity());
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            throw new GeoLocationResolverException("Cannot save the location to database", e);
        } finally {
            DatabaseUtils.getInstance().closeAllConnections(statement, null, null);
        }
    }

    private long getIpV4ToLong(String ipAddress) {

        Long ipToLong = ipToLongCache.get(ipAddress);
        if (ipToLong == null) {
            String[] ipAddressInArray = ipAddress.split("\\.");
            long longValueOfIp = 0;
            int i = 0;
            for (String ipChunk : ipAddressInArray) {
                int power = 3 - i;
                int ip = Integer.parseInt(ipChunk);
                longValueOfIp += ip * Math.pow(256, power);
                i++;
            }
            ipToLongCache.put(ipAddress, longValueOfIp);
            ipToLong = longValueOfIp;
        }
        return ipToLong;
    }

    private boolean isCIDR(String ipAddress) {
        return (ipAddress.split("\\.").length == 4) && (ipAddress.indexOf("/") >= 1);
    }

}
