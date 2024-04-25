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
    private final Provider<Optional<String>> fromAddressProvider;
    private final Provider<Optional<String>> serverHostnameProvider;
    private final Provider<Optional<Integer>> serverPortProvider;
    private final Provider<Optional<String>> usernameProvider;
    private final Provider<Optional<String>> passwordProvider;
    private final Provider<Optional<Boolean>> tlsEnabledProvider;
    private final Provider<Optional<Boolean>> trustCertificateProvider;

    SendMailPublisherConfig(
            @ConfigProperty(name = "dtrack.email.smtp.enabled") final Provider<Optional<Boolean>> smtpEnabledProvider,
            @ConfigProperty(name = "dtrack.email.smtp.from.address") final Provider<Optional<String>> fromAddressProvider,
            @ConfigProperty(name = "dtrack.email.smtp.server.hostname") final Provider<Optional<String>> serverHostnameProvider,
            @ConfigProperty(name = "dtrack.email.smtp.server.port") final Provider<Optional<Integer>> serverPortProvider,
            @ConfigProperty(name = "dtrack.email.smtp.username") final Provider<Optional<String>> usernameProvider,
            @ConfigProperty(name = "dtrack.email.smtp.password") final Provider<Optional<String>> passwordProvider,
            @ConfigProperty(name = "dtrack.email.smtp.ssltls") final Provider<Optional<Boolean>> tlsEnabledProvider,
            @ConfigProperty(name = "dtrack.email.smtp.trustcert") final Provider<Optional<Boolean>> trustCertificateProvider
    ) {
        this.smtpEnabledProvider = smtpEnabledProvider;
        this.fromAddressProvider = fromAddressProvider;
        this.serverHostnameProvider = serverHostnameProvider;
        this.serverPortProvider = serverPortProvider;
        this.usernameProvider = usernameProvider;
        this.passwordProvider = passwordProvider;
        this.tlsEnabledProvider = tlsEnabledProvider;
        this.trustCertificateProvider = trustCertificateProvider;
    }

    Optional<Boolean> isSmtpEnabled() {
        return smtpEnabledProvider.get();
    }

    Optional<String> fromAddress() {
        return fromAddressProvider.get();
    }

    Optional<String> serverHostname() {
        return serverHostnameProvider.get();
    }

    Optional<Integer> serverPort() {
        return serverPortProvider.get();
    }

    Optional<String> username() {
        return usernameProvider.get();
    }

    Optional<String> password() {
        return passwordProvider.get();
    }

    Optional<Boolean> tlsEnabled() {
        return tlsEnabledProvider.get();
    }

    Optional<Boolean> trustCertificate() {
        return trustCertificateProvider.get();
    }

}
