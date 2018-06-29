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

package org.wso2.extension.siddhi.execution.geo.internal;

import org.wso2.extension.siddhi.execution.geo.api.Location;

/**
 * This class provide a single LRUCache instance.
 */
public class LRUCacheStore {
    private static LRUCache<String, Location> instance;

    /**
     * Initialize the LRU cache with given size.
     *
     * @param cacheSize
     */
    public static void init(int cacheSize) {
        instance = new LRUCache<>(cacheSize);
    }

    /**
     * Provide the initialized LRUCache instance.
     *
     * @return LRUCache instance
     */
    public static LRUCache<String, Location> getInstance() {
        return instance;
    }
}
