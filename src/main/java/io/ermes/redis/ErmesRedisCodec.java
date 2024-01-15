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

import io.lettuce.core.codec.RedisCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A ServerlessRedisCodec encoding keys and values sent to Redis, and decodes keys and values in the command output. The
 * keys are of type {@link ErmesRedisKey}.
 *
 * @author Paolo Longo
 * @since 0.0.1
 */
public class ErmesRedisCodec implements RedisCodec<ErmesRedisKey, byte[]> {
    /**
     * Empty byte array.
     */
    private static final byte[] EMPTY = new byte[0];

    /**
     * Decode the key output by redis.
     *
     * @param bytes Raw bytes of the key.
     * @return the decoded key.
     */
    @Override
    public @Nullable ErmesRedisKey decodeKey(@NotNull ByteBuffer bytes) {
        return ErmesRedisKey.decode(bytes);
    }

    /**
     * Decode the value output by redis.
     *
     * @param bytes Raw bytes of the value.
     * @return The decoded value.
     */
    @Override
    public byte @Nullable [] decodeValue(@NotNull ByteBuffer bytes) {
        return getBytes(bytes);
    }

    /**
     * Encode the key for output to redis.
     *
     * @param ermesRedisKey the key.
     * @return the decoded value.
     */
    @Override
    public @NotNull ByteBuffer encodeKey(@Nullable ErmesRedisKey ermesRedisKey) {
        return ErmesRedisKey.encode(ermesRedisKey);
    }

    /**
     * Encode the value for output to redis.
     *
     * @param value the value.
     * @return The encoded value.
     */
    @Override
    public @NotNull ByteBuffer encodeValue(byte @Nullable [] value) {
        return ByteBuffer.wrap(Objects.requireNonNullElse(value, EMPTY));
    }

    /**
     * Get the remaining bytes from the buffer.
     *
     * @param buffer The buffer.
     * @return The remaining bytes.
     */
    private static byte @NotNull [] getBytes(@NotNull ByteBuffer buffer) {
        int remaining = buffer.remaining();

        if (remaining == 0) {
            return EMPTY;
        }

        byte[] b = new byte[remaining];
        buffer.get(b);
        return b;
    }
}