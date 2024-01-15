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

package io.ermes.infrastructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param areas is an array of Area objects. Every location object has the fields: location_id, openfaas_gateway,
 *              openfaas_password, redis_host, redis_port, redis_password.
 */
public record Area(
        @NotNull String             areaName,
        @NotNull ErmesNode ermesNode,
        @NotNull Area[]             areas) {
    @JsonCreator
    public Area(@JsonProperty(value = "areaName",     required = true) @NotNull  String areaName,
                @JsonProperty(value = "mainLocation", required = true) @NotNull ErmesNode ermesNode,
                @JsonProperty(value = "areas")                         @Nullable Area[] areas) {
        assert areaName != null;
        assert ermesNode != null;

        this.areaName = areaName;
        this.ermesNode = ermesNode;
        this.areas = areas == null ? new Area[0] : areas;
    }
}