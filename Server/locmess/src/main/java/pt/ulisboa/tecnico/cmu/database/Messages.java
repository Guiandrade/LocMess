package pt.ulisboa.tecnico.cmu.database;
import pt.ulisboa.tecnico.cmu.models.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.json.JSONObject;

public class Messages {

  private int lastID=-1;
  private HashMap<String,Message> messages= new HashMap<String,Message>(); //nao vao para users com estes keypairs
  public void createMessage(String username,String location , String time, String body, HashMap<String,Set<String>> whitelist,  HashMap<String,Set<String>> backlist){
    lastID+=1;
    messages.put(lastID+"",new Message(lastID+"",username,location , time, body, whitelist,  backlist));
  }
  public Set<JSONObject> getMessages(String location,HashMap<String,Set<String>> userKeys){
    System.out.println("getMessages");
    Set<JSONObject> userMessages= new HashSet<JSONObject>();
    for (int i = 0; i < lastID+1; i++) {
      Message m=messages.get(i+"");
      System.out.println(m.toJson());
      if(m.isInLocation(location)){
        System.out.println("is in location");
        //falta o if do tempo TODO
        //falta ver para o caso de nao haver white nem backlist TODO
        if(m.isInWhiteList(userKeys)){
          System.out.println("is in whitelist");
          if(!m.isInBackList(userKeys)){
            System.out.println("is not  in backlist");

            userMessages.add(m.toJson());
          }
        }
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
