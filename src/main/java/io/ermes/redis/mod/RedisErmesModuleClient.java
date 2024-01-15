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
import com.redis.lettucemod.util.RedisModulesClientBuilder;
import io.lettuce.core.*;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.protocol.PushHandler;
import io.lettuce.core.resource.ClientResources;

import java.time.Duration;

/**
 * A scalable and thread-safe client for
 * <a href="https://redis.io/modules">Redis Modules</a> supporting synchronous,
 * asynchronous and reactive execution models. Multiple threads may share one
 * connection if they avoid blocking and transactional operations such as BLPOP
 * and MULTI/EXEC.
 * <p>
 * {@link RedisErmesModuleClient} can be used with:
 * <ul>
 * <li>Redis Standalone</li>
 * </ul>
 *
 * <p>
 * {@link RedisErmesModuleClient} is an expensive resource. It holds a set of
 * netty's {@link io.netty.channel.EventLoopGroup}'s that use multiple threads.
 * Reuse this instance as much as possible or share a {@link ClientResources}
 * instance amongst multiple client instances.
 *
 * @author Julien Ruaux
 * @see RedisURI
 * @see StatefulRedisModulesConnection
 * @see RedisFuture
 * @see reactor.core.publisher.Mono
 * @see reactor.core.publisher.Flux
 * @see RedisCodec
 * @see ClientOptions
 * @see ClientResources
 * @see MasterReplica
 */
public class RedisErmesModuleClient extends RedisClient {

	public static final ClientOptions DEFAULT_CLIENT_OPTIONS = ClientOptions.builder()
			.protocolVersion(ProtocolVersion.RESP2).build();

	protected RedisErmesModuleClient(ClientResources clientResources, RedisURI redisURI) {
		super(clientResources, redisURI);
		setOptions(DEFAULT_CLIENT_OPTIONS);
	}

	/**
	 * Creates a uri-less RedisModulesClient. You can connect to different Redis
	 * servers but you must supply a {@link RedisURI} on connecting. Methods without
	 * having a {@link RedisURI} will fail with a
	 * {@link IllegalStateException}. Non-private constructor to make
	 * {@link RedisErmesModuleClient} proxyable.
	 */
	protected RedisErmesModuleClient() {
		super();
		setOptions(DEFAULT_CLIENT_OPTIONS);
	}

	public static RedisModulesClientBuilder builder(RedisURI redisURI) {
		return RedisModulesClientBuilder.create(redisURI);
	}

	/**
	 * Creates a uri-less RedisModulesClient with default {@link ClientResources}.
	 * You can connect to different Redis servers but you must supply a
	 * {@link RedisURI} on connecting. Methods without having a {@link RedisURI}
	 * will fail with a {@link IllegalStateException}.
	 *
	 * @return a new instance of {@link RedisErmesModuleClient}
	 */
	public static RedisErmesModuleClient create() {
		return new RedisErmesModuleClient();
	}

	/**
	 * Create a new client that connects to the supplied {@link RedisURI uri} with
	 * default {@link ClientResources}. You can connect to different Redis servers
	 * but you must supply a {@link RedisURI} on connecting.
	 *
	 * @param redisURI the Redis URI, must not be {@code null}
	 * @return a new instance of {@link RedisErmesModuleClient}
	 */
	public static RedisErmesModuleClient create(RedisURI redisURI) {
		assertNotNull(redisURI);
		return new RedisErmesModuleClient(null, redisURI);
	}

	/**
	 * Create a new client that connects to the supplied uri with default
	 * {@link ClientResources}. You can connect to different Redis servers but you
	 * must supply a {@link RedisURI} on connecting.
	 *
	 * @param uri the Redis URI, must not be {@code null}
	 * @return a new instance of {@link RedisErmesModuleClient}
	 */
	public static RedisErmesModuleClient create(String uri) {
		LettuceAssert.notEmpty(uri, "URI must not be empty");
		return new RedisErmesModuleClient(null, RedisURI.create(uri));
	}

	/**
	 * Creates a uri-less RedisModulesClient with shared {@link ClientResources}.
	 * You need to shut down the {@link ClientResources} upon shutting down your
	 * application. You can connect to different Redis servers but you must supply a
	 * {@link RedisURI} on connecting. Methods without having a {@link RedisURI}
	 * will fail with a {@link IllegalStateException}.
	 *
	 * @param clientResources the client resources, must not be {@code null}
	 * @return a new instance of {@link RedisErmesModuleClient}
	 */
	public static RedisErmesModuleClient create(ClientResources clientResources) {
		assertNotNull(clientResources);
		return new RedisErmesModuleClient(clientResources, new RedisURI());
	}

	/**
	 * Create a new client that connects to the supplied uri with shared
	 * {@link ClientResources}.You need to shut down the {@link ClientResources}
	 * upon shutting down your application. You can connect to different Redis
	 * servers but you must supply a {@link RedisURI} on connecting.
	 *
	 * @param clientResources the client resources, must not be {@code null}
	 * @param uri             the Redis URI, must not be {@code null}
	 *
	 * @return a new instance of {@link RedisErmesModuleClient}
	 */
	public static RedisErmesModuleClient create(ClientResources clientResources, String uri) {
		assertNotNull(clientResources);
		LettuceAssert.notEmpty(uri, "URI must not be empty");
		return create(clientResources, RedisURI.create(uri));
	}

	private static void assertNotNull(ClientResources clientResources) {
		LettuceAssert.notNull(clientResources, "ClientResources must not be null");
	}

	/**
	 * Create a new client that connects to the supplied {@link RedisURI uri} with
	 * shared {@link ClientResources}. You need to shut down the
	 * {@link ClientResources} upon shutting down your application.You can connect
	 * to different Redis servers but you must supply a {@link RedisURI} on
	 * connecting.
	 *
	 * @param clientResources the client resources, must not be {@code null}
	 * @param redisURI        the Redis URI, must not be {@code null}
	 * @return a new instance of {@link RedisErmesModuleClient}
	 */
	public static RedisErmesModuleClient create(ClientResources clientResources, RedisURI redisURI) {
		assertNotNull(clientResources);
		assertNotNull(redisURI);
		return new RedisErmesModuleClient(clientResources, redisURI);
	}

	/**
	 * Open a new connection to a Redis server that treats keys and values as UTF-8
	 * strings.
	 *
	 * @return A new stateful Redis connection
	 */
	@Override
	public StatefulRedisModulesConnection<String, String> connect() {
		return connect(newStringStringCodec());
	}

	/**
	 * Open a new connection to a Redis server. Use the supplied {@link RedisCodec
	 * codec} to encode/decode keys and values.
	 *
	 * @param codec Use this codec to encode/decode keys and values, must not be
	 *              {@code null}
	 * @param <K>   Key type
	 * @param <V>   Value type
	 * @return A new stateful Redis connection
	 */
	@Override
	public <K, V> StatefulRedisModulesConnection<K, V> connect(RedisCodec<K, V> codec) {
		return (StatefulRedisModulesConnection<K, V>) super.connect(codec);
	}

	/**
	 * Open a new connection to a Redis server using the supplied {@link RedisURI}
	 * that treats keys and values as UTF-8 strings.
	 *
	 * @param redisURI the Redis server to connect to, must not be {@code null}
	 * @return A new connection
	 */
	@Override
	public StatefulRedisModulesConnection<String, String> connect(RedisURI redisURI) {
		return (StatefulRedisModulesConnection<String, String>) super.connect(redisURI);
	}

	/**
	 * Open a new connection to a Redis server using the supplied {@link RedisURI}
	 * and the supplied {@link RedisCodec codec} to encode/decode keys.
	 *
	 * @param codec    Use this codec to encode/decode keys and values, must not be
	 *                 {@code null}
	 * @param redisURI the Redis server to connect to, must not be {@code null}
	 * @param <K>      Key type
	 * @param <V>      Value type
	 * @return A new connection
	 */
	@Override
	public <K, V> StatefulRedisModulesConnection<K, V> connect(RedisCodec<K, V> codec, RedisURI redisURI) {
		return (StatefulRedisModulesConnection<K, V>) super.connect(codec, redisURI);
	}

	private static void assertNotNull(RedisURI redisURI) {
		LettuceAssert.notNull(redisURI, "RedisURI must not be null");
	}

	/**
	 * Create a new instance of {@link StatefulRedisErmesModuleConnectionImpl} or a
	 * subclass.
	 * <p>
	 * Subclasses of {@link RedisErmesModuleClient} may override that method.
	 *
	 * @param channelWriter the channel writer
	 * @param codec         codec
	 * @param timeout       default timeout
	 * @param <K>           Key-Type
	 * @param <V>           Value Type
	 * @return new instance of StatefulRedisModulesConnectionImpl
	 */
	@Override
	protected <K, V> StatefulRedisErmesModuleConnectionImpl<K, V> newStatefulRedisConnection(
			RedisChannelWriter channelWriter, PushHandler pushHandler, RedisCodec<K, V> codec, Duration timeout) {
		return new StatefulRedisErmesModuleConnectionImpl<>(channelWriter, pushHandler, codec, timeout);
	}

}
