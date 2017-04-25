package pt.ulisboa.tecnico.cmu;
import static spark.Spark.*;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.Set;


public class Routes
{
  private Database dataObj =new Database();
  private Class dataClass=dataObj.getClass();
  private SessionsID sessions=new SessionsID();
  public void verification(){
    before((request, response) -> {

      System.out.println("cons");
      System.out.println(request.body());
      boolean authenticated=true;
      if (!(request.pathInfo().equals("/login")||request.pathInfo().equals("/signup"))){
        try{
          String token = request.headers("Authorization").split("Basic ")[1];
          System.out.println("token: "+token);
          System.out.println(sessions.exists(token));
          if(!sessions.exists(token)){
            authenticated=false;
          }
        }catch(Exception e){
          authenticated=false;
        }
      }
    if (!authenticated) {

      halt(401, "You are not welcome here");
    }
});
  }
  public void Post(String endpoint,String func){
    post(endpoint, (request, response) -> {
      System.out.println("POST "+endpoint);
      System.out.println(request.queryParams() );
      return process(request,func);

    });
  }

  public void Get(String endpoint,String func){
    get(endpoint, (request, response) -> {
      System.out.println("GET "+endpoint);
      System.out.println(request.queryParams() );
      return process(request,func);
    });
  }
  public void Put(String endpoint,String func){
    put(endpoint, (request, response) -> {
      System.out.println("PUT "+endpoint);
      System.out.println(request.queryParams() );
      return process(request,func);

    });
  }
  public void Delete(String endpoint,String func){
    delete(endpoint, (request, response) -> {
      System.out.println("DELETE "+endpoint);
      System.out.println(request.queryParams() );
      return process(request,func);

    });
  }

  public JSONObject process(spark.Request request,String func){
    JSONObject res;
    try {
      Method method = dataClass.getMethod(func, new Class[] { JSONObject.class });
      JSONObject reqObj = new JSONObject();
      if(request.body().length()==0){
        for (String s : request.queryParams()) {
          System.out.println(s + " : "+ request.queryParams(s));

          reqObj.put(s,request.queryParams(s));
        }
      }else{
        reqObj = new JSONObject(request.body().toString());
      }

      if (!(request.pathInfo().equals("/login")||request.pathInfo().equals("/signup"))){

        String token = request.headers("Authorization").split("Basic ")[1];
        String username=sessions.getUsername(token);
        reqObj.put("username",username);
      }
      res = (JSONObject) method.invoke(dataObj, new Object[] {reqObj});
      if (request.pathInfo().equals("/login")||request.pathInfo().equals("/signup")){
          res.put("token",sessions.addSession(reqObj.getString("username"),reqObj.getString("password")));
      }
      return res;

    } catch(Exception exception_name) {
        System.out.println("Routes");
        exception_name.printStackTrace();
        res = new JSONObject();
        res.put("status","error");
        return res;
    }
  }
}
