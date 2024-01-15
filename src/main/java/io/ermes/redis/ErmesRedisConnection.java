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

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link StatefulRedisModulesConnection} to the Redis database. The connection can be acquired multiple times. The
 * connection is closed when the number of consumers is zero.
 *
 * @author Paolo Longo
 * @see ErmesRedisClient
 * @see StatefulRedisModulesConnection
 * @since 0.0.1
 */
public class ErmesRedisConnection implements AutoCloseable {
    /**
     * The thread id of the thread that created the connection.
     */
    private final long threadId;
    /**
     * The Redis client.
     */
    private final @NotNull ErmesRedisClient ermesRedisClient;
    /**
     * The Redis connection.
     */
    private final @NotNull StatefulRedisModulesConnection<ErmesRedisKey, byte[]> statefulRedisConnection;
    /**
     * The number of consumers of the instance.
     */
    private int consumers = 0;
    /**
     * If the connection has been closed.
     */
    private boolean closed = false;

    /**
     * Create a connection record.
     *
     * @param statefulRedisConnection the Redis connection.
     */
    public ErmesRedisConnection(
            @NotNull Long threadId,
            @NotNull ErmesRedisClient ermesRedisClient,
            @NotNull StatefulRedisModulesConnection<ErmesRedisKey, byte[]> statefulRedisConnection) {
        this.threadId = threadId;
        this.ermesRedisClient = ermesRedisClient;
        this.statefulRedisConnection = statefulRedisConnection;

        ermesRedisClient.addConnection(this);
    }

    /**
     * Get the thread id.
     *
     * @return the thread id.
     */
    public long getThreadId() {
        return this.threadId;
    }

    /**
     * Get the Redis connection.
     *
     * @return the Redis client.
     */
    public @NotNull StatefulRedisModulesConnection<ErmesRedisKey, byte[]> getStatefulRedisConnection() {
        return this.statefulRedisConnection;
    }

    /**
     * Acquire the connection.
     */
    public void acquire() {
        if (this.closed) throw new IllegalStateException("Connection is closed.");
        this.consumers++;
    }

    /**
     * Close the connection.
     */
    @Override
    public void close() {
        if (!this.closed) {
            if (this.consumers == 0 || --this.consumers == 0) {
                this.ermesRedisClient.removeConnection(this);
                this.statefulRedisConnection.close();
                this.closed = true;
            }
        }
    }
}