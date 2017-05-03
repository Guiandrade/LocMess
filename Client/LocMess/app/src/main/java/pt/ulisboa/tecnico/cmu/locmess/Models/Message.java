package pt.ulisboa.tecnico.cmu.locmess.Models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by wazamaisers on 27-03-2017.
 */

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String message;
    private String owner;
    private Location location;
    private HashMap<String, Set<String>> whitelistKeyPairs;
    private HashMap<String, Set<String>> blacklistKeyPairs;
    private TimeWindow timeWindow;

    public Message(String title, String message, String owner, Location location,
                   HashMap<String, Set<String>> whitelistKeyPairs,
                   HashMap<String, Set<String>> blacklistKeyPairs, TimeWindow timeWindow) {
        this.title = title;
        this.message = message;
        this.owner = owner;
        this.location = location;
        this.whitelistKeyPairs = whitelistKeyPairs;
        this.blacklistKeyPairs = blacklistKeyPairs;
        this.timeWindow = timeWindow;
    }

    public Message(String id, String title, String message, String owner, Location location,
                   HashMap<String, Set<String>> whitelistKeyPairs,
                   HashMap<String, Set<String>> blacklistKeyPairs, TimeWindow timeWindow) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.owner = owner;
        this.location = location;
        this.whitelistKeyPairs = whitelistKeyPairs;
        this.blacklistKeyPairs = blacklistKeyPairs;
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

    public HashMap<String, Set<String>> getWhitelistKeyPairs() {
        return whitelistKeyPairs;
    }

    public void setWhitelistKeyPairs(HashMap<String, Set<String>> whitelistKeyPairs) {
        this.whitelistKeyPairs = whitelistKeyPairs;
    }

    public HashMap<String, Set<String>> getBlacklistKeyPairs() {
        return blacklistKeyPairs;
    }

    public void setBlacklistKeyPairs(HashMap<String, Set<String>> blacklistKeyPairs) {
        this.blacklistKeyPairs = blacklistKeyPairs;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }

    public String getId() {
        return id;
    }
}
