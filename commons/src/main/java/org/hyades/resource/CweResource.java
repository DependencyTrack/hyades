package org.hyades.resource;

import org.hyades.model.Cwe;
import org.hyades.resolver.CweResolver;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@ApplicationScoped
@Path("/cwe")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CweResource {
    Logger logger = Logger.getLogger("CweResource");

    @GET
    @Path("/data")
    public Response getCweData(@QueryParam("id") int id) {
        Cwe result = CweResolver.getInstance().lookup(id);
        logger.info("Printing result here: " + result);
        if (result != null) {
            return Response.ok(result).build();
        } else {
            return Response.status(Status.NOT_FOUND.getStatusCode(),
                    "Cwe with this id not found " + id).build();
        }
    }

}