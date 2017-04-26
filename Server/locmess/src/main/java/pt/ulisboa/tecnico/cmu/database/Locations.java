package pt.ulisboa.tecnico.cmu.database;
import pt.ulisboa.tecnico.cmu.models.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.json.JSONObject;


public class Locations {
  private Set<GpsLocation> gpsLocations = new HashSet<GpsLocation>();
  private Set<SSID> ssidLocations = new HashSet<SSID>();
  public void addLocation(String ssid){

    boolean check=false;
    for (SSID l : ssidLocations) {
      if(l.verify(ssid)){
        check=true;
      }
    }
    if(!check){
      SSID loc =new SSID(ssid);
      ssidLocations.add(loc);
    }
    System.out.println(ssidLocations.size());

  }


  public void addLocation(String location, double latitude, double longitude, int radius){

    boolean check=false;
    for (GpsLocation l : gpsLocations) {
      if(l.getLocation().equals(location)){
        check=true;
      }
    }
    if(!check){
      GpsLocation loc =new GpsLocation(location, latitude, longitude, radius);
      gpsLocations.add(loc);
    }

  }


  public Set<String> getUserLocation(double latitude, double longitude){

    Set<String> locationUser = new HashSet<String>();
    for (GpsLocation l : gpsLocations) {
      if(l.inRadius(latitude,longitude)){
        System.out.println(l.getLocation());
        locationUser.add(l.getLocation());
      }
    }
    return locationUser;
  }
  public void deleteLocation(String location){
    for (Iterator<GpsLocation> iterator = gpsLocations.iterator(); iterator.hasNext();) {
      GpsLocation l =  iterator.next();
      if (l.getLocation().equals(location)) {
        iterator.remove();
      }


    }
    for (Iterator<SSID> it = ssidLocations.iterator(); it.hasNext();) {
      SSID ssid =  it.next();
      System.out.println(ssid.getSSID());
      if (ssid.verify(location)) {
        it.remove();
      }
    }

  }

  public Set<JSONObject> getLocationsJson(){
    Set<JSONObject> locationsJson = new HashSet<JSONObject>();
    for (GpsLocation l : gpsLocations) {
      locationsJson.add(l.toJson());
    }
    for (SSID l : ssidLocations) {
      locationsJson.add(l.toJson());
    }
    return locationsJson;
  }
}
