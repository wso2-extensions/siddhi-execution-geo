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
package org.wso2.extension.siddhi.execution.geo;

import org.wso2.extension.siddhi.execution.geo.api.GeoCoordinate;
import org.wso2.extension.siddhi.execution.geo.api.GeoCoordinateResolver;
import org.wso2.extension.siddhi.execution.geo.internal.exception.GeoLocationResolverException;
import org.wso2.extension.siddhi.execution.geo.internal.impl.GeoCoordinateResolverHolder;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is to get longitude and latitude value of login location based on ip address.
 */
@Extension(
        name = "geocoordinate",
        namespace = "geo",
        description = "The geocoordinate stream function returns the longitude and latitude" +
                " values of a location relating to a given IPV4 or IPV6 address.",
        parameters = {
                @Parameter(
                        name = "ip",
                        description = "The IP address for which the user needs the longitude and latitude values.",
                        type = {DataType.STRING})
        },
        returnAttributes = {
                @ReturnAttribute(
                        name = "longitude",
                        description = "The longitude of the location corresponding to the given IP.",
                        type = DataType.DOUBLE
                ),
                @ReturnAttribute(
                        name = "latitude",
                        description = "The latitude of the location corresponding to the given IP.",
                        type = DataType.DOUBLE
                )
        },
        systemParameter = {
                @SystemParameter(
                        name = "apiurl",
                        description = "ipInfoDB(https://www.ipinfodb.com/) provides an API to" +
                                "get information relating to an IP address based on their geolocation database." +
                                "This API provides a URL to get the latitude and longitude of a given" +
                                " IP address.",
                        defaultValue = "N/A",
                        possibleParameters = "N/A"
                )
        },
        examples = @Example(
                syntax = "define stream IpStream(ip string); " +
                        "from IpStream#geo:geocoordinate(ip) " +
                        "select latitude, longitude " +
                        "insert into OutputStream;",
                description = "This returns the longitude and the latitude of the given IPV4 or IPV6 address. " +
                        "The results for the geocoordinate(95.31.18.119) are 55.7522 and 37.6156.")
)
public class GeoCoordinateStreamFunctionProcessor extends StreamFunctionProcessor {
    private static GeoCoordinateResolver geoCoordinateResolverImpl;
    private static final String DEFAULT_GEOCOORDINATE_RESOLVER_CLASSNAME =
            "org.wso2.extension.siddhi.execution.geo.internal.impl.APIBasedGeoCoordinateResolver";

    @Override
    protected Object[] process(Object[] data) {
        throw new IllegalStateException("geocoordinate cannot execute for single data ");
    }

    @Override
    protected Object[] process(Object data) {
        String ip = data.toString();
        GeoCoordinate geoCoordinate = geoCoordinateResolverImpl.getGeoCoordinateInfo(ip);
        return new Object[]{geoCoordinate.getLatitude(), geoCoordinate.getLongitude()};
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                                   SiddhiAppContext siddhiAppContext) {
        initializeExtensionConfigs(configReader);
        if (attributeExpressionExecutors.length != 1) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to geo:geocoordinate(ip) " +
                    "function, required 1, but found " + attributeExpressionExecutors.length);
        }
        Attribute.Type attributeType = attributeExpressionExecutors[0].getReturnType();
        if (attributeType != Attribute.Type.STRING) {
            throw new SiddhiAppValidationException("Invalid parameter type found for first argument ip of " +
                    "geo:geocoordinate(ip) function, required " + Attribute.Type.STRING + ", but found " + attributeType
                    .toString());
        }
        List<Attribute> attributes = new ArrayList<Attribute>(2);
        attributes.add(new Attribute("latitude", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("longitude", Attribute.Type.DOUBLE));
        return attributes;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {

    }

    private void initializeExtensionConfigs(ConfigReader configReader)
            throws SiddhiAppCreationException, SiddhiAppValidationException {
        String geoResolverImplClassName = configReader.readConfig("geoCoordinateResolverClass",
                DEFAULT_GEOCOORDINATE_RESOLVER_CLASSNAME);
        try {
            geoCoordinateResolverImpl = GeoCoordinateResolverHolder.
                    getGeoCoordinationResolverHolderInstance().getGeoCoordinateResolver(geoResolverImplClassName);
            geoCoordinateResolverImpl.init(configReader);
        } catch (InstantiationException e) {
            throw new SiddhiAppValidationException("Cannot instantiate GeoCoordinateResolverHolder holder class '"
                    + geoCoordinateResolverImpl , e);
        } catch (IllegalAccessException e) {
            throw new SiddhiAppValidationException("Cannot access GeoCoordinateResolverHolder holder class '"
                    + geoCoordinateResolverImpl , e);
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException("Cannot find GeoCoordinateResolverHolder holder class '"
                    + geoCoordinateResolverImpl , e);
        } catch (GeoLocationResolverException e) {
            throw new SiddhiAppCreationException("Configuration error in geocoordinate stream function" , e);
        }
    }
}
