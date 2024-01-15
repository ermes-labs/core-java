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
import com.openfaas.function.commands.wrappers.WrapperOffloadSession;
import com.openfaas.function.daos.ConfigurationDAO;
import com.openfaas.function.daos.SessionsLocksDAO;
import com.openfaas.function.model.SessionToken;
import com.openfaas.function.utils.EdgeInfrastructureUtils;
import com.openfaas.function.utils.Logger;
import com.openfaas.function.utils.MigrateUtils;
import com.openfaas.model.IRequest;
import com.openfaas.model.IResponse;

import java.util.Base64;

@RequiresHeaderAnnotation.RequiresHeader(header = "X-session-token")
public class OffloadSession implements ICommand {

    public void Handle(IRequest req, IResponse res) {
        String sessionJson = new String(Base64.getDecoder().decode(req.getHeader("X-session-token")));
        String offloading = ConfigurationDAO.getOffloading();
        if (offloading.equals("accept")) {
            // offload accepted
            Logger.log("Offloading accepted");

            SessionToken sessionToken = SessionToken.Builder.buildFromJSON(sessionJson);

            String sessionId = sessionToken.session;
            while (!acquireLock(res, sessionId)) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            SessionToken newSession = MigrateUtils.migrateSessionFromRemoteToLocal(sessionJson);

            releaseLock(res, sessionId);

            res.setStatusCode(200);
            res.setBody("Offloaded:\n\tOld session: " + sessionJson + "\n\tNew session: " + newSession.getJson());
        } else {
            // offload redirected to parent
            Logger.log("Offloading not accepted");
            Logger.log("Redirecting session to parent:\n\t" + EdgeInfrastructureUtils.getParentLocationId(System.getenv("LOCATION_ID")) + "\n\t" + sessionJson);

            // call parent node to offload the session
            SessionToken sessionToOffload = SessionToken.Builder.buildFromJSON(sessionJson);

            new WrapperOffloadSession()
                    .gateway(EdgeInfrastructureUtils.getParentHost(System.getenv("LOCATION_ID")))
                    .sessionToOffload(sessionToOffload)
                    .call();
        }
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
