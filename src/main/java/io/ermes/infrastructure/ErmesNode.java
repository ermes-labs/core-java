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
 * @param openfaasGateway
 * @param openfaasPassword
 * @param redisHost
 * @param redisPort
 * @param redisPassword
 * @param locationId
 */

// @formatter:off
public record ErmesNode(
        @Nullable String id,
        @NotNull  String openfaasGateway,
        @NotNull  String openfaasPassword,
        @NotNull  String redisHost,
                  int    redisPort,
        @NotNull  String redisPassword) {

    @JsonCreator
    public ErmesNode (@JsonProperty(value = "locationId")                        @Nullable String id,
                      @JsonProperty(value = "openfaasGateway",  required = true) @NotNull  String openfaasGateway,
                               @JsonProperty(value = "openfaasPassword", required = true) @NotNull  String openfaasPassword,
                               @JsonProperty(value = "redisHost",        required = true) @NotNull  String redisHost,
                               @JsonProperty(value = "redisPort",        required = true)           int    redisPort,
                               @JsonProperty(value = "redisPassword",    required = true) @NotNull  String redisPassword) {
        assert openfaasGateway  != null;
        assert openfaasPassword != null;
        assert redisHost        != null;
        assert redisPassword    != null;

        this.locationId       = locationId;
        this.openfaasGateway  = openfaasGateway;
        this.openfaasPassword = openfaasPassword;
        this.redisHost        = redisHost;
        this.redisPort        = redisPort;
        this.redisPassword    = redisPassword;
    }
}
