package org.acme.client.snyk;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@RegisterRestClient
public interface SnykClient {

    @GET
    @Path("/rest/orgs/{orgId}/packages/{purl}/issues")
    @Consumes("application/vnd.api+json")
    Page<Issue> getIssues(@HeaderParam("Authorization") final String token, @PathParam("orgId") final String orgId,
                          @PathParam("purl") @Encoded final String purl, @QueryParam("version") final String version);

}
