package pt.ulisboa.tecnico.cmu;
import static spark.Spark.*;
import org.json.JSONObject;
import java.io.StringWriter;
import io.jsonwebtoken.*;


public class Server
{
  public static void main( String[] args )
  {
    System.out.println( "Hello World!" );
    System.out.println( "Server address: http://localhost:" + args[0]);
    port(Integer.parseInt(args[0]));
    //Routes routes = new Routes();
    post("/init", (request, response) -> {
      //curl -X POST -s -D - -d '{"nounce":"12345","publicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlqs1p0CgN0AxoY2s/99yaohsDbQViDVXbujBbTPeS0rOYo0hBlHPKEvcdlY8ztuX8KFnQRt4HzHXhsSEBZ86DJTNt/MMoSM9FWM5tCRTG9YOH8LYjC4RU4dlvI8uqMNAWOTCzyx+b84TSZDNLoBWA0GgsefRpMFBMmNR2PmXe7OZQHroJd1toPfJ/rnmArKRhQbUUA36qIVgr1rB31kM4igl0Vuy5urxmqVcQ1fEQb4sYh3ssFgeayD4vBNW48RqWMWyC1+ZxLTMditGUsqzhh6keSoB+AZiDnMKl/lT6J9jWkUL5bjp1fDSGM4gml0l1HkCM+wXm+azNtC4+1OV+QIDAQAB"}' http://localhost:8080/register

      return "json";

    });
  }
}
