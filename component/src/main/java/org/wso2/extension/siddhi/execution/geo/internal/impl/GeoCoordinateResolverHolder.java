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
import org.wso2.extension.siddhi.execution.geo.internal.exception.GeoLocationResolverException;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

/**
 * A singleton class to initialize extension.
 */
public class GeoCoordinateResolverHolder {
    private static final Object lock = new Object();
    private static GeoCoordinateResolverHolder geoCoordinateResolverHolder;
    private static String defaultGeocoordinateResolverClassname =
            "org.wso2.extension.siddhi.execution.geo.internal.impl.APIBasedGeoCoordinateResolver";

    static {
        try {
            geoCoordinateResolverHolder = new GeoCoordinateResolverHolder();
        } catch (InstantiationException e) {
            throw new SiddhiAppValidationException("Cannot instantiate GeoCoordinateResolverHolder holder class '"
                    + geoCoordinateResolverHolder , e);
        } catch (IllegalAccessException e) {
            throw new SiddhiAppValidationException("Cannot access GeoCoordinateResolverHolder holder class '"
                    + geoCoordinateResolverHolder , e);
        } catch (ClassNotFoundException e) {
            throw new SiddhiAppValidationException("Cannot find GeoCoordinateResolverHolder holder class '"
                    + geoCoordinateResolverHolder , e);
        } catch (GeoLocationResolverException e) {
            throw new SiddhiAppValidationException("Configuration error in geocoordinate stream function" , e);
        }
    }

    private GeoCoordinateResolver geoCoordinateResolver;

    private GeoCoordinateResolverHolder() throws
            ClassNotFoundException, IllegalAccessException, InstantiationException, GeoLocationResolverException {
        geoCoordinateResolver = (GeoCoordinateResolver) Class.forName
                (defaultGeocoordinateResolverClassname).newInstance();
    }

    public static GeoCoordinateResolverHolder getGeoCoordinationResolverInstance(String geoResolverImplClassName) {
        defaultGeocoordinateResolverClassname = geoResolverImplClassName;
        return geoCoordinateResolverHolder;
    }

    public GeoCoordinateResolver getGeoCoordinateResolver() {
        return geoCoordinateResolver;
    }
}
