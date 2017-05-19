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
    public static ArrayList<BroadcastReceiver> broadcastReceivers = new ArrayList<BroadcastReceiver>();
    public static ArrayList<String> SSIDs = new ArrayList<String>();
    private String token;
    private String SERVER_IP;
    private Location location;
    public static LocationModel loc;
    private static Context context;
    private Thread thread;
    boolean running = false;
    Http http;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        http=new Http(getApplicationContext());
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
            //Log.d("NotificationService","backgroundThread");

            Wifi wifiDirect = Wifi.getWifiInstance();
            wifiDirect.setup(NotificationService.this);
            broadcastReceivers.add(wifiDirect.unregister());

            while(running) {


                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }

                SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
                String username = prefs.getString("username","");

                if(!username.equals("")) {
                    getLocation();
                    getSSIDs();
                    loc = new LocationModel("", new Coordinates("0", "0"));
                    if (!(location == null)) {
                        loc = new LocationModel("", new Coordinates(String.valueOf(location.getLatitude()),
                                String.valueOf(location.getLongitude())));
                    }
                    getNearbyMessages(loc, SSIDs);
                    wifiDirect.getNearbyDevices();
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SERVER_IP = http.getServerIp();
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
            broadcastReceivers.add(bReciever);
            this.registerReceiver(bReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            wifi.startScan();
        } else {
            /*ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
            }, 0);*/

        }
    }

    public void checkMessageCache(JSONObject obj){
        try{
            SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
            String username = prefs.getString("username","");
            Set<String> messagesSet = prefs.getStringSet("messages" + username, null);
            if(!username.equals("")){
                if(messagesSet==null) {
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

        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void getNearbyMessages(LocationModel location, final ArrayList<String> ssids){
        for(String ssid: ssids){
            System.out.println("A VER A NET: "+ ssid);
        }

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
        http.addQueue(jsObjRequest);
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
            e.printStackTrace();
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
        http.destroyQueue();

        for(int i=0;i<broadcastReceivers.size();i++){
            try{
                this.unregisterReceiver(broadcastReceivers.get(i));
            }catch (Exception e){}
        }
        Wifi wifiDirect = Wifi.getWifiInstance();
        this.unbindService(wifiDirect.conn());
        super.onDestroy();
        endThread();


    }


    public static Context getContext(){
        return context;
    }
}
