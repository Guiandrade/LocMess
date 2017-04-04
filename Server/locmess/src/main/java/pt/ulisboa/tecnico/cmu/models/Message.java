package pt.ulisboa.tecnico.cmu.models;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import org.json.JSONObject;


public class Message {
  private String id;
  private String username;
  private String location;
  private String time;
  private String body;
  private HashMap<String,Set<String>> whitelist= new HashMap<String,Set<String>>(); //vao so para users com estes keypairs
  private HashMap<String,Set<String>> backlist= new HashMap<String,Set<String>>(); //nao vao para users com estes keypairs

  public Message (String id,String username,String location , String time, String body, HashMap<String,Set<String>> whitelist,  HashMap<String,Set<String>> backlist) {
    this.id=id;
    this.username=username;
    this.location=location;
    this.time=time;
    this.body=body;
    this.whitelist=whitelist;
    this.backlist=backlist;
  }
  public boolean isInLocation(String location){
    return this.location.equals(location);
  }
  public boolean isTime(String time){
    return this.time.equals(time);
  }

  public String getBody(){
    return this.body;
  }
  public JSONObject toJson(){
    JSONObject message= new JSONObject();
    message.put("id",this.id);
    message.put("username",this.username);
    message.put("location",this.location);
    message.put("time",this.time);
    message.put("body",this.body);
    return message;
  }

  public boolean isInWhiteList(HashMap<String,Set<String>> userKeys){
    System.out.println(this.whitelist);
    if(this.whitelist==null) return true;
    for(Map.Entry<String,Set<String>> e : userKeys.entrySet()) {
       String key = e.getKey();
       if (whitelist.containsKey(key)){
         Set<String> value = e.getValue();
         for (String s : value) {
           if(whitelist.get(key).contains(s)){
             return true;
           }
         }
       }
     }
    return false;
  }

  public boolean isInBackList(HashMap<String,Set<String>> userKeys){
    if(this.backlist==null) return false;
    for(Map.Entry<String,Set<String>> e : userKeys.entrySet()) {
       String key = e.getKey();
       if (backlist.containsKey(key)){
         Set<String> value = e.getValue();
         for (String s : value) {
           if(backlist.get(key).contains(s)){
             return true;
           }
         }
       }
     }
    return false;
  }





}
