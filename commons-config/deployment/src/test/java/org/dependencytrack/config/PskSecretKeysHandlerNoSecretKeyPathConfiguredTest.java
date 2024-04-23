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
package org.dependencytrack.config;

import io.quarkus.test.QuarkusUnitTest;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PskSecretKeysHandlerNoSecretKeyPathConfiguredTest extends AbstractPskSecretKeyHandlerTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(
                            "PskSecretKeysHandler/application-nosecretkeypath.properties",
                            "application.properties"
                    ));

    @Test
    void test() {
        final Config config = ConfigProvider.getConfig();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> config.getOptionalValue("foo.bar", String.class))
                .withMessage("Decryption with dtrack-psk was requested, but no secret.key.path is configured");
    }

}
