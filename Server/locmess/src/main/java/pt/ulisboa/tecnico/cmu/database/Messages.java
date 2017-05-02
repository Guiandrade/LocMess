package pt.ulisboa.tecnico.cmu.database;
import pt.ulisboa.tecnico.cmu.models.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.json.JSONObject;
import java.util.Map;


public class Messages {

  private int lastID=-1;
  private HashMap<String,Message> messages= new HashMap<String,Message>(); //nao vao para users com estes keypairs
  public void createMessage(String title,String username,String location , String initTime,String endTime, String body, HashMap<String,Set<String>> whitelist,  HashMap<String,Set<String>> blacklist) {
    lastID+=1;
    messages.put(lastID+"",new Message(lastID+"",title,username,location , initTime,endTime, body, whitelist,  blacklist));
  }
  public Set<JSONObject> getMessages(String location,HashMap<String,Set<String>> userKeys){

    Set<JSONObject> userMessages= new HashSet<JSONObject>();
    for(Map.Entry<String,Message> e : messages.entrySet()) {
      String key = e.getKey();
      Message m=messages.get(key);
      System.out.println(m.toJson());
      if(m.isTime()){
        if(m.isInLocation(location)){
          System.out.println("is in location");
          //falta o if do tempo TODO
          //falta ver para o caso de nao haver white nem blacklist TODO
          if(m.isInWhiteList(userKeys)){
            System.out.println("is in whitelist");
            if(!m.isInBackList(userKeys)){
              System.out.println("is not  in blacklist");

              userMessages.add(m.toJson());
            }
          }
        }
      }
    }
    return userMessages;

  }


  public Set<JSONObject> getUserMessages(String username){
    System.out.println("getUserMessages");
    Set<JSONObject> userMessages= new HashSet<JSONObject>();
    for(Map.Entry<String,Message> e : messages.entrySet()) {
      String key = e.getKey();
      Message m=messages.get(key);
      System.out.println(m.toJson());
      if(m.isUsername(username)){
        userMessages.add(m.toJson());
      }
    }
    return userMessages;

  }

  public boolean deleteMessage(String username,String id){
    if(messages.get(id).getUsername().equals(username)) {
      messages.remove(id);
      return true;
    }
    return false;

  }

}
