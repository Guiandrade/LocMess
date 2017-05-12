package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmu.locmess.Services.NotificationService;

/**
 * Created by guiandrade on 10-05-2017.
 */

public class ReceiveMessage extends AsyncTask<String, String, Void> {

    private SimWifiP2pSocketServer mSrvSocket;
    private int server_port;

    public ReceiveMessage(int port){
        this.server_port = port;
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.d("ReceiveMessage","doInBackground");

        try {
            Log.d("ReceiveMessage","Before Socket creation");
            mSrvSocket = new SimWifiP2pSocketServer(server_port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!Thread.currentThread().isInterrupted()) {
            Log.d("ReceiveMessage","inside While");

            try {
                SimWifiP2pSocket sock = mSrvSocket.accept();
                Log.d("ReceiveMessage","Accepted Socket");
                try {
                    // Read data
                    BufferedReader socketIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    String str = socketIn.readLine();
                    Log.d("ReceiveMessage",str);
                    try{
                        JSONObject json = new JSONObject(str);
                        if(json.has("Keys")){
                            JSONArray array = json.getJSONArray("Keys");
                            Set<JSONObject> setJson = new HashSet<JSONObject>();
                            for(int i=0; i<array.length();i++){
                                setJson.add(array.getJSONObject(i));
                            }
                            Set<String> ids = compareKeys(setJson);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("ids",ids);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    // Show data
                    publishProgress(str);
                    sock.getOutputStream().write(("\n").getBytes());
                } catch (IOException e) {
                    Log.d("Error: Reading socket", e.getMessage());
                } finally {
                    sock.close();
                }
            } catch (IOException e) {
                Log.d("Error: Socket", e.getMessage());
                break;
            }
        }
        return null;
    }

    public Set<String> compareKeys(Set<JSONObject> keys){
        Set<String> ids = new HashSet<String>();
        for(JSONObject msg : keys){
            try{
                Set<String> valuesSet = new HashSet<String>();

                JSONObject whitelist = msg.getJSONObject("whitelist");
                Iterator<String> whitelistKeys = whitelist.keys();
                HashMap<String,Set<String>> whitelistHash = new HashMap<String,Set<String>>();
                while(whitelistKeys.hasNext()) {
                    String key = (String) whitelistKeys.next();
                    JSONArray values = whitelist.getJSONArray(key);
                    for(int i=0; i<values.length(); i++) {
                        String value = values.getString(i);
                        if (whitelistHash.containsKey(key)) {
                            whitelistHash.get(key).add(value);
                        } else {
                            Set<String> val = new HashSet<String>();
                            val.add(value);
                            whitelistHash.put(key, val);
                        }
                    }
                }

                //TESTE
                System.out.println("WHITELIST");
                for(Map.Entry<String,Set<String>> set : whitelistHash.entrySet()){
                    for(String str : set.getValue()){
                        System.out.println(set.getKey() + " - " + str);
                    }
                }

                JSONObject blacklist = msg.getJSONObject("blacklist");
                Iterator<String> blacklistKeys = blacklist.keys();
                HashMap<String,Set<String>> blacklistHash = new HashMap<String,Set<String>>();
                while(blacklistKeys.hasNext()) {
                    String key = (String) blacklistKeys.next();
                    JSONArray values = blacklist.getJSONArray(key);
                    for (int i = 0; i < values.length(); i++) {
                        valuesSet.add(values.getString(i));
                    }
                    blacklistHash.put(key, valuesSet);
                }

                //TESTE
                System.out.println("BLACKLIST");
                for(Map.Entry<String,Set<String>> set : blacklistHash.entrySet()){
                    for(String str : set.getValue()){
                        System.out.println(set.getKey() + " - " + str);
                    }
                }

                SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
                Set<String> userKeySet = prefs.getStringSet("Keys", null);
                HashMap<String,Set<String>> userKeys = new HashMap<String,Set<String>>();
                if(!(userKeySet==null)){
                    for(String pair : userKeySet){
                        String key = pair.split(" = ")[0];
                        String value = pair.split(" = ")[1];
                        if(userKeys.containsKey(key)){
                            userKeys.get(key).add(value);
                        }
                        else{
                            Set<String> val = new HashSet<String>();
                            val.add(value);
                            userKeys.put(key,val);
                        }
                    }
                }

                //TESTE
                System.out.println("USERKEYS");
                for(Map.Entry<String,Set<String>> set : userKeys.entrySet()){
                    for(String str : set.getValue()){
                        System.out.println(set.getKey() + " - " + str);
                    }
                }

                if(isInWhiteList(userKeys,whitelistHash) && !isInBackList(userKeys,blacklistHash)){
                    ids.add(msg.getString("id"));
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        System.out.println("IDSSSS " + ids);
        return ids;
    }

    public boolean isInWhiteList(HashMap<String,Set<String>> userKeys, HashMap<String,Set<String>> whitelist){
        if(whitelist.size()==0) return true;
        if(userKeys.size()==0) return false;
        for(Map.Entry<String,Set<String>> e : whitelist.entrySet()) {
            String key = e.getKey();
            if (userKeys.containsKey(key)){
                Set<String> value = e.getValue();
                if(!userKeys.get(key).containsAll(value)){
                    return false;
                }
            }else{
                return false;
            }
        }
        return true;
    }

    public boolean isInBackList(HashMap<String,Set<String>> userKeys, HashMap<String,Set<String>> blacklist){
        if(blacklist.size()==0) return false;
        for(Map.Entry<String,Set<String>> e : userKeys.entrySet()) {
            String key = e.getKey();
            if (blacklist.containsKey(key)){
                Set<String> value = e.getValue();
                for (String s : value) {
                    if(blacklist.get(key).contains(s)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
