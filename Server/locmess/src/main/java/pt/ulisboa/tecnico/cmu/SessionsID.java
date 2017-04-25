package pt.ulisboa.tecnico.cmu;
import java.util.HashMap;
import java.util.Set;
import java.util.Base64;



public class SessionsID {

  private HashMap<String,String> sessions= new HashMap<String,String>();

  public String addSession (String username, String password) {
    String text=username+":"+password;
    String token=new String(Base64.getEncoder().encode(text.getBytes()));
    sessions.put(token,username);
    System.out.println(token);
    return token;
  }
  public boolean exists (String token){
    return sessions.containsKey(token);
  }

  public String getUsername (String token) {
    return new String(Base64.getDecoder().decode(token.getBytes())).split(":")[0];
  }
  public void remove (String token) {
    sessions.remove(token);
  }


}
