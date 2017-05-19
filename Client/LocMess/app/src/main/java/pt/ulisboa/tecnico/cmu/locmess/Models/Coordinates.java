package pt.ulisboa.tecnico.cmu.locmess.Models;

import java.io.Serializable;



public class Coordinates implements Serializable{

    private static final long serialVersionUID = 1L;

    private String latitude;
    private String longitude;
    private String radius;

    public Coordinates(String latitude, String longitude, String radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public Coordinates(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }
}
