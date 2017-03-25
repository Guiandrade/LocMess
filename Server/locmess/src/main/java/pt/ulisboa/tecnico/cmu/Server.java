package pt.ulisboa.tecnico.cmu;
import static spark.Spark.*;


public class Server
{
  public static void main( String[] args )
  {
    System.out.println( "Hello World!" );
    System.out.println( "Server address: http://localhost:" + args[0]);
    port(Integer.parseInt(args[0]));
    Routes.Get("/login");
    Routes.Get("/signup");
    Routes.Post("/signup");
    Routes.Get("/keypairs");
    Routes.Get("/profile");
    Routes.Put("/profile");
    Routes.Post("/locations");
    Routes.Get("/locations");
    Routes.Post("/messages");
    Routes.Get("/messages");
    Routes.Delete("/messages");

  }
}
