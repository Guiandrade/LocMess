package pt.ulisboa.tecnico.cmu.locmess.Services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.ulisboa.tecnico.cmu.locmess.Activities.MessageActivity;
import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;
import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Models.TimeWindow;
import pt.ulisboa.tecnico.cmu.locmess.R;
import pt.ulisboa.tecnico.cmu.locmess.Security.SecurityHandler;
import pt.ulisboa.tecnico.cmu.locmess.WiFiDirect.ReceiveMessage;
import pt.ulisboa.tecnico.cmu.locmess.WiFiDirect.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmu.locmess.Utils.Http;
import pt.ulisboa.tecnico.cmu.locmess.WiFiDirect.Wifi;


public class NotificationService extends Service {

    private BroadcastReceiver bReciever;
    private ArrayList<String> SSIDs = new ArrayList<String>();
    private String token;
    private String SERVER_IP;
    private static Location location;
    private static Context context;
    private Thread thread;
    boolean running = false;

    @Override
    public void onCreate() {
        context = getApplicationContext();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Runnable backgroundThread = new Runnable() {

        @Override
        public void run() {
            running = true;
            Log.d("NotificationService","backgroundThread");

            Wifi wifiDirect = Wifi.getWifiInstance();
            wifiDirect.setup(NotificationService.this);

            while(running) {
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
                getLocation();
                getSSIDs();
                LocationModel loc = new LocationModel("",new Coordinates("0", "0"));
                if(!(location==null)){
                    loc = new LocationModel("",new Coordinates(String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude())));
                }
                getNearbyMessages(loc, SSIDs);
                sendMyKeys(loc,SSIDs);
                wifiDirect.getNearbyDevices();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SERVER_IP = new Http().getServerIp();
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token","");

        thread = new Thread(backgroundThread);
        thread.start();
        return START_STICKY;
    }

    private void getLocation(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String bestProvider = String.valueOf(manager.getBestProvider(new Criteria(), true));
            try {
                location = manager.getLastKnownLocation(bestProvider);
            }
            catch (SecurityException e){

            }
        }
    }

    public void getSSIDs() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {

            final WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi.isWifiEnabled() == false) {
                wifi.setWifiEnabled(true);
            }

            bReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    List<ScanResult> results = wifi.getScanResults();
                    SSIDs = new ArrayList<String>();
                    int size = results.size();
                    for (int i = 0; i < size; i++) {
                        if(!SSIDs.contains(results.get(i).SSID)) {
                            SSIDs.add(results.get(i).SSID);
                        }
                    }
                }
            };

            this.registerReceiver(bReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            wifi.startScan();
        } else {
            /*ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
            }, 0);*/

        }
    }

    private void checkMessageCache(JSONObject obj){
        try{
            SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
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

    public void getNearbyMessages(LocationModel location, final ArrayList<String> ssids){
        for(String ssid: ssids){
            System.out.println("A VER A NET: "+ ssid);
        }
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/messages";
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("latitude",location.getCoordinates().getLatitude().toString());
            jsonBody.put("longitude",location.getCoordinates().getLongitude().toString());
            jsonBody.put("ssids",new JSONArray(ssids));
            System.out.println("A ENVIAR: " + jsonBody.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                               Log.d("onResponse","RESPOSTA : "+response.get("status"));
                                JSONArray array = response.getJSONArray("messages");
                                for(int i=0;i<array.length();i++){
                                    checkMessageCache(array.getJSONObject(i));
                                    Log.d("onResponse","MESSAGE : "+ array.getJSONObject(i).toString());
                                }
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
    }

    public void sendMyKeys(LocationModel location, final ArrayList<String> locations){
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        SecurityHandler.allowAllSSL();
        String url = "https://" + new Http().getServerIp() + "/myLocations";
        JSONObject jsonBody = new JSONObject();
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
                                SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
                                Set<String> messagesSet = prefs.getStringSet("WifiMessages" + prefs.getString("username",""), null);
                                Set<JSONObject> setKeyMessages = new HashSet<JSONObject>();
                                for(String loc : locations){
                                    for(String msg : messagesSet){
                                        if(new JSONObject(msg).getString("location").equals(loc)){
                                            JSONObject wifiKeyMessage = new JSONObject();
                                            wifiKeyMessage.put("whitelist",new JSONObject(msg).getJSONObject("whitelist"));
                                            wifiKeyMessage.put("blacklist",new JSONObject(msg).getJSONObject("blacklist"));
                                            wifiKeyMessage.put("id", new JSONObject(msg).getString("id"));
                                            setKeyMessages.add(wifiKeyMessage);
                                        }
                                    }
                                }
                                System.out.println(setKeyMessages);
                                compareKeys(setKeyMessages);// USAR CLASS DO GUI E FAZER SEND
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

                SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
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

    public Set<JSONObject> getMessagesByIds(Set<String> ids){
        SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
        Set<String> messagesSet = prefs.getStringSet("WifiMessages" + prefs.getString("username",""), null);
        Set<JSONObject> msgsToSend = new HashSet<JSONObject>();
        for(String msg : messagesSet){
            for(String id : ids){
                try{
                    if(new JSONObject(msg).getJSONObject("id").equals(id)){
                        msgsToSend.add(new JSONObject(msg));
                    }
                }catch (Exception e){

                }
            }
        }
        return msgsToSend;
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

        final Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("Message",mssg);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("New message received!")
                        .setContentText("From: "+ username)
                        .setContentIntent(pendingIntent).setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(new Random().nextInt(), mBuilder.build());
    }

    public void endThread(){
        running=false;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        endThread();
        //this.unregisterReceiver(bReciever);
    }

    public static Context getContext(){
        return context;
    }
}
