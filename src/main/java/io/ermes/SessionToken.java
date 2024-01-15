/*
 * Copyright 2023-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ermes;

import io.lettuce.core.GeoCoordinates;

public class SessionToken {
    public String SessionId;
    public String CurrentNodeId;
    public String CurrentNodeIp;
    public GeoCoordinates bestClientCoordinatesApproximation;

    private class SerializableGeoCoordinates extends GeoCoordinates {
        /**
         * Creates new {@link GeoCoordinates}.
         *
         * @param x the longitude, must not be {@code null}.
         * @param y the latitude, must not be {@code null}.
         */
        public SerializableGeoCoordinates(Number x, Number y) {
            super(x, y);
        }
    }
}
