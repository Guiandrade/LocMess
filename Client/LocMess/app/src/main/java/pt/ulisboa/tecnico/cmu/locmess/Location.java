package pt.ulisboa.tecnico.cmu.locmess;

import java.io.Serializable;

/**
 * Created by wazamaisers on 25-03-2017.
 */

public class Location implements Serializable{

    private static final long serialVersionUID = 1L;

    private String name;
    private Coordinates coordinates;
    private String ssid;
    private String mac;

    public Location(String name, Coordinates coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public Location(String ssid, String mac){
        this.ssid = ssid;
        this.mac = mac;
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

    public String getMac() {
        return mac;
    }
}
