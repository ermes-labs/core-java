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

package io.ermes.api;

import io.ermes.Parser;
import io.ermes.Session;
import io.ermes.redis.ErmesRedisKey;
import io.lettuce.core.GeoCoordinates;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.ScriptOutputType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static io.ermes.Parser.b;

public final class ErmesRedisSessionService {

    private static Exception composeException(@NotNull RedisCommandExecutionException e) {
        if (e.getMessage().startsWith("[Ermes]:")) {
            throw new ErmesRedisSessionException(e.getMessage().substring(8));
        }

        return e;
    }

    public static void createSession () {
        Session session = new Session();
        byte[] sessionId;

        do {
            sessionId = Parser.b(UUID.randomUUID().toString());
        } while (!commands.setnx(getSessionDataKey(sessionId), Parser.toJSON(session)));

        return session;
    }

    public static void create_and_rw_acquire(@NotNull ErmesRedisKey session,
                                             @Nullable GeoCoordinates coordinates,
                                             @NotNull String createdIn,
                                             @Nullable Long expiresAt) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.create_and_rw_acquire",
                ScriptOutputType.STATUS,
                new ErmesRedisKey[]{session},
                coordinates == null ? null : b(coordinates.getX().toString()),
                coordinates == null ? null : b(coordinates.getY().toString()),
                b(createdIn),
                expiresAt == null ? null : b(String.valueOf(expiresAt)));
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void create_and_ro_acquire(@NotNull ErmesRedisKey session,
                                             @Nullable GeoCoordinates coordinates,
                                             @NotNull String createdIn,
                                             @Nullable Long expiresAt) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.create_and_ro_acquire",
                ScriptOutputType.STATUS,
                new ErmesRedisKey[]{session},
                coordinates == null ? null : b(coordinates.getX().toString()),
                coordinates == null ? null : b(coordinates.getY().toString()),
                b(createdIn),
                expiresAt == null ? null : b(String.valueOf(expiresAt)));
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void onload_start(@NotNull ErmesRedisKey session,
                                    @Nullable GeoCoordinates coordinates,
                                    @NotNull String createdIn,
                                    @NotNull Long createdAt,
                                    @Nullable Long updatedAt,
                                    @Nullable Long expiresAt,
                                    byte @NotNull [] sessionData) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.onload_start",
                ScriptOutputType.STATUS,
                new ErmesRedisKey[]{session},
                coordinates == null ? null : b(coordinates.getX().toString()),
                coordinates == null ? null : b(coordinates.getY().toString()),
                expiresAt == null ? null : b(String.valueOf(expiresAt)),
                sessionData);
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void onload_finish(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.onload_finish",
                ScriptOutputType.STATUS,
                session);
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void rw_acquire(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.rw_acquire",
                ScriptOutputType.STATUS,
                session);
            // Catch lettuce redis exception
        } catch (RedisCommandExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ro_acquire(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.ro_acquire",
                ScriptOutputType.STATUS,
                session);
        } catch (RedisCommandExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void rw_release(@NotNull ErmesRedisKey session) throws Exception{
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.rw_acquire",
                ScriptOutputType.STATUS,
                session);
        } catch (RedisCommandExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ro_release(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.ro_acquire",
                ScriptOutputType.STATUS,
                session);
        } catch (RedisCommandExecutionException e) {

        }
    }

    public static void offload_start(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.offload_start",
                ScriptOutputType.STATUS,
                session);
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void offload_finish(@NotNull ErmesRedisKey session,
                                      @NotNull String newEndpoint,
                                      @NotNull String newSessionId) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.offload_finish",
                ScriptOutputType.STATUS,
                new ErmesRedisKey[]{session},
                b(newEndpoint),
                b(newSessionId));
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void offload_cancel(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.offload_cancel",
                ScriptOutputType.STATUS,
                session);
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void delete_session(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.delete_session",
                ScriptOutputType.STATUS,
                session);
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void delete_session_metadata(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.delete_session_metadata",
                ScriptOutputType.STATUS,
                session);
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void set_expire_time(@NotNull ErmesRedisCommands commands,
                                       @NotNull ErmesRedisKey session,
                                       @NotNull Long expiresAt) throws Exception {
        try {
            commands.fcall(
                "ermes.set_expire_time",
                ScriptOutputType.STATUS,
                new ErmesRedisKey[]{session},
                b(String.valueOf(expiresAt)));
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static void update_coordinates(@NotNull ErmesRedisKey session,
                                          @NotNull GeoCoordinates coordinates) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            commands.fcall(
                "ermes.update_coordinates",
                ScriptOutputType.STATUS,
                new ErmesRedisKey[]{session},
                b(coordinates.getX().toString()),
                b(coordinates.getY().toString()));
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static Object get_session_data(@NotNull ErmesRedisKey session) throws Exception {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            return commands.fcall(
                "ermes.get_session_data",
                ScriptOutputType.OBJECT,
                session);
        } catch (RedisCommandExecutionException e) {
            throw composeException(e);
        }
    }

    public static class ErmesRedisSessionException extends RuntimeException {
        public ErmesRedisSessionException(String message) {
            super(message);
        }
    }
}
