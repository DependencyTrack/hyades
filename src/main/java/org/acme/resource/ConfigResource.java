package org.acme.resource;

import alpine.model.ConfigProperty;
import io.swagger.annotations.ApiOperation;
import org.acme.consumer.ConfigConsumer;
import org.acme.producer.ConfigProducer;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

    Logger logger = Logger.getLogger("ConfigResource");

    @Inject
    ConfigProducer configProducer;

    @Inject
    ConfigConsumer configConsumer;

    @POST
    @ApiOperation(
            value = "Updates an array of config properties",
            response = ConfigProperty.class,
            responseContainer = "List"
    )
    public Response updateConfiguration(List<ConfigProperty> list) {
        logger.info("Updating configuration properties from dependency-track.");
            // push the configurations on topic 'configuration'
            configProducer.sendConfigToKafka(list);
        return Response.accepted().build();
    }

    @GET
    public Response getConfiguration() {
        List<ConfigProperty> configProperties = configConsumer.getConfigProperties();
        if (configProperties!=null) {
            return Response.ok(configProperties).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND.getStatusCode(),
                    "No config properties found.").build();
        }
    }
}
