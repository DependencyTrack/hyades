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
package org.dependencytrack.vulnmirror.datasource;

import java.util.concurrent.Future;

public interface DatasourceMirror {

    /**
     * Determine whether a given {@link Datasource} is supported by this mirror.
     *
     * @param datasource The {@link Datasource} to check
     * @return {@code true} when supported, otherwise {@code false}
     */
    boolean supportsDatasource(final Datasource datasource);

    /**
     * <em>Asynchronously</em> execute a mirroring operating.
     *
     * @return A {@link Future} for tracking completion of the operation
     */
    Future<?> doMirror();

}
