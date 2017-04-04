package pt.ulisboa.tecnico.cmu;
import static spark.Spark.*;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.Set;


public class Routes
{
  private Database dataObj =new Database();
  private Class dataClass=dataObj.getClass();
  public void verification(){
    before((request, response) -> {
      boolean authenticated=true;
      if (!(request.pathInfo().equals("/login")||request.pathInfo().equals("/signup"))){
        if((String)request.session().attribute("username")==null){
          authenticated=false;
        }
      }
    System.out.println("before");
    System.out.println((String)request.session().attribute("username") );
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
      for (String s : request.queryParams()) {
        System.out.println(s + " : "+ request.queryParams(s));

        reqObj.put(s,request.queryParams(s));
      }
      if (!(request.pathInfo().equals("/login")||request.pathInfo().equals("/signup"))){
        reqObj.put("username",(String)request.session().attribute("username"));
      }
      res = (JSONObject) method.invoke(dataObj, new Object[] {reqObj});
      if (request.pathInfo().equals("/login")||request.pathInfo().equals("/signup")){
        if(res.get("status").equals("ok")){
          request.session().attribute("username", reqObj.get("username").toString());
        }
      }
      return res;

    } catch(Exception exception_name) {
        System.out.println(exception_name);
        res = new JSONObject();
        res.put("status","error");
        return res;
    }
  }
}
