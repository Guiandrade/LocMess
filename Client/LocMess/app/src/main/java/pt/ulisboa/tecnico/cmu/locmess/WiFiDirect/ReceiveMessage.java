package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
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
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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
import pt.ulisboa.tecnico.cmu.locmess.Security.SecurityHandler;
import pt.ulisboa.tecnico.cmu.locmess.Services.NotificationService;
import pt.ulisboa.tecnico.cmu.locmess.Utils.Http;

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
                //Log.d("ReceiveMessage","Accepted Socket");
                try {
                    BufferedReader socketIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    String str = socketIn.readLine();
                    Log.d("ReceiveMessage",str);
                    JSONArray ids = new JSONArray();
                    try{
                        /*if(str.equals("update")){
                            Log.d("RecieveMessage", "Recieved update");

                            sendMyKeys(NotificationService.loc, NotificationService.SSIDs,sock);

                        }*/
                        JSONObject json = new JSONObject(str);
                        if(json.has("KeysUser")){
                            Log.d("RecieveMessage", "KeysUser");
                            JSONObject jsonObject = new JSONObject();
                            JSONArray array = json.getJSONArray("KeysUser");
                            Log.d("RecieveMessage", "Array User Keys" + array);
                            Set<JSONObject> setJson = new HashSet<JSONObject>();
                            for(int i=0; i<array.length();i++){
                                setJson.add(array.getJSONObject(i));
                            }
                            compareKeys(setJson,ids);

                            array = json.getJSONArray("KeysMule");
                            Log.d("RecieveMessage", "KeysMules " + array);
                            setJson = new HashSet<JSONObject>();
                            for(int i=0; i<array.length();i++){
                                setJson.add(array.getJSONObject(i));
                            }
                            compareKeys(setJson,ids);
                            jsonObject.put("ids",ids);
                            Log.d("RecieveMessage", "Ids " + jsonObject);

                            sock.getOutputStream().write((jsonObject.toString()+"\n").getBytes());
                        }
                        if(ids.length()!=0){
                            socketIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                            str = socketIn.readLine();
                            json = new JSONObject(str);
                            if(json.has("messagesDirect")){
                                JSONArray array = json.getJSONArray("messagesDirect");
                                //Log.d("RecieveMessage", array.toString());
                                for(int i=0; i<array.length();i++){
                                    checkMessageCache(array.getJSONObject(i));
                                }
                                SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
                                final int mules = Integer.parseInt(prefs.getString("mules", ""));
                                array = json.getJSONArray("messagesMule");
                                Set<String> setMules = prefs.getStringSet("mules", null);
                                if(setMules==null){
                                    setMules = new HashSet<String>();
                                    for(int i=0;i<array.length();i++){
                                        if((i)==mules){
                                            break;
                                        }
                                        setMules.add(array.get(i).toString());
                                    }
                                }
                                else{
                                    JSONArray newArray = new JSONArray();
                                    if(array.length()>mules){
                                        for(int i=0;i<mules;i++){
                                            newArray.put(array.getString(i));
                                        }
                                    }
                                    else{
                                        newArray = array;
                                    }
                                    int element = mules-setMules.size()-newArray.length();
                                    if(element>=0){
                                        for(int i=0;i<newArray.length();i++){
                                            setMules.add(newArray.getString(i));
                                        }
                                    }
                                    else{
                                        Iterator<String> it = setMules.iterator();
                                        while(it.hasNext() && element!=0){
                                            it.remove();
                                            element++;
                                        }
                                        for(int i=0;i<newArray.length();i++){
                                            setMules.add(newArray.getString(i));
                                        }
                                    }
                                }
                                Log.d("ReceiveMessage","FIM " + setMules);
                            }
                        }
                        sock.close();

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    Log.d("Error: Reading socket", e.getMessage());
                } finally {

                }
            } catch (IOException e) {
                Log.d("Error: Socket", e.getMessage());
                break;
            }
        }
        return null;
    }

    /*public void sendMyKeys(LocationModel location, final ArrayList<String> locations, final SimWifiP2pSocket sock){
        RequestQueue queue;
        queue = Volley.newRequestQueue(NotificationService.getContext());
        SecurityHandler.allowAllSSL();
        String url = "https://" + new Http().getServerIp() + "/myLocations";
        JSONObject jsonBody = new JSONObject();
        SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
        final String token = prefs.getString("token", "");
        try{
            jsonBody.put("latitude",location.getCoordinates().getLatitude().toString());
            jsonBody.put("longitude",location.getCoordinates().getLongitude().toString());
            System.out.println("A ENVIAR LOC: " + jsonBody.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                                Log.d("onResponse","RESPOSTA : "+response.get("status"));
                                JSONArray array = response.getJSONArray("locations");
                                for(int i=0;i<array.length();i++){
                                    locations.add(array.getString(i));
                                }
                                SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
                                Set<String> messagesSet = prefs.getStringSet("WifiMessages" + prefs.getString("username",""), null);
                                JSONArray setKeyMessages = new JSONArray();
                                JSONObject json = new JSONObject();
                                for(String loc : locations){
                                    for(String msg : messagesSet){
                                        if(new JSONObject(msg).getString("location").equals(loc)){
                                            JSONObject wifiKeyMessage = new JSONObject();
                                            wifiKeyMessage.put("whitelist",new JSONObject(msg).getJSONObject("whitelist"));
                                            wifiKeyMessage.put("blacklist",new JSONObject(msg).getJSONObject("blacklist"));
                                            wifiKeyMessage.put("id", new JSONObject(msg).getString("id"));
                                            setKeyMessages.put(wifiKeyMessage);
                                        }
                                    }
                                }
                                String message = json.put("Keys",setKeyMessages) + "\n";
                                sock.getOutputStream().write((message+"\n").getBytes());
                                Log.d("ReceiveMessage", "Sent keys white and black " + message);
                                BufferedReader socketIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                                String resp = socketIn.readLine();
                                Log.d("ReceiveMessage", "Resposta Ids " + resp);
                                json = new JSONObject(resp);
                                if(json.has("ids")){
                                    array = json.getJSONArray("ids");
                                    if(array.length()!=0){
                                        Set<String> ids = new HashSet<String>();
                                        for(int i=0; i<array.length();i++){
                                            ids.add(array.getString(i));
                                        }
                                        JSONObject Resp = new JSONObject();
                                        Resp.put("messages",getMessagesByIds(ids));
                                        OutputStream out1 = sock.getOutputStream();
                                        Log.d("ReceiveMessage", "Mensagens enviadas " + Resp.toString());
                                        out1.write((Resp + "\n").getBytes());
                                    }
                                }
                                sock.close();
                            }
                            else{
                                try{
                                    //Toast.makeText(NotificationService.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Basic " + token);
                return headers;
            }
        };
        queue.add(jsObjRequest);
    }*/

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

    public JSONArray compareKeys(Set<JSONObject> keys, JSONArray ids){
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
