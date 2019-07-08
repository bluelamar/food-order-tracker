package org.bluelamar.fotracker;
 
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
 
import org.bluelamar.fotracker.service.FoodOrderResource;

@WebListener
@ApplicationPath("/fotracker")
public class FOTrackerApplication extends Application implements ServletContextListener {

	FoodOrderResource rsrc = new FoodOrderResource();
   private Set<Object> singletons = new HashSet<Object>();
   private Set<Class<?>> empty = new HashSet<Class<?>>();
 
   public FOTrackerApplication() {
      singletons.add(rsrc);
   }
 
   @Override
   public Set<Class<?>> getClasses() {
      return empty;
   }
 
   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }
   
   @Override
   public void 	contextDestroyed(ServletContextEvent sce) {
	   // Receives notification that the ServletContext is about to be shut down
	   rsrc.Shutdown();
   }
   
   @Override
   public void contextInitialized(ServletContextEvent sce) {}
}
