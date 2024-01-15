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

import com.redis.lettucemod.search.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RedisErmesModuleReactiveCommands<K, V> {

	@SuppressWarnings("unchecked")
	Mono<String> ftCreate(K index, Field<K>... fields);

	@SuppressWarnings("unchecked")
	Mono<String> ftCreate(K index, CreateOptions<K, V> options, Field<K>... fields);

	Mono<String> ftDropindex(K index);

	Mono<String> ftDropindexDeleteDocs(K index);

	Mono<String> ftAlter(K index, Field<K> field);

	Flux<Object> ftInfo(K index);

	Mono<String> ftAliasadd(K name, K index);

	Mono<String> ftAliasupdate(K name, K index);

	Mono<String> ftAliasdel(K name);

	Flux<K> ftList();

	Mono<SearchResults<K, V>> ftSearch(K index, V query);

	Mono<SearchResults<K, V>> ftSearch(K index, V query, SearchOptions<K, V> options);

	Mono<AggregateResults<K>> ftAggregate(K index, V query);

	Mono<AggregateResults<K>> ftAggregate(K index, V query, AggregateOptions<K, V> options);

	Mono<AggregateWithCursorResults<K>> ftAggregate(K index, V query, CursorOptions cursor);

	Mono<AggregateWithCursorResults<K>> ftAggregate(K index, V query, CursorOptions cursor,
			AggregateOptions<K, V> options);

	Mono<AggregateWithCursorResults<K>> ftCursorRead(K index, long cursor);

	Mono<AggregateWithCursorResults<K>> ftCursorRead(K index, long cursor, long count);

	Mono<String> ftCursorDelete(K index, long cursor);

	Flux<V> ftTagvals(K index, K field);

	Mono<Long> ftSugadd(K key, Suggestion<V> suggestion);

	Mono<Long> ftSugaddIncr(K key, Suggestion<V> suggestion);

	Flux<Suggestion<V>> ftSugget(K key, V prefix);

	Flux<Suggestion<V>> ftSugget(K key, V prefix, SuggetOptions options);

	Mono<Boolean> ftSugdel(K key, V string);

	Mono<Long> ftSuglen(K key);

	@SuppressWarnings("unchecked")
	Mono<Long> ftDictadd(K dict, V... terms);

	@SuppressWarnings("unchecked")
	Mono<Long> ftDictdel(K dict, V... terms);

	Flux<V> ftDictdump(K dict);

}
