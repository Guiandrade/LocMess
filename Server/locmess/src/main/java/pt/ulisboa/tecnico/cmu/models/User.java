package pt.ulisboa.tecnico.cmu.models;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class User {
  private String username;
  private String password;
  private HashMap<String,Set<String>> keys= new HashMap<String,Set<String>>(); //key-value
  
  public User (String username , String password ) {
    this.username=username;
    this.password=password;
  }
  public boolean addKey(String key, String value){
    if (keys.containsKey(key)){
      keys.get(key).add(value);
      return true;
    }else{
      Set<String> val = new HashSet<String>();
      val.add(value);
      keys.put(key,val);
      return true;
    }
  }
  public boolean removeKey(String key, String value){
    if (keys.containsKey(key)){
      return keys.get(key).remove(value);
    }else{
      return false;
    }
  }
  public HashMap getKeys(){
    return keys;
  }
  public String getPassword(){
    return password;
  }

}
