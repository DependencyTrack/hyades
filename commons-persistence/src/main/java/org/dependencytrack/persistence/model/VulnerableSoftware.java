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
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.persistence.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.jdbi.v3.json.Json;

import java.util.List;

@RegisterForReflection
public record VulnerableSoftware(
        String cpe23,
        String part,
        String vendor,
        String product,
        String version,
        String update,
        String edition,
        String language,
        String swEdition,
        String targetSw,
        String targetHw,
        String other,
        String versionEndExcluding,
        String versionEndIncluding,
        String versionStartExcluding,
        String versionStartIncluding,
        @Json List<VulnIdAndSource> vulnerabilities
) {

    @RegisterForReflection
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({"vulnId", "source"})
    public record VulnIdAndSource(String vulnId, String source) {
    }

}
