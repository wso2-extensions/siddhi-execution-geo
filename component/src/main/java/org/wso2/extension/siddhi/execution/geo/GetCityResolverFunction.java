/*
 * Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.geo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.extension.siddhi.execution.geo.api.GeoLocationResolver;
import org.wso2.extension.siddhi.execution.geo.api.Location;
import org.wso2.extension.siddhi.execution.geo.internal.LRUCacheStore;
import org.wso2.extension.siddhi.execution.geo.internal.exception.GeoLocationResolverException;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The class provides implementation for getting city based on the given ip.
 */
@Extension(
        name = "findCityFromIP",
        namespace = "geo",
        description = "Returns the city which is related to the give ip address.",
        parameters = {
                @Parameter(
                        name = "ip",
                        description = "The IP address that the user need to get the relevant city",
                        type = {DataType.STRING})
        },
        returnAttributes = @ReturnAttribute(
                description = "The city which is related to the given IP",
                type = {DataType.STRING}),
        examples = @Example(
                description = "This will return the corresponding city to the given ip address",
                syntax = "define stream IpStream(ip string);\n" +
                        "from IpStream\n" +
                        "select geo:getCity(ip) as city\n" +
                        "insert into outputStream;")
)
public class GetCityResolverFunction extends FunctionExecutor {
    private static final Log log = LogFactory.getLog(GetCityResolverFunction.class);

    private static final String CACHE_SIZE_KEY = "cacheSize";
    private static final String IS_CACHE_ENABLED = "isCacheEnabled";
    private static final int DEFAULT_CACHE_SIZE = 10000;
    private static final String DEFAULT_GEOLOCATION_RESOLVER_CLASSNAME =
            "org.wso2.extension.siddhi.execution.geo.internal.impl.DefaultDBBasedGeoLocationResolver";

    private static GeoLocationResolver geoLocationResolverImpl;
    private static LRUCacheStore lruCacheStore;
    private static boolean isCacheEnabled = true;
    private static int cacheSize = 10000;
    private static AtomicBoolean isExtensionConfigInitialized = new AtomicBoolean(false);

    /**
     * The initialization method for {@link FunctionExecutor}, which will be called before other methods and validate
     * the all configuration and getting the initial values.
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the Function
     * @param configReader                 this hold the {@link FunctionExecutor} extensions configuration reader.
     * @param siddhiAppContext             Siddhi app runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to geoIp:getCity() function, " +
                    "required 1, but found " + attributeExpressionExecutors.length);
        }

        Attribute.Type attributeType = attributeExpressionExecutors[0].getReturnType();
        if (attributeType != Attribute.Type.STRING) {
            throw new SiddhiAppValidationException("Invalid parameter type found for first argument ip of " +
                    "geoIp:getCity() function, required " + Attribute.Type.STRING + ", but found " + attributeType
                    .toString());
        }

        synchronized (isExtensionConfigInitialized) {
            if (!isExtensionConfigInitialized.get()) {
                initializeExtensionConfigs(configReader);
            }
        }

    }

    /**
     * The main execution method which will be called upon event arrival
     * when there are more than one Function parameter
     *
     * @param data the runtime values of Function parameters
     * @return the Function result
     */
    @Override
    protected Object execute(Object[] data) {
        return null;
    }

    /**
     * The main execution method which will be called upon event arrival
     * when there are zero or one Function parameter
     *
     * @param data null if the Function parameter count is zero or
     *             runtime data value of the Function parameter
     * @return the Function result
     */
    @Override
    protected Object execute(Object data) {
        Location location = null;
        String ip = data.toString();
        if (isCacheEnabled) {
            location = lruCacheStore.getInstance().get(data.toString());
        }
        if (location == null) {
            location = geoLocationResolverImpl.getGeoLocationInfo(data.toString());
            if (location != null) {
                lruCacheStore.getInstance().put(data.toString(), location);
            } else {
                lruCacheStore.getInstance().put(data.toString(), new Location("", "", ip));
            }
        }
        return location != null ? location.getCity() : "";
    }

    /**
     * return a Class object that represents the formal return type of the method represented by this Method object.
     *
     * @return the return type for the method this object represents
     */
    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.STRING;
    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as an map
     */
    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the processing element as a map.
     *              This is the same map that is created upon calling currentState() method.
     */
    @Override
    public void restoreState(Map<String, Object> state) {

    }

    private void initializeExtensionConfigs(ConfigReader configReader) throws SiddhiAppValidationException {
        String geoResolverImplClassName = configReader.readConfig("geoLocationResolverClass",
                DEFAULT_GEOLOCATION_RESOLVER_CLASSNAME);
        isCacheEnabled = Boolean.parseBoolean(configReader.readConfig(IS_CACHE_ENABLED, "true"));
        String configCacheValue = configReader.readConfig(CACHE_SIZE_KEY, String.valueOf(cacheSize));
        if (isCacheEnabled) {
            try {
                cacheSize = Integer.parseInt(configCacheValue);
            } catch (NumberFormatException e) {
                cacheSize = DEFAULT_CACHE_SIZE;
                log.warn("The config '" + configCacheValue + "' provided in 'cacheSize' is not a valid integer. " +
                        "Hence using the default cache size '" + DEFAULT_CACHE_SIZE + "'");
            } finally {
                lruCacheStore.init(cacheSize);
            }
        }

        try {
            geoLocationResolverImpl = (GeoLocationResolver) Class.forName(geoResolverImplClassName).newInstance();
            geoLocationResolverImpl.init(configReader);
            isExtensionConfigInitialized.set(true);
        } catch (InstantiationException e) {
            throw new SiddhiAppValidationException("Cannot instantiate GeoLocationResolver implementation class '"
                    + geoResolverImplClassName + "' given in the configuration", e);
        } catch (IllegalAccessException e) {
            throw new SiddhiAppValidationException("Cannot access GeoLocationResolver implementation class '"
                    + geoResolverImplClassName + "' given in the configuration", e);
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException("Cannot find GeoLocationResolver implementation class '"
                    + geoResolverImplClassName + "' given in the configuration", e);
        } catch (ClassCastException e) {
            throw new SiddhiAppValidationException("Cannot cast GeoLocationResolver implementation class '"
                    + geoResolverImplClassName + "' to 'GeoLocationResolver'", e);
        } catch (GeoLocationResolverException e) {
            throw new SiddhiAppValidationException("Cannot initialize GeoLocationResolver implementation class '"
                    + geoResolverImplClassName + "' given in the configuration", e);
        }

    }
}
