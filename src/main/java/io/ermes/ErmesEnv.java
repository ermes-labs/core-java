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

import org.jetbrains.annotations.NotNull;

/**
 * This class contains all the environment variables used by the application.
 *
 * @since 0.0.1
 * @author Paolo Longo
 */
public final class ErmesEnv {
    public static final class Redis {
        public final static String host = getenv("REDIS_HOST");
        public final static String password = getenv("REDIS_PASSWORD");
        public final static String port = getenv("REDIS_PORT");
        public final static String redisUri = "redis://" + password + "@" + host + ":" + port;
    }

    public static final class Infrastructure {
        public final static String locationId = getenv("LOCATION_ID");
    }

    public static final class HTTP {
        public final static String sessionTokenHTTPCookieName = getenvOrDefault("ERMES-SESSION-TOKEN-HTTP-COOKIE-NAME", "ermes-session-token");
    }

    public static final class Config {
        public final static int sessionDuration = getenvOrDefault("ERMES-SESSION-DURATION", Integer::parseInt, (60 * 60 * 24 * 7)); // 1 week
    }

    private static @NotNull String getenv(@NotNull String key) {
        String value = System.getenv(key);
        assert value != null : "Environment variable " + key + " must be set.";
        return value;
    }

    // Get env with a parser String -> Integer
    private static <T> @NotNull T getenv(@NotNull String key, @NotNull Parser<T> parser) {
        String value = System.getenv(key);
        try {
            return parser.parse(value);
        } catch (Exception e) {
            throw new RuntimeException("Cannot correctly parse environment variable " + key + ".", e);
        }
    }

    private static @NotNull String getenvOrDefault(@NotNull String key, @NotNull String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    private static <T> @NotNull T getenvOrDefault(@NotNull String key, @NotNull Parser<T> parser, @NotNull T defaultValue) {
        String value = System.getenv(key);
        try {
            return value != null ? parser.parse(value) : defaultValue;
        } catch (Exception e) {
            throw new RuntimeException("Cannot correctly parse environment variable " + key + ".", e);
        }
    }

    private interface Parser<T> {
        T parse(@NotNull String value) throws Exception;
    }
}
