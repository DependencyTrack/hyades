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

import com.google.protobuf.util.JsonFormat;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import jakarta.json.JsonObject;
import org.dependencytrack.proto.ProtobufUtil;
import org.dependencytrack.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.BomProcessingFailedSubject;
import org.dependencytrack.proto.notification.v1.BomValidationFailedSubject;
import org.dependencytrack.proto.notification.v1.NewVulnerabilitySubject;
import org.dependencytrack.proto.notification.v1.NewVulnerableDependencySubject;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.PolicyViolationAnalysisDecisionChangeSubject;
import org.dependencytrack.proto.notification.v1.PolicyViolationSubject;
import org.dependencytrack.proto.notification.v1.ProjectVulnAnalysisCompleteSubject;
import org.dependencytrack.proto.notification.v1.UserSubject;
import org.dependencytrack.proto.notification.v1.VexConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_PORTFOLIO;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_SYSTEM;

public interface Publisher {

    String CONFIG_TEMPLATE_KEY = "template";

    String CONFIG_TEMPLATE_MIME_TYPE_KEY = "mimeType";

    String CONFIG_DESTINATION = "destination";

    String CONFIG_TOKEN = "token";

    String CONFIG_TOKEN_HEADER = "tokenHeader";


    void inform(final PublishContext ctx, final Notification notification, final JsonObject config) throws Exception;

    PebbleEngine getTemplateEngine();

    default PebbleTemplate getTemplate(JsonObject config) {
        try {
            String literalTemplate = config.getString(CONFIG_TEMPLATE_KEY);
            return getTemplateEngine().getLiteralTemplate(literalTemplate);
        } catch (NullPointerException | ClassCastException templateException) {
            throw new PublisherException(templateException.getMessage(), templateException);
        }
    }

    default String getTemplateMimeType(JsonObject config) {
        try {
            return config.getString(CONFIG_TEMPLATE_MIME_TYPE_KEY);
        } catch (NullPointerException | ClassCastException templateException) {
            throw new PublisherException(templateException.getMessage(), templateException);
        }
    }

    default String prepareTemplate(final Notification notification, final PebbleTemplate template, JsonObject config) throws IOException {
        final String baseUrl = ConfigProvider.getConfig()
                .getOptionalValue("dtrack.general.base.url", String.class)
                .map(value -> value.replaceAll("/$", ""))
                .orElse("");

        final Map<String, Object> context = new HashMap<>();
        final long epochSecond = notification.getTimestamp().getSeconds();
        context.put("timestampEpochSecond", epochSecond);
        context.put("timestamp", ProtobufUtil.formatTimestamp(notification.getTimestamp()));
        context.put("notification", notification);
        context.put("baseUrl", baseUrl);

        if (notification.getScope() == SCOPE_PORTFOLIO) {
            if (notification.getSubject().is(NewVulnerabilitySubject.class)) {
                final var subject = notification.getSubject().unpack(NewVulnerabilitySubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(NewVulnerableDependencySubject.class)) {
                final var subject = notification.getSubject().unpack(NewVulnerableDependencySubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(VulnerabilityAnalysisDecisionChangeSubject.class)) {
                final var subject = notification.getSubject().unpack(VulnerabilityAnalysisDecisionChangeSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(PolicyViolationAnalysisDecisionChangeSubject.class)) {
                final var subject = notification.getSubject().unpack(PolicyViolationAnalysisDecisionChangeSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(BomConsumedOrProcessedSubject.class)) {
                final var subject = notification.getSubject().unpack(BomConsumedOrProcessedSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(BomProcessingFailedSubject.class)) {
                final var subject = notification.getSubject().unpack(BomProcessingFailedSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(BomValidationFailedSubject.class)) {
                final var subject = notification.getSubject().unpack(BomValidationFailedSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(VexConsumedOrProcessedSubject.class)) {
                final var subject = notification.getSubject().unpack(VexConsumedOrProcessedSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(PolicyViolationSubject.class)) {
                final var subject = notification.getSubject().unpack(PolicyViolationSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            } else if (notification.getSubject().is(ProjectVulnAnalysisCompleteSubject.class)) {
                final var subject = notification.getSubject().unpack(ProjectVulnAnalysisCompleteSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            }
        }
        else if  (notification.getScope() == SCOPE_SYSTEM) {
            if (notification.getSubject().is(UserSubject.class)) {
                final var subject = notification.getSubject().unpack(UserSubject.class);
                context.put("subject", subject);
                context.put("subjectJson", JsonFormat.printer().print(subject));
            }
        }

        enrichTemplateContext(context, config);

        try (Writer writer = new StringWriter()) {
            template.evaluate(writer, context);
            return writer.toString();
        }
    }

    default void enrichTemplateContext(final Map<String, Object> context, JsonObject config) {
    }
}
