package pt.ulisboa.tecnico.cmu.locmess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import pt.ulisboa.tecnico.cmu.locmess.Activities.LoginActivity;
import pt.ulisboa.tecnico.cmu.locmess.Activities.UserAreaActivity;

/**
 * Created by wazamaisers on 06-05-2017.
 */

public class Http {

    String SERVER_IP = "192.168.1.183:8080";

    public String getServerIp(){
        return SERVER_IP;
    }

    public void session(final JSONObject jsonBody, final Context context, final String type){

        final boolean returnStatus = false;
        RequestQueue queue;
        queue = Volley.newRequestQueue(context);
        String url = "";
        if (type.equals("login")){
            url = "http://" + SERVER_IP + "/login";
        }
        else if (type.equals("register")){
            url = "http://" + SERVER_IP + "/signup";
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                                SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("token",response.getString("token"));
                                editor.putString("username",jsonBody.getString("username"));
                                editor.apply();
                                Intent loginIntent = new Intent(context, UserAreaActivity.class);
                                loginIntent.putExtra("serverIP", SERVER_IP);
                                context.startActivity(loginIntent);
                                if (type.equals("login")){
                                    Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show();
                                }
                                else if (type.equals("register")){
                                    Toast.makeText(context, "Register successful", Toast.LENGTH_LONG).show();
                                }
                            }
                            else{
                                try{
                                    Toast.makeText(context, "Error: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(context, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(context, "Lost connection...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        queue.add(jsObjRequest);
    }


}
