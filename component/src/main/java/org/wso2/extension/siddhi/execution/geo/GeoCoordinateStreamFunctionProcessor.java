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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.extension.siddhi.execution.geo.api.GeoCoordinate;
import org.wso2.extension.siddhi.execution.geo.api.GeoCoordinateResolver;
import org.wso2.extension.siddhi.execution.geo.internal.impl.APIBasedGeoCoordinateResolver;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is to get longitude and latitude value of login location based on ip address.
 */

@Extension(
        name = "geocoordinate",
        namespace = "geo",
        description = "stream function",
        examples = @Example(
                description = "This will returns the Longitude and latitude of the given login ip",
                syntax = "geocoordinate(\"ip address\")"),
        parameters = {
                @Parameter(
                        name = "ip",
                        description = "login ip address",
                        type = {DataType.STRING})
        },
        returnAttributes = {
                @ReturnAttribute(
                        name = "longitude",
                        description = "Longitude of the location", type = DataType.DOUBLE
                ),
                @ReturnAttribute(
                        name = "latitude",
                        description = "Latitude of the location", type = DataType.DOUBLE
                )
        }
)

public class GeoCoordinateStreamFunctionProcessor extends StreamFunctionProcessor {

    private static final Log log = LogFactory.getLog(GeoCoordinateStreamFunctionProcessor.class);
    private GeoCoordinateResolver geoVelocityResolverImpl = new APIBasedGeoCoordinateResolver();
    private String apikey;

    /**
     * The process method used when more than one function parameters are provided
     *
     * @param data the data values for the function parameters
     * @return the data for additional output attributes introduced by the function
     */
    @Override
    protected Object[] process(Object[] data) {
        return new Object[0];
    }

    /**
     * The process method used when zero or one function parameter is provided
     *
     * @param data null if the function parameter count is zero or runtime data value of the function parameter
     * @return the data for additional output attribute introduced by the function
     */
    @Override
    protected Object[] process(Object data) {
        GeoCoordinate geoCoordinate;
        String ip = data.toString();
        geoCoordinate = geoVelocityResolverImpl.getGeoCoordinateInfo(apikey, ip);
        return new Object[]{geoCoordinate.getLatitude(), geoCoordinate.getLongitude()};
    }

    /**
     * The initialization method for {@link StreamFunctionProcessor},
     * which will be called before other methods and validate
     * the all configuration and getting the initial values.
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param configReader                 this hold the {@link StreamFunctionProcessor} extensions
     *                                     configuration reader.
     * @param siddhiAppContext             the context of the siddhi app
     * @return the output's additional attributes list introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                                   SiddhiAppContext siddhiAppContext) {
        configReader.getAllConfigs().forEach((key, value) -> {
            apikey = value;
        });
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(6);
        attributes.add(new Attribute("latitude", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("longitude", Attribute.Type.DOUBLE));
        return attributes;
    }

    /**
     * This will be called only once and this can be used to acquire
     * required resources for the processing element.
     * This will be called after initializing the system and before
     * starting to process the events.
     */
    @Override
    public void start() {

    }

    /**
     * This will be called only once and this can be used to release
     * the acquired resources for processing.
     * This will be called before shutting down the system.
     */
    @Override
    public void stop() {

    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as an map
     */
    @Override
    public Map<String, Object> currentState() {
        return new HashMap<String, Object>();
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
}
