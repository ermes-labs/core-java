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

import com.openfaas.model.AbstractHandler;

import java.util.ServiceLoader;

public class HandlerProvider {
    private static com.openfaas.entrypoint.HandlerProvider provider;
    private ServiceLoader<AbstractHandler> loader;
    private HandlerProvider() {
        loader = ServiceLoader.load(AbstractHandler.class);
    }

    public static com.openfaas.entrypoint.HandlerProvider getInstance() {
        if(provider == null) {
            provider = new com.openfaas.entrypoint.HandlerProvider();
        }
        return provider;
    }

    public AbstractHandler getHandler() {
        AbstractHandler service = loader.iterator().next();
        if(service != null) {
            return service;
        } else {
            throw new java.util.NoSuchElementException(
                    "No implementation for HandlerProvider");
        }
    }
}
