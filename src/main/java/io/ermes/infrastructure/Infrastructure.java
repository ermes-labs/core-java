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

import java.io.IOException;

// formatter:off
public record Infrastructure(
        @NotNull String[] areaTypesIdentifiers,
        @NotNull Area[][]   hierarchy) {

    @JsonCreator
    public Infrastructure (@JsonProperty(value = "areaTypesIdentifiers", required = true) @NotNull String[] areaTypesIdentifiers,
                           @JsonProperty(value = "hierarchy",            required = true) @NotNull Area[]   hierarchy) {
        assert areaTypesIdentifiers != null;
        assert hierarchy            != null;

        this.areaTypesIdentifiers = areaTypesIdentifiers;
        this.hierarchy            = hierarchy;
    }
}