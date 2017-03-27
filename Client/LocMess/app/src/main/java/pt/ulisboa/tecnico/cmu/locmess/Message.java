package pt.ulisboa.tecnico.cmu.locmess;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by wazamaisers on 27-03-2017.
 */

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private String message;
    private String owner;
    private Location location;
    private Policy policy;
    private HashMap<String,String> keyPairs;
    private TimeWindow timeWindow;

    public Message(String title, String message, String owner, Location location, Policy policy,
                   HashMap<String, String> keyPairs, TimeWindow timeWindow) {
        this.title = title;
        this.message = message;
        this.owner = owner;
        this.location = location;
        this.policy = policy;
        this.keyPairs = keyPairs;
        this.timeWindow = timeWindow;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public HashMap<String, String> getKeyPairs() {
        return keyPairs;
    }

    public void setKeyPairs(HashMap<String, String> keyPairs) {
        this.keyPairs = keyPairs;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }
}
