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

import java.util.List;

public interface RedisErmesModuleCommands<K, V> {

	@SuppressWarnings("unchecked")
	String ftCreate(K index, Field<K>... fields);

	@SuppressWarnings("unchecked")
	String ftCreate(K index, CreateOptions<K, V> options, Field<K>... fields);

	String ftDropindex(K index);

	String ftDropindexDeleteDocs(K index);

	String ftAlter(K index, Field<K> field);

	List<Object> ftInfo(K index);

	String ftAliasadd(K name, K index);

	String ftAliasupdate(K name, K index);

	String ftAliasdel(K name);

	/**
	 * 
	 * @return List of RediSearch indexes
	 */
	List<K> ftList();

	SearchResults<K, V> ftSearch(K index, V query);

	SearchResults<K, V> ftSearch(K index, V query, SearchOptions<K, V> options);

	AggregateResults<K> ftAggregate(K index, V query);

	AggregateResults<K> ftAggregate(K index, V query, AggregateOptions<K, V> options);

	AggregateWithCursorResults<K> ftAggregate(K index, V query, CursorOptions cursor);

	AggregateWithCursorResults<K> ftAggregate(K index, V query, CursorOptions cursor, AggregateOptions<K, V> options);

	AggregateWithCursorResults<K> ftCursorRead(K index, long cursor);

	AggregateWithCursorResults<K> ftCursorRead(K index, long cursor, long count);

	String ftCursorDelete(K index, long cursor);

	List<V> ftTagvals(K index, K field);

	Long ftSugadd(K key, Suggestion<V> suggestion);

	Long ftSugaddIncr(K key, Suggestion<V> suggestion);

	List<Suggestion<V>> ftSugget(K key, V prefix);

	List<Suggestion<V>> ftSugget(K key, V prefix, SuggetOptions options);

	Boolean ftSugdel(K key, V string);

	Long ftSuglen(K key);

	@SuppressWarnings("unchecked")
	Long ftDictadd(K dict, V... terms);

	@SuppressWarnings("unchecked")
	Long ftDictdel(K dict, V... terms);

	List<V> ftDictdump(K dict);
}
