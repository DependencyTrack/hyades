package org.acme.client.snyk;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@RegisterRestClient
public interface SnykClient {

    @GET // Quarkus and Snyk disagree on how the purl should be encoded. Weird hack below.
    @Path("/rest/orgs/{orgId}/packages/pkg%3A{purlType}%2F{purlNamespace}%2F{purlName}%40{purlVersion}/issues")
    @Consumes("application/vnd.api+json")
    Page<Issue> getIssues(@HeaderParam("Authorization") final String token, @PathParam("orgId") final String orgId,
                          @PathParam("purlType") final String purlType, @PathParam("purlNamespace") final String purlNamespace,
                          @PathParam("purlName") final String purlName, @PathParam("purlVersion") final String purlVersion,
                          @QueryParam("version") final String version);

    @GET // Quarkus and Snyk disagree on how the purl should be encoded. Weird hack below.
    @Path("/rest/orgs/{orgId}/packages/pkg%3A{purlType}%2F{purlName}%40{purlVersion}/issues")
    @Consumes("application/vnd.api+json")
    Page<Issue> getIssues(@HeaderParam("Authorization") final String token, @PathParam("orgId") final String orgId,
                          @PathParam("purlType") final String purlType, @PathParam("purlName") final String purlName,
                          @PathParam("purlVersion") final String purlVersion, @QueryParam("version") final String version);

}
