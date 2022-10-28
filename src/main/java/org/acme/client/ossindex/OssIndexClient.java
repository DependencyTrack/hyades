package org.acme.client.ossindex;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient
@ClientHeaderParam(name = "Authorization", value = "{buildAuthorization}", required = false)
public interface OssIndexClient {

    @POST
    @Path("/api/v3/component-report")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<ComponentReport> getComponentReports(final ComponentReportRequest request);

    @POST
    @Path("/api/v3/component-report")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<ComponentReport> getComponentReports(@HeaderParam("Authorization") final String authorization, final ComponentReportRequest request);

}
