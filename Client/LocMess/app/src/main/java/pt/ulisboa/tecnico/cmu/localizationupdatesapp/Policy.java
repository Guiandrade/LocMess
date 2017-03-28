package pt.ulisboa.tecnico.cmu.localizationupdatesapp;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by wazamaisers on 27-03-2017.
 */

public class Policy implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private HashMap<String,String> keyPairs;
    private TimeWindow timeWindow;

    public Policy(String type, HashMap<String, String> keyPairs, TimeWindow timeWindow) {
        this.type = type;
        this.keyPairs = keyPairs;
        this.timeWindow = timeWindow;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setTiemeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }
}
