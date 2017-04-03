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

    Set<JSONObject> userMessages= new HashSet<JSONObject>();
    for (int i = 0; i < lastID+1; i++) {
      Message m=messages.get(i+"");
      if(m.isInLocation(location)){
        //falta o if do tempo TODO
        //falta ver para o caso de nao haver white nem backlist TODO
        if(m.isInWhiteList(userKeys)){
          if(!m.isInBackList(userKeys)){
            userMessages.add(m.toJson());
          }
        }
      }
    }
    return userMessages;

  }

  public void deleteMessage(String id){
    messages.remove(id);
  }

}