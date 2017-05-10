package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.ulisboa.tecnico.cmu.locmess.Services.NotificationService;

import static android.os.Looper.getMainLooper;

/**
 * Created by guiandrade on 09-05-2017.
 */

public class Wifi implements SimWifiP2pManager.GroupInfoListener {

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;

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

    public void getNearbyDevices() {
        // Wi-fi direct requests
        if (mManager != null){
            mManager.requestGroupInfo(mChannel,this);
        }

    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {
        // TO-DO : Handle group information
        Log.d("Wifi","OnGroupInfoAvailable!");
    }


}
