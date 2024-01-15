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
import com.openfaas.function.utils.Logger;
import com.openfaas.model.IRequest;
import com.openfaas.model.IResponse;

@RequiresQueryAnnotation.RequiresQuery(query = "status")
public class SetOffloadStatus implements ICommand {

    public void Handle(IRequest req, IResponse res) {
        String offloading = req.getQuery().get("status");

        if (!offloading.equals("accept") && !offloading.equals("reject")) {
            String message = "Malformed request: <" + offloading + "> is not a valid offloading status (valid offloading statuses: accept/reject)";

            Logger.log(message);
            res.setBody(message);
            res.setStatusCode(400);
        } else {
            String message = "Offloading status from <" + ConfigurationDAO.getOffloading() + "> to <" + offloading + ">";
            if (offloading.equals("accept"))
                ConfigurationDAO.acceptOffloading();
            else
                ConfigurationDAO.rejectOffloading();

            Logger.log(message);
            res.setBody(message);
            res.setStatusCode(200);
        }
    }
}
