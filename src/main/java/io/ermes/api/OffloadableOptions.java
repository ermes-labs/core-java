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

package io.ermes.api;

public class OffloadableOptions {
    boolean reuseInternalConnection = false;
    boolean readOnly = false;

    public OffloadableOptions reuseInternalConnection(boolean reuseInternalConnection) {
        this.reuseInternalConnection = reuseInternalConnection;
        return this;
    }

    public OffloadableOptions readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public static class Builder {
        private Builder() {
        }

        public static OffloadableOptions reuseInternalConnection(boolean reuseInternalConnection) {
            return new OffloadableOptions().reuseInternalConnection(reuseInternalConnection);
        }

        public static OffloadableOptions readOnly(boolean readOnly) {
            return new OffloadableOptions().readOnly(readOnly);
        }
    }
}
