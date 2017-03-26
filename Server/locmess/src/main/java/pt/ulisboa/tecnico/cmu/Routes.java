package pt.ulisboa.tecnico.cmu;
import static spark.Spark.*;
import org.json.JSONObject;

public class Routes
{
  public static void Post(String endpoint){
    post(endpoint, (request, response) -> {
      System.out.println("POST "+endpoint);
      System.out.println(request.queryParams() );
      return "json";

    });
  }

  public static void Get(String endpoint){
    get(endpoint, (request, response) -> {
      System.out.println("GET "+endpoint);
      System.out.println(request.queryParams() );


      return "json";

    });
  }
  public static void Put(String endpoint){
    put(endpoint, (request, response) -> {
      System.out.println("PUT "+endpoint);
      System.out.println(request.queryParams() );
      return "json";

    });
  }
  public static void Delete(String endpoint){
    delete(endpoint, (request, response) -> {
      System.out.println("DELETE "+endpoint);
      System.out.println(request.queryParams() );
      return "json";

    });
  }
}
