package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
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

public class SendMessage extends AsyncTask<String, String, Void> {

    private String ipToSendMsg;
    private int socketPort;

    public SendMessage(String ip,int port){
        this.socketPort = port;
        this.ipToSendMsg = ip;
    }

    @Override
    protected Void doInBackground(String... data) {
        Log.d("SendMessage","doInBackground");
        SimWifiP2pSocket socket;

        try{
            Log.d("SendMessage","Sending to device with ip "+ipToSendMsg);
            socket = new SimWifiP2pSocket(ipToSendMsg, socketPort);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        try {
            Log.d("Send Message", "A Mensagem " + data[0]);
            OutputStream out = socket.getOutputStream();
            out.write(data[0].getBytes());
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = socketIn.readLine();
            Log.d("Send Message", "A resposta " + response);
            JSONObject json = new JSONObject(response);
            //Log.d("SendMessage",response);
            if(json.has("ids")){
                //Log.d("SendMessage",json.toString());
                JSONArray array = json.getJSONArray("ids");

                    Set<String> ids = new HashSet<String>();
                    for(int i=0; i<array.length();i++){
                        ids.add(array.getString(i));
                    }
                    JSONObject resp = new JSONObject();
                    //Log.d("SendMessage", "SENDOU CARALHO");
                    ArrayList<String> locations = Wifi.localizacao;
                    JSONArray msgsToSend = new JSONArray();
                    SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
                    Set<String> messagesSet = prefs.getStringSet("WifiMessages" + prefs.getString("username",""), null);
                    Log.d("Send Message", "messages set " +messagesSet);
                    if(messagesSet==null){
                        messagesSet=new HashSet<>();
                    }
                    for(String msg : messagesSet){
                        try{
                            Log.d("Send Message", "Locations " + new JSONObject(msg).getString("location"));
                            Log.d("Send Message", "Verif " + locations.contains(new JSONObject(msg).getString("location")));
                            if(!(locations.contains(new JSONObject(msg).getString("location")))){
                                msgsToSend.put(new JSONObject(msg));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                }
                resp.put("messagesDirect",getMessagesByIds(ids));
                resp.put("messageMule",msgsToSend);
                Log.d("Send Message", "Message Mule " + getMessagesByIds(ids));
                Log.d("Send Message", "Message Mule " + msgsToSend);
                OutputStream out1 = socket.getOutputStream();
                //Log.d("SendMessage", resp.toString());
                out1.write((resp + "\n").getBytes());


            }
            /*JSONArray ids = new JSONArray();
            if(json.has("Keys")){
                Log.d("Send Message", "Verificação de keys " + json.toString());
                JSONArray array = json.getJSONArray("Keys");
                Set<JSONObject> setJson = new HashSet<JSONObject>();
                for(int i=0; i<array.length();i++){
                    setJson.add(array.getJSONObject(i));
                }
                ids = compareKeys(setJson);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ids",ids);
                Log.d("Send Message", "Ids " + jsonObject.toString());
                socket.getOutputStream().write((jsonObject.toString()+"\n").getBytes());
            }
            if(ids.length()!=0){
                Log.d("Send Message", "Ids diferentes de zero");
                socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = socketIn.readLine();
                Log.d("Send Message", "As mensagens recebidas " + str);
                json = new JSONObject(str);
                if(json.has("messages")){
                    JSONArray array = json.getJSONArray("messages");
                    //Log.d("SendMessage", array.toString());
                    for(int i=0; i<array.length();i++){
                        checkMessageCache(array.getJSONObject(i));
                    }
                }
            }*/
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
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

    public JSONArray getMessagesByIds(Set<String> ids){
        SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
        Set<String> messagesSet = prefs.getStringSet("WifiMessages" + prefs.getString("username",""), null);
        JSONArray msgsToSend = new JSONArray();
        if (messagesSet==null){
            messagesSet=new HashSet<>();
        }
        for(String msg : messagesSet){
            for(String id : ids){
                try{
                    if(new JSONObject(msg).getString("id").equals(id)){
                        msgsToSend.put(new JSONObject(msg));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        messagesSet = prefs.getStringSet("muleMessages", null);
        if(messagesSet!=null){
            for(String msg : messagesSet){
                for(String id : ids){
                    try{
                        if(new JSONObject(msg).getString("id").equals(id)){
                            msgsToSend.put(new JSONObject(msg));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return msgsToSend;
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

    @Override
    protected void onPostExecute(Void result) {
        Log.d("SendMessage","Successfully sent message!");
    }
}
