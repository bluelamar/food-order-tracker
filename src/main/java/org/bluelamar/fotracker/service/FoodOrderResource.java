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
 
import org.bluelamar.fotracker.IdGenerator;
import org.bluelamar.fotracker.Decayer;
import org.bluelamar.fotracker.model.FoodOrder;
import org.bluelamar.fotracker.model.FoodOrders;
import org.bluelamar.fotracker.model.TrackedOrder;
//import org.bluelamar.fotracker.model.common.Message;
//import org.bluelamar.fotracker.model.common.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * This REST resource has common path "/orders" and
 * represents configurations collection resource as well as individual collection resources.
 *
 * Default MIME type for this resource is "application/json"
 * */
@Path("/order")
@Produces(MediaType.APPLICATION_JSON)
public class FoodOrderResource
{
	private static final Logger LOG = LoggerFactory.getLogger(FoodOrderResource.class);

    static final String ID_GENERATOR_PROP = "fotracker.idgeneratorclass";
    static final String DEF_ID_GENERATOR = "org.bluelamar.fotracker.service.OrderIdGenerator";
    static final String FOOD_DECAYER_PROP = "fotracker.fooddecayerclass";
    static final String DEF_FOOD_DECAYER = "org.bluelamar.fotracker.service.FoodDecayer";
    static final String MAX_SHELF_CNT_PROP = "fotracker.shelfmaxcnt";
    static final int DEF_MAX_SHELF_CNT = 15;
    static final String MAX_OF_SHELF_CNT_PROP = "fotracker.overflowshelfmaxcnt";
    static final int DEF_MAX_OF_SHELF_CNT = 20;
    
    static Map<FoodShelf.TempType, FoodShelf> shelves;
    static FoodShelf overFlow;
    static int maxShelfCnt;
    static int maxOfShelfCnt;
    static IdGenerator idGenerator;
    static Decayer foodDecayer;
    
    /**
     * Use uriInfo to get current context path and to build HATEOAS links
     * */
    @Context
    UriInfo uriInfo;
     
    /**
     * Get FoodOrder collection resource mapped at path "HTTP GET /order"
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
     * Get individual FoodOrder resource mapped at path "HTTP GET /order/{id}"
     * */
    @GET
    @Path("/{id}")
    public Response getFoodOrderById(@PathParam("id") Integer id){
        /* FIX
    	Configuration config = ConfigurationDB.getConfiguration(id);
         
        if(config == null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
        }
          
        if(config != null){
            UriBuilder builder = UriBuilder.fromResource(FoodOrderResource.class)
                                            .path(FoodOrderResource.class, "getConfigurationById");
            Link link = Link.fromUri(builder.build(id)).rel("self").build();
            config.setLink(link);
        } */
    	FoodOrder fo = new FoodOrder();
        fo.setName("hot dog");
        fo.setTemp("hot");
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(fo).build();
    }
     
    /**
     * Create NEW FoodOrder resource in Food orders collection resource
     * */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createFoodOrder(FoodOrder forder) {
        
    	// check for valid temp
    	FoodShelf.TempType tt;
    	try {
    		tt = FoodShelf.TempType.xlate(forder.getTemp());
    	} catch (FoodShelf.InvalidTempException exc) {
    		return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).
                    entity("failed").build();
    	}
    	// FIX TODO check if shelves full - return internal error if so
    	int id = idGenerator.generate();
    	TrackedOrder to = new TrackedOrder(id);
    	to.setFoodOrder(forder);
    	long orderTime = System.currentTimeMillis() / 1000;
    	to.setOrderTimeSecs(orderTime);
    	shelves.get(tt).addOrder(to);
 
    	LOG.info("CFO:POST: placed order on shelf: order=" + to);
        return Response.status(javax.ws.rs.core.Response.Status.CREATED).entity("ok").build();
    }

    /**
     * Delete FoodOrder resource by it's "id" at path "/order/{id}"
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
    
    static Object loadClass(String propName, String defaultClass) {

    	String className = System.getProperty(propName, defaultClass);
    	Object obj;
    	try {
    		// FIX Constructor.newInstance()
    		obj = Class.forName(className).newInstance();
    	} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid class specified=").append(className).
                append(" : for property=").append(propName).
                append(" : error=").append(e.getMessage());
            LOG.error(sb.toString());
            throw new java.util.MissingResourceException(sb.toString(), className, propName);
        }

        return obj;
    }
    
    static int getNumProp(String propName, int defaultVal) {
    	String numStr = System.getProperty(propName);
        if (numStr != null) {
        	// convert string to a number
        	return Integer.parseInt(numStr);
        }
        return defaultVal;
    }

    /**
     * Initialize the application with default configuration
     * */
    static {
        //ConfigurationDB.createConfiguration("Some Content", Status.ACTIVE);
        //ConfigurationDB.createConfiguration("Some More Content", Status.INACTIVE);
    	// initialize configured params
    	// load instance of IdFactory
        Object obj = loadClass(ID_GENERATOR_PROP, DEF_ID_GENERATOR);
        idGenerator = (IdGenerator)obj;
        
        obj = loadClass(FOOD_DECAYER_PROP, DEF_FOOD_DECAYER);
        foodDecayer = (FoodDecayer)obj;
        
        maxShelfCnt = getNumProp(MAX_SHELF_CNT_PROP, DEF_MAX_SHELF_CNT);
        shelves = new HashMap<>();
        shelves.put(FoodShelf.TempType.HOT, new FoodShelf(maxShelfCnt, FoodShelf.TempType.HOT));
        shelves.put(FoodShelf.TempType.COLD, new FoodShelf(maxShelfCnt, FoodShelf.TempType.COLD));
        shelves.put(FoodShelf.TempType.FROZEN, new FoodShelf(maxShelfCnt, FoodShelf.TempType.FROZEN));
        
        maxOfShelfCnt = getNumProp(MAX_OF_SHELF_CNT_PROP, DEF_MAX_OF_SHELF_CNT);
        overFlow = new FoodShelf(maxOfShelfCnt, FoodShelf.TempType.AGNOSTIC);
    }
}
