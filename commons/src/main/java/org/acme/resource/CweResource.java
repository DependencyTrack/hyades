package org.acme.resource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.acme.Main;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/cwe")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CweResource {
    Logger logger = Logger.getLogger("CweResource");

    @GET
    @Path("/data")
    public Response getCweData(@QueryParam("id") int id) {
        if (Main.cweInfo.isEmpty()) {
            return Response.status(Status.NOT_FOUND.getStatusCode(),
                    "Cwe values have not been loaded into application on startup. Please check. ").build();
        } else {
            String result = Main.cweInfo.get(id);
            logger.info("Printing result here: " + result);
            if (result != null) {
                return Response.ok(result).build();
            } else {
                return Response.status(Status.NOT_FOUND.getStatusCode(),
                        "Cwe with this id not found " + id).build();
            }
        }
    }

}