package pt.ulisboa.tecnico.cmu.database;
import pt.ulisboa.tecnico.cmu.models.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.json.JSONObject;


public class Locations {
  private Set<GpsLocation> locations = new HashSet<GpsLocation>();


  public void addLocation(String location, double latitude, double longitude, int radius){

    boolean check=false;
    for (GpsLocation l : locations) {
      if(l.getLocation().equals(location)){
        check=true;
      }
    }
    if(!check){
      GpsLocation loc =new GpsLocation(location, latitude, longitude, radius);
      locations.add(loc);
    }

  }
  public Set<GpsLocation> getLocations(){
    return locations;
  }
  public Set<String> getUserLocation(double latitude, double longitude){

    Set<String> locationUser = new HashSet<String>();
    for (GpsLocation l : locations) {
      if(l.inRadius(latitude,longitude)){
        System.out.println(l.getLocation());
        locationUser.add(l.getLocation());
      }
    }
    return locationUser;
  }
  public void deleteLocation(String location){
    for (Iterator<GpsLocation> iterator = locations.iterator(); iterator.hasNext();) {
      GpsLocation l =  iterator.next();
      if (l.getLocation().equals(location)) {
        iterator.remove();
      }
    }

  }
  public Set<JSONObject> getLocationsJson(){
    Set<JSONObject> locationsJson = new HashSet<JSONObject>();
    for (GpsLocation l : locations) {
      locationsJson.add(l.toJson());
    }
    return locationsJson;
  }
}
