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

package io.ermes.redis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class to represent a Redis key with a keyspace represented by a single character, an optional session and a nullable
 * key. In the context of serverless and mainly in edge computing, we should limit the number of connections to the Redis
 * server. Since using different databases to differentiate the keyspace would require a different connection for each
 * database, we can use keyspace to separate the data.
 *
 * @author Paolo Longo
 * @since 0.0.1
 * @see ErmesRedisKeyspace
 */
public final class ErmesRedisKey {
    /**
     * The logger.
     */
    public static final Logger logger = LoggerFactory.getLogger(ErmesRedisCodec.class);
    /**
     * The charset.
     */
    private static final Charset charset = StandardCharsets.UTF_8;
    /**
     * Session length
     */
    private static final int sessionIdLength = 36;

    /**
     * The keyspace.
     */
    private final ErmesRedisKeyspace ermesRedisKeyspace;
    /**
     * The session associated with the key.
     */
    private final byte @Nullable [] sessionId;
    /**
     * The key.
     */
    private final byte @Nullable [] key;

    /**
     * Create a new Redis key with an id and a key.
     *
     * @param ermesRedisKeyspace The keyspace.
     * @param sessionId      The session.
     * @param key       The key.
     */
    private ErmesRedisKey(@NotNull ErmesRedisKeyspace ermesRedisKeyspace, byte @Nullable [] sessionId, byte @Nullable [] key) {
        assert ermesRedisKeyspace.isSessionSpecific() == (sessionId != null) : "The session must be set for all and only session specific keyspaces.";
        assert sessionId == null || sessionId.length == sessionIdLength : "The session must be " + sessionIdLength + " bytes long.";

        this.ermesRedisKeyspace = ermesRedisKeyspace;
        this.sessionId = sessionId;
        this.key = key;
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Session keyspace.
     *
     * @param session The session.
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getSessionKey(byte @NotNull [] session, byte @Nullable [] key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.Session, session, key);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Session keyspace.
     *
     * @param session The session.
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getSessionKey(byte @NotNull [] session, @Nullable String key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.Session, session, key == null ? null : key.getBytes(charset));
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Node keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getNodeKey(byte @Nullable [] key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.Node, null, key);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Node keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getNodeKey(@Nullable String key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.Node, null, key == null ? null : key.getBytes(charset));
    }

    /**
     * Create a new {@link ErmesRedisKey} with the SessionMetadata keyspace.
     *
     * @param session The session.
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getSessionMetadataKey(byte @NotNull [] session, byte @Nullable [] key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.SessionMetadata, session, key);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the SessionMetadata keyspace.
     *
     * @param session The session.
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getSessionMetadataKey(byte @NotNull [] session, @Nullable String key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.SessionMetadata, session, key == null ? null : key.getBytes(charset));
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Config keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getConfigKey(byte @Nullable [] key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.Config, null, key);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Config keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getConfigKey(@Nullable String key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.Config, null, key == null ? null : key.getBytes(charset));
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Infrastructure keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getInfrastructureKey(byte @Nullable [] key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.Infrastructure, null, key);
    }

    /**
     * Create a new {@link ErmesRedisKey} with the Infrastructure keyspace.
     *
     * @param key The key.
     * @return The {@link ErmesRedisKey}.
     */
    public static @NotNull ErmesRedisKey getInfrastructureKey(@Nullable String key) {
        return new ErmesRedisKey(ErmesRedisKeyspace.Infrastructure, null, key == null ? null : key.getBytes(charset));
    }

    /**
     * Decode the key output by redis.
     *
     * @param bytes Raw bytes of the key.
     * @return the decoded key, the ServerlessRedisKey key may be {@code null}.
     */
    public static @Nullable ErmesRedisKey decode(@NotNull ByteBuffer bytes) {
        // Check if the buffer has at least 2 bytes.
        if (bytes.remaining() < 2 && bytes.get(1) != ':') {
            logger.error("Error decoding a key, returning null.");
            return null;
        }

        // Getting the keyspace.
        char keyspace = (char) bytes.get();
        // It must be a valid keyspace.
        ErmesRedisKeyspace ermesRedisKeyspace = ErmesRedisKeyspace.valueOf(keyspace);
        // If the keyspace is not valid, return null.
        if (ermesRedisKeyspace == null) {
            logger.error("Error decoding a key, the keyspace is not valid, returning null.");
            return null;
        }

        // Setting position to skip the keyspace and the colon.
        bytes.position(bytes.position() + 1);
        // The session.
        byte[] session = null;

        // If the keyspace is session-specific, read the session.
        if (ermesRedisKeyspace.isSessionSpecific()) {
            if (bytes.remaining() < sessionIdLength + 1) {
                logger.error("Error decoding a key, the session is not valid, returning null.");
                return null;
            }

            session = new byte[sessionIdLength];
            // Read the session bytes.
            bytes.get(session);
            // Move the cursor to the next byte
            bytes.position(bytes.position() + 1);
        }

        // Get the remaining bytes in an array
        byte[] key = new byte[bytes.remaining()];
        bytes.get(key);
        // Return the key.
        return new ErmesRedisKey(ermesRedisKeyspace, session, key);
    }

    /**
     * Encode the key for output to redis.
     *
     * @param ermesRedisKey the key.
     * @return the decoded value.
     */
    public static @NotNull ByteBuffer encode(@Nullable ErmesRedisKey ermesRedisKey) {
        // If the key is null, use the default keyspace.
        if (ermesRedisKey == null) {
            logger.error("Error encoding a key, using the node keyspace with a null key.");
            return encode(ErmesRedisKey.getNodeKey((byte[]) null));
        }

        int size = 2
            + (ermesRedisKey.sessionId == null ? 0 : sessionIdLength + 1)
            + (ermesRedisKey.key == null ? 0 : ermesRedisKey.key.length + 1);

        // Create a byte buffer of the exact size.
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        // Add the keyspace.
        byteBuffer.put((byte) ermesRedisKey.getKeyspaceId());
        byteBuffer.put((byte) ':');
        // Add the session.
        if (ermesRedisKey.sessionId != null) {
            byteBuffer.put(ermesRedisKey.sessionId);
            byteBuffer.put((byte) ':');
        }
        // Add the key.
        if (ermesRedisKey.key != null) {
            byteBuffer.put(ermesRedisKey.key);
        }
        // Return the buffer.
        return byteBuffer;
    }

    /**
     * Get the keyspace name.
     *
     * @return the keyspace name.
     */
    char getKeyspaceId() {
        return this.ermesRedisKeyspace.getId();
    }

    /**
     * Get the keyspace name.
     *
     * @return the keyspace name.
     */
    public String getKeyspaceName() {
        return this.ermesRedisKeyspace.name();
    }

    /**
     * Check if the keyspace is session-specific.
     *
     * @return {@code true} if the keyspace is session-specific, {@code false} otherwise.
     */
    boolean isSessionSpecific() {
        return this.ermesRedisKeyspace.isSessionSpecific();
    }

    /**
     * Get the session.
     *
     * @return the session.
     */
    byte @Nullable [] getSessionId() {
        return this.sessionId;
    }

    /**
     * Get the session.
     *
     * @return the session.
     */
    @Nullable String getSessionString() {
        return this.sessionId == null ? null : new String(this.sessionId, charset);
    }

    /**
     * Get the key.
     *
     * @return the key.
     */
    public byte @Nullable [] getKey() {
        return this.key;
    }

    /**
     * Get the key as a string.
     *
     * @return the key.
     */
    public @Nullable String getKeyString() {
        return this.key == null ? null : new String(this.key, charset);
    }

    /**
     * Get the string representation of the Redis key.
     *
     * @return the string representation of the Redis key.
     */
    @Override
    public @NotNull String toString() {
        return this.ermesRedisKeyspace.getId() + ":" +
                (this.sessionId == null ? "" : (new String(this.sessionId, charset) + ":")) +
                (this.key == null ? "" : new String(this.key, charset));
    }

    /**
     * Get the hash code of the Redis key.
     *
     * @return the hash code of the Redis key.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.ermesRedisKeyspace.getId(), Arrays.hashCode(this.key));
    }

    /**
     * Check if the Redis key is equal to another object.
     *
     * @param obj The object to compare to.
     * @return {@code true} if the Redis key is equal to the object, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        // If the object is null or not an instance of ServerlessRedisKey, return false.
        if (!(obj instanceof ErmesRedisKey ermesRedisKey)) return false;
        // If the object is the same instance as this, return true.
        if (obj == this) return true;
        // Check if the keyspace and the key are equal.
        return Objects.equals(this.ermesRedisKeyspace, ermesRedisKey.ermesRedisKeyspace) &&
                Arrays.equals(this.sessionId, ermesRedisKey.sessionId) &&
                Arrays.equals(this.key, ermesRedisKey.key);
    }

    /**
     * Class to represent a Redis key with a keyspace and a generic key. In the context of serverless and mainly in edge
     * computing, we should limit the number of connections to the Redis server. Since there can be only one connection per
     * Redis database, we can limit the number of connections by using a single database and using keyspace to separate
     * the data. The keyspace is represented by a single character and may be session-specific.
     *
     * @since 0.0.1
     * @see ErmesRedisKey
     * @author Paolo Longo
     */
    enum ErmesRedisKeyspace {
        Session('S', true),
        SessionMetadata('M', true),
        Node('N', false),
        Config('C', false),
        Infrastructure('I', false);

        /**
         * The keyspace.
         */
        private final char id;
        /**
         * If the keyspace is session-specific.
         */
        private final boolean sessionSpecific;

        /**
         * Map of keyspace.
         */
        private static final @NotNull Map<@NotNull Character, @NotNull ErmesRedisKeyspace> map =
            EnumSet.allOf(ErmesRedisKeyspace.class)
                .stream()
                .collect(Collectors.toMap(ErmesRedisKeyspace::getId, e -> e));

        /**
         * Get the keyspace from a character.
         *
         * @param keyspace The keyspace.
         * @return The keyspace.
         */
        static @Nullable ErmesRedisKeyspace valueOf(char keyspace) {
            return map.get(keyspace);
        }

        /**
         * Create a new ErmesRedisKeyspace.
         *
         * @param id The keyspace.
         * @param sessionSpecific If the keyspace is session-specific.
         */
        ErmesRedisKeyspace(char id, boolean sessionSpecific) {
            this.id = id;
            this.sessionSpecific = sessionSpecific;
        }

        /**
         * Get the keyspace.
         *
         * @return The keyspace.
         */
        char getId() {
            return this.id;
        }

        /**
         * If the keyspace is session-specific.
         *
         * @return {@code true} if the keyspace is session-specific, {@code false} otherwise.
         */
        boolean isSessionSpecific() {
            return this.sessionSpecific;
        }
    }
}
