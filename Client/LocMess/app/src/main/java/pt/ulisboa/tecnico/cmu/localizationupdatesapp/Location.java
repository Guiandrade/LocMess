package pt.ulisboa.tecnico.cmu.localizationupdatesapp;

import java.io.Serializable;

/**
 * Created by wazamaisers on 25-03-2017.
 */

public class Location implements Serializable{

    private static final long serialVersionUID = 1L;

    private String name;
    private Coordinates coordinates;

    public Location(String name, Coordinates coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }
}
