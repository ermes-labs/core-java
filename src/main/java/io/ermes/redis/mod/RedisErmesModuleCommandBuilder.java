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

package io.ermes.redis.mod;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.BaseRedisCommandBuilder;
import io.lettuce.core.protocol.CommandArgs;

public class RedisErmesModuleCommandBuilder<K, V> extends BaseRedisCommandBuilder<K, V> {

	private static final String MUST_NOT_BE_NULL = "must not be null";
	private static final String MUST_NOT_BE_EMPTY = "must not be empty";

	protected CommandArgs<K, V> args(K key) {
		notNullKey(key);
		return new CommandArgs<>(codec).addKey(key);
	}

	protected RedisErmesModuleCommandBuilder(RedisCodec<K, V> codec) {
		super(codec);
	}

	protected static void notNull(Object arg, String name) {
		LettuceAssert.notNull(arg, name + " " + MUST_NOT_BE_NULL);
	}

	protected static void notNull(KeyStreamingChannel<?> channel) {
		LettuceAssert.notNull(channel, "KeyValueStreamingChannel " + MUST_NOT_BE_NULL);
	}

	protected static void notNull(ValueStreamingChannel<?> channel) {
		LettuceAssert.notNull(channel, "ValueStreamingChannel " + MUST_NOT_BE_NULL);
	}

	protected static void notNull(KeyValueStreamingChannel<?, ?> channel) {
		LettuceAssert.notNull(channel, "KeyValueStreamingChannel " + MUST_NOT_BE_NULL);
	}

	protected static void notEmptyKeys(Object[] keys) {
		notNull(keys, "Keys");
		LettuceAssert.notEmpty(keys, "Keys " + MUST_NOT_BE_EMPTY);
	}

	protected static void notEmptyValues(Object[] values) {
		notNull(values, "Values");
		LettuceAssert.notEmpty(values, "Values " + MUST_NOT_BE_EMPTY);
	}

	protected static void notEmpty(Object[] array, String name) {
		notNull(array, name);
		LettuceAssert.notEmpty(array, name + " " + MUST_NOT_BE_EMPTY);
	}

	protected static void notNullKey(Object key) {
		notNull(key, "Key");
	}

}
