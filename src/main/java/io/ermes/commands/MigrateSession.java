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
import com.openfaas.function.daos.SessionsDataDAO;
import com.openfaas.function.daos.SessionsRequestsDAO;
import com.openfaas.function.model.sessiondata.SessionData;
import com.openfaas.function.utils.Logger;
import com.openfaas.model.IRequest;
import com.openfaas.model.IResponse;

@RequiresQueryAnnotation.RequiresQuery(query = "session")
@RequiresQueryAnnotation.RequiresQuery(query = "data-type")
public class MigrateSession implements ICommand {

    @Override
    public void Handle(IRequest req, IResponse res) {

        String sessionId = req.getQuery().get("session");
        String dataType = req.getQuery().get("data-type");

        Logger.log("About to migrate Session Id: " + sessionId);

        if (dataType.equals("sessionData")) {
            Logger.log("Migrating session data");

            SessionData data = SessionsDataDAO.getSessionData(sessionId);

            res.setBody(data.toJSON());
            res.setStatusCode(200);

            SessionsDataDAO.deleteSessionData(sessionId);
        } else if (dataType.equals("requestIds")) {
            Logger.log("Migrating session request ids");

            String data = SessionsRequestsDAO.getSessionRequests(sessionId).toString();
            data = data.substring(1, data.length() - 1);
            data = data.replaceAll(" ", "");

            res.setBody(data);
            res.setStatusCode(200);

            SessionsRequestsDAO.deleteSessionRequest(sessionId);
        } else {
            String message = "Data-type <" + dataType + "> not recognized";
            Logger.log(message);
            res.setBody(message);
            res.setStatusCode(400);
        }
    }
}
