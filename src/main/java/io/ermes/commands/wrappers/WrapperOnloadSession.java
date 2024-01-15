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

public class WrapperOnloadSession extends HTTPWrapper {

    private String action;
    private String session;
    private String randomValue;

    public WrapperOnloadSession() {
        super();
    }

    public WrapperOnloadSession gateway(String gateway) {
        this.setGateway(gateway);
        return this;
    }

    public WrapperOnloadSession actionGetSession() {
        action = "get-session";
        return this;
    }

    public WrapperOnloadSession actionReleaseSession() {
        action = "release-session";
        return this;
    }

    public String getRandomValue() {
        return randomValue;
    }

    public WrapperOnloadSession setRandomValue(String randomValue) {
        this.randomValue = randomValue;
        return this;
    }

    public WrapperOnloadSession setSession(String session) {
        this.session = session;
        return this;
    }

    @Override
    public Response call() {
        if ("get-session".equals(action))
            setRemoteFunction("/function/session-offloading-manager?command=onload-session&action=" + action);
        else
            setRemoteFunction("/function/session-offloading-manager?command=onload-session&" +
                    "action=" + action + "&session=" + session + "&random-value=" + randomValue);
        setHeader("X-onload-location", System.getenv("LOCATION_ID"));
        try {
            get();
            if ("get-session".equals(action) && getStatusCode() == 200)
                this.randomValue = getResponseHeader("X-random-value");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new Response(getBody(), getStatusCode());
    }
}
