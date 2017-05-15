package pt.ulisboa.tecnico.cmu.models;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class User {
  private String username;
  private String password;
  private String mules;
  private HashMap<String,Set<String>> keys= new HashMap<String,Set<String>>(); //key-value

  public User (String username , String password, String mules ) {
    this.username=username;
    this.password=password;
    this.mules=mules;
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
      keys.get(key).remove(value);
      if(keys.get(key).isEmpty()){
        keys.remove(key);
      }
      return true;
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
  public String getMules(){
    return this.mules;
  }
  public void setMules(String mule){
    this.mules=mule;
  }

}
