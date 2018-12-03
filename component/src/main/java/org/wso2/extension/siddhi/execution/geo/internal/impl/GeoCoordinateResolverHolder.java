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

import org.wso2.extension.siddhi.execution.geo.api.GeoCoordinateResolver;

/**
 * A singleton class to initialize extension.
 */
public class GeoCoordinateResolverHolder {

    private static final Object lock = new Object();
    private static GeoCoordinateResolverHolder geoCoordinateResolverHolder;
    private static String defaultGeocoordinateResolverClassname =
            "org.wso2.extension.siddhi.execution.geo.internal.impl.APIBasedGeoCoordinateResolver";
    private GeoCoordinateResolver geoCoordinateResolver;

    private GeoCoordinateResolverHolder() throws
            ClassNotFoundException, IllegalAccessException, InstantiationException {
        geoCoordinateResolver = (GeoCoordinateResolver) Class.forName
                (defaultGeocoordinateResolverClassname).newInstance();
    }

    public static GeoCoordinateResolverHolder getGeoCoordinationResolverInstance(String geoResolverImplClassName)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (geoCoordinateResolverHolder == null) {
            synchronized (lock) {
                if (geoCoordinateResolverHolder == null) {
                    defaultGeocoordinateResolverClassname = geoResolverImplClassName;
                    createInstance();
                }
            }
        }
        return geoCoordinateResolverHolder;
    }

    private static void createInstance() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        synchronized (lock) {
            if (geoCoordinateResolverHolder == null) {
                geoCoordinateResolverHolder = new GeoCoordinateResolverHolder();
            }
        }
    }

    public GeoCoordinateResolver getGeoCoordinateResolver() {
        return geoCoordinateResolver;
    }
}
