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

import io.lettuce.core.KeyScanArgs;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.CommandArgs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This class represents the arguments for the SCAN command, it extends {@link KeyScanArgs} to add the ability to filter
 * by key space. It can be used in {@link RedisCommands#scan}.
 *
 * @since 0.0.1
 * @see ScanArgs
 * @see KeyScanArgs
 * @see ErmesRedisCommandsSync#scan
 * @author Paolo Longo
 */
public class KeyspaceScanArgs extends KeyScanArgs {
    /**
     * The logger.
     */    
    private static final Logger logger = LoggerFactory.getLogger(KeyspaceScanArgs.class);
    /**
     * The provided match filter.
     */
    private byte @NotNull [] providedMatch = {'*'};
    /**
     * The key space match filter.
     */
    private byte @NotNull [] keyspaceMatch = {};
    /**
     * The type of keys to return.
     */
    private @Nullable String type;
    /**
     * Create a new {@link KeyspaceScanArgs} that matches all the keys of the current session as default.
     */
    public KeyspaceScanArgs() {
    }

    public static class Builder {
        /**
         * Utility constructor.
         */
        private Builder() {
        }

        /**
         * Creates new {@link KeyspaceScanArgs} with {@literal LIMIT} set.
         *
         * @param count number of elements to scan
         * @return new {@link KeyspaceScanArgs} with {@literal LIMIT} set.
         * @see KeyspaceScanArgs#limit(long)
         */
        public static  KeyspaceScanArgs limit(long count) {
            return new KeyspaceScanArgs().limit(count);
        }

        /**
         * Creates new {@link KeyspaceScanArgs} with {@literal MATCH} set.
         *
         * @param match the filter.
         * @return new {@link KeyspaceScanArgs} with {@literal MATCH} set.
         * @see KeyspaceScanArgs#match(String)
         */
        public static  KeyspaceScanArgs match(byte @NotNull [] match) {
            return new KeyspaceScanArgs().match(match);
        }

        /**
         * Creates new {@link KeyspaceScanArgs} with {@literal MATCH} set.
         *
         * @param match the filter.
         * @return new {@link KeyspaceScanArgs} with {@literal MATCH} set.
         * @see KeyspaceScanArgs#match(String)
         */
        public static  KeyspaceScanArgs match(@NotNull String match) {
            return new KeyspaceScanArgs().match(match);
        }

        /**
         * Creates new {@link KeyspaceScanArgs} with {@literal MATCH} set.
         *
         * @param match the filter.
         * @param charset the charset for match.
         * @return new {@link KeyspaceScanArgs} with {@literal MATCH} set.
         * @see KeyspaceScanArgs#match(String)
         */
        public static  KeyspaceScanArgs match(@NotNull String match, @NotNull Charset charset) {
            return new KeyspaceScanArgs().match(match, charset);
        }

        /**
         * Creates new {@link KeyspaceScanArgs} with {@literal TYPE} set.
         *
         * @param type the filter.
         * @return new {@link KeyspaceScanArgs} with {@literal TYPE} set.
         * @see KeyspaceScanArgs#type(String)
         */
        public static  KeyspaceScanArgs type(@NotNull String type) {
            return new KeyspaceScanArgs().type(type);
        }
    }

    /**
     * Return keys only of a specified type.
     *
     * @param type of keys as returned by TYPE command
     * @return {@literal this} {@link KeyspaceScanArgs}.
     */
    public KeyspaceScanArgs type(@NotNull String type) {
        super.type(type);
        return this;
    }

    /**
     * Set the match filter. This filter is merged with the key space filter.
     *
     * @param match the filter.
     * @return {@literal this} {@link KeyspaceScanArgs}.
     */
    @Override
    public KeyspaceScanArgs match(byte @NotNull [] match) {
        this.providedMatch = match;
        this.composeMatch();
        return this;
    }

    /**
     * Set the match filter. Uses {@link StandardCharsets#UTF_8 UTF-8} to encode {@code match}. This filter is merged
     * with the key space filter.
     *
     * @param match the filter.
     * @return {@literal this} {@link KeyspaceScanArgs}.
     */
    @Override
    public KeyspaceScanArgs match(@NotNull String match) {
        this.providedMatch = match.getBytes(StandardCharsets.UTF_8);
        this.composeMatch();
        return this;
    }

    /**
     * Set the match filter along the given {@link Charset}. This filter is merged with the key space filter.
     *
     * @param match the filter.
     * @param charset the charset for match.
     * @return {@literal this} {@link KeyspaceScanArgs}.
     */
    @Override
    public KeyspaceScanArgs match(@NotNull String match, @NotNull Charset charset) {
        this.providedMatch = match.getBytes(charset);
        this.composeMatch();
        return this;
    }

    /**
     * Set the key space.
     *
     * @param ermesRedisKey The key space.
     * @return {@literal this} {@link KeyspaceScanArgs}.
     */
    public KeyspaceScanArgs setKeyspace(@NotNull ErmesRedisKey ermesRedisKey) {
        this.keyspaceMatch = ErmesRedisKey.encode(ermesRedisKey).array();
        this.composeMatch();
        return this;
    }

    /**
     * Set the match filter.
     */
    private void composeMatch() {
        // Merge the 2 arrays into one, providedMatch and keySpaceMatch
        byte[] match = new byte[this.providedMatch.length + this.keyspaceMatch.length];
        System.arraycopy(this.providedMatch, 0, match, 0, this.providedMatch.length);
        System.arraycopy(this.keyspaceMatch, 0, match, this.providedMatch.length, this.keyspaceMatch.length);
        super.match(match);
    }

    /**
     * Limit the scan by count
     *
     * @param count number of elements to scan
     * @return {@literal this} {@link KeyspaceScanArgs}.
     */
    @Override
    public KeyspaceScanArgs limit(long count) {
        super.limit(count);
        return this;
    }

    /**
     * Build the arguments.
     *
     * @param args The {@link CommandArgs}.
     */
    @Override
    public <CK, CV> void build(@NotNull CommandArgs<CK, CV> args) {
        super.build(args);

        if (this.keyspaceMatch.length == 0) {
            logger.error("No keyspace set, it won't be added to the command.");
        }
    }

    /**
     * Generate a {@link KeyspaceScanArgs} from a {@link ScanArgs}.
     *
     * @param scanArgs The {@link ScanArgs}.
     * @return The {@link KeyspaceScanArgs}.
     */
    protected static KeyspaceScanArgs from(@NotNull ScanArgs scanArgs) {
        KeyspaceScanArgs keyspaceScanArgs = new KeyspaceScanArgs();
        ScanArgsReader.copy(scanArgs, keyspaceScanArgs);
        return keyspaceScanArgs;
    }

    /**
     * A {@link CommandArgs} implementation that reads the arguments from a {@link ScanArgs}.
     *
     * @since 0.0.1
     * @see ScanArgs
     * @see KeyScanArgs
     * @author Paolo Longo
     */
    private static final class ScanArgsReader extends CommandArgs<String, String> {
        /**
         * The {@link ScanArgs} to read from.
         */
        private final @NotNull ScanArgs scanArgs;

        /**
         * Copy the arguments from a {@link ScanArgs} or {@link KeyScanArgs} to a {@link ScanArgs} or {@link KeyScanArgs}.
         *
         * @param from The {@link ScanArgs} or {@link KeyScanArgs} to copy from.
         * @param to The {@link ScanArgs} or {@link KeyScanArgs} to copy to.
         */
        static void copy(@NotNull ScanArgs from, @NotNull ScanArgs to) {
            from.build(new ScanArgsReader(to));
        }
        /**
         * Utility constructor.
         */
        private ScanArgsReader(@NotNull ScanArgs scanArgs) {
            super(StringCodec.UTF8);
            this.scanArgs = scanArgs;
        }

        /**
         * Add an argument.
         *
         * @param n the argument.
         * @return {@literal this} {@link CommandArgs}.
         */
        @Override
        public CommandArgs<String, String> add(long n) {
            this.scanArgs.limit(n);
            return this;
        }

        /**
         * Add an argument.
         *
         * @param value the byte-array.
         * @return {@literal this} {@link CommandArgs}.
         */
        @Override
        public CommandArgs<String, String> add(byte @NotNull[] value) {
            this.scanArgs.match(value);
            return this;
        }

        /**
         * Add an argument.
         *
         * @param s the string.
         * @return {@literal this} {@link CommandArgs}.
         */
        @Override
        public CommandArgs<String, String> add(@NotNull String s) {
            if (this.scanArgs instanceof KeyScanArgs keyScanArgs) {
                keyScanArgs.type(s);
            }

            return this;
        }
    }
}
