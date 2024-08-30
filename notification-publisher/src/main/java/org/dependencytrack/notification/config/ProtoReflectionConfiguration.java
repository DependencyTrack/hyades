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
package org.dependencytrack.notification.config;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.dependencytrack.proto.notification.v1.BackReference;
import org.dependencytrack.proto.notification.v1.Bom;
import org.dependencytrack.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.BomProcessingFailedSubject;
import org.dependencytrack.proto.notification.v1.BomValidationFailedSubject;
import org.dependencytrack.proto.notification.v1.Component;
import org.dependencytrack.proto.notification.v1.ComponentVulnAnalysisCompleteSubject;
import org.dependencytrack.proto.notification.v1.Group;
import org.dependencytrack.proto.notification.v1.Level;
import org.dependencytrack.proto.notification.v1.NewVulnerabilitySubject;
import org.dependencytrack.proto.notification.v1.NewVulnerableDependencySubject;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.Policy;
import org.dependencytrack.proto.notification.v1.PolicyCondition;
import org.dependencytrack.proto.notification.v1.PolicyViolation;
import org.dependencytrack.proto.notification.v1.PolicyViolationAnalysis;
import org.dependencytrack.proto.notification.v1.PolicyViolationAnalysisDecisionChangeSubject;
import org.dependencytrack.proto.notification.v1.PolicyViolationSubject;
import org.dependencytrack.proto.notification.v1.Project;
import org.dependencytrack.proto.notification.v1.ProjectVulnAnalysisCompleteSubject;
import org.dependencytrack.proto.notification.v1.ProjectVulnAnalysisStatus;
import org.dependencytrack.proto.notification.v1.Scope;
import org.dependencytrack.proto.notification.v1.UserSubject;
import org.dependencytrack.proto.notification.v1.VexConsumedOrProcessedSubject;
import org.dependencytrack.proto.notification.v1.Vulnerability;
import org.dependencytrack.proto.notification.v1.VulnerabilityAnalysis;
import org.dependencytrack.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;

/**
 * Reflection configuration for notification Protobuf classes.
 */
@SuppressWarnings("unused")
@RegisterForReflection(
        targets = {
                // Classes of the Protobuf library.
                // Can possibly remove them once Quarkus recognizes them.
                com.google.protobuf.DescriptorProtos.class,
                // Our own Protobuf classes.
                Any.class,
                BackReference.class,
                Bom.class,
                BomConsumedOrProcessedSubject.class,
                BomProcessingFailedSubject.class,
                BomValidationFailedSubject.class,
                Component.class,
                ComponentVulnAnalysisCompleteSubject.class,
                Group.class,
                NewVulnerabilitySubject.class,
                NewVulnerableDependencySubject.class,
                Notification.class,
                Level.class,
                Policy.class,
                PolicyCondition.class,
                PolicyViolation.class,
                PolicyViolationAnalysis.class,
                PolicyViolationAnalysisDecisionChangeSubject.class,
                PolicyViolationSubject.class,
                Project.class,
                ProjectVulnAnalysisCompleteSubject.class,
                UserSubject.class,
                ProjectVulnAnalysisStatus.class,
                Scope.class,
                Timestamp.class,
                VexConsumedOrProcessedSubject.class,
                Vulnerability.class,
                VulnerabilityAnalysis.class,
                VulnerabilityAnalysisDecisionChangeSubject.class,
        },
        ignoreNested = false
)
public class ProtoReflectionConfiguration {
}