package org.bluelamar.fotracker.service;

import java.util.ArrayList;
import java.util.List;
 
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
 
//import net.restfulapi.app.dao.ConfigurationDB;
import org.bluelamar.fotracker.model.FoodOrder;
import org.bluelamar.fotracker.model.FoodOrders;
//import org.bluelamar.fotracker.model.common.Message;
//import org.bluelamar.fotracker.model.common.Status;
 
/**
 * This REST resource has common path "/orders" and
 * represents configurations collection resource as well as individual collection resources.
 *
 * Default MIME type for this resource is "application/json"
 * */
@Path("/order")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationResource
{
    /**
     * Use uriInfo to get current context path and to build HATEOAS links
     * */
    @Context
    UriInfo uriInfo;
     
    /**
     * Get configurations collection resource mapped at path "HTTP GET /configurations"
     * */
    @GET
    public FoodOrders getFoodOrders() {
          
        //List<Configuration> list = ConfigurationDB.getAllConfigurations();
        List<FoodOrder> list = getAllOrders();
        //Configurations configurations = new Configurations();
        FoodOrders fos = new FoodOrders();
        fos.setFoodOrders(list);
        //configurations.setConfigurations(list);
        //configurations.setSize(list.size());
          
        //Set link for primary collection
        //Link link = Link.fromUri(uriInfo.getPath()).rel("uri").build();
        //configurations.setLink(link);
          
        //Set links in configuration items
        //for(Configuration c: list){
        //    Link lnk = Link.fromUri(uriInfo.getPath() + "/" + c.getId()).rel("self").build();
        //    c.setLink(lnk);
        //}
        //return configurations;
        return fos;
    }
      
    /**
     * Get individual configuration resource mapped at path "HTTP GET /configurations/{id}"
     * */
    @GET
    @Path("/{id}")
    public Response getConfigurationById(@PathParam("id") Integer id){
        /* FIX
    	Configuration config = ConfigurationDB.getConfiguration(id);
         
        if(config == null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
        }
          
        if(config != null){
            UriBuilder builder = UriBuilder.fromResource(ConfigurationResource.class)
                                            .path(ConfigurationResource.class, "getConfigurationById");
            Link link = Link.fromUri(builder.build(id)).rel("self").build();
            config.setLink(link);
        } */
    	FoodOrder fo = new FoodOrder();
        fo.setName("hot dog");
        fo.setTemp("hot");
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(fo).build();
    }
     
    /**
     * Create NEW configuration resource in configurations collection resource
     * */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createFoodOrder(FoodOrder forder) {
        /* FIX TODO
    	if(forder.getContent() == null)  {
            return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)
                            .entity(new Message("Config content not found"))
                            .build();
        } */
 
        //Integer id = ConfigurationDB.createConfiguration(config.getContent(), config.getStatus());
        //Link lnk = Link.fromUri(uriInfo.getPath() + "/" + id).rel("self").build();
        //return Response.status(javax.ws.rs.core.Response.Status.CREATED).location("/TODO" /* lnk.getUri() */).build();
        return Response.status(javax.ws.rs.core.Response.Status.CREATED).build();
    }

    /**
     * Delete configuration resource by it's "id" at path "/configurations/{id}"
     * */
    @DELETE
    @Path("/{id}")
    public Response deleteFoodOrder(@PathParam("id") Integer id){
         
        /* FIX Configuration origConfig = ConfigurationDB.getConfiguration(id);
        if(origConfig == null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
        } */
         
        //ConfigurationDB.removeConfiguration(id);
        return Response.status(javax.ws.rs.core.Response.Status.OK).build();
    }

    List<FoodOrder> getAllOrders() {
        // FIX stubbed for now
        FoodOrder fo = new FoodOrder();
        fo.setName("ice cream");
        fo.setTemp("frozen");
        List<FoodOrder> list = new ArrayList<>();
        list.add(fo);
        return list;
    }

    /**
     * Initialize the application with default configuration
     * */
    static {
        //ConfigurationDB.createConfiguration("Some Content", Status.ACTIVE);
        //ConfigurationDB.createConfiguration("Some More Content", Status.INACTIVE);
    }
}
