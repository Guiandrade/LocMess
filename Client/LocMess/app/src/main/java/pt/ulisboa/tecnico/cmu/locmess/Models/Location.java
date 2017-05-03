package pt.ulisboa.tecnico.cmu.locmess.Models;

import java.io.Serializable;

import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;

/**
 * Created by wazamaisers on 25-03-2017.
 */

public class Location implements Serializable{

    private static final long serialVersionUID = 1L;

    private String name;
    private Coordinates coordinates;
    private String ssid;

    public Location(String name, Coordinates coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public Location(String ssid){
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
