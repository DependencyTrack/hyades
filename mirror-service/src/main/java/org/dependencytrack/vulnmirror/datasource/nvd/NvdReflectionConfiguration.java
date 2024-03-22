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
package org.dependencytrack.vulnmirror.datasource.nvd;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Register classes of {@code open-vulnerability-clients} for the NVD data source for reflection.
 * <p>
 * The model classes are generated from JSON in {@code nvd-lib}.
 * When inspecting the library's code on GitHub, you won't find these classes.
 */
@SuppressWarnings("unused")
@RegisterForReflection(
        targets = {
                io.github.jeremylong.openvulnerability.client.nvd.Config.class,
                io.github.jeremylong.openvulnerability.client.nvd.CpeMatch.class,
                io.github.jeremylong.openvulnerability.client.nvd.CveApiJson20.class,
                io.github.jeremylong.openvulnerability.client.nvd.CveItem.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV2.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV2.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV3.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV3Data.class,
                io.github.jeremylong.openvulnerability.client.nvd.DefCveItem.class,
                io.github.jeremylong.openvulnerability.client.nvd.LangString.class,
                io.github.jeremylong.openvulnerability.client.nvd.Metrics.class,
                io.github.jeremylong.openvulnerability.client.nvd.Node.class,
                io.github.jeremylong.openvulnerability.client.nvd.Reference.class,
                io.github.jeremylong.openvulnerability.client.nvd.VendorComment.class,
                io.github.jeremylong.openvulnerability.client.nvd.Weakness.class
        },
        ignoreNested = false
)
class NvdReflectionConfiguration {
}
