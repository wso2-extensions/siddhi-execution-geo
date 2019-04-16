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

import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.config.ConfigReader;
import org.apache.log4j.Logger;
import org.wso2.carbon.utils.StringUtils;
import org.wso2.extension.siddhi.execution.geo.api.GeoCoordinate;
import org.wso2.extension.siddhi.execution.geo.api.GeoCoordinateResolver;
import org.wso2.extension.siddhi.execution.geo.internal.exception.GeoLocationResolverException;
import org.wso2.extension.siddhi.execution.geo.internal.utils.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * The default implementation of the GeoCoordinateResolver interface. This implementation is based on "ipInfoDB" API.
 */
public class APIBasedGeoCoordinateResolver implements GeoCoordinateResolver {
    private static final Logger LOGGER = Logger.getLogger(APIBasedGeoCoordinateResolver.class);
    private String apikey;
    private URL url;
    private String locationDetails[];

    @Override
    public void init(ConfigReader configReader) throws GeoLocationResolverException {
        apikey = configReader.readConfig("apiurl", "");
        if (StringUtils.isNullOrEmpty(apikey)) {
            throw new GeoLocationResolverException("Error in reading the configuration of apiurl");
        }
    }

    @Override
    public GeoCoordinate getGeoCoordinateInfo(String ip) {
        double latitude;
        double longitude;
        ip = ip.trim();
        try {
            if (Utilities.isIpAddress(ip)) {
                url = new URL(apikey + ip);
            } else {
                LOGGER.error("The ip address : " + ip + " is invalid");
            }
        } catch (MalformedURLException e) {
            throw new SiddhiAppRuntimeException("Error in connecting to the API " +
                    "with the given key value of the API");
        }
        try (InputStreamReader inputStreamReader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String ipInformation;
            while ((ipInformation = bufferedReader.readLine()) != null) {
                locationDetails = ipInformation.split(";");
            }
            latitude = Double.parseDouble(locationDetails[8]);
            longitude = Double.parseDouble(locationDetails[9]);
        } catch (IOException e) {
            throw new SiddhiAppRuntimeException("Cannot retrieve longitute and latitude " +
                    "due to error in connecting to the API", e);
        }
        return new GeoCoordinate(latitude, longitude);
    }
}
