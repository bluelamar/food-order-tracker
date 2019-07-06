package org.bluelamar.fotracker;
 
import java.util.HashSet;
import java.util.Set;
 
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
 
import org.bluelamar.fotracker.service.ConfigurationResource;
 
@ApplicationPath("/fotracker")
public class FOTrackerApplication extends Application {
 
   private Set<Object> singletons = new HashSet<Object>();
   private Set<Class<?>> empty = new HashSet<Class<?>>();
 
   public FOTrackerApplication() {
      singletons.add(new ConfigurationResource());
   }
 
   @Override
   public Set<Class<?>> getClasses() {
      return empty;
   }
 
   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }
}
