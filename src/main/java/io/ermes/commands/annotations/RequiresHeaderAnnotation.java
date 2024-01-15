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

package io.ermes.commands.annotations;

import com.openfaas.model.IRequest;
import io.ermes.commands.annotations.exceptions.HeaderRequiredException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class RequiresHeaderAnnotation {

    public static void verify(Object object, IRequest req) throws HeaderRequiredException {
        Class<?> clazz = object.getClass();
        if (clazz.isAnnotationPresent(RequiresHeader.class)) {
            String requiredHeader = clazz.getAnnotation(RequiresHeader.class).header();
            if (req.getHeader(requiredHeader) == null)
                throw new HeaderRequiredException("Missing header <" + requiredHeader + "> in request");
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface RequiresHeader {
        String header();
    }
}
