package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;
import java.util.HashSet;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.ulisboa.tecnico.cmu.locmess.Services.NotificationService;


import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;

/**
 * Created by guiandrade on 09-05-2017.
 */

public class Wifi implements SimWifiP2pManager.GroupInfoListener {

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private HashSet<String> groupDevices = new HashSet<>();
    private int port = 10001;

    public Wifi (NotificationService service){
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

    public void sendMessage(String ip, int port) {
        String messageToSend = "This is a message from the device with ip "+ip;
        new SendMessage(ip,port).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,messageToSend);
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {
        // TO-DO : Handle group information
        Log.d("Wifi", "OnGroupInfoAvailable!");
        HashSet<String> newNetworkDevices = new HashSet();

        // Look for devices on group
        for (String device  : simWifiP2pInfo.getDevicesInNetwork()){
            // Add new device with it's IP
            SimWifiP2pDevice deviceP2p = simWifiP2pDeviceList.getByName(device);
            String deviceIp = deviceP2p.getVirtIp();
            newNetworkDevices.add(deviceIp);
            Log.d("onGroupInfoAvailable","New device with ip: " + deviceIp + " with name: "+ device);
        }

        // Add new devices on group
        for (String ip : newNetworkDevices){
            if (!groupDevices.contains(ip)){

                groupDevices.add(ip);
                // Send Message to new ip
                sendMessage(ip,port);
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


}
