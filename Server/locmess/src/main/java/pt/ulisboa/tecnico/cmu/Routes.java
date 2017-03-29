package pt.ulisboa.tecnico.cmu;
import static spark.Spark.*;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.Set;


public class Routes
{
  private Database dataObj =new Database();
  private Class dataClass=dataObj.getClass();
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
        reqObj.put(s,request.queryParams(s));
      }
      res = (JSONObject) method.invoke(dataObj, new Object[] {reqObj});
      return res;

    } catch(Exception exception_name) {
        res = new JSONObject();
        res.put("status","error");
        return res;
    }
  }
}
