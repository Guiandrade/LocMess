package pt.ulisboa.tecnico.cmu.locmess.WiFiDirect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.ulisboa.tecnico.cmu.locmess.Services.NotificationService;

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
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = socketIn.readLine();

            JSONObject json = new JSONObject(response);
            Log.d("SendMessage",response);
            if(json.has("ids")){
                Log.d("ReceiveMessage",json.toString());
                JSONArray array = json.getJSONArray("ids");
                if(array.length()!=0){
                    Set<String> ids = new HashSet<String>();
                    for(int i=0; i<array.length();i++){
                        ids.add(array.getString(i));
                    }
                    JSONObject resp = new JSONObject();
                    Log.d("SendMessage", "SENDOU CARALHO");
                    resp.put("messages",getMessagesByIds(ids));
                    OutputStream out1 = socket.getOutputStream();
                    Log.d("SendMessage", resp.toString());
                    out1.write((resp + "\n").getBytes());
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray getMessagesByIds(Set<String> ids){
        SharedPreferences prefs = NotificationService.getContext().getSharedPreferences("userInfo", NotificationService.getContext().MODE_PRIVATE);
        Set<String> messagesSet = prefs.getStringSet("WifiMessages" + prefs.getString("username",""), null);
        JSONArray msgsToSend = new JSONArray();
        for(String msg : messagesSet){
            for(String id : ids){
                try{
                    if(new JSONObject(msg).getString("id").equals(id)){
                        msgsToSend.put(new JSONObject(msg));
                    }
                }catch (Exception e){

                }
            }
        }
        return msgsToSend;
    }

    @Override
    protected void onPostExecute(Void result) {
        Log.d("SendMessage","Successfully sent message!");
    }
}
