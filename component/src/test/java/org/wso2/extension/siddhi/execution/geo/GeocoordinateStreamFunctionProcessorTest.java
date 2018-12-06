/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
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
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test case for Geocoordinate Stream Function.
 */
public class GeocoordinateStreamFunctionProcessorTest {
    private static final Logger logger = Logger.getLogger(GeocoordinateStreamFunctionProcessorTest.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;
    private static int eventCount = 0;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    /**
     * Creating test for Geocoordinate Stream Function.
     *
     * @throws Exception Interrupted exception
     */
    @Test
    public void geocoordinateStreamFunctionProcessorTest1() throws Exception {
        logger.info("Test Geocoordinate 1 - Test geocoordinates for given IP");
        Map<String, String> masterConfigs = new HashMap<>();
        //The key value is a dummy value. You have to get a key value from ipInfoDB
        //(IpInfoDB : https://www.ipinfodb.com/ )
        masterConfigs.put("geo.geocoordinate.apiurl", "http://api.ipinfodb.com/v3/ip-city/?key=" +
                "39a2202599d94b2432ff1a075f1a35cfe99cb40982d1181adb2c41b6c947a251&ip=");
        SiddhiManager siddhiManager = new SiddhiManager();
        long start = System.currentTimeMillis();
        InMemoryConfigManager inMemoryConfigManager = new InMemoryConfigManager(masterConfigs, null);
        inMemoryConfigManager.generateConfigReader("geo", "geocoordinate");
        siddhiManager.setConfigManager(inMemoryConfigManager);
        String inStreamDefinition = "define stream inputStream (ip String);";
        String query = ("@info(name = 'query') "
                + "from inputStream#geo:geocoordinate(ip) "
                + "select longitude, latitude "
                + "insert into outputStream;"
        );
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to add query: [%f sec]", ((end - start) / 1000f)));

        List<Object[]> data = new ArrayList<Object[]>();
        data.add(new Object[]{"2a01:7e00::f03c:91ff:fe44:6903"});
        data.add(new Object[]{"95.31.18.119"});
        final List<Object[]> expectedResult = new ArrayList<Object[]>();
        expectedResult.add(new Object[]{-0.12574, 51.50853d});
        expectedResult.add(new Object[]{37.6156d, 55.7522d});
        siddhiAppRuntime.addCallback("query", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    Object[] expected = expectedResult.get(eventCount);

                    if (count.get() == 1) {
                        if ((Double) event.getData(0) != -1.0) {
                            AssertJUnit.assertEquals((Double) expected[0], (Double) event.getData(0), 1e-2);
                            AssertJUnit.assertEquals((Double) expected[1], (Double) event.getData(1), 1e-2);
                        }
                        eventArrived = true;

                    }
                    if (count.get() == 2) {
                        if ((Double) event.getData(0) != -1.0) {
                            AssertJUnit.assertEquals((Double) expected[0], (Double) event.getData(0), 1e-2);
                            AssertJUnit.assertEquals((Double) expected[1], (Double) event.getData(1), 1e-2);
                        }
                        eventArrived = true;

                    }
                    eventCount++;
                }
            }
        });

        siddhiAppRuntime.start();

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        for (Object[] dataLine : data) {
            inputHandler.send(dataLine);
        }
        SiddhiTestHelper.waitForEvents(100, 2, count, 60000);
        AssertJUnit.assertEquals(2, count.get());
        AssertJUnit.assertTrue(eventArrived);

    }

    @Test(expectedExceptions = SiddhiAppCreationException.class, dependsOnMethods =
            "geocoordinateStreamFunctionProcessorTest1")
    public void geocoordinateStreamFunctionProcessorTest2() throws Exception {
        logger.info("Test Geocoordinate 2 - Invalid number of input parameters");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.createSiddhiAppRuntime
                ("define stream inputStream (ip String, city String);"
                        + "@info(name = 'query') "
                        + "from inputStream#geo:geocoordinate(ip, city) "
                        + "select longitude, latitude "
                        + "insert into outputStream");
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class, dependsOnMethods =
            "geocoordinateStreamFunctionProcessorTest2")
    public void geocoordinateStreamFunctionProcessorTest3() throws Exception {
        logger.info("Test Geocoordinate 3 - Invalid type of input parameters");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.createSiddhiAppRuntime
                ("define stream inputStream (ip float);"
                        + "@info(name = 'query') "
                        + "from inputStream#geo:geocoordinate(ip) "
                        + "select longitude, latitude "
                        + "insert into outputStream");
    }
}

