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
package org.dependencytrack.notification.publisher;

import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

/**
 * As of Quarkus 3.9 / smallrye-config 3.7, it is not possible to use {@link ConfigMapping}
 * interfaces with {@link Provider} fields. We need {@link Provider} fields in order to support
 * configuration changes at runtime. Refer to <em>Injecting Dynamic Values</em> in the
 * {@link ConfigProperty} JavaDoc for details.
 *
 * @see <a href="https://github.com/smallrye/smallrye-config/issues/664">Related smallrye-config issue</a>
 */
@ApplicationScoped
class SendMailPublisherConfig {

    private final Provider<Optional<Boolean>> smtpEnabledProvider;

    SendMailPublisherConfig(
            @ConfigProperty(name = "dtrack.email.smtp.enabled") final Provider<Optional<Boolean>> smtpEnabledProvider
    ) {
        this.smtpEnabledProvider = smtpEnabledProvider;
    }

    Optional<Boolean> isSmtpEnabled() {
        return smtpEnabledProvider.get();
    }

}
