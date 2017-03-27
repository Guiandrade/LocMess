package pt.ulisboa.tecnico.cmu.locmess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wazamaisers on 27-03-2017.
 */

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private HashMap<String,String> keyPairs;
    private ArrayList<Message> createdMessages;
    private ArrayList<Message> recievedMessages;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashMap<String, String> getKeyPairs() {
        return keyPairs;
    }

    public void setKeyPairs(HashMap<String, String> keyPairs) {
        this.keyPairs = keyPairs;
    }

    public ArrayList<Message> getCreatedMessages() {
        return createdMessages;
    }

    public void setCreatedMessages(ArrayList<Message> createdMessages) {
        this.createdMessages = createdMessages;
    }

    public ArrayList<Message> getRecievedMessages() {
        return recievedMessages;
    }

    public void setRecievedMessages(ArrayList<Message> recievedMessages) {
        this.recievedMessages = recievedMessages;
    }
}
