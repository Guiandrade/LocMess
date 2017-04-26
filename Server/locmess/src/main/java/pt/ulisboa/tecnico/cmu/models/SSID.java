package pt.ulisboa.tecnico.cmu.models;
import org.json.JSONObject;


public class SSID {
  private String ssid;
  private String mac;


  public SSID(String ssid){
    this.ssid=ssid;


  }
  public String getSSID(){
    return this.ssid;
  }

  public Boolean verify(String ssid){
    return this.ssid.equals(ssid);
  }

  public JSONObject toJson(){
    JSONObject json= new JSONObject();
    json.put("ssid",this.ssid);
    return json;
  }

}
