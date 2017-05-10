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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulisboa.tecnico.cmu.locmess.Activities.MessageActivity;
import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;
import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Models.TimeWindow;
import pt.ulisboa.tecnico.cmu.locmess.R;
import pt.ulisboa.tecnico.cmu.locmess.Security.SecurityHandler;
import pt.ulisboa.tecnico.cmu.locmess.Utils.Http;

public class NotificationService extends Service {

    BroadcastReceiver bReciever;
    ArrayList<String> SSIDs = new ArrayList<String>();
    Timer timer;
    String token;
    String SERVER_IP;
    private Location location;
    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        Log.d("NotificationService","Saved Application Context!");
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SERVER_IP = new Http().getServerIp();
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token","");
        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                getLocation();
                getSSIDs();
                LocationModel loc = new LocationModel("",new Coordinates("0", "0"));
                if(!(location==null)){
                    loc = new LocationModel("",new Coordinates(String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude())));
                }
                getNearbyMessages(loc, SSIDs);
            }
        }, 0, 5000);
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
            Set<String> messagesSet = prefs.getStringSet("messages", null);
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
            editor.putStringSet("messages", messagesSet);
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
        }catch (Exception e){
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                                System.out.println("RESPOSTA : "+response.get("status"));
                                JSONArray array = response.getJSONArray("messages");
                                for(int i=0;i<array.length();i++){
                                    checkMessageCache(array.getJSONObject(i));
                                    System.out.println("MESSAGE : "+ array.getJSONObject(i).toString());
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        timer.cancel();
        this.unregisterReceiver(bReciever);
    }

    public static Context getContext(){
        return context;
    }
}
