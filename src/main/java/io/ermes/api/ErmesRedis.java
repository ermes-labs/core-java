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

package io.ermes.api;

import io.ermes.ErmesEnv;
import io.ermes.Parser;
import io.ermes.redis.ErmesRedisClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.ermes.redis.ErmesRedisKey;

/**
 * The Redis API. This class is used to manage a singleton instance of the {@link ErmesRedisClient} with some utility methods.
 *
 * @since 0.0.1
 * @see ErmesRedisClient
 * @see ErmesRedisKey
 * @see ErmesRedisCommands
 * @see ErmesRedisAsyncCommands
 * @author Paolo Longo
 */
public final class ErmesRedis {
    /**
     * The Redis client.
     */
    private static @Nullable ErmesRedisClient ermesRedisClient = null;
    /**
     * The current session id.
     */
    private static byte @Nullable [] sessionId = null;

    /**
     * Utility constructor.
     */
    private static @NotNull ErmesRedisClient getErmesRedisClient() {
        if (ErmesRedis.ermesRedisClient == null) {
            ErmesRedis.ermesRedisClient = ErmesRedisClient.create(ErmesEnv.Redis.redisUri);
        }

        return ErmesRedis.ermesRedisClient;
    }

    /**
     * Shutdown the Redis client.
     */
    static void shutdown() {
        if (ErmesRedis.ermesRedisClient != null) {
            ErmesRedis.ermesRedisClient.shutdown();
            ErmesRedis.ermesRedisClient = null;
        }
    }

    /**
     * Get the {@link ErmesRedisCommands} object.
     *
     * @return The {@link ErmesRedisCommands} object.
     */
    public static @NotNull ErmesRedisCommands sync() {
        return ErmesRedis.getErmesRedisClient().sync();
    }

    /**
     * Get the {@link ErmesRedisAsyncCommands} object.
     *
     * @return The {@link ErmesRedisAsyncCommands} object.
     */
    public static @NotNull ErmesRedisAsyncCommands async() {
        return  ErmesRedis.getErmesRedisClient().async();
    }

    /**
     * Get the session id.
     *
     * @return The session id.
     */
    static byte @NotNull [] getSessionId() {
        assert ErmesRedis.sessionId != null : "ErmesRedis.sessionId must be set.";
        return ErmesRedis.sessionId;
    }

    /**
     * Set the session id.
     *
     * @param sessionId The session id.
     */
    static void setSessionId(@NotNull String sessionId) {
        ErmesRedis.sessionId = b(sessionId);
    }

    /**
     * Convert a byte array to a string.
     *
     * @param bytes The byte array.
     * @return The string.
     */
    public static String s(byte @Nullable [] bytes) {
        return Parser.s(bytes);
    }

    /**
     * Convert a string to a byte array.
     *
     * @param string The string.
     * @return The byte array.
     */
    public static byte[] b(@Nullable String string) {
        return Parser.b(string);
    }

    /**
     * Convert an object to a JSON byte array.
     *
     * @param value The object.
     * @return The JSON byte array.
     */
    public static byte[] toJSON(@Nullable Object value) {
        return Parser.toJSON(value);
    }

    /**
     * Convert a JSON byte array to an object.
     *
     * @param src The JSON byte array.
     * @param valueType The object type.
     * @return The object.
     * @param <T> The object type.
     */
    public static <T> T fromJSON(byte @Nullable [] src, @NotNull Class<T> valueType) {
        return Parser.fromJSON(src, valueType);
    }

    /**
     * Convert a JSON byte array to an object.
     *
     * @param src The JSON byte array.
     * @param valueType The object type.
     * @return The object.
     * @param <T> The object type.
     */
    public static <T> T fromJSON(@Nullable String src, @NotNull Class<T> valueType) {
        return Parser.fromJSON(src, valueType);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Session keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey session(@Nullable String key) {
        assert sessionId != null : "ErmesRedis.sessionId must be set.";
        return ErmesRedisKey.getSessionKey(sessionId, key);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Session keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey session(byte @Nullable [] key) {
        assert sessionId != null : "ErmesRedis.sessionId must be set.";
        return ErmesRedisKey.getSessionKey(sessionId, key);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Node keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey node(byte @Nullable [] key) {
        return ErmesRedisKey.getNodeKey(key);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Node keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey node(@Nullable String key) {
        return ErmesRedisKey.getNodeKey(key);
    }
}
