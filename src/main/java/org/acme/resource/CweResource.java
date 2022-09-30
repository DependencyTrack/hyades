package org.acme.resource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.acme.consumer.CweConsumer;
import org.acme.model.Cwe;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/cwe")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CweResource {
    Logger logger = Logger.getLogger("CweResource");

    @Inject
    CweConsumer cweConsumer;

    @GET
    @Path("/data")
    public Response getCweData(@QueryParam("id") int id) {
        String result = cweConsumer.getCweValues(id);
        logger.info("Printing result here: "+result);
        if (result!=null) {
            return Response.ok(result).build();
        } else {
            return Response.status(Status.NOT_FOUND.getStatusCode(),
                    "No data found for weather station " + id).build();
        }
    }
}