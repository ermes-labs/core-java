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

import io.ermes.api.ErmesRedisAsyncCommands;
import io.ermes.api.ErmesRedisCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class is used to create a {@link Proxy} instance for the {@link ErmesRedisCommands} and {@link ErmesRedisAsyncCommands}
 * interfaces.
 *
 * @author Paolo Longo
 * @since 0.0.1
 */
class ErmesRedisCommandsImpl implements InvocationHandler, AutoCloseable {
    /**
     * The connection to the Redis database.
     */
    private final @NotNull ErmesRedisConnection ermesRedisConnection;
    /**
     * The command object for the currently selected database, either {@link com.redis.lettucemod.api.sync.RedisModulesCommands}
     * or {@link com.redis.lettucemod.api.async.RedisModulesAsyncCommands}.
     */
    private final @NotNull Object commands;
    /**
     * The status of the connection.
     */
    private boolean isClosed = false;

    /**
     * Create a new connection to the Redis database.
     */
    private ErmesRedisCommandsImpl(
            @NotNull ErmesRedisConnection ermesRedisConnection,
            @NotNull Object commands) {
        this.ermesRedisConnection = ermesRedisConnection;
        this.commands = commands;

        ermesRedisConnection.acquire();
    }

    /**
     * Create and return a Proxy instance of the sync command object using this class as InvocationHandler.
     *
     * @param ermesRedisConnection The connection to the Redis database.
     * @return The sync command object.
     */
    public static  @NotNull ErmesRedisCommands sync(@NotNull ErmesRedisConnection ermesRedisConnection) {
        return (ErmesRedisCommands) Proxy.newProxyInstance(
                ErmesRedisCommands.class.getClassLoader(),
                new Class<?>[]{ErmesRedisCommands.class},
                new ErmesRedisCommandsImpl(ermesRedisConnection, ermesRedisConnection.getStatefulRedisConnection().sync()));
    }

    /**
     * Create and return a Proxy instance of the async command object using this class as InvocationHandler.
     *
     * @param ermesRedisConnection The connection to the Redis database.
     * @return The async command object.
     */
    public static  @NotNull ErmesRedisAsyncCommands async(@NotNull ErmesRedisConnection ermesRedisConnection) {
        return (ErmesRedisAsyncCommands) Proxy.newProxyInstance(
                ErmesRedisAsyncCommands.class.getClassLoader(),
                new Class<?>[]{ErmesRedisAsyncCommands.class},
                new ErmesRedisCommandsImpl(ermesRedisConnection, ermesRedisConnection.getStatefulRedisConnection().async()));
    }

    /**
     * Invoke the specified method on the command object.
     *
     * @param proxy  The proxy instance that the method was invoked on.
     * @param method The {@code Method} instance corresponding to the interface method invoked on the proxy instance.
     * @param args   An array of objects containing the values of the arguments passed in the method invocation on the proxy instance, or {@code null} if interface method takes no arguments.
     * @return The value to return from the method invocation on the proxy instance.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object... args) throws InvocationTargetException, IllegalAccessException {
        // If commands belong to the AutoCloseable interface, then invoke the method on this object.
        if (RedisCommands.class.isAssignableFrom(method.getDeclaringClass())) return method.invoke(this, args);
        // If the connection is closed, throw an exception.
        if (this.isClosed) throw new IllegalStateException("Connection is not open.");
        // Invoke the method on the command object.
        return method.invoke(this.commands, args);
    }

    /**
     * Close the connection to the Redis database.
     */
    public synchronized void close() {
        if (!this.isClosed) {
            this.ermesRedisConnection.close();
            this.isClosed = true;
        }
    }
}
