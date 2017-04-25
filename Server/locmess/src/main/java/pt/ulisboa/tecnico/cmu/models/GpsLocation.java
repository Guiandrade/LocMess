package pt.ulisboa.tecnico.cmu.models;
import org.json.JSONObject;


public class GpsLocation {
  private String location;
  private double latitude;
  private double longitude;
  private int radius;

  public GpsLocation(String location, double latitude, double longitude, int radius){
    this.location=location;
    this.latitude=latitude;
    this.longitude=longitude;
    this.radius=radius;
  }
  public String getLocation(){
    return this.location;
  }
  public double getLatitude(){
    return this.latitude;
  }
  public double getLongitude(){
    return this.longitude;
  }
  public JSONObject toJson(){
    JSONObject json= new JSONObject();
    json.put("location",this.location);
    json.put("latitude",this.latitude);
    json.put("longitude",this.longitude);
    return json;
  }

  public boolean inRadius(double lat, double lng) {
    double earthRadius = 6371000.0;
    double dLat = Math.toRadians(lat-this.latitude);
    double dLng = Math.toRadians(lng-this.longitude);
    double sindLat = Math.sin(dLat / 2);
    double sindLng = Math.sin(dLng / 2);
    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
            * Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat));
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    double dist = earthRadius * c;
    System.out.println(dist);

    return dist<this.radius;
    }

}
