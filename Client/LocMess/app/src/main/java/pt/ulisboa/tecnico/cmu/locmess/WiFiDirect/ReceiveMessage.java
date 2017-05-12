package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmu.locmess.Activities.MessageActivity;
import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;
import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Models.TimeWindow;
import pt.ulisboa.tecnico.cmu.locmess.R;
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
                    JSONArray ids = new JSONArray();
                    try{
                        JSONObject json = new JSONObject(str);
                        if(json.has("Keys")){
                            JSONArray array = json.getJSONArray("Keys");
                            Set<JSONObject> setJson = new HashSet<JSONObject>();
                            for(int i=0; i<array.length();i++){
                                setJson.add(array.getJSONObject(i));
                            }
                            ids = compareKeys(setJson);
                            JSONObject jsonObject = new JSONObject();
                            Log.d("RecieveMessage", "SEND IDS");
                            jsonObject.put("ids",ids);
                            sock.getOutputStream().write((jsonObject.toString()+"\n").getBytes());
                        }
                        if(ids.length()!=0){
                            socketIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                            str = socketIn.readLine();
                            json = new JSONObject(str);
                            if(json.has("messages")){
                                JSONArray array = json.getJSONArray("messages");
                                Log.d("RecieveMessage", array.toString());
                                for(int i=0; i<array.length();i++){
                                    checkMessageCache(array.getJSONObject(i));
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

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

    public void checkMessageCache(JSONObject obj){
        try{
            SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
            String username = prefs.getString("username","");
            Set<String> messagesSet = prefs.getStringSet("messages" + username, null);
            if(messagesSet==null){
                messagesSet = new HashSet<String>();
            }
            for (String message: messagesSet) {
                if (new JSONObject(message).getString("id").equals(obj.getString("id"))){
                    return;
                }
            }
            messagesSet.add(obj.toString());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet("messages"  + username, messagesSet);
            editor.apply();
            launchNotification(obj);
        }
        catch (JSONException e){ }
    }

    public void launchNotification(JSONObject message){
        String username = new String();
        Message mssg = null;
        try{
            username = message.get("username").toString();

            LocationModel location = new LocationModel(message.get("location").toString(),(Coordinates) null);
            int initHour = Integer.parseInt(message.get("initTime").toString().split(":")[0]);
            int initMinute = Integer.parseInt(message.get("initTime").toString().split(":")[1].split("-")[0]);
            int initDay = Integer.parseInt(message.get("initTime").toString().split("/")[0].split("-")[1]);
            int initMonth = Integer.parseInt(message.get("initTime").toString().split("/")[1]);
            int initYear = Integer.parseInt(message.get("initTime").toString().split("/")[2]);
            int endHour = Integer.parseInt(message.get("endTime").toString().split(":")[0]);
            int endMinute = Integer.parseInt(message.get("endTime").toString().split(":")[1].split("-")[0]);
            int endDay = Integer.parseInt(message.get("endTime").toString().split("/")[0].split("-")[1]);
            int endMonth = Integer.parseInt(message.get("endTime").toString().split("/")[1]);
            int endYear = Integer.parseInt(message.get("endTime").toString().split("/")[2]);

            TimeWindow timeWindow = new TimeWindow(initHour,initMinute,initDay,initMonth,
                    initYear,endHour,endMinute,endDay,endMonth,endYear);

            String id = message.get("id").toString();
            String msg = message.get("body").toString();
            String owner = message.get("username").toString();
            String title = message.get("title").toString();

            mssg = new Message(id,title,msg,owner,location,null,null,timeWindow);
        }catch (Exception e){

        }

        final Intent intent = new Intent(NotificationService.getContext(), MessageActivity.class);
        intent.putExtra("Message",mssg);
        PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.getContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(NotificationService.getContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("New message received!")
                        .setContentText("From: "+ username)
                        .setContentIntent(pendingIntent).setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) NotificationService.getContext().getSystemService(NotificationService.getContext().NOTIFICATION_SERVICE);
        notificationManager.notify(new Random().nextInt(), mBuilder.build());
    }

    public JSONArray compareKeys(Set<JSONObject> keys){
        JSONArray ids = new JSONArray();
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
                    ids.put(msg.getString("id"));
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

    public JSONArray getMessagesByIds(Set<String> ids){
        SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
        Set<String> messagesSet = prefs.getStringSet("WifiMessages" + prefs.getString("username",""), null);
        JSONArray msgsToSend = new JSONArray();
        for(String msg : messagesSet){
            for(String id : ids){
                try{
                    if(new JSONObject(msg).getJSONObject("id").equals(id)){
                        msgsToSend.put(new JSONObject(msg));
                    }
                }catch (Exception e){

                }
            }
        }
        return msgsToSend;
    }
}
