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

package io.ermes.daos;

import io.ermes.api.ErmesRedisCommands;
import io.lettuce.core.RedisException;
import io.lettuce.core.ScriptOutputType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.ermes.ErmesEnv;
import io.ermes.Parser;
import io.ermes.Session;
import io.ermes.redis.ErmesRedisKey;

import java.util.UUID;

public final class ErmesRedisDAOSession {
    private static final Logger logger = LoggerFactory.getLogger(ErmesRedisDAOSession.class);
    private static ErmesRedisKey getSessionsSetKey() {
        // Ordered-set containing all the sessions, ordered by expiration time.
        return ErmesRedisKey.getConfigKey("sessionsSet");
    }

    private static ErmesRedisKey getSessionDataKey(byte[] sessionId) {
        return ErmesRedisKey.getSessionMetadataKey(sessionId, "data");
    }

    private static ErmesRedisKey getIsOffloadingKey(byte[] sessionId) {
        return ErmesRedisKey.getSessionMetadataKey(sessionId, "isOffloading");
    }

    public static @NotNull Session createSession(ErmesRedisCommands commands) {
        Session session = new Session();
        byte[] sessionId;

        do {
            sessionId = Parser.b(UUID.randomUUID().toString());
        } while (!commands.setnx(getSessionDataKey(sessionId), Parser.toJSON(session)));

        return session;
    }

    public static void voidCleanUpSession(byte[] sessionId, ErmesRedisCommandsSync commands) {
        commands.del(getSessionsDataKey(sessionId));
        commands.del(getIsOffloadingKey(sessionId));
        commands.del(getLockKey(sessionId));
    }

    public static Session getSession(byte[] sessionId, boolean executionDuringOffload, ErmesRedisCommandsSync commands) throws UnableToUseSessionDuringOffloadException {
        if (executionDuringOffload) {
            commands.set()
        }

        final String script = """
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return redis.error_reply("Unable to acquire lock during offloading.")
            else
                redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[2])
                return "OK"
            end
            """;

        try {
            commands.eval(
                script,
                ScriptOutputType.VALUE,
                new ErmesRedisKey[]{getIsOffloadingKey(sessionId), getSessionDataKey(sessionId)},
                Parser.toJSON("session"), Parser.toJSON(ErmesEnv.Config.sessionDuration));
        } catch (RedisException e) {
            throw new UnableToUseSessionDuringOffloadException("Unable to acquire or release lock for session.");
        }
    }

    @SuppressWarnings("unchecked")
    public static void releaseLock(byte[] sessionId, ErmesRedisCommandsSync<String, byte[]> commands) {
        commands.del(getLockKey(sessionId));
    }

    static final private class UnableToUseSessionDuringOffloadException extends Exception {
        UnableToUseSessionDuringOffloadException(String message) {
            super(message);
        }
    }
}
