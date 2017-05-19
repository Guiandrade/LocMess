package pt.ulisboa.tecnico.cmu.locmess.Models;

import java.io.Serializable;

import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;



public class LocationModel implements Serializable{

    private static final long serialVersionUID = 1L;

    private String name;
    private Coordinates coordinates;
    private String ssid;

    public LocationModel(String name, Coordinates coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public LocationModel(String ssid){
        this.ssid = ssid;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String getSSID() {
        return ssid;
    }

}
