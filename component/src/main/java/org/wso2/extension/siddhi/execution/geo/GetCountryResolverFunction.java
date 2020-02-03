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

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.ReturnAttribute;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.executor.function.FunctionExecutor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.extension.siddhi.execution.geo.api.GeoLocationResolver;
import org.wso2.extension.siddhi.execution.geo.api.Location;
import org.wso2.extension.siddhi.execution.geo.internal.LRUCacheStore;
import org.wso2.extension.siddhi.execution.geo.internal.exception.GeoLocationResolverException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The class provides implementation for getting country based on the given ip.
 */
@Extension(
        name = "findCountryFromIP",
        namespace = "geo",
        description = "This function returns the country that is related to the given IP address.",
        parameters = {
                @Parameter(
                        name = "ip",
                        description = "The IP address of which the related country needs to be fetched.",
                        type = {DataType.STRING})
        },
        returnAttributes = @ReturnAttribute(
                description = "The country related to the IP address provided.",
                type = {DataType.STRING}),
        examples = @Example(
                syntax = "define stream IpStream(ip string);\n" +
                        "from IpStream\n" +
                        "select geo:findCountryFromIP(ip) as country \n" +
                        "insert into OutputStream;",
                description = "This query returns the country corresponding to the given IP address.")
)
public class GetCountryResolverFunction extends FunctionExecutor<State> {
    private static final Log log = LogFactory.getLog(GetCountryResolverFunction.class);
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
     * @param siddhiQueryContext           current siddhi query context
     */
    @Override
    protected StateFactory<State> init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                                       SiddhiQueryContext siddhiQueryContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to geo:getCountry() function, " +
                    "required 1, but found " + attributeExpressionExecutors.length);
        }

        Attribute.Type attributeType = attributeExpressionExecutors[0].getReturnType();
        if (attributeType != Attribute.Type.STRING) {
            throw new SiddhiAppValidationException("Invalid parameter type found for first argument 'ip' of " +
                    "geo:getCountry() function, required " + Attribute.Type.STRING + ", but found " + attributeType
                    .toString());
        }

        synchronized (isExtensionConfigInitialized) {
            if (!isExtensionConfigInitialized.get()) {
                initializeExtensionConfigs(configReader);
            }
        }
        return null;
    }

    /**
     * The main execution method which will be called upon event arrival
     * when there are more than one Function parameter
     *
     * @param data the runtime values of Function parameters
     * @return the Function result
     */
    @Override
    protected Object execute(Object[] data, State state) {
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
    protected Object execute(Object data, State state) {
        Location location = null;
        String ip = data.toString();
        if (isCacheEnabled) {
            location = lruCacheStore.getInstance().get(ip);
        }
        if (location == null) {
            location = geoLocationResolverImpl.getGeoLocationInfo(ip);
            if (location != null) {
                lruCacheStore.getInstance().put(ip, location);
            } else {
                lruCacheStore.getInstance().put(ip, new Location("", "", ip));
            }
        }
        return location != null ? location.getCountry() : "";
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
