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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GeocodeStreamFunctionProcessorTest {

    private static final Logger LOGGER = Logger.getLogger(GeocodeStreamFunctionProcessorTest.class);
    private static int eventCount = 0;
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }


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
        data.add(new Object[]{"5 Avenue Anatole France, 75007 Paris, France"});
        data.add(new Object[]{"Cathedrale Notre Dame,75004 Paris, France",
                "Sun Nov 12 13:36:05 +0000 2014"});
        data.add(new Object[]{"Piazza del Colosseo, 1, 00184 Roma, Italy", "Sun Nov 10 13:36:05 +0000 2014"});
        data.add(new Object[]{"Westminster, London SW1A 0AA, UK", "Regular", "Sun Nov 02 13:36:05 +0000 2014"});

        final List<Object[]> expectedResult = new ArrayList<Object[]>();
        expectedResult.add(new Object[]{48.8588871d, 2.2944861d,
                "5 Avenue Anatole France, 75007 Paris, France"});
        expectedResult.add(new Object[]{48.852968d, 2.349902d, "6 Parvis Notre-Dame - Pl. Jean-Paul II," +
                " 75004 Paris, France"});
        expectedResult.add(new Object[]{41.8900275d, 12.4939171d, "Piazza del Colosseo, 1, 00184 Roma RM, Italy"});
        expectedResult.add(new Object[]{51.4998403d, -0.1246627d, "Westminster, London SW1A 0AA, UK"});


        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    Object[] expected = expectedResult.get(eventCount);

                    if (count.get() == 1) {
                        if ((Double) event.getData(0) != -1.0) {
                            // If default values returned skip assert since geocoder has not returned a response
                            // due to over query limit reached
                            AssertJUnit.assertEquals((Double) expected[0], (Double) event.getData(0), 1e-2);
                            AssertJUnit.assertEquals((Double) expected[1], (Double) event.getData(1), 1e-2);
                            AssertJUnit.assertEquals(expected[2], event.getData(2));
                        }
                        eventArrived = true;

                    }
                    if (count.get() == 2) {
                        if ((Double) event.getData(0) != -1.0) {
                            // If default values returned skip assert since geocoder has not returned a response
                            // due to over query limit reached
                            AssertJUnit.assertEquals((Double) expected[0], (Double) event.getData(0), 1e-2);
                            AssertJUnit.assertEquals((Double) expected[1], (Double) event.getData(1), 1e-2);
                            AssertJUnit.assertEquals(expected[2], event.getData(2));
                        }
                        eventArrived = true;

                    }
                    if (count.get() == 3) {
                        if ((Double) event.getData(0) != -1.0) {
                            // If default values returned skip assert since geocoder has not returned a response
                            // due to over query limit reached
                            AssertJUnit.assertEquals((Double) expected[0], (Double) event.getData(0), 1e-2);
                            AssertJUnit.assertEquals((Double) expected[1], (Double) event.getData(1), 1e-2);
                            AssertJUnit.assertEquals(expected[2], event.getData(2));
                        }
                        eventArrived = true;

                    }
                    if (count.get() == 4) {
                        if ((Double) event.getData(0) != -1.0) {
                            // If default values returned skip assert since geocoder has not returned a response
                            // due to over query limit reached
                            AssertJUnit.assertEquals((Double) expected[0], (Double) event.getData(0), 1e-2);
                            AssertJUnit.assertEquals((Double) expected[1], (Double) event.getData(1), 1e-2);
                            AssertJUnit.assertEquals(expected[2], event.getData(2));
                        }
                        eventArrived = true;

                    }
                    eventCount++;
                }
            }
        });

        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("geocodeStream");
        for (Object[] dataLine : data) {
            inputHandler.send(dataLine);
        }
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        AssertJUnit.assertTrue(eventArrived);

    }
}
