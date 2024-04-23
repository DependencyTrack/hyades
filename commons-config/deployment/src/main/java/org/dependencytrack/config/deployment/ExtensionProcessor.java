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
package org.dependencytrack.config.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigBuilderBuildItem;
import org.dependencytrack.config.DatabaseConfigSourceFactoryBuilder;
import org.dependencytrack.config.PskSecretKeysHandlerFactoryBuilder;

public class ExtensionProcessor {

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("dependencytrack-config");
    }

    @BuildStep
    void configFactory(final BuildProducer<RunTimeConfigBuilderBuildItem> runTimeConfigBuilder) {
        runTimeConfigBuilder.produce(new RunTimeConfigBuilderBuildItem(DatabaseConfigSourceFactoryBuilder.class.getName()));
        runTimeConfigBuilder.produce(new RunTimeConfigBuilderBuildItem(PskSecretKeysHandlerFactoryBuilder.class.getName()));
    }

}
