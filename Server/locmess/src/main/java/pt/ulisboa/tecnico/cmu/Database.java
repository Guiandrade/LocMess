package pt.ulisboa.tecnico.cmu;
import pt.ulisboa.tecnico.cmu.database.*;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;


public class Database {
  private Profiles profiles;
  public Database(){
    profiles=new Profiles ();

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
    JSONObject res= new JSONObject();
    HashMap<String,Set<String>> keys=profiles.getUserKeys(username);
    if (keys !=null){
      for(Map.Entry<String,Set<String>> e : keys.entrySet()) {
         String key = e.getKey();
         Set<String> value = e.getValue();
         res.put(key,value);
     }
   }else{
     res.put("status","authentication failed");
   }
    return res;
  }

  public JSONObject addKey(JSONObject req){
    String username=req.get("username").toString();
    String key=req.get("key").toString();
    String value=req.get("value").toString();
    JSONObject res= new JSONObject();
    if (profiles.addKey(username, key,value)){

      res.put("status","ok");
      return res;
    }
    res.put("status","authentication failed");
    return res;
  }

  public JSONObject removeKey(JSONObject req ){
    String username=req.get("username").toString();
    String key=req.get("key").toString();
    String value=req.get("value").toString();
    JSONObject res= new JSONObject();
    if (profiles.removeKey(username, key,value)){

      res.put("status","ok");
      return res;
    }
    res.put("status","error");
    return res;
  }


}
