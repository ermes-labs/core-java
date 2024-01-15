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

import com.openfaas.function.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public abstract class HTTPWrapper {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final Hashtable<String, String> headers = new Hashtable<>();
    private String bodyPOST = null;
    private int statusCode;
    private String body;
    private String gateway;
    private String remoteFunction;
    private Map<String, List<String>> responseHeaders;

    protected void setBodyPOSTRequest(String body) {
        this.bodyPOST = body;
    }

    protected void setGateway(String gateway) {
        this.gateway = gateway;
    }

    protected void setRemoteFunction(String remoteFunction) {
        this.remoteFunction = remoteFunction;
    }

    protected void setHeader(String key, String value) {
        headers.put(key, value);
    }

    protected void get() throws Exception {
        String uri = gateway + remoteFunction;

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri))
                .setHeader("User-Agent", "session-offloading-manager");

        Logger.log("Sending GET:\n\tURI:" + uri);
        // Add all the additional headers to the request
        for (var entry : headers.entrySet()) {
            requestBuilder.setHeader(entry.getKey(), entry.getValue());
            Logger.log("\tExtra header: " + entry.getKey() + " = " + entry.getValue());
        }

        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        statusCode = response.statusCode();
        body = response.body();
        responseHeaders = response.headers().map();

        Logger.log("(HTTPWrapper.get) Response: ");
        Logger.log("(HTTPWrapper.get) \tStatusCode: " + statusCode);
        Logger.log("(HTTPWrapper.get) \tBody: " + body);
    }

    protected void post() throws Exception {
        String uri = gateway + remoteFunction;

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(bodyPOST))
                .uri(URI.create(uri))
                .setHeader("User-Agent", "session-offloading-manager")
                .header("Content-Type", "application/json");

        Logger.log("Sending POST:\n\tURI:" + uri);
        // Add all the additional headers to the request
        for (var entry : headers.entrySet()) {
            requestBuilder.setHeader(entry.getKey(), entry.getValue());
            Logger.log("\tExtra header: " + entry.getKey() + " = " + entry.getValue());
        }
        Logger.log("\tBody: " + bodyPOST);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        statusCode = response.statusCode();
        body = response.body();
        responseHeaders = response.headers().map();

        Logger.log("(HTTPWrapper.post) Response: ");
        Logger.log("(HTTPWrapper.post) \tStatusCode: " + statusCode);
        Logger.log("(HTTPWrapper.post) \tBody: " + body);
    }

    public abstract Response call();

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public String getResponseHeader(String header) {
        return responseHeaders.get(header).get(0);
    }
}
