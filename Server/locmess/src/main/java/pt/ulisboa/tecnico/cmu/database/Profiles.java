package pt.ulisboa.tecnico.cmu.database;
import pt.ulisboa.tecnico.cmu.models.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Profiles {
  private HashMap<String,User> users= new HashMap<String,User>();
  private Set<String> allKeys = new HashSet<String>();
  public boolean login(String username,String password){
    System.out.println(users.size());
    if (users.containsKey(username)){
      System.out.println( "username existe");
      if(users.get(username).getPassword().equals(password)) {
        System.out.println("password passou");
        return true;
      }
      System.out.println("password falhou");
    }
    System.out.println("user falhou");
    return false;
  }

  public boolean signup(String username,String password){
    System.out.println("if");
    if (users.containsKey(username)){
      System.out.println( "username existe");
      return false;
    }
    System.out.println( "username nao existe");
    User user=new User(username,password);
    users.put(username, user);
    return true;
  }

  public Set getAllKeys(){
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
        allKeys.add(key);
        return true;
    }
    return false;
  }

  public boolean removeKey(String username,String key, String value ){
    if (users.containsKey(username)){
      return users.get(username).removeKey(key,value);
    }
    return false;
  }
}
