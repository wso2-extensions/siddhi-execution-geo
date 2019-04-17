/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;
import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.AbstractDefinition;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This extension transforms a latitude and longitude coordinates into precise address information.
 * The output contains string properties streetNumber, neighborhood, route, administrativeAreaLevelTwo,
 * administrativeAreaLevelOne, country, countryCode, postalCode and formattedAddress in order.
 * However, these information are not available for all the geo coordinates. For example, if the latitude
 * and longitude represent a place in a forest, only the high level information like country will be returned.
 * For those which are not available, this extension will return "N/A" as the value.
 */
@Extension(
        name = "reversegeocode",
        namespace = "geo",
        description = "This extension transforms pairs of latitude and longitude coordinates into precise address " +
                "information. The output contains string properties including the 'streetNumber', 'neighborhood'," +
                " 'route', 'administrativeAreaLevelTwo', 'administrativeAreaLevelOne', 'country', 'countryCode'," +
                " 'postalCode', and the 'formattedAddress' in the given order. However, this information is not " +
                "available for all the geo coordinates. For example, if the latitude and longitude represent a " +
                "place in a forest, only the high level information such as the country is returned. " +
                "In such scenarios, \"N/A\" is returned as the value for return attributes of which the" +
                " values cannot be derived.",
        parameters = {
                @Parameter(
                        name = "longitude",
                        description = "The longitude value required in order to derive at the location.",
                        type = DataType.DOUBLE
                ),
                @Parameter(
                        name = "latitude",
                        description = "The latitude value required in order to derive at the location.",
                        type = DataType.DOUBLE
                )
        },
        examples = @Example(
                syntax = "reversegeocode(6.909785, 79.852603)",
                description = "This query returns the precise address information of the given location. In this " +
                        "example, it returns the following value:\n" +
                        " \"27\", \"N/A\", \"Palm Grove\", \"Colombo\", \"Western Province\"," +
                        " \"Sri Lanka\", \"LK\", \"00300\", \"27 Palm Grove, Colombo 00300, " +
                        "Sri Lanka\".")

)
public class ReverseGeocodeStreamFunctionProcessor extends StreamFunctionProcessor<State> {

    private static final Logger LOGGER = Logger.getLogger(ReverseGeocodeStreamFunctionProcessor.class);
    private final Geocoder geocoder = new Geocoder();
    private boolean debugModeOn;
    private ArrayList<Attribute> attributes = new ArrayList<Attribute>(9);

    /**
     * The process method of the StreamFunction, used when more then one function parameters are provided
     *
     * @param data the data values for the function parameters
     * @return the data for additional output attributes introduced by the function
     */
    @Override
    protected Object[] process(Object[] data) {
        if (data[0] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given" +
                    " to geo:reversegeocode() function. The first argument cannot be null");
        }
        if (data[1] == null) {
            throw new SiddhiAppRuntimeException("Invalid input given" +
                    " to geo:reversegeocode() function. The second argument cannot be null");
        }

        BigDecimal latitude = new BigDecimal((Double) data[0]);
        BigDecimal longitude = new BigDecimal((Double) data[1]);

        LatLng coordinate = new LatLng(latitude, longitude);

        // Make the geocode request to API library
        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
                .setLocation(coordinate)
                .setLanguage("en")
                .getGeocoderRequest();

        String streetNumber = "N/A";
        String neighborhood = "N/A";
        String route = "N/A";
        String administrativeAreaLevelTwo = "N/A";
        String administrativeAreaLevelOne = "N/A";
        String country = "N/A";
        String countryCode = "N/A";
        String postalCode = "N/A";
        String formattedAddress = "N/A";

        try {
            GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
            GeocoderStatus status = geocoderResponse.getStatus();
            if (status == GeocoderStatus.OK && !geocoderResponse.getResults().isEmpty()) {
                formattedAddress = geocoderResponse.getResults().get(0).getFormattedAddress();
                List<GeocoderAddressComponent> addressComponents = geocoderResponse
                        .getResults().get(0).getAddressComponents();
                for (GeocoderAddressComponent component : addressComponents) {
                    List<String> types = component.getTypes();
                    if (types.contains("street_number")) {
                        streetNumber = component.getLongName();
                    } else if (types.contains("neighborhood")) {
                        neighborhood = component.getLongName();
                    } else if (types.contains("route")) {
                        route = component.getLongName();
                    } else if (types.contains("administrative_area_level_2")) {
                        administrativeAreaLevelTwo = component.getLongName();
                    } else if (types.contains("administrative_area_level_1")) {
                        administrativeAreaLevelOne = component.getLongName();
                    } else if (types.contains("country")) {
                        country = component.getLongName();
                        countryCode = component.getShortName();
                    } else if (types.contains("postal_code")) {
                        postalCode = component.getLongName();
                    }
                }
            } else {
                LOGGER.error("Geocoder request failed with a response of: " + status.value());
            }
        } catch (IOException e) {
            throw new SiddhiAppRuntimeException("Error in connection to Google Maps API.", e);
        }

        if (debugModeOn) {
            String message = String.format("Street Number: %s, Neighborhood: %s," +
                            " Route: %s, Administrative Area Level 2: %s, Administrative Area Level 1: %s, " +
                            "Country: %s, ISO Country code: %s, Postal code: %s, Formatted address: %s",
                    streetNumber, neighborhood, route, administrativeAreaLevelTwo,
                    administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress);
            LOGGER.debug(message);
        }
        return new Object[]{streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne,
                country, countryCode, postalCode, formattedAddress};
    }

    /**
     * The process method of the StreamFunction, used when zero or one function parameter is provided
     *
     * @param data null if the function parameter count is zero or runtime data value of the function parameter
     * @return the data for additional output attribute introduced by the function
     */
    @Override
    protected Object[] process(Object data) {
        throw new RuntimeException("");
    }

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param abstractDefinition              the incoming stream definition
     * @param attributeExpressionExecutors    the executors of each function parameters
     * @param outputExpectsExpiredEvents      whether output can be expired event
     * @param siddhiQueryContext              siddhi query context
     * @param configReader                    this hold the stream Processor configuration reader.
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected StateFactory<State> init(AbstractDefinition abstractDefinition,
                                                ExpressionExecutor[] attributeExpressionExecutors,
                                                ConfigReader configReader, boolean outputExpectsExpiredEvents,
                                                SiddhiQueryContext siddhiQueryContext) {
        debugModeOn = LOGGER.isDebugEnabled();
        if (attributeExpressionExecutors.length != 2) {
            throw new SiddhiAppValidationException("Invalid no of arguments " +
                    "passed to geo:reversegeocode() function, required 1, " +
                    "but found " + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.DOUBLE ||
                attributeExpressionExecutors[1].getReturnType() != Attribute.Type.DOUBLE) {
            throw new SiddhiAppCreationException("Both input parameters should be of type double");
        }
        attributes.add(new Attribute("streetNumber", Attribute.Type.STRING));
        attributes.add(new Attribute("neighborhood", Attribute.Type.STRING));
        attributes.add(new Attribute("route", Attribute.Type.STRING));
        attributes.add(new Attribute("administrativeAreaLevelTwo", Attribute.Type.STRING));
        attributes.add(new Attribute("administrativeAreaLevelOne", Attribute.Type.STRING));
        attributes.add(new Attribute("country", Attribute.Type.STRING));
        attributes.add(new Attribute("countryCode", Attribute.Type.STRING));
        attributes.add(new Attribute("postalCode", Attribute.Type.STRING));
        attributes.add(new Attribute("formattedAddress", Attribute.Type.STRING));
        return null;
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

    @Override
    public List<Attribute> getReturnAttributes() {
        return attributes;
    }
}
