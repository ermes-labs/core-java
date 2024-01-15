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

import com.openfaas.function.commands.annotations.RequiresQueryAnnotation;
import com.openfaas.function.daos.ConfigurationDAO;
import com.openfaas.function.daos.SessionsDAO;
import com.openfaas.function.daos.SessionsDataDAO;
import com.openfaas.function.model.SessionToken;
import com.openfaas.function.utils.Logger;
import com.openfaas.model.IRequest;
import com.openfaas.model.IResponse;

@RequiresQueryAnnotation.RequiresQuery(query = "type")
@RequiresQueryAnnotation.RequiresQuery(query = "value")
public class TestFunction implements ICommand {

    public void Handle(IRequest req, IResponse res) {
        String typeRequested = req.getQuery().get("type");
        String valueRequested = req.getQuery().get("value");

        Logger.log("About to test: " + typeRequested + " with " + valueRequested);

        switch (typeRequested) {
            case "configuration":
                testConfiguration(res);
                break;
            case "sessionMetadata":
                testSessionMetadata(valueRequested, res);
                break;
            case "sessionMetadataLocations":
                testSessionMetadataLocations(valueRequested, res);
                break;
            case "sessionData":
                testSessionData(valueRequested, res);
                break;
            case "session":
                testSession(valueRequested, res);
                break;
            default:
                error(typeRequested, res);
        }
    }

    private void testConfiguration(IResponse res) {
        String message =
                "Offloading status: " + ConfigurationDAO.getOffloading();

        Logger.log(message);
        res.setBody(message);
        res.setStatusCode(200);
    }

    private void testSessionMetadata(String sessionId, IResponse res) {
        String message =
                "Session metadata <" + sessionId + ">: " + getSessionToken(sessionId) + "\n";

        Logger.log(message);
        res.setBody(message);
        res.setStatusCode(200);
    }

    private void testSessionMetadataLocations(String sessionId, IResponse res) {
        String message =
                "Session metadata <" + sessionId + ">: " + getSessionTokenLocations(sessionId) + "\n";

        Logger.log(message);
        res.setBody(message);
        res.setStatusCode(200);
    }

    private void testSessionData(String sessionId, IResponse res) {
        String message =
                "Session data <" + sessionId + ">: " + SessionsDataDAO.getSessionData(sessionId).toJSON() + "\n";

        Logger.log(message);
        res.setBody(message);
        res.setStatusCode(200);
    }

    private void testSession(String sessionId, IResponse res) {
        String message =
                "Session metadata <" + sessionId + ">: " + getSessionTokenLocations(sessionId) + "\n" +
                        "Session data <" + sessionId + ">: " + SessionsDataDAO.getSessionData(sessionId).toJSON() + "\n";

        Logger.log(message);
        res.setBody(message);
        res.setStatusCode(200);
    }

    private void error(String type, IResponse res) {
        String message = "Type <" + type + "> is not valid.\n";
        Logger.log(message);
        res.setBody(message);
        res.setStatusCode(400);
    }

    private String getSessionToken(String sessionId) {
        SessionToken token = SessionsDAO.getSessionToken(sessionId);
        if (token == null) {
            return "<session_not_present_in_this_node>";
        } else {
            return token.getJson();
        }
    }

    private String getSessionTokenLocations(String sessionId) {
        SessionToken token = SessionsDAO.getSessionToken(sessionId);
        if (token == null) {
            return "<session_not_present_in_this_node>";
        } else {
            return token.getJsonLocationsOnly();
        }
    }
}
