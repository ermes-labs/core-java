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

public class WrapperGarbageCollector extends HTTPWrapper {

    private String sessionToDelete;

    public WrapperGarbageCollector() {
        super();
    }

    public WrapperGarbageCollector gateway(String gateway) {
        this.setGateway(gateway);
        return this;
    }

    public WrapperGarbageCollector sessionToDelete(String session) {
        this.sessionToDelete = session;
        return this;
    }

    @Override
    public Response call() {
        setRemoteFunction("/function/session-offloading-manager-garbage-collector?" +
                "command=garbage-collector&" +
                "deletePolicy=forced&" +
                "sessionId=" + sessionToDelete);

        try {
            get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new Response(getBody(), getStatusCode());
    }
}
