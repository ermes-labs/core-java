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

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.reactive.RedisModulesReactiveCommands;
import com.redis.lettucemod.gears.*;
import com.redis.lettucemod.json.*;
import com.redis.lettucemod.output.ExecutionResults;
import com.redis.lettucemod.search.*;
import com.redis.lettucemod.timeseries.CreateOptions;
import com.redis.lettucemod.timeseries.*;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisReactiveCommandsImpl;
import io.lettuce.core.codec.RedisCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@SuppressWarnings("unchecked")
public class RedisErmesModuleReactiveCommandsImpl<K, V> extends RedisReactiveCommandsImpl<K, V>
		implements RedisModulesReactiveCommands<K, V> {

	private final StatefulRedisModulesConnection<K, V> connection;
	private final TimeSeriesCommandBuilder<K, V> timeSeriesCommandBuilder;
	private final GearsCommandBuilder<K, V> gearsCommandBuilder;
	private final SearchCommandBuilder<K, V> searchCommandBuilder;
	private final JSONCommandBuilder<K, V> jsonCommandBuilder;

	public RedisErmesModuleReactiveCommandsImpl(StatefulRedisModulesConnection<K, V> connection, RedisCodec<K, V> codec) {
		super(connection, codec);
		this.connection = connection;
		this.gearsCommandBuilder = new GearsCommandBuilder<>(codec);
		this.timeSeriesCommandBuilder = new TimeSeriesCommandBuilder<>(codec);
		this.searchCommandBuilder = new SearchCommandBuilder<>(codec);
		this.jsonCommandBuilder = new JSONCommandBuilder<>(codec);
	}

	@Override
	public StatefulRedisModulesConnection<K, V> getStatefulConnection() {
		return connection;
	}

	@Override
	public Mono<ExecutionResults> rgPyexecute(String function, V... requirements) {
		return createMono(() -> gearsCommandBuilder.pyExecute(function, requirements));
	}

	@Override
	public Mono<String> rgPyexecuteUnblocking(String function, V... requirements) {
		return createMono(() -> gearsCommandBuilder.pyExecuteUnblocking(function, requirements));
	}

	@Override
	public Flux<Object> rgTrigger(String trigger, V... args) {
		return createDissolvingFlux(() -> gearsCommandBuilder.trigger(trigger, args));
	}

	@Override
	public Mono<String> rgUnregister(String id) {
		return createMono(() -> gearsCommandBuilder.unregister(id));
	}

	@Override
	public Mono<String> rgAbortexecution(String id) {
		return createMono(() -> gearsCommandBuilder.abortExecution(id));
	}

	@Override
	public Flux<V> rgConfigget(K... keys) {
		return createDissolvingFlux(() -> gearsCommandBuilder.configGet(keys));
	}

	@Override
	public Flux<V> rgConfigset(Map<K, V> map) {
		return createDissolvingFlux(() -> gearsCommandBuilder.configSet(map));
	}

	@Override
	public Mono<String> rgDropexecution(String id) {
		return createMono(() -> gearsCommandBuilder.dropExecution(id));
	}

	@Override
	public Flux<Execution> rgDumpexecutions() {
		return createDissolvingFlux(gearsCommandBuilder::dumpExecutions);
	}

	@Override
	public Flux<Registration> rgDumpregistrations() {
		return createDissolvingFlux(gearsCommandBuilder::dumpRegistrations);
	}

	@Override
	public Mono<ExecutionDetails> rgGetexecution(String id) {
		return createMono(() -> gearsCommandBuilder.getExecution(id));
	}

	@Override
	public Mono<ExecutionDetails> rgGetexecution(String id, ExecutionMode mode) {
		return createMono(() -> gearsCommandBuilder.getExecution(id, mode));
	}

	@Override
	public Mono<ExecutionResults> rgGetresults(String id) {
		return createMono(() -> gearsCommandBuilder.getResults(id));
	}

	@Override
	public Mono<ExecutionResults> rgGetresultsblocking(String id) {
		return createMono(() -> gearsCommandBuilder.getResultsBlocking(id));
	}

	@Override
	public Mono<String> tsCreate(K key, CreateOptions<K, V> options) {
		return createMono(() -> timeSeriesCommandBuilder.create(key, options));
	}

	@Override
	public Mono<String> tsAlter(K key, AlterOptions<K, V> options) {
		return createMono(() -> timeSeriesCommandBuilder.alter(key, options));
	}

	@Override
	public Mono<Long> tsAdd(K key, Sample sample) {
		return createMono(() -> timeSeriesCommandBuilder.add(key, sample));
	}

	@Override
	public Mono<Long> tsAdd(K key, Sample sample, AddOptions<K, V> options) {
		return createMono(() -> timeSeriesCommandBuilder.add(key, sample, options));
	}

	@Override
	public Mono<Long> tsIncrby(K key, double value) {
		return createMono(() -> timeSeriesCommandBuilder.incrby(key, value, null));
	}

	@Override
	public Mono<Long> tsIncrby(K key, double value, IncrbyOptions<K, V> options) {
		return createMono(() -> timeSeriesCommandBuilder.incrby(key, value, options));
	}

	@Override
	public Mono<Long> tsDecrby(K key, double value) {
		return createMono(() -> timeSeriesCommandBuilder.decrby(key, value, null));
	}

	@Override
	public Mono<Long> tsDecrby(K key, double value, IncrbyOptions<K, V> options) {
		return createMono(() -> timeSeriesCommandBuilder.decrby(key, value, options));
	}

	@Override
	public Flux<Long> tsMadd(KeySample<K>... samples) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.madd(samples));
	}

	@Override
	public Mono<String> tsCreaterule(K sourceKey, K destKey, CreateRuleOptions options) {
		return createMono(() -> timeSeriesCommandBuilder.createRule(sourceKey, destKey, options));
	}

	@Override
	public Mono<String> tsDeleterule(K sourceKey, K destKey) {
		return createMono(() -> timeSeriesCommandBuilder.deleteRule(sourceKey, destKey));
	}

	@Override
	public Flux<Sample> tsRange(K key, TimeRange range) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.range(key, range));
	}

	@Override
	public Flux<Sample> tsRange(K key, TimeRange range, RangeOptions options) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.range(key, range, options));
	}

	@Override
	public Flux<Sample> tsRevrange(K key, TimeRange range) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.revrange(key, range));
	}

	@Override
	public Flux<Sample> tsRevrange(K key, TimeRange range, RangeOptions options) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.revrange(key, range, options));
	}

	@Override
	public Flux<RangeResult<K, V>> tsMrange(TimeRange range) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.mrange(range));
	}

	@Override
	public Flux<RangeResult<K, V>> tsMrange(TimeRange range, MRangeOptions<K, V> options) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.mrange(range, options));
	}

	@Override
	public Flux<RangeResult<K, V>> tsMrevrange(TimeRange range) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.mrevrange(range));
	}

	@Override
	public Flux<RangeResult<K, V>> tsMrevrange(TimeRange range, MRangeOptions<K, V> options) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.mrevrange(range, options));
	}

	@Override
	public Mono<Sample> tsGet(K key) {
		return createMono(() -> timeSeriesCommandBuilder.get(key));
	}

	@Override
	public Flux<GetResult<K, V>> tsMget(MGetOptions<K, V> options) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.mget(options));
	}

	@Override
	public Flux<GetResult<K, V>> tsMget(V... filters) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.mget(filters));
	}

	@Override
	public Flux<GetResult<K, V>> tsMgetWithLabels(V... filters) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.mgetWithLabels(filters));
	}

	@Override
	public Flux<Object> tsInfo(K key) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.info(key, false));
	}

	@Override
	public Flux<Object> tsInfoDebug(K key) {
		return createDissolvingFlux(() -> timeSeriesCommandBuilder.info(key, true));
	}

	@Override
	public Flux<V> tsQueryIndex(V... filters){return createDissolvingFlux(()->timeSeriesCommandBuilder.queryIndex(filters));}

	@Override
	public Mono<Long> tsDel(K key, TimeRange timeRange) { return createMono(()->timeSeriesCommandBuilder.tsDel(key, timeRange));}

	@Override
	public Mono<String> ftCreate(K index, Field<K>... fields) {
		return ftCreate(index, null, fields);
	}

	@Override
	public Mono<String> ftCreate(K index, com.redis.lettucemod.search.CreateOptions<K, V> options, Field<K>... fields) {
		return createMono(() -> searchCommandBuilder.create(index, options, fields));
	}

	@Override
	public Mono<String> ftDropindex(K index) {
		return createMono(() -> searchCommandBuilder.dropIndex(index, false));
	}

	@Override
	public Mono<String> ftDropindexDeleteDocs(K index) {
		return createMono(() -> searchCommandBuilder.dropIndex(index, true));
	}

	@Override
	public Flux<Object> ftInfo(K index) {
		return createDissolvingFlux(() -> searchCommandBuilder.info(index));
	}

	@Override
	public Mono<SearchResults<K, V>> ftSearch(K index, V query) {
		return createMono(() -> searchCommandBuilder.search(index, query, null));
	}

	@Override
	public Mono<SearchResults<K, V>> ftSearch(K index, V query, SearchOptions<K, V> options) {
		return createMono(() -> searchCommandBuilder.search(index, query, options));
	}

	@Override
	public Mono<AggregateResults<K>> ftAggregate(K index, V query) {
		return createMono(() -> searchCommandBuilder.aggregate(index, query, null));
	}

	@Override
	public Mono<AggregateResults<K>> ftAggregate(K index, V query, AggregateOptions<K, V> options) {
		return createMono(() -> searchCommandBuilder.aggregate(index, query, options));
	}

	@Override
	public Mono<AggregateWithCursorResults<K>> ftAggregate(K index, V query, CursorOptions cursor) {
		return createMono(() -> searchCommandBuilder.aggregate(index, query, cursor, null));
	}

	@Override
	public Mono<AggregateWithCursorResults<K>> ftAggregate(K index, V query, CursorOptions cursor,
			AggregateOptions<K, V> options) {
		return createMono(() -> searchCommandBuilder.aggregate(index, query, cursor, options));
	}

	@Override
	public Mono<AggregateWithCursorResults<K>> ftCursorRead(K index, long cursor) {
		return createMono(() -> searchCommandBuilder.cursorRead(index, cursor, null));
	}

	@Override
	public Mono<AggregateWithCursorResults<K>> ftCursorRead(K index, long cursor, long count) {
		return createMono(() -> searchCommandBuilder.cursorRead(index, cursor, count));
	}

	@Override
	public Mono<String> ftCursorDelete(K index, long cursor) {
		return createMono(() -> searchCommandBuilder.cursorDelete(index, cursor));
	}

	@Override
	public Mono<Long> ftSugadd(K key, Suggestion<V> suggestion) {
		return createMono(() -> searchCommandBuilder.sugadd(key, suggestion));
	}

	@Override
	public Mono<Long> ftSugaddIncr(K key, Suggestion<V> suggestion) {
		return createMono(() -> searchCommandBuilder.sugaddIncr(key, suggestion));
	}

	@Override
	public Flux<Suggestion<V>> ftSugget(K key, V prefix) {
		return createDissolvingFlux(() -> searchCommandBuilder.sugget(key, prefix));
	}

	@Override
	public Flux<Suggestion<V>> ftSugget(K key, V prefix, SuggetOptions options) {
		return createDissolvingFlux(() -> searchCommandBuilder.sugget(key, prefix, options));
	}

	@Override
	public Mono<Boolean> ftSugdel(K key, V string) {
		return createMono(() -> searchCommandBuilder.sugdel(key, string));
	}

	@Override
	public Mono<Long> ftSuglen(K key) {
		return createMono(() -> searchCommandBuilder.suglen(key));
	}

	@Override
	public Mono<String> ftAlter(K index, Field<K> field) {
		return createMono(() -> searchCommandBuilder.alter(index, field));
	}

	@Override
	public Mono<String> ftAliasadd(K name, K index) {
		return createMono(() -> searchCommandBuilder.aliasAdd(name, index));
	}

	@Override
	public Mono<String> ftAliasupdate(K name, K index) {
		return createMono(() -> searchCommandBuilder.aliasUpdate(name, index));
	}

	@Override
	public Mono<String> ftAliasdel(K name) {
		return createMono(() -> searchCommandBuilder.aliasDel(name));
	}

	@Override
	public Flux<K> ftList() {
		return createDissolvingFlux(searchCommandBuilder::list);
	}

	@Override
	public Flux<V> ftTagvals(K index, K field) {
		return createDissolvingFlux(() -> searchCommandBuilder.tagVals(index, field));
	}

	@Override
	public Mono<Long> ftDictadd(K dict, V... terms) {
		return createMono(() -> searchCommandBuilder.dictadd(dict, terms));
	}

	@Override
	public Mono<Long> ftDictdel(K dict, V... terms) {
		return createMono(() -> searchCommandBuilder.dictdel(dict, terms));
	}

	@Override
	public Flux<V> ftDictdump(K dict) {
		return createDissolvingFlux(() -> searchCommandBuilder.dictdump(dict));
	}

	@Override
	public Mono<Long> jsonDel(K key) {
		return jsonDel(key, null);
	}

	@Override
	public Mono<Long> jsonDel(K key, String path) {
		return createMono(() -> jsonCommandBuilder.del(key, path));
	}

	@Override
	public Mono<V> jsonGet(K key, K... paths) {
		return jsonGet(key, null, paths);
	}

	@Override
	public Mono<V> jsonGet(K key, GetOptions options, K... paths) {
		return createMono(() -> jsonCommandBuilder.get(key, options, paths));
	}

	@Override
	public Flux<KeyValue<K, V>> jsonMget(String path, K... keys) {
		return createDissolvingFlux(() -> jsonCommandBuilder.mgetKeyValue(path, keys));
	}

	public Flux<KeyValue<K, V>> jsonMget(String path, Iterable<K> keys) {
		return createDissolvingFlux(() -> jsonCommandBuilder.mgetKeyValue(path, keys));
	}

	@Override
	public Mono<String> jsonSet(K key, String path, V json) {
		return jsonSet(key, path, json, null);
	}

	@Override
	public Mono<String> jsonSet(K key, String path, V json, SetMode mode) {
		return createMono(() -> jsonCommandBuilder.set(key, path, json, mode));
	}

	@Override
	public Mono<String> jsonType(K key) {
		return jsonType(key, null);
	}

	@Override
	public Mono<String> jsonType(K key, String path) {
		return createMono(() -> jsonCommandBuilder.type(key, path));
	}

	@Override
	public Mono<V> jsonNumincrby(K key, String path, double number) {
		return createMono(() -> jsonCommandBuilder.numIncrBy(key, path, number));
	}

	@Override
	public Mono<V> jsonNummultby(K key, String path, double number) {
		return createMono(() -> jsonCommandBuilder.numMultBy(key, path, number));
	}

	@Override
	public Mono<Long> jsonStrappend(K key, V json) {
		return jsonStrappend(key, null, json);
	}

	@Override
	public Mono<Long> jsonStrappend(K key, String path, V json) {
		return createMono(() -> jsonCommandBuilder.strAppend(key, path, json));
	}

	@Override
	public Mono<Long> jsonStrlen(K key, String path) {
		return createMono(() -> jsonCommandBuilder.strLen(key, path));
	}

	@Override
	public Mono<Long> jsonArrappend(K key, String path, V... jsons) {
		return createMono(() -> jsonCommandBuilder.arrAppend(key, path, jsons));
	}

	@Override
	public Mono<Long> jsonArrindex(K key, String path, V scalar) {
		return jsonArrindex(key, path, scalar, null);
	}

	@Override
	public Mono<Long> jsonArrindex(K key, String path, V scalar, Slice slice) {
		return createMono(() -> jsonCommandBuilder.arrIndex(key, path, scalar, slice));
	}

	@Override
	public Mono<Long> jsonArrinsert(K key, String path, long index, V... jsons) {
		return createMono(() -> jsonCommandBuilder.arrInsert(key, path, index, jsons));
	}

	@Override
	public Mono<Long> jsonArrlen(K key) {
		return jsonArrlen(key, null);
	}

	@Override
	public Mono<Long> jsonArrlen(K key, String path) {
		return createMono(() -> jsonCommandBuilder.arrLen(key, path));
	}

	@Override
	public Mono<V> jsonArrpop(K key) {
		return jsonArrpop(key, null);
	}

	@Override
	public Mono<V> jsonArrpop(K key, ArrpopOptions<K> options) {
		return createMono(() -> jsonCommandBuilder.arrPop(key, options));
	}

	@Override
	public Mono<Long> jsonArrtrim(K key, String path, long start, long stop) {
		return createMono(() -> jsonCommandBuilder.arrTrim(key, path, start, stop));
	}

	@Override
	public Flux<K> jsonObjkeys(K key) {
		return jsonObjkeys(key, null);
	}

	@Override
	public Flux<K> jsonObjkeys(K key, String path) {
		return createDissolvingFlux(() -> jsonCommandBuilder.objKeys(key, path));
	}

	@Override
	public Mono<Long> jsonObjlen(K key) {
		return jsonObjlen(key, null);
	}

	@Override
	public Mono<Long> jsonObjlen(K key, String path) {
		return createMono(() -> jsonCommandBuilder.objLen(key, path));
	}

}
