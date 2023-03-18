package org.hyades.apiserver;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.hyades.apiserver.model.BomProcessingResponse;
import org.hyades.apiserver.model.BomUploadRequest;
import org.hyades.apiserver.model.BomUploadResponse;
import org.hyades.apiserver.model.CreateTeamRequest;
import org.hyades.apiserver.model.CreateVulnerabilityRequest;
import org.hyades.apiserver.model.Finding;
import org.hyades.apiserver.model.Project;
import org.hyades.apiserver.model.Team;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

@Path("/api/v1")
@RegisterClientHeaders(ApiServerClientHeaderFactory.class)
public interface ApiServerClient {

    @POST
    @Path("/user/forceChangePassword")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void forcePasswordChange(@FormParam("username") final String username,
                             @FormParam("password") final String password,
                             @FormParam("newPassword") final String newPassword,
                             @FormParam("confirmPassword") final String confirmPassword);

    @POST
    @Path("/user/login")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    String login(@FormParam("username") final String username,
                 @FormParam("password") final String password);

    @PUT
    @Path("/team")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Team createTeam(final CreateTeamRequest request);

    @POST
    @Path("/permission/{permission}/team/{uuid}")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    Team addPermissionToTeam(@PathParam("uuid") final UUID teamUuid,
                             @PathParam("permission") final String permission);

    @PUT
    @Path("/bom")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    BomUploadResponse uploadBom(final BomUploadRequest request);

    @GET
    @Path("/bom/token/{token}")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.WILDCARD)
    BomProcessingResponse isBomBeingProcessed(@PathParam("token") final String token);

    @PUT
    @Path("/vulnerability")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void createVulnerability(final CreateVulnerabilityRequest request);

    @GET
    @Path("/finding/project/{uuid}")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    List<Finding> getFindings(@PathParam("uuid") final UUID projectUuid);

    @GET
    @Path("/project/lookup")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    Project lookupProject(@QueryParam("name") final String name, @QueryParam("version") final String version);

}
