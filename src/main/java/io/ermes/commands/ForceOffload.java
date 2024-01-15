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

package io.ermes.commands;

import com.openfaas.function.commands.annotations.RequiresHeaderAnnotation;
import com.openfaas.function.commands.services.ForceOffloadService;
import com.openfaas.function.daos.SessionsLocksDAO;
import com.openfaas.function.utils.Logger;
import com.openfaas.model.IRequest;
import com.openfaas.model.IResponse;

/**
 * force-offload API:
 * Header X-forced-session: sessionId of the session to offload
 */
@RequiresHeaderAnnotation.RequiresHeader(header = "X-forced-session")
public class ForceOffload implements ICommand {

    public void Handle(IRequest req, IResponse res) {
        String forcedSessionId = req.getHeader("X-forced-session");

        Logger.log("Header X-forced-session: " + forcedSessionId);

        /* --------- Checks before using the session --------- */
        if (!acquireLock(res, forcedSessionId))
            return;

        new ForceOffloadService().Handle(res, forcedSessionId);

        /* --------- Release session --------- */
        releaseLock(res, forcedSessionId);
    }

    private boolean acquireLock(IResponse res, String session) {
        if (!SessionsLocksDAO.lockSession(session)) {
            Logger.log("Cannot acquire lock on session <" + session + ">");
            res.setStatusCode(400);
            res.setBody("Cannot acquire lock on session <" + session + ">");
            return false;
        }
        return true;
    }

    private boolean releaseLock(IResponse res, String session) {
        if (!SessionsLocksDAO.unlockSession(session)) {
            Logger.log("Cannot release lock on session <" + session + ">");
            res.setStatusCode(500);
            res.setBody("Cannot release lock on session <" + session + ">");
            return false;
        }
        return true;
    }
}
