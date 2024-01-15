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

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErmesSessionToken {
    final private static @NotNull Gson gson = new Gson();
    final private @NotNull String sessionId;
    final private @NotNull String proprietaryLocationId;
    final private @NotNull String currentLocationId;
    final private @NotNull String timestampCreation;
    final private @NotNull String timestampLastAccess;

    public ErmesSessionToken(@NotNull String sessionId, @NotNull String locationId, @NotNull String timestamp) {
        this.sessionId = sessionId;
        this.proprietaryLocationId = locationId;
        this.currentLocationId = locationId;
        this.timestampCreation = timestamp;
        this.timestampLastAccess = timestamp;
    }

    public static ErmesSessionToken buildFromJSON(String json) {
        return ErmesSessionToken.gson.fromJson(json, ErmesSessionToken.class);
    }

    public static String getJson(ErmesSessionToken ermesSessionToken) {
        return ErmesSessionToken.gson.toJson(ermesSessionToken);
    }

    public static ErmesSessionToken getSessionToken(@NotNull String sessionId) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String now = LocalDateTime.now(Clock.systemUTC()).format(formatter);
        return new ErmesSessionToken(sessionId, ErmesEnv.locationId, now);
    }
}