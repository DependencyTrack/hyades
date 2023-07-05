package org.hyades.notification.config;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.proto.notification.v1.BackReference;
import org.hyades.proto.notification.v1.Bom;
import org.hyades.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.hyades.proto.notification.v1.BomProcessingFailedSubject;
import org.hyades.proto.notification.v1.Component;
import org.hyades.proto.notification.v1.Group;
import org.hyades.proto.notification.v1.Level;
import org.hyades.proto.notification.v1.NewVulnerabilitySubject;
import org.hyades.proto.notification.v1.NewVulnerableDependencySubject;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.proto.notification.v1.Policy;
import org.hyades.proto.notification.v1.PolicyCondition;
import org.hyades.proto.notification.v1.PolicyViolation;
import org.hyades.proto.notification.v1.PolicyViolationAnalysis;
import org.hyades.proto.notification.v1.PolicyViolationAnalysisDecisionChangeSubject;
import org.hyades.proto.notification.v1.PolicyViolationSubject;
import org.hyades.proto.notification.v1.Project;
import org.hyades.proto.notification.v1.Scope;
import org.hyades.proto.notification.v1.VexConsumedOrProcessedSubject;
import org.hyades.proto.notification.v1.Vulnerability;
import org.hyades.proto.notification.v1.VulnerabilityAnalysis;
import org.hyades.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;

/**
 * Reflection configuration for notification Protobuf classes.
 */
@SuppressWarnings("unused")
@RegisterForReflection(
        targets = {
                Any.class,
                BackReference.class,
                Bom.class,
                BomConsumedOrProcessedSubject.class,
                BomProcessingFailedSubject.class,
                Component.class,
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