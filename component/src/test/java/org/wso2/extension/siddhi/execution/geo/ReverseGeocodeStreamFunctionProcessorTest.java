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
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class ReverseGeocodeStreamFunctionProcessorTest {

    private static final Logger LOGGER = Logger.getLogger(ReverseGeocodeStreamFunctionProcessorTest.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testReverseGeocode1() throws Exception {
        LOGGER.info("Test Reverse Geocode 1 - Coordinates in LK");

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream LocationStream (deviceId string, timestamp long, latitude double," +
                        " longitude double); " +
                        "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                        "select streetNumber, neighborhood, route, administrativeAreaLevelTwo," +
                        " administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                        " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.incrementAndGet();
                if (!events[0].getData()[0].equals("N/A")) {
                    // If default values returned skip assert since geocoder has not returned a response
                    // due to over query limit reached
                    AssertJUnit.assertArrayEquals(new Object[]{"16", "N/A", "Palm Grove", "Colombo", "Western Province",
                                    "Sri Lanka", "LK", "00300", "16 Palm Grove, Colombo 00300, Sri Lanka"},
                            events[0].getData());
                }
                eventArrived = true;
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 6.909785, 79.852603});
        SiddhiTestHelper.waitForEvents(100, 1, count, 60000);
        AssertJUnit.assertEquals(1, count.get());
        AssertJUnit.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = "testReverseGeocode1")
    public void testReverseGeocode2() throws Exception {
        LOGGER.info("Test Reverse Geocode 2 - Coordinates in USA");

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream LocationStream (deviceId string, timestamp long," +
                        " latitude double, longitude double);"
                        + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                        " select streetNumber, neighborhood, route, administrativeAreaLevelTwo," +
                        "administrativeAreaLevelOne," +
                        " country, countryCode, postalCode, formattedAddress " +
                        " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.incrementAndGet();
                if (!events[0].getData()[0].equals("N/A")) {
                    // If default values returned skip assert since geocoder has not returned a response
                    // due to over query limit reached
                    AssertJUnit.assertArrayEquals(new Object[]{"67-53", "Flushing", "Loubet Street", "Queens County",
                                    "New York", "United States", "US", "11375", "67-53 Loubet St, Flushing, " +
                                    "NY 11375, USA"},
                            events[0].getData());
                }
                eventArrived = true;
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 40.715229, -73.8564082});
        SiddhiTestHelper.waitForEvents(100, 1, count, 60000);
        AssertJUnit.assertEquals(1, count.get());
        AssertJUnit.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = "testReverseGeocode2")
    public void testReverseGeocode3() throws Exception {
        LOGGER.info("Test Reverse Geocode 3 - Coordinates in UK");

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream LocationStream " +
                        "(deviceId string, timestamp long, latitude double, longitude double);"
                        + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                        " select streetNumber, neighborhood, route, administrativeAreaLevelTwo," +
                        " administrativeAreaLevelOne," +
                        " country, countryCode, postalCode, formattedAddress " +
                        " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.incrementAndGet();
                if (!events[0].getData()[0].equals("N/A")) {
                    // If default values returned skip assert since geocoder has not returned a response
                    // due to over query limit reached
                    AssertJUnit.assertArrayEquals(new Object[]{"10", "Westminster", "Great George Street",
                            "Greater London", "England", "United Kingdom", "GB", "N/A", "10 Great George St, " +
                            "Westminster, London, UK"}, events[0].getData());
                }
                eventArrived = true;
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 51.50161, -0.127909});
        SiddhiTestHelper.waitForEvents(100, 1, count, 60000);
        AssertJUnit.assertEquals(1, count.get());
        AssertJUnit.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = "testReverseGeocode3")
    public void testReverseGeocode4() throws Exception {
        LOGGER.info("Test Reverse Geocode 4 - Less precised input");

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream LocationStream " +
                        "(deviceId string, timestamp long, latitude double, longitude double);"
                        + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                        " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, " +
                        "administrativeAreaLevelOne," +
                        " country, countryCode, postalCode, formattedAddress " +
                        " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.incrementAndGet();
                if (!events[0].getData()[0].equals("N/A")) {
                    // If default values returned skip assert since geocoder has not returned a response
                    // due to over query limit reached
                    AssertJUnit.assertArrayEquals(new Object[]{"1490", "N/A",
                                                               "Colombo - Ratnapura - Wellawaya - Batticaloa",
                                                               "Colombo", "Western Province",
                                                               "Sri Lanka", "LK", "00700",
                                                               "1490 Colombo - Ratnapura - Wellawaya - Batticaloa, "
                                                                       + "Colombo 00700, Sri Lanka"},
                            events[0].getData());
                }
                eventArrived = true;
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 6.90, 79.86});
        SiddhiTestHelper.waitForEvents(100, 1, count, 60000);
        AssertJUnit.assertEquals(1, count.get());
        AssertJUnit.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = "testReverseGeocode4")
    public void testReverseGeocode5() throws Exception {
        LOGGER.info("Test Reverse Geocode 4 - Amazon Rain Forest");

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream LocationStream " +
                        "(deviceId string, timestamp long, latitude double, longitude double);"
                        + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                        " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, " +
                        "administrativeAreaLevelOne, " +
                        "country, countryCode, postalCode, formattedAddress " +
                        " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                count.incrementAndGet();
                if (count.get() == 1) {
                    if (!events[0].getData()[0].equals("N/A")) {
                        // If default values returned skip assert since geocoder has not returned a response
                        // due to over query limit reached
                        AssertJUnit.assertArrayEquals(new Object[]{"5", "N/A", "Avenue Anatole France", "Paris",
                                        "Île-de-France", "France", "FR", "75007", "Tour Eiffel, 5 Avenue Anatole" +
                                        " France, 75007 Paris, France"},
                                events[0].getData());
                    }
                    eventArrived = true;

                }
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 48.8584, 2.2945});
        SiddhiTestHelper.waitForEvents(100, 1, count, 60000);
        AssertJUnit.assertEquals(1, count.get());
        AssertJUnit.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = "testReverseGeocode5")
    public void testReverseGeocode6() throws Exception {
        LOGGER.info("Test Reverse Geocode 5 - null inputs");

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream LocationStream (deviceId string, timestamp long," +
                        " latitude double, longitude double);"
                        + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                        " select streetNumber, neighborhood, route," +
                        " administrativeAreaLevelTwo, administrativeAreaLevelOne," +
                        " country, countryCode, postalCode, formattedAddress " +
                        " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                count.incrementAndGet();
                if (count.get() == 1) {
                    AssertJUnit.fail();
                }
            }
        });

        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), null, null});
        SiddhiTestHelper.waitForEvents(100, 0, count, 60000);
        executionPlanRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class, dependsOnMethods = "testReverseGeocode6")
    public void testReverseGeocode7() throws Exception {
        LOGGER.info("Test Reverse Geocode 6 - Invalid number of input parameters");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.createSiddhiAppRuntime("define stream LocationStream (deviceId string," +
                " timestamp long, latitude double, longitude double);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne," +
                " country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class, dependsOnMethods = "testReverseGeocode7")
    public void testReverseGeocode8() throws Exception {
        LOGGER.info("Test Reverse Geocode 7 - Invalid type of input parameters");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.createSiddhiAppRuntime("define stream LocationStream " +
                "(deviceId string, timestamp long, latitude float, longitude float);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne," +
                " country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");
    }
}
