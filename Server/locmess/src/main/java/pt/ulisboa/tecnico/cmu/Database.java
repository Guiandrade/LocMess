package pt.ulisboa.tecnico.cmu;
import pt.ulisboa.tecnico.cmu.database.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;


public class Database {
  private Profiles profiles;
  private Locations locations;
  private Messages messages;
  public Database(){
    profiles=new Profiles ();
    locations=new Locations();
    messages=new Messages();
  }

  public JSONObject login(JSONObject req){
    String username=req.get("username").toString();
    String password=req.get("password").toString();
    JSONObject res= new JSONObject();
    if (profiles.login(username, password)){

      res.put("status","ok");
      return res;
    }
    res.put("status","authentication failed");
    return res;
  }

  public JSONObject signup(JSONObject req){
    String username=req.get("username").toString();
    String password=req.get("password").toString();
    JSONObject res= new JSONObject();
    if (profiles.signup(username, password)){

      res.put("status","ok");
      return res;
    }
    res.put("status","User already exists");
    return res;
  }

  public JSONObject getAllKeys(JSONObject req){
    JSONObject res= new JSONObject();
    res.put("status","ok");
    res.put("keys",profiles.getAllKeys());
    return res;
  }

  public JSONObject getUserKeys(JSONObject req){
    String username=req.get("username").toString();
    System.out.println(username);
    JSONObject res= new JSONObject();
    HashMap<String,Set<String>> keys=profiles.getUserKeys(username);
    if (keys !=null){
      for(Map.Entry<String,Set<String>> e : keys.entrySet()) {
         String key = e.getKey();
         Set<String> value = e.getValue();
         res.put(key,value);

     }
     res.put("status","ok");
   }else{
     res.put("status","authentication failed");
   }
    return res;
  }

  public JSONObject addKey(JSONObject req){
    String username=req.get("username").toString();
    JSONObject res= new JSONObject();
    HashMap<String,Set<String>> keys=new HashMap<String,Set<String>>();
    getKeyValue(req,"keys",keys);
    for(Map.Entry<String,Set<String>> e : keys.entrySet()) {
       String key = e.getKey();
       for(String value : keys.get(key)){
         profiles.addKey(username, key,value);
       }
     }

      res.put("status","ok");
      return res;

  }

  public JSONObject removeKey(JSONObject req ){
    String username=req.get("username").toString();
    JSONObject res= new JSONObject();
    HashMap<String,Set<String>> keys=new HashMap<String,Set<String>>();
    getKeyValue(req,"keys",keys);
    for(Map.Entry<String,Set<String>> e : keys.entrySet()) {
       String key = e.getKey();
       for(String value : keys.get(key)){
         profiles.removeKey(username, key,value);
       }
     }
    res.put("status","ok");
    return res;
  }
  public JSONObject getLocations(JSONObject req){
    JSONObject res= new JSONObject();
    res.put("locations",locations.getLocationsJson());
    res.put("status","ok");
    return res;

  }

  public JSONObject addLocation(JSONObject req){
    JSONObject res= new JSONObject();
    if(req.has("ssid")){
      String ssid=req.getString("ssid");

      locations.addLocation(ssid);

    }else{
      String location=req.getString("location");
      double latitude=req.getDouble("latitude");
      double longitude=req.getDouble("longitude");
      int radius=Integer.parseInt(req.get("radius").toString());
      locations.addLocation(location, latitude, longitude, radius);
    }

    res.put("status","ok");
    return res;
  }

  public JSONObject deleteMessage(JSONObject req){
    JSONObject res= new JSONObject();
    String username=req.get("username").toString();

      JSONArray array=req.getJSONArray("ids");
     for (int i = 0 ; i < array.length(); i++) {
      messages.deleteMessage(username,array.getString(i));
    }

      res.put("status","ok");

    return res;
  }

  public JSONObject deleteLocation(JSONObject req){
    JSONObject res= new JSONObject();
    JSONArray array=req.getJSONArray("locations");
    for (int i = 0 ; i < array.length(); i++) {
      locations.deleteLocation(array.getString(i));
    }


    res.put("status","ok");

    return res;
  }
  public  JSONObject getMessages(JSONObject req){
    JSONObject res= new JSONObject();
    Set<JSONObject> mess=new HashSet<JSONObject>() ;
    Set<String> location=new HashSet<String>() ;

    String username=req.get("username").toString();
    if(req.has("ssids")){
      JSONArray array=req.getJSONArray("ssids");
      for (int i = 0 ; i < array.length(); i++) {
        location.add(array.getString(i));
      }
    }

    if(req.has("latitude")){
      double latitude=Double.parseDouble(req.getString("latitude"));
      double longitude=Double.parseDouble(req.getString("longitude"));
      location.addAll(locations.getUserLocation(latitude,longitude));
    }

    HashMap<String,Set<String>> userKeys=profiles.getUserKeys(username);
    for (String l : location) {
      System.out.println("location");
      System.out.println(l);
      Set<JSONObject> message=messages.getMessages(l,userKeys);
      if(message.size()!=0){
        mess.addAll(message);
      }
    }
    res.put("messages",mess);
    res.put("status","ok");
    return res;
  }

  public  JSONObject getUserMessages(JSONObject req){
    JSONObject res= new JSONObject();

    String username=req.get("username").toString();
    res.put("messages",messages.getUserMessages(username));
    res.put("status","ok");
    return res;
  }

  public JSONObject createMessage(JSONObject req){
    String title=req.get("title").toString();
    String username=req.get("username").toString();
    String location=req.get("location").toString();
    String initTime=req.get("initTime").toString();
    String endTime=req.get("endTime").toString();
    String body=req.get("body").toString();
    HashMap<String,Set<String>> whitelist=new HashMap<String,Set<String>>();
    HashMap<String,Set<String>> blacklist=new HashMap<String,Set<String>>();

    getKeyValue(req,"whitelist",whitelist);
    System.out.println(whitelist.get("carros"));

    getKeyValue(req,"blacklist",blacklist);
      System.out.println(blacklist);


    messages.createMessage(title,username,location ,initTime ,endTime, body, whitelist, blacklist);
    JSONObject res= new JSONObject();
    res.put("status","ok");
    return res;
  }

  public void getKeyValue(JSONObject req,String nameParams,HashMap<String,Set<String>> list){
    req=req.getJSONObject(nameParams);

    Iterator<String> iter = req.keys();
    while (iter.hasNext()) {
      String key = iter.next();
      try {
            JSONArray array=req.getJSONArray(key);
           for (int i = 0 ; i < array.length(); i++) {
             String value = array.getString(i);
             if(list.containsKey(key)){
               list.get(key).add(value);
               System.out.println("cona");
             }else{
                Set<String> val = new HashSet<String>();
                val.add(value);
                list.put(key,val);
             }

           }

      } catch (Exception e) {
          e.printStackTrace();
          // Something went wrong!
      }
    }


  }




}
