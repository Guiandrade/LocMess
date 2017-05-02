package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static android.R.attr.data;

public class UserAreaActivity extends AppCompatActivity {

    String SERVER_IP;
    String username;
    String token;
    int MAIN_ACTIVITY_REQUEST_CODE = 1;
    int POST_MESSAGE_REQUEST_CODE = 2;
    int USER_PROFILE_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        Intent serviceIntent = new Intent(UserAreaActivity.this, NotificationService.class);
        serviceIntent.putExtra("serverIP", SERVER_IP);
        startService(serviceIntent);

        SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token","");
        username = sharedPreferences.getString("username","");

        final ImageButton ibGridMenu = (ImageButton) findViewById(R.id.ibGridMenu);
        final Button btPostMessage = (Button) findViewById(R.id.btPostMessage);
        final ImageButton ibUserProfile = (ImageButton) findViewById(R.id.ibUserProfile);
        final Button btLogout = (Button) findViewById(R.id.btLogout);

        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("token","");
                editor.putStringSet("messages",null);  // ISTO E PARA SAIR
                editor.apply();
                Intent logoutIntent = new Intent(UserAreaActivity.this, LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(logoutIntent);
                stopService(new Intent(UserAreaActivity.this, NotificationService.class));
            }
        });

        ibGridMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainMenuIntent = new Intent(UserAreaActivity.this, MainMenuActivity.class);
                mainMenuIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(mainMenuIntent,MAIN_ACTIVITY_REQUEST_CODE);
            }
        });

        btPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listLocations("post");
            }
        });

        ibUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getKeys();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
            }
        }
        else if (requestCode == POST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Message message = (Message) data.getSerializableExtra("messagePosted");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                postMessage(message);
            }
        }
        else if (requestCode == USER_PROFILE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                HashMap<String, Set<String>> addedKeyPairs = (HashMap<String, Set<String>>) data.getSerializableExtra("addedKeys");
                HashMap<String, Set<String>> deletedKeyPairs = (HashMap<String, Set<String>>) data.getSerializableExtra("deletedKeys");

                JSONObject res = new JSONObject();
                JSONObject json = new JSONObject();
                for (Map.Entry<String, Set<String>> entry : addedKeyPairs.entrySet()) {
                    try{
                        String key = entry.getKey();
                        Set<String> val = entry.getValue();
                        json.put(key,new JSONArray(val));
                    }catch (Exception e){

                    }
                }
                try{
                    res.put("keys",json);
                    addKeys(res);
                }catch (Exception e){

                }


                JSONObject res1 = new JSONObject();
                JSONObject json1 = new JSONObject();
                for (Map.Entry<String, Set<String>> entry : deletedKeyPairs.entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue());
                    for (String str : entry.getValue()) {
                        try{
                            String key = entry.getKey();
                            Set<String> val = entry.getValue();
                            json1.put(key,new JSONArray(val));
                        }catch (Exception e){

                        }
                    }
                }
                try{
                    res1.put("keys",json1);
                    deletedKeyPairs(res1);
                }catch (Exception e){

                }
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    public ArrayList<Location> listLocations (final String str){
        final ArrayList<Location> locations = new ArrayList<Location>();
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/locations";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")) {
                                for (int i = 0; i < response.getJSONArray("locations").length(); i++) {
                                    JSONObject arr = (JSONObject) response.getJSONArray("locations").get(i);
                                    if(!arr.has("ssid")){
                                        Coordinates coordinates = new Coordinates(arr.get("latitude").toString().substring(0,7),arr.get("longitude").toString().substring(0,7));
                                        Location location = new Location(arr.get("location").toString(),coordinates);
                                        locations.add(location);
                                    }
                                    else{
                                        Location ssid = new Location(arr.get("ssid").toString());
                                        locations.add(ssid);
                                    }
                                }

                                Intent postMessageIntent = new Intent(UserAreaActivity.this, PostMessageActivity.class);
                                postMessageIntent.putExtra("serverIP", SERVER_IP);
                                postMessageIntent.putExtra("locations", locations);
                                startActivityForResult(postMessageIntent,POST_MESSAGE_REQUEST_CODE);

                            }
                            else{
                                try{
                                    Toast.makeText(UserAreaActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(UserAreaActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(UserAreaActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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
        return locations;
    }

    public void postMessage(Message message){
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/messages";
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("title",message.getTitle());
            if(!(message.getLocation().getSSID() == null)) {
                jsonBody.put("location",message.getLocation().getSSID());
            }
            else{
                jsonBody.put("location",message.getLocation().getName());
            }

            String initHour = "" + message.getTimeWindow().getStartingHour();
            String initMinute = "" + message.getTimeWindow().getStartingMinute();
            String initDay = "" + message.getTimeWindow().getStartingDay();
            String initMonth = "" + message.getTimeWindow().getStartingMonth();
            String initYear = "" + message.getTimeWindow().getStartingYear();
            jsonBody.put("initTime",initHour + ":" + initMinute + "-" + initDay + "/" + initMonth + "/" + initYear);
            String endHour = "" + message.getTimeWindow().getEndingHour();
            String endMinute = "" + message.getTimeWindow().getEndingMinutes();
            String endDay = "" + message.getTimeWindow().getEndingDay();
            String endMonth = "" + message.getTimeWindow().getEndingMonth();
            String endYear = "" + message.getTimeWindow().getEndingYear();
            jsonBody.put("endTime",endHour + ":" + endMinute + "-" + endDay + "/" + endMonth + "/" + endYear);
            jsonBody.put("body",message.getMessage());

            JSONObject json = new JSONObject();
            for (Map.Entry<String, Set<String>> entry : message.getWhitelistKeyPairs().entrySet()) {
                try{
                    String key = entry.getKey();
                    Set<String> val = entry.getValue();
                    json.put(key,new JSONArray(val));
                }catch (Exception e){

                }
            }
            try{
                jsonBody.put("whitelist",json);
            }catch (Exception e){

            }

            JSONObject json1 = new JSONObject();
            for (Map.Entry<String, Set<String>> entry : message.getBlacklistKeyPairs().entrySet()) {
                try{
                    String key = entry.getKey();
                    Set<String> val = entry.getValue();
                    json1.put(key,new JSONArray(val));
                }catch (Exception e){

                }
            }
            try{
                jsonBody.put("blacklist",json1);
            }catch (Exception e){

            }

        }catch (Exception e){

        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                                // boa puto
                            }
                            else{
                                try{
                                    Toast.makeText(UserAreaActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(UserAreaActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(UserAreaActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void deletedKeyPairs(JSONObject jsonBody) {
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/removeKey";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                                // boa puto
                            }
                            else{
                                try{
                                    Toast.makeText(UserAreaActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(UserAreaActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(UserAreaActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void addKeys(JSONObject jsonBody){
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/profile";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                                // boa puto
                            }
                            else{
                                try{
                                    Toast.makeText(UserAreaActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(UserAreaActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(UserAreaActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void getAllKeys(final HashMap<String, Set<String>> keys){
        final ArrayList<String> allKeys = new ArrayList<String>();

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/keys";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.get("status").toString().equals("ok")){
                        JSONArray keysJsonArray = response.getJSONArray("keys");
                        for(int i=0; i<keysJsonArray.length();i++){
                            allKeys.add(keysJsonArray.getString(i));
                        }

                        Intent userProfileIntent = new Intent(UserAreaActivity.this, UserProfileActivity.class);
                        userProfileIntent.putExtra("serverIP", SERVER_IP);
                        userProfileIntent.putExtra("keys", keys);
                        userProfileIntent.putExtra("allKeys", allKeys);
                        startActivityForResult(userProfileIntent,USER_PROFILE_REQUEST_CODE);
                    }
                    else{
                        try{
                            Toast.makeText(UserAreaActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(UserAreaActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(UserAreaActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void getKeys(){
        final HashMap<String, Set<String>> keys = new HashMap<String, Set<String>>();

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/profile";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.get("status").toString().equals("ok")){
                        Iterator<String> iter = response.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            Set<String> set = new HashSet<String>();
                            try {
                                for(int i = 0;i<response.getJSONArray(key).length();i++){
                                    set.add(response.getJSONArray(key).getString(i));
                                }
                                keys.put(key,set);
                            } catch (JSONException e) {
                                // Something went wrong!
                            }
                        }
                        getAllKeys(keys);
                    }
                    else{
                        try{
                            Toast.makeText(UserAreaActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(UserAreaActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(UserAreaActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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
