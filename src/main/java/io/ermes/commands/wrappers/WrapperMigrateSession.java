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

package io.ermes.commands.wrappers;

public class WrapperMigrateSession extends HTTPWrapper {

    private String sessionToMigrate;
    private String typeOfMigrate;

    public WrapperMigrateSession() {
        super();
    }

    public WrapperMigrateSession gateway(String gateway) {
        this.setGateway(gateway);
        return this;
    }

    public WrapperMigrateSession sessionToMigrate(String session) {
        this.sessionToMigrate = session;
        return this;
    }

    public WrapperMigrateSession typeSessionData() {
        this.typeOfMigrate = "sessionData";
        return this;
    }

    public WrapperMigrateSession typeRequestIds() {
        this.typeOfMigrate = "requestIds";
        return this;
    }

    @Override
    public Response call() {
        setRemoteFunction("/function/session-offloading-manager-migrate-session?" +
                "command=migrate-session&" +
                "session=" + sessionToMigrate + "&" +
                "data-type=" + typeOfMigrate);

        try {
            get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new Response(getBody(), getStatusCode());
    }
}
