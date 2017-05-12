package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.Security.SecurityHandler;
import pt.ulisboa.tecnico.cmu.locmess.Services.NotificationService;


import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.ulisboa.tecnico.cmu.locmess.Utils.Http;

/**
 * Created by guiandrade on 09-05-2017.
 */

public class Wifi implements SimWifiP2pManager.GroupInfoListener {

    private static final Wifi ourInstance = new Wifi();

    public static Wifi getWifiInstance() {
        return ourInstance;
    }

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private HashSet<String> groupDevices;
    private int port = 10001;

    public void setup (NotificationService service){
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(NotificationService.getContext());

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        SimWifiP2pBroadcastReceiver receiver = new SimWifiP2pBroadcastReceiver();
        service.registerReceiver(receiver, filter);

        // bind the Termite Service
        Intent intent = new Intent(NotificationService.getContext(), SimWifiP2pService.class);
        service.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(NotificationService.getContext(), Looper.getMainLooper(),null);
            Log.d("Wifi","onServiceConnected");

            new ReceiveMessage(port).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            Log.d("Wifi","onServiceDisconnected");
        }
    };


    public void getNearbyDevices() {
        // Wi-fi direct requests
        if (mManager != null){
            mManager.requestGroupInfo(mChannel,this);
        }

    }

    public void sendMessageToAll(String message,int port, String id){
        for(String ip : groupDevices){
            sendMyKeys(NotificationService.loc, NotificationService.SSIDs, ip, id);
        }
    }

    /*public void sendUpdateToAll(){
        for(String ip : groupDevices){
            sendMessage("update\n",ip,10001);
        }
    }*/

    public void sendMessage(String message,String ip, int port) {
        new SendMessage(ip,port).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,message);
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {
        Log.d("Wifi", "OnGroupInfoAvailable!");
        if(groupDevices==null){
            groupDevices = new HashSet<>();
        }
        HashSet<String> newNetworkDevices = new HashSet();

        // Look for devices on group
        for (String device  : simWifiP2pInfo.getDevicesInNetwork()){
            // Add new device with it's IP
            SimWifiP2pDevice deviceP2p = simWifiP2pDeviceList.getByName(device);
            String deviceIp = deviceP2p.getVirtIp();
            newNetworkDevices.add(deviceIp);
            Log.d("onGroupInfoAvailable","Device on network with ip: " + deviceIp + " with name: "+ device);
        }

        // Add new devices on group
        for (String ip : newNetworkDevices){
            if (!groupDevices.contains(ip)){
            Log.d("SendMessageNewDevices","Before sending message to ip "+ip);
                groupDevices.add(ip);
                // Send Message to new ip
                sendMyKeys(NotificationService.loc, NotificationService.SSIDs, ip, null);
            }
        }

        // Remove devices no longer on group
        for (String ip : groupDevices){
            if (!newNetworkDevices.contains(ip)){
                groupDevices.remove(ip);
                Log.d("Wifi","Removed device with ip "+ip);
            }
        }
    }

    public void sendMyKeys(LocationModel location, final ArrayList<String> locations, final String ip, final String id){
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
                                            if(id==null){
                                                JSONObject wifiKeyMessage = new JSONObject();
                                                wifiKeyMessage.put("whitelist",new JSONObject(msg).getJSONObject("whitelist"));
                                                wifiKeyMessage.put("blacklist",new JSONObject(msg).getJSONObject("blacklist"));
                                                wifiKeyMessage.put("id", new JSONObject(msg).getString("id"));
                                                setKeyMessages.put(wifiKeyMessage);
                                            }
                                            else if(id!=null && new JSONObject(msg).getString("id").equals(id)){
                                                JSONObject wifiKeyMessage = new JSONObject();
                                                wifiKeyMessage.put("whitelist",new JSONObject(msg).getJSONObject("whitelist"));
                                                wifiKeyMessage.put("blacklist",new JSONObject(msg).getJSONObject("blacklist"));
                                                wifiKeyMessage.put("id", new JSONObject(msg).getString("id"));
                                                setKeyMessages.put(wifiKeyMessage);
                                                break;
                                            }
                                        }
                                    }
                                }
                                String message = json.put("Keys",setKeyMessages) + "\n";
                                sendMessage(message,ip,port);
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
}
