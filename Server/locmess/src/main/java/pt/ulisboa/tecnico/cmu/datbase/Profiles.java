package pt.ulisboa.tecnico.cmu.database;
import pt.ulisboa.tecnico.cmu.models.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Profiles {
  private HashMap<String,User> users= new HashMap<String,User>();
  private HashMap<String,Set<String>> allKeys= new HashMap<String,Set<String>>();
  
  public boolean login(String username,String password){
    if (users.containsKey(username)){
      if(users.get(username).equals(password)) {
        return true;
      }
    }
    return false;
  }

  public boolean signup(String username,String password){
    if (users.containsKey(username)){
      return false;
    }
    User user=new User(username,password);
    users.put(username, user);
    return true;
  }

  public HashMap getAllKeys(){
    return allKeys;
  }

  public HashMap getUserKeys(String username){
    if (users.containsKey(username)){
        return users.get(username).getKeys();
    }
    return null;
  }

  public boolean addKey(String username, String key, String value){
    if (users.containsKey(username)){
        users.get(username).addKey(key,value);
        if(allKeys.containsKey(key)){
          allKeys.get(key).add(value);
        }else{
          Set<String> val = new HashSet<String>();
          val.add(value);
          allKeys.put(key,val);
        }
        return true;
    }
    return false;
  }

  public boolean removeKey(String username,String key, String value ){
    if (users.containsKey(username)){
      return users.get(username).remove(key,value);
    }
    return false;
  }
}
