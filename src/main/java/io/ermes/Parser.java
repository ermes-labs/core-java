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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Parser {
    /**
     * The {@link ObjectMapper} used to convert objects to JSON.
     */
    private static final @NotNull ObjectMapper objectMapper = new ObjectMapper().disable(SerializationFeature.INDENT_OUTPUT);
    /**
     * The charset used to convert strings to byte arrays and vice versa.
     */
    private static final @NotNull Charset charset = StandardCharsets.UTF_8;

    /**
     * Convert a byte array to a string.
     *
     * @param bytes The byte array.
     * @return The string.
     */
    public static String s(byte @Nullable [] bytes) {
        if (bytes == null) {
            return null;
        }

        return new String(bytes, charset);
    }

    /**
     * Convert a string to a byte array.
     *
     * @param string The string.
     * @return The byte array.
     */
    public static byte[] b(@Nullable String string) {
        if (string == null) {
            return null;
        }

        return string.getBytes(charset);
    }

    /**
     * Convert an object to a JSON byte array.
     *
     * @param value The object.
     * @return The JSON byte array.
     */
    public static byte[] toJSON(@Nullable Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
        try {
            return objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        try {
            return objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
