package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import java.io.IOException;
import java.io.OutputStream;

import android.os.AsyncTask;
import android.util.Log;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;

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
            OutputStream out = socket.getOutputStream();
            out.write(data[0].getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Log.d("SendMessage","Successfully sent message!");
    }
}
