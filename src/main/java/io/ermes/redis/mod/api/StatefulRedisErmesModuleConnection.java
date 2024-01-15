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

package io.ermes.redis.mod.api;

import io.ermes.redis.mod.api.async.RedisModulesAsyncCommands;
import io.ermes.redis.mod.api.reactive.RedisModulesReactiveCommands;
import io.ermes.redis.mod.api.sync.RedisModulesCommands;
import io.lettuce.core.api.StatefulRedisConnection;

public interface StatefulRedisErmesModuleConnection<K, V> extends StatefulRedisConnection<K, V> {

	RedisModulesCommands<K, V> sync();

	RedisModulesAsyncCommands<K, V> async();

	RedisModulesReactiveCommands<K, V> reactive();
}
