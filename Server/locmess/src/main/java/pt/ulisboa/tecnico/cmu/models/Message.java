package pt.ulisboa.tecnico.cmu.models;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import org.json.JSONObject;
import java.util.Calendar;

public class Message {
  private String id;
  private String username;
  private String title;
  private String location;
  private String initTime;
  private String endTime;
  private String body;
  private HashMap<String,Set<String>> whitelist= new HashMap<String,Set<String>>(); //vao so para users com estes keypairs
  private HashMap<String,Set<String>> blacklist= new HashMap<String,Set<String>>(); //nao vao para users com estes keypairs

  public Message (String id,String title,String username,String location , String initTime,String endTime, String body, HashMap<String,Set<String>> whitelist,  HashMap<String,Set<String>> blacklist) {
    this.id=id;
    this.title=title;
    this.username=username;
    this.location=location;
    this.initTime=initTime;
    this.endTime=endTime;
    this.body=body;
    this.whitelist=whitelist;
    this.blacklist=blacklist;
  }
  public boolean isUsername(String username){
    return this.username.equals(username);
  }
  public boolean isInLocation(String location){
    return this.location.equals(location);
  }
  public boolean isTime(String time){
    return this.endTime.equals(time);
  }
  public String getUsername(){
    return this.username;
  }
  public String getBody(){
    return this.body;
  }
  public JSONObject toJson(){
    JSONObject message= new JSONObject();
    message.put("id",this.id);
    message.put("title",this.title);
    message.put("username",this.username);
    message.put("location",this.location);
    message.put("initTime",this.initTime);
    message.put("endTime",this.endTime);
    message.put("body",this.body);
    return message;
  }

  public boolean isInWhiteList(HashMap<String,Set<String>> userKeys){
    System.out.println(this.whitelist);
    if(this.whitelist.size()==0) return true;
    if(userKeys.size()==0) return false;
    for(Map.Entry<String,Set<String>> e : this.whitelist.entrySet()) {
       String key = e.getKey();
       if (userKeys.containsKey(key)){
         Set<String> value = e.getValue();
         if(!userKeys.get(key).containsAll(value)){
           return false;
         }
       }else{
         return false;
       }
     }
    return true;
  }

  public boolean isInBackList(HashMap<String,Set<String>> userKeys){
    if(this.blacklist.size()==0) return false;
    for(Map.Entry<String,Set<String>> e : userKeys.entrySet()) {
       String key = e.getKey();
       if (blacklist.containsKey(key)){
         Set<String> value = e.getValue();
         for (String s : value) {
           if(blacklist.get(key).contains(s)){
             return true;
           }
         }
       }
     }
    return false;
  }

  public boolean isTime(){
    int initHour = Integer.parseInt(this.initTime.split(":")[0]);

    int initMinute = Integer.parseInt(this.initTime.split(":")[1].split("-")[0]);
    int initDay = Integer.parseInt(this.initTime.split("/")[0].split("-")[1]);
    int initMonth = Integer.parseInt(this.initTime.split("/")[1]);
    int initYear = Integer.parseInt(this.initTime.split("/")[2]);
    int endHour = Integer.parseInt(this.endTime.split(":")[0]);
    int endMinute = Integer.parseInt(this.endTime.split(":")[1].split("-")[0]);
    int endDay = Integer.parseInt(this.endTime.split("/")[0].split("-")[1]);
    int endMonth = Integer.parseInt(this.endTime.split("/")[1]);
    int endYear = Integer.parseInt(this.endTime.split("/")[2]);
    Calendar init =  Calendar.getInstance();
    init.set(initYear,initMonth-1,initDay,initHour-1,initMinute);

    Calendar end =  Calendar.getInstance();
    end.set(endYear,endMonth-1,endDay,endHour-1,endMinute);

    Calendar now = Calendar.getInstance();
    if(now.after(init) && now.before(end)){
      return true;
    }
    return false;



  }





}
