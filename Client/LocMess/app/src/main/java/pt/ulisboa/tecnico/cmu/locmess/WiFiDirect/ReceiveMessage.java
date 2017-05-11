package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.AsyncTask;
import android.util.Log;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

/**
 * Created by guiandrade on 10-05-2017.
 */

public class ReceiveMessage extends AsyncTask<String, String, Void> {

    private int socketPort;
    private SimWifiP2pSocketServer mSrvSocket;

    public ReceiveMessage(int port){
        this.socketPort = port;
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.d("ReceiveMessage","doInBackground");

        try {
            mSrvSocket = new SimWifiP2pSocketServer(socketPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!Thread.currentThread().isInterrupted()) {
            Log.d("ReceiveMessage","inside While");

            try {
                SimWifiP2pSocket sock = mSrvSocket.accept();
                try {
                    // Read data
                    BufferedReader socketIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    String str = socketIn.readLine();

                    // Show data
                    publishProgress(str);
                    Log.d("ReceiveMessage","Message received: "+str);
                    sock.getOutputStream().write(("\n").getBytes());
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

}
