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

import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import io.ermes.api.ErmesRedisAsyncCommands;
import io.ermes.api.ErmesRedisCommands;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to connect to the Redis database, and its design decisions are made to be used in a serverless
 * environment. By default, each Thread can access any number of command objects using the try-with-resources statement.
 * The statement scope defines the command and its connection lifetime, and, in case of nested scopes, the connection is
 * shared among commands and kept open until the last (higher) scope is closed. Instead of using different databases
 * that would require a connection for each database, we enforce the use of different prefixes for the keys. This allows
 * using a single connection. To create a new client, use the following code:
 *
 * <pre>{@code
 * 		ErmesRedisClient redisClient = ErmesRedisClient.create(redisURI);
 * }</pre>
 * <p>
 * It is possible to require sync and async commands by using the following code:
 *
 * <pre>{@code
 *     try (ErmesRedisCommands commands = redisClient.sync()) {
 *     		commands.set(key, "value");
 *     }
 * }</pre>
 * <p>
 * In the following code, the two different command objects will share the same connection that will be closed after
 * the top level scope is closed:
 *
 * <pre>{@code
 *     try (ErmesRedisCommands commands1 = redisClient.sync()) {
 *     		commands1.set(key, "value");
 *
 *     		try (ErmesRedisCommands commands2 = redisClient.sync()) {
 *     			commands2.set(key2, "value2");
 *          }
 *     }
 * }</pre>
 * <p>
 * An example with threads. Note that more complex and dynamic scenarios are possible by accessing the commands outside
 * the try-with-resource and by using a simple try-finally block.
 *
 * <pre>{@code
 * 		try (ErmesRedisCommands commands = redisClient.sync(),
 * 				ErmesRedisAsyncCommands asyncCommands = redisClient.async()) {
 *
 * 			Thread thread1 = startThread(commands);
 * 		    Thread thread2 = startThread(asyncCommands);
 *
 * 		    awaitThreads(thread1, thread2);
 *        }
 * }</pre>
 * <p>
 * It is possible and recommended most of the time to share the connection between different threads (see <a href="https://lettuce.io/core/release/reference/#asynchronous-api.impact-of-asynchronicity-to-the-synchronous-api">lettuce.io</a>).
 * However, as stated in the documentation, this is not suitable for some cases. This is the reason why the default
 * behavior is to create a new connection for each thread, but to also allow easily to share command objects with the
 * same connection. Here's a piece of documentation from lettuce.io:
 * <br>
 * <cite>
 * "The only difference (between sync and async commands) is a blocking behavior of the caller that is using the
 * synchronous API. Blocking happens on command level and affects only the command completion part, meaning multiple
 * clients using the synchronous API can invoke commands on the same connection and at the same time without
 * blocking each other. A call on the synchronous API is unblocked at the moment a command response was processed."
 * <br>
 * We can use a shared connection for most of the operations in the same database. Basically a single connection
 * acts as a pool of connections, but better because there is no overhead for the handshake. Note that the
 * connection object automatically reconnect after disconnections.
 * <br>
 * However, as stated in the documentation, this is not suitable for some cases, and therefore we allow to create
 * a new connection for those cases.
 * "However, there are some cases you should not share a connection among threads to avoid side effects. The cases are:
 * - Disabling flush-after-command to improve performance
 * - The use of blocking operations like BLPOP. Blocking operations are queued on Redis until they can be executed.
 * While one connection is blocked, other connections can issue commands to Redis. Once a command unblocks the
 * blocking command (that said an LPUSH or RPUSH hits the list), the blocked connection is unblocked and can
 * proceed after that.
 * - Transactions
 * - Using multiple databases"
 * </cite> *
 * 
 * @see ErmesRedisConnection
 * @see ErmesRedisCommandsImpl
 * @see ErmesRedisKey
 * @author Paolo Longo
 * @since 0.0.1
 */
public class ErmesRedisClient {
    /**
     * The redis client
     */
    private final @NotNull RedisModulesClient redisClient;
    /**
     * A map of the singleton instances, one for each thread.
     */
    private final @NotNull Map<Long, ErmesRedisConnection> instances = new HashMap<>(2);

    /**
     * Create a new ServerlessRedisClient.
     *
     * @param redisClient The Redis client.
     */
    private ErmesRedisClient(@NotNull RedisModulesClient redisClient) {
        this.redisClient = redisClient;
    }

    /**
     * Create a new client that connects to the supplied uri with default {@link ClientResources}.You can connect to
     * different Redis servers, but you must supply a {@link RedisURI} on connecting.
     *
     * @return The ServerlessRedisClient.
     */
    public static  ErmesRedisClient create(@NotNull String uri) {
        return new ErmesRedisClient(RedisModulesClient.create(RedisURI.create(uri)));
    }

    /**
     * Create a new client that connects to the supplied {@link RedisURI uri} with default {@link ClientResources}.
     * You can connect to different Redis servers, but you must supply a {@link RedisURI} on connecting.
     *
     * @return The ServerlessRedisClient.
     */
    public static  ErmesRedisClient create(@NotNull RedisURI redisURI) {
        return new ErmesRedisClient(RedisModulesClient.create(redisURI));
    }

    /**
     * Create a new client that connects to the supplied uri with shared {@link ClientResources} and the supplied.
     * You need to shut down the {@link ClientResources} upon shutting down your application. You can connect to
     * different Redis servers, but you must supply a {@link RedisURI} on connecting.
     *
     * @param clientResources The client resources.
     * @return The ServerlessRedisClient.
     */
    public static  ErmesRedisClient create(@NotNull ClientResources clientResources, @NotNull String uri) {
        return new ErmesRedisClient(RedisModulesClient.create(clientResources, RedisURI.create(uri)));
    }

    /**
     * Create a new client that connects to the supplied {@link RedisURI uri} with shared {@link ClientResources}.
     * You need to shut down the {@link ClientResources} upon shutting down your application. You can connect to
     * different Redis servers, but you must supply a {@link RedisURI} on connecting.
     *
     * @param clientResources The client resources.
     * @return The ServerlessRedisClient.
     */
    public static  ErmesRedisClient create(@NotNull ClientResources clientResources, @NotNull RedisURI redisURI) {
        return new ErmesRedisClient(RedisModulesClient.create(clientResources, redisURI));
    }

    /**
     * Create and return a sync command object using the default Redis URI. If a connection exists for the current
     * thread, it is used; otherwise a new connection is created. If no default URI is set, an exception is thrown.
     *
     * @return The sync command object.
     */
    public @NotNull ErmesRedisCommands sync() {
        return ErmesRedisCommandsImpl.sync(this.getConnection());
    }

    /**
     * Create and return an async command object using the default Redis URI. If a connection exists for the current
     * thread, it is used; otherwise a new connection is created. If no default URI is set, an exception is thrown.
     *
     * @return The async command object.
     */
    public @NotNull ErmesRedisAsyncCommands async() {
        return ErmesRedisCommandsImpl.async(this.getConnection());
    }

    /**
     * Return the connection for the current thread, or create a new one if it does not exist.
     *
     * @return The connection.
     */
    private synchronized @NotNull ErmesRedisConnection getConnection() {
        Long threadId = Thread.currentThread().getId();
        // Get the singleton instances for the current thread.
        ErmesRedisConnection ermesRedisConnection = this.instances.get(threadId);

        if (ermesRedisConnection == null) {
            // Create a new connection.
            StatefulRedisModulesConnection<ErmesRedisKey, byte[]> statefulRedisConnection = this.redisClient.connect(new ErmesRedisCodec());
            ermesRedisConnection = new ErmesRedisConnection(threadId, this, statefulRedisConnection);
        }

        return ermesRedisConnection;
    }

    /**
     * Add a connection to the map of instances.
     *
     * @param ermesRedisConnection The connection to add.
     */
    protected void addConnection(ErmesRedisConnection ermesRedisConnection) {
        this.instances.put(ermesRedisConnection.getThreadId(), ermesRedisConnection);
    }

    /**
     * Remove a connection from the map of instances.
     *
     * @param ermesRedisConnection The connection to remove.
     */
    protected void removeConnection(ErmesRedisConnection ermesRedisConnection) {
        this.instances.remove(ermesRedisConnection.getThreadId());
    }

    /**
     * Close all the connections to the Redis database and shutdown the Redis client.
     */
    public synchronized void shutdown() {
        this.instances.forEach((__, connection) -> connection.close());
        this.instances.clear();
        this.redisClient.shutdown();
    }
}
