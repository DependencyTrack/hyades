package org.hyades.cyclonedx;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Register classes of {@code cyclonedx-core-java} for reflection.
 * <p>
 * Because the CycloneDX library uses Jackson for (de-)serialization, reflective access
 * to model classes and custom (de-)serializers is required. It's not possible to register
 * entire packages, so we're left with having to register every single class by hand. Fun!
 */
@SuppressWarnings("unused")
@RegisterForReflection(
        targets = {
                org.cyclonedx.model.Ancestors.class,
                org.cyclonedx.model.AttachmentText.class,
                org.cyclonedx.model.Attribute.class,
                org.cyclonedx.model.Bom.class,
                org.cyclonedx.model.BomReference.class,
                org.cyclonedx.model.Commit.class,
                org.cyclonedx.model.Component.class,
                org.cyclonedx.model.ComponentWrapper.class,
                org.cyclonedx.model.Composition.class,
                org.cyclonedx.model.Copyright.class,
                org.cyclonedx.model.Dependency.class,
                org.cyclonedx.model.DependencyList.class,
                org.cyclonedx.model.Descendants.class,
                org.cyclonedx.model.Diff.class,
                org.cyclonedx.model.Evidence.class,
                org.cyclonedx.model.ExtensibleElement.class,
                org.cyclonedx.model.ExtensibleType.class,
                org.cyclonedx.model.Extension.class,
                org.cyclonedx.model.ExternalReference.class,
                org.cyclonedx.model.Hash.class,
                org.cyclonedx.model.IdentifiableActionType.class,
                org.cyclonedx.model.Issue.class,
                org.cyclonedx.model.License.class,
                org.cyclonedx.model.LicenseChoice.class,
                org.cyclonedx.model.Metadata.class,
                org.cyclonedx.model.OrganizationalContact.class,
                org.cyclonedx.model.OrganizationalEntity.class,
                org.cyclonedx.model.Patch.class,
                org.cyclonedx.model.Pedigree.class,
                org.cyclonedx.model.Property.class,
                org.cyclonedx.model.ReleaseNotes.class,
                org.cyclonedx.model.Service.class,
                org.cyclonedx.model.ServiceData.class,
                org.cyclonedx.model.Signature.class,
                org.cyclonedx.model.Source.class,
                org.cyclonedx.model.Swid.class,
                org.cyclonedx.model.Tool.class,
                org.cyclonedx.model.Variants.class,
                org.cyclonedx.model.vulnerability.Rating.class,
                org.cyclonedx.model.vulnerability.Vulnerability.class,
                org.cyclonedx.model.vulnerability.Vulnerability10.class,
                org.cyclonedx.util.CollectionTypeSerializer.class,
                org.cyclonedx.util.ComponentWrapperDeserializer.class,
                org.cyclonedx.util.ComponentWrapperSerializer.class,
                org.cyclonedx.util.CustomDateSerializer.class,
                org.cyclonedx.util.DependencyDeserializer.class,
                org.cyclonedx.util.DependencySerializer.class,
                org.cyclonedx.util.ExtensibleTypesSerializer.class,
                org.cyclonedx.util.ExtensionDeserializer.class,
                org.cyclonedx.util.ExtensionSerializer.class,
                org.cyclonedx.util.ExternalReferenceSerializer.class,
                org.cyclonedx.util.LicenseChoiceSerializer.class,
                org.cyclonedx.util.LicenseDeserializer.class,
                org.cyclonedx.util.VulnerabilityDeserializer.class
        },
        ignoreNested = false
)
public class CycloneDxReflectionConfiguration {
}
