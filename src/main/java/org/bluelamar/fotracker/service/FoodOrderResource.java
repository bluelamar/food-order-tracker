package org.bluelamar.fotracker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
 
import org.bluelamar.fotracker.model.FoodOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * Default MIME type for this resource is "application/json"
 */
@Path("/order")
@Produces(MediaType.APPLICATION_JSON)
public class FoodOrderResource
{
    private static final Logger LOG = LoggerFactory.getLogger(FoodOrderResource.class);

    static FoodOrderService foSvc;
    
    /**
     * Use uriInfo to get current context path and to build HATEOAS links
     * */
    @Context
    UriInfo uriInfo;

    /**
     * Create NEW FoodOrder resource in Food orders collection resource
     * */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createFoodOrder(FoodOrder forder) {
        
        FoodOrderService.RetCode ret = foSvc.PlaceOrder(forder);
        if (ret == FoodOrderService.RetCode.CREATED) {
            return Response.status(javax.ws.rs.core.Response.Status.CREATED).
                    entity("ok").build();
        }
        if (ret == FoodOrderService.RetCode.BAD_REQ) {
            return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).
                    entity("failed").build();
        }
        if (ret == FoodOrderService.RetCode.UNAVAIL) {
            return Response.status(javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE).
                    entity("failed").build();
        }
        
        return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).
                    entity("failed").build();
    }

    public void Shutdown() {
        foSvc.Shutdown();
    }

    /**
     * Initialize the application with default configuration
     * */
    static {

        foSvc = new FoodOrderService();
        foSvc.Init();
    }
}
