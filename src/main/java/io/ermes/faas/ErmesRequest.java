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

package io.ermes.faas;

import com.openfaas.model.Request;

import java.util.Map;

public class ErmesRequest extends Request {
    public ErmesRequest(String body, Map<String, String> headers) {
        super(body, headers);
    }

    public ErmesRequest(String body, Map<String, String> headers, String queryRaw, String path) {
        super(body, headers, queryRaw, path);
    }
}