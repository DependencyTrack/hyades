/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.acme.util;

import io.quarkus.vertx.http.runtime.devmode.Json;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public final class JsonUtil {

    /**
     * Private constructor.
     */
    private JsonUtil() { }

    public static Json.JsonObjectBuilder add(final Json.JsonObjectBuilder builder, final String key, final String value) {
        if (value != null) {
            builder.put(key, value);
        }
        return builder;
    }

    public static Json.JsonObjectBuilder add(final Json.JsonObjectBuilder builder, final String key, final BigInteger value) {
        if (value != null) {
            builder.put(key, String.valueOf(value));
        }
        return builder;
    }

    public static Json.JsonObjectBuilder add(final Json.JsonObjectBuilder builder, final String key, final BigDecimal value) {
        if (value != null) {
            builder.put(key, String.valueOf(value));
        }
        return builder;
    }

    public static Json.JsonObjectBuilder add(final Json.JsonObjectBuilder builder, final String key, final Enum value) {
        if (value != null) {
            builder.put(key, value.name());
        }
        return builder;
    }

    public static ZonedDateTime jsonStringToTimestamp(final String s) {
        if (s == null) {
            return null;
        }
        try {
            return ZonedDateTime.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
