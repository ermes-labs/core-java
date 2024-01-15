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

import com.openfaas.model.AbstractHandler;
import com.openfaas.model.IRequest;
import com.openfaas.model.IResponse;
import com.openfaas.model.Response;
import io.ermes.SessionToken;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.ermes.ErmesEnv;
import io.ermes.Session;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.ermes.ErmesRedis.b;

public abstract class Offloadable extends AbstractHandler {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(Offloadable.class);
    private final @NotNull OffloadableOptions options;

    public Offloadable() {
        this(new OffloadableOptions());
    }

    public Offloadable(@NotNull OffloadableOptions options) {
        this.options = options;
    }

    public abstract IResponse HandleOffload(IRequest request);

    public IResponse Handle(@NotNull IRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("[Offloadable] -------- BEGIN OFFLOADABLE -------- \n");
            logger.debug("[Offloadable] Queries:\n");
            request.getQuery().keySet().forEach(v ->
                logger.debug("[Offloadable] - \t{}:\t{}\n", v, request.getQuery().get(v)));
            logger.debug("[Offloadable] Headers:\n");
            request.getHeaders().keySet().forEach(v ->
                logger.debug("[Offloadable] - \t{}:\t{}\n", v, request.getHeaders().get(v)));
        }

        IResponse response = this.handleErmesRequest(request);

        if (logger.isDebugEnabled()) {
            logger.debug("[Offloadable] Response:\n");
            response.getHeaders().keySet().forEach(v ->
                logger.debug("[Offloadable] - \t{}:\t{}\n", v, response.getHeaders().get(v)));
            logger.debug("[Offloadable] Body:\n");
            logger.debug("[Offloadable] - \t{}\n", response.getBody());
            logger.debug("[Offloadable] -------- END OFFLOADABLE -------- \n");
        }

        return response;
    }

    private IResponse handleErmesRequest(@NotNull IRequest request) {
        try (ErmesRedisCommands commands = ErmesRedis.sync()) {
            SessionToken sessionToken;
            Session session = null;

            try {
                sessionToken = this.getSessionToken(request);
            } catch (MalformedSessionTokenException e) {
                return this.malformedSessionTokenResponse(e);
            }

            if (sessionToken == null && this.mustRedirectNewRequests(commands)) {
                return this.redirectNewRequest(commands);
            }

            if (sessionToken != null && this.dummyClientNeedsRedirect(sessionToken)) {
                return this.redirectDummyClientResponse(sessionToken);
            }

            try {
                session = sessionToken == null
                    ? this.createSession(commands)
                    : this.getSession(sessionToken, commands);

                //
                if (this.sessionHasBeenMoved(session)) {
                    //
                    this.cleanupSession(session, commands);
                    return this.redirectClientResponse(session);
                }

                //
                this.setGlobalSession(session);
                //
                this.ensureAllSessionEntriesDoNotExpireDuringRequestHandle(session, commands);
                //
                if (!this.options.reuseInternalConnection) commands.close();

                IResponse response = this.handleRequest(request);
                return this.withHeaders(response, session);
            } catch (UnableToUseReadOnlySessionException e) {
                //
                return this.unableToUseReadOnlySessionException(sessionToken, e);
            } finally {
                if (session != null) {
                    try (ErmesRedisCommands maybeReusedCommands = ErmesRedis.sync()) {
                        this.setExpireTimeOfAllSessionEntries(session, maybeReusedCommands);
                        this.releaseLock(session, maybeReusedCommands);
                    }
                }
            }
        } catch (Exception e) {
            return this.handleServerErrorResponse(e);
        } finally {
            ErmesRedis.shutdown();
        }
    }

    private String getCookie(@NotNull IRequest request) {
        String cookie = request.getHeaders().get("Cookie");
        if (cookie == null) {
            return null;
        }

        String[] cookieParts = cookie.split("; ");
        for (String cookiePart : cookieParts) {
            int equalIndex = cookiePart.indexOf('=');
            if (equalIndex != -1) {
                String name = cookiePart.substring(0, equalIndex);
                if (name.equals(ErmesEnv.HTTP.sessionTokenHTTPCookieName)) {
                    return cookiePart.substring(equalIndex + 1);
                }
            }
        }

        return null;
    }

    private SessionToken getSessionToken(@NotNull IRequest request) throws MalformedSessionTokenException {
        // Get cookie from request.
        String cookie = this.getCookie(request);

        if (cookie == null) {
            return null;
        }

        try {
            // Parse cookie.
            return ErmesRedis.fromJSON(cookie, SessionToken.class);
        } catch (Exception e) {
            throw new MalformedSessionTokenException("Malformed session token", e);
        }
    }

    private IResponse malformedSessionTokenResponse(@NotNull MalformedSessionTokenException e) {
        logger.error("[Offloadable] Malformed session token: ", e);
        IResponse response = new Response();
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/400
        response.setStatusCode(400);
        response.setBody("400 Malformed session token.");
        return response;
    }

    private boolean dummyClientNeedsRedirect(@NotNull SessionToken sessionToken) {
        return !Objects.equals(sessionToken.CurrentNodeId, ErmesEnv.Infrastructure.locationId);
    }

    private IResponse redirectDummyClientResponse(@NotNull SessionToken sessionToken) {
        // 308 redirect
        String redirectUrl = sessionToken.CurrentNodeIp;
        IResponse response = new Response();
        response.setStatusCode(308);
        response.setBody("308 Redirecting to " + redirectUrl);
        response.setHeader("Location", redirectUrl);

        return response;
    }\

    private boolean mustRedirectNewRequests(@NotNull ErmesRedisCommandsSync<String, byte[]> commands) {
        return commands.get("ermes:offloading:accept").equals("false");
    }

    private IResponse redirectNewRequest(@NotNull ErmesRedisCommandsSync<String, byte[]> commands) {
        IResponse response = new Response();
        response.setStatusCode(308);
        response.setBody("308 Redirecting to " + ErmesEnv.HTTP.redirectTo);
        response.setHeader("Location", ErmesEnv.HTTP.redirectTo);
        return response;
    }

    private Session createSession(ErmesRedisCommandsSync<String, byte[]> commands) {
        String sessionId;
        do {
            sessionId = UUID.randomUUID().toString();
            // NOTE: LOCK is NOT session-sèecific or use sessionmetadata.
        } while (commands.setnx(lock(b(sessionId)), b("1")));

        return Session.create(sessionId);
    }

    private Session getSession(SessionToken sessionToken, ErmesRedisCommandsSync<String, byte[]> commands) throws UnableToLockSessionException, UnableToUseSessionDuringOffloadException {


        this.options.exclusiveSessionAccess
        this.options.executionDuringOffload
        Session session = Session.fromJson(commands.get(sessionToken.session));
        if (session == null) {
            throw new UnableToLockSessionException("Unable to lock session <" + sessionToken.session + ">");
        }

        if (session.isOffloading != null && session.isOffloading.equals("true")) {
            if (!executionDuringOffload) {
                throw new UnableToUseSessionDuringOffloadException("Session <" + sessionToken.session + "> is offloading");
            }
        } else {
            session.isOffloading = false;
        }

        if (exclusiveSessionAccess) {
            if (!commands.setnx(session.session, session.getJson())) {
                throw new UnableToLockSessionException("Unable to lock session <" + sessionToken.session + ">");
            }
        } else {
            if (!commands.incr(session.session)) {
                throw new UnableToLockSessionException("Unable to lock session <" + sessionToken.session + ">");
            }
        }

        return session;
    }

    private IResponse unableToLockSessionResponse(SessionToken sessionToken, UnableToLockSessionException e) {
    }

    private IResponse unableToUseSessionDuringOffloadResponse(SessionToken sessionToken, UnableToUseSessionDuringOffloadException e) {
    }

    private boolean sessionHasBeenMoved(Session session) {
    }

    private void cleanupSession(Session session, ErmesRedisCommandsSync commands) {
    }

    private IResponse redirectClientResponse(Session session) {
    }

    private void setGlobalSession(Session session) {
    }

    private void ensureAllSessionEntriesDoNotExpireDuringRequestHandle(Session session, ErmesRedisCommandsSync commands) {
    }

    private IResponse handleRequest(IRequest request) {
    }

    private IResponse withHeaders(IResponse response, Session session) {
    }

    private void setExpireTimeOfAllSessionEntries(Session session, ErmesRedisCommandsSync maybeReusedCommands) {
    }

    private void releaseLock(Session session, ErmesRedisCommandsSync maybeReusedCommands) {
    }

    private IResponse handleServerErrorResponse(Exception e) {
    }

    private String checkSessionHeader(IRequest req, IResponse res) {
        String sessionId = req.getHeader("X-session");
        logger.log("[Offloadable] X-session: " + sessionId);
        if (sessionId == null) {
            logger.log("[Offloadable] X-session is null, sending 300");
            res.setStatusCode(300);
            res.setBody("300 Header X-session is not present");
        }
        return sessionId;
    }

    private String checkRequestIdHeader(IRequest req, IResponse res) {
        String requestId = req.getHeader("X-session-request-id");
        logger.log("[Offloadable] X-session-request-id: " + requestId);
        if (requestId == null) {
            logger.log("[Offloadable] X-session-request-id is null, sending 300");
            res.setStatusCode(300);
            res.setBody("300 Header X-session-request-id is not present");
        } else {
            Pattern UUID_REGEX =
                Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

            if (!UUID_REGEX.matcher(requestId).matches()) {
                logger.log("[Offloadable] X-session-request-id <" + requestId + "> is not a UUID string, sending 300");
                res.setStatusCode(300);
                res.setBody("300 Header X-session-request-id <" + requestId + "> is not a UUID string");
                requestId = null;
            }
        }
        return requestId;
    }

    private boolean checkRequestIdUniqueness(String sessionId, String requestId, IResponse res) {
        if (SessionsRequestsDAO.existsSessionRequest(sessionId, requestId)) {
            logger.log("[Offloadable] X-session-request-id was already processed, sending 208");
            res.setStatusCode(208);
            res.setBody("208 Header X-session-request-id was already processed");
            return true;
        }
        return false;
    }

    private boolean acquireLock(String sessionId, IResponse res) {
        if (!SessionsLocksDAO.lockSession(sessionId)) {
            logger.log("[Offloadable] Session <" + sessionId + "> not available. Can't acquire the session's lock");
            res.setStatusCode(503);
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
            res.setHeader("Retry-After", "5");
            res.setBody("503 Session <" + sessionId + "> not available");
            return false;
        }
        return true;
    }

    private IResponse handleNewSession(IRequest req, String sessionId, String requestId) {
        IResponse res = new Response();

        SessionToken sessionToken = new SessionToken();
        sessionToken.init(sessionId);
        logger.log("[Offloadable] New session created: \n\t" + sessionToken.getJson());

        SessionsDAO.setSessionToken(sessionToken);
        logger.log("[Offloadable] Session saved in Redis");

        if (ConfigurationDAO.getOffloading().equals("accept")) {
            res = this.handle(req, sessionId, requestId);
        } else {
            logger.log("[Offloadable] Node is at full capacity, offloading the new session");
            new ForceOffloadService().Handle(res, sessionId);
            res = this.handleRemoteSession(req, sessionToken);
            SessionsLocksDAO.unlockSession(sessionId);
        }

        return res;
    }

    private IResponse handleRemoteSession(IRequest req, SessionToken sessionToken) {
        SessionsLocksDAO.unlockSession(sessionToken.session);

        String redirectUrl =
            EdgeInfrastructureUtils.getGateway(sessionToken.currentLocation) +
                "/function/" +
                System.getenv("FUNCTION_NAME") + "?" +
                req.getQueryRaw();

        logger.log("[Offloadable] Redirecting session <" + sessionToken.getJson() + "> to: " + redirectUrl);

        Response res = new Response();
        res.setStatusCode(307);
        res.setBody("307 Session is remote. Location: " + redirectUrl);
        res.setHeader("Location", redirectUrl);
        return res;
    }

    private IResponse handleLocalSession(IRequest req, String sessionId, String requestId) {
        IResponse res;
        res = this.handle(req, sessionId, requestId);
        return res;
    }

    private IResponse handle(IRequest req, String sessionId, String requestId) {
        IResponse res = new Response();
        if (!this.checkRequestIdUniqueness(sessionId, requestId, res)) {
            EdgeDB.setCurrentSession(sessionId);
            res = this.HandleOffload(req);
            // The access time is updated regardless of a successful update of the data
            SessionsDAO.updateAccessTimestampToNow(sessionId);

            // If the unlocking or the saving of data is unsuccessful, return failure
            if (!SessionsLocksDAO.unlockSessionAndUpdateData(sessionId, EdgeDB.getCache(), requestId)) {
                String message = "423 Locked: can't release lock and/or can't save data on redis";
                res = new Response();
                res.setStatusCode(423);
                res.setBody(message);
                logger.log(message);
            }
        } else {
            SessionsLocksDAO.unlockSession(sessionId);
        }
        return res;
    }

    static final private class MalformedSessionTokenException extends Exception {
        MalformedSessionTokenException(String message, Exception e) {
            super(message, e);
        }
    }



    static final private class UnableToLockSessionException extends Exception {
        UnableToLockSessionException(String message) {
            super(message);
        }
    }
}
