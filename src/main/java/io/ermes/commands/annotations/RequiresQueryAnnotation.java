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
import io.ermes.commands.annotations.exceptions.QueryRequiredException;

import java.lang.annotation.*;

public class RequiresQueryAnnotation {

    public static void verify(Object object, IRequest req) throws QueryRequiredException {
        Class<?> clazz = object.getClass();
        RequiresQuery[] requiresQueries = clazz.getAnnotationsByType(RequiresQuery.class);
        for (RequiresQuery requiredQuery : requiresQueries) {
            String query = requiredQuery.query();
            if (req.getQuery().get(query) == null) {
                throw new QueryRequiredException("Missing query <" + query + "> in request");
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Repeatable(RequiresQueries.class)
    public @interface RequiresQuery {
        String query();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface RequiresQueries {
        RequiresQuery[] value();
    }
}
