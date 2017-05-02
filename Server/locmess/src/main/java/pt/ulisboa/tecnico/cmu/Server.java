package pt.ulisboa.tecnico.cmu;
import java.util.HashSet;
import java.util.Set;

import static spark.Spark.*;
import pt.ulisboa.tecnico.cmu.models.*;
public class Server{
  public static void main( String[] args ){
    GpsLocation gps=new GpsLocation( "LARANJEIRAS",  38.755997,  -9.176876, 800);
    boolean dist=gps.inRadius(38.755936,-9.171843);
    System.out.println(dist);
    Set<String>  setWhite = new HashSet<String>();
    setWhite.add("one");
    setWhite.add("three");
    setWhite.add("two");

    Set<String> setUser;
    setUser = new HashSet<String>();
    setUser.add("one");
    setUser.add("two");

    System.out.println(setWhite.containsAll(setUser));

    System.out.println( "Server address: http://localhost:" + args[0]);
    //secure("deploy/keystore.jks", "password", null, null);
    port(Integer.parseInt(args[0]));
    Routes routes= new Routes();
    routes.verification();
    routes.Post("/login","login");
    routes.Post("/signup","signup");
    routes.Put("/profile","addKey");
    routes.Get("/profile","getUserKeys");
    routes.Post("/removeKey","removeKey");
    routes.Get("/keys","getAllKeys");
    routes.Put("/locations","addLocation");
    routes.Get("/locations","getLocations");
    routes.Post("/deleteLocation","deleteLocation");
    routes.Post("/messages","createMessage");
    routes.Put("/messages","getMessages");
    routes.Get("/userMessages","getUserMessages");
    routes.Post("/deleteMessages","deleteMessage");


  }
}
