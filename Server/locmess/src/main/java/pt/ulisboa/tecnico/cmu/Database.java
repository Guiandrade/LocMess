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
    String location=req.get("location").toString();
    double latitude=req.getDouble("latitude");
    double longitude=req.getDouble("longitude");
    int radius=Integer.parseInt(req.get("radius").toString());
    JSONObject res= new JSONObject();
    locations.addLocation(location, latitude, longitude, radius);
    res.put("status","ok");
    return res;
  }

  public JSONObject deleteMessage(JSONObject req){
    JSONObject res= new JSONObject();
    String username=req.get("username").toString();
    Set<String> lisId = new HashSet<String>();
    int i=0;
    while(req.has("id"+i)!=false){
      System.out.println(req.get("id"+i).toString());
      boolean check=messages.deleteMessage(username,req.get("id"+i).toString());

      if(check){
        lisId.add("id"+i);
      }
      i++;
    }
        res.put("deleted",lisId);
        res.put("status","ok");

    return res;
  }
  public  JSONObject getMessages(JSONObject req){
    JSONObject res= new JSONObject();

    String username=req.get("username").toString();


    double latitude=req.getDouble("latitude");
    double longitude=req.getDouble("longitude");
    Set<String> location=locations.getUserLocation(latitude,longitude);
    location.add("sintra");
    HashMap<String,Set<String>> userKeys=profiles.getUserKeys(username);
    for (String l : location) {
      System.out.println(l);
      Set<JSONObject> message=messages.getMessages(l,userKeys);
      if(message.size()!=0){
        res.put(l,message);
      }
    }
    res.put("status","ok");
    return res;
  }

  public JSONObject createMessage(JSONObject req){
    String username=req.get("username").toString();
    String location=req.get("location").toString();
    String time=req.get("time").toString();
    String body=req.get("body").toString();
    HashMap<String,Set<String>> whitelist=new HashMap<String,Set<String>>();
    HashMap<String,Set<String>> backlist=new HashMap<String,Set<String>>();

    getKeyValue(req,"whitelist",whitelist);
    System.out.println(whitelist.get("carros"));

    getKeyValue(req,"backlist",backlist);
      System.out.println(backlist);


    messages.createMessage(username,location , time, body, whitelist, backlist);
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
