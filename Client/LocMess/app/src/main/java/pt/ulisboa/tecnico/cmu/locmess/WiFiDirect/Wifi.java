package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.ulisboa.tecnico.cmu.locmess.Services.NotificationService;

import static android.os.Looper.getMainLooper;

/**
 * Created by guiandrade on 09-05-2017.
 */

public class Wifi implements SimWifiP2pManager.GroupInfoListener {
    private SimWifiP2pManager mManager;
    private SimWifiP2pManager.Channel mChannel;
    private Messenger mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel
                    =
                    mManager.initialize(NotificationService.getContext(),
                            getMainLooper(),
                            null);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;

        }
    };
    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {
        // TO-DO : Handle group information
    }

    private void getNearbyDevices() {
        // Wi-fi direct requests
        mManager.requestGroupInfo(mChannel,this);
    }
}
