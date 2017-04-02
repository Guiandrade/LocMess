package pt.ulisboa.tecnico.cmu;

import static spark.Spark.*;
import pt.ulisboa.tecnico.cmu.models.*;
public class Server{
  public static void main( String[] args ){
    GpsLocation gps=new GpsLocation( "LARANJEIRAS",  38.755997,  -9.176876, 800);
    boolean dist=gps.inRadius(38.755936,-9.171843);
    System.out.println(dist);
    System.out.println( "Hello World!" );
    System.out.println( "Server address: http://localhost:" + args[0]);
    secure("deploy/keystore.jks", "password", null, null);
    port(Integer.parseInt(args[0]));
    //curl -sS -k 'https://localhost:8080/login?username=3&month=1'
    Routes routes= new Routes();
    routes.verification();
    routes.Post("/login","login");
    routes.Post("/signup","signup");
    routes.Put("/profile","addKey");
    routes.Get("/profile","getUserKeys");
    routes.Delete("/profile","removeKey");
    routes.Get("/keys","getAllKeys");
    routes.Put("/locations","addLocation");
    routes.Get("/locations","getLocations");
    /*


    ;
    routes.Get("/locations");
    routes.Delete("/locations");
    routes.Post("/messages");
    routes.Get("/messages");
    routes.Delete("/messages");*/

  }
}
