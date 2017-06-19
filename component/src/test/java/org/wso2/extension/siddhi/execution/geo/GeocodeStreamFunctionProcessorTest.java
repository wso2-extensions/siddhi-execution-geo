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

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.List;

public class GeocodeStreamFunctionProcessorTest {

    private static final Logger LOGGER = Logger.getLogger(GeocodeStreamFunctionProcessorTest.class);
    private static int eventCount = 0;

    @Test
    public void testProcess() throws Exception {
        LOGGER.info("Init Siddhi setUp");

        SiddhiManager siddhiManager = new SiddhiManager();
        long start = System.currentTimeMillis();
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.
                createSiddhiAppRuntime("define stream geocodeStream " +
                        "(location string, level string, time string);"
                        + "@info(name = 'query1') from geocodeStream#geo:geocode(location) " +
                        " select latitude, longitude, formattedAddress " +
                        " insert into dataOut");
        long end = System.currentTimeMillis();
        LOGGER.info(String.format("Time to add query: [%f sec]", ((end - start) / 1000f)));

        List<Object[]> data = new ArrayList<Object[]>();
        data.add(new Object[]{"Champ de Mars, 5 Avenue Anatole France," +
                " 75007 Paris, France", "Regular", "Sun Nov 02 13:36:05 +0000 2014"});
        data.add(new Object[]{"6 Parvis Notre-Dame - Pl. Jean-Paul II, 75004 Paris, France",
                "Sun Nov 12 13:36:05 +0000 2014"});
        data.add(new Object[]{"Piazza del Colosseo, 1, 00184 Roma, Italy", "Sun Nov 10 13:36:05 +0000 2014"});
        data.add(new Object[]{"Westminster, London SW1A 0AA, UK", "Regular", "Sun Nov 02 13:36:05 +0000 2014"});

        final List<Object[]> expectedResult = new ArrayList<Object[]>();
        expectedResult.add(new Object[]{48.8588871d, 2.2944861d, "5 Avenue Anatole France, 75007 Paris, France"});
        expectedResult.add(new Object[]{48.85267d, 2.3492923d, "6 Parvis Notre-Dame - Pl. Jean-Paul II," +
                " 75004 Paris, France"});
        expectedResult.add(new Object[]{41.8900275d, 12.4939171d, "Piazza del Colosseo, 1, 00184 Roma, Italy"});
        expectedResult.add(new Object[]{51.4998403d, -0.1246627d, "Westminster, London SW1A 0AA, UK"});


        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    Object[] expected = expectedResult.get(eventCount);
                    AssertJUnit.assertEquals((Double) expected[0], (Double) event.getData(0), 1e-2);
                    AssertJUnit.assertEquals((Double) expected[1], (Double) event.getData(1), 1e-2);
                    AssertJUnit.assertEquals(expected[2], event.getData(2));
                    eventCount++;
                }
            }
        });

        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("geocodeStream");
        for (Object[] dataLine : data) {
            inputHandler.send(dataLine);
        }
        Thread.sleep(2000);
    }
}
