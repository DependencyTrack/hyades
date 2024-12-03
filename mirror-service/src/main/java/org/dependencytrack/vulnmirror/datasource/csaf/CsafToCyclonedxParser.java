package org.dependencytrack.vulnmirror.datasource.csaf;

import org.cyclonedx.proto.v1_6.Vulnerability;

import io.github.csaf.sbom.schema.generated.Csaf;
import io.github.csaf.sbom.schema.generated.Csaf.Acknowledgment;
import io.github.csaf.sbom.schema.generated.Csaf.Remediation;

public class CsafToCyclonedxParser {

    public void sampleConvert() {

        Csaf.Vulnerability in = new Csaf.Vulnerability();
        Vulnerability.Builder out = Vulnerability.newBuilder();

        /** Contains a list of acknowledgment elements associated with this vulnerability item. */
        // in.getAcknowledgments().forEach(a -> {
        //     /** Contains the names of contributors being recognized. */
        //     a.getNames();
            
        //     /** Contains the name of a contributing organization being recognized. */
        //     a.getOrganization();
        //     /** SHOULD represent any contextual details the document producers wish to make known about the acknowledgment or acknowledged parties. */
        //     a.getSummary();
        //     /** Specifies a list of URLs or location of the reference to be acknowledged. */
        //     a.getUrls();    
        // });

        // /** Holds the MITRE standard Common Vulnerabilities and Exposures (CVE) tracking number for the vulnerability. */
        // in.getCve();

        // /** Holds the MITRE standard Common Weakness Enumeration (CWE) for the weakness associated. */
        // in.getCwe()
        // out.setCwe
        
        // /** Holds the date and time the vulnerability was originally discovered. */
        // val discovery_date: JsonOffsetDateTime? = null,
        // /** Contains a list of machine readable flags. */
        // val flags: Set<Flag>? = null,
        // /** Represents a list of unique labels or tracking IDs for the vulnerability (if such information exists). */
        // val ids: Set<Id>? = null,
        // /** Contains a list of involvements. */
        // val involvements: Set<Involvement>? = null,
        // /** Holds notes associated with this vulnerability item. */
        // val notes: List<Note>? = null,
        // /** Contains different lists of product_ids which provide details on the status of the referenced product related to the current vulnerability. */
        // val product_status: ProductStatus? = null,
        // /** Holds a list of references associated with this vulnerability item. */
        // val references: List<Reference>? = null,
        // /** Holds the date and time the vulnerability was originally released into the wild. */
        // val release_date: JsonOffsetDateTime? = null,
        // /** Contains a list of remediations. */
        // val remediations: List<Remediation>? = null,
        // /** Contains score objects for the current vulnerability. */
        // val scores: List<Score>? = null,
        // /** Contains information about a vulnerability that can change with time. */
        // val threats: List<Threat>? = null,
        // /** Gives the document producer the ability to apply a canonical name or title to the vulnerability. */
        // val title: String? = null

        

    }
    
}
