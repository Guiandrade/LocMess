package pt.ulisboa.tecnico.cmu;

import static spark.Spark.*;
public class Server{
  public static void main( String[] args ){

    System.out.println( "Hello World!" );
    System.out.println( "Server address: http://localhost:" + args[0]);
    secure("deploy/keystore.jks", "password", null, null);
    port(Integer.parseInt(args[0]));
    //curl -sS -k 'https://localhost:8080/login?username=3&month=1'
    Routes routes= new Routes();

    routes.Post("/login","login");
    routes.Post("/signup","signup");
    routes.Put("/profile","addKey");
    routes.Get("/profile","getUserKeys");
    routes.Delete("/profile","removeKey");
    routes.Get("/keypairs","getAllKeys");
    /*


    routes.Post("/locations");
    routes.Get("/locations");
    routes.Delete("/locations");
    routes.Post("/messages");
    routes.Get("/messages");
    routes.Delete("/messages");*/

  }
}
