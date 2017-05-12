package pt.ulisboa.tecnico.cmu.locmess.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.cmu.locmess.Activities.ListLocationsActivity;
import pt.ulisboa.tecnico.cmu.locmess.Activities.MainMenuActivity;
import pt.ulisboa.tecnico.cmu.locmess.Activities.PostMessageActivity;
import pt.ulisboa.tecnico.cmu.locmess.Activities.RemovableItemListActivity;
import pt.ulisboa.tecnico.cmu.locmess.Activities.UnpostMessageActivity;
import pt.ulisboa.tecnico.cmu.locmess.Activities.UserAreaActivity;
import pt.ulisboa.tecnico.cmu.locmess.Activities.UserProfileActivity;
import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;
import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Models.TimeWindow;
import pt.ulisboa.tecnico.cmu.locmess.Security.SecurityHandler;

/**
 * Created by wazamaisers on 06-05-2017.
 */

public class Http {

    static String SERVER_IP = "istcmu.tk:8080";
    int REMOVE_LOCATIONS_REQUEST_CODE = 2;
    int POST_MESSAGE_REQUEST_CODE = 3;
    int UNPOST_MESSAGE_REQUEST_CODE = 4;
    int USER_PROFILE_REQUEST_CODE = 5;

    public String getServerIp(){
        return SERVER_IP;
    }

    public void session(final JSONObject jsonBody, final Context context, final String type){

        final boolean returnStatus = false;
        RequestQueue queue;
        queue = Volley.newRequestQueue(context);
        SecurityHandler.allowAllSSL();
        String url = "";
        if (type.equals("login")){
            url = "https://" + SERVER_IP + "/login";
        }
        else if (type.equals("register")){
            url = "https://" + SERVER_IP + "/signup";
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
                            error.printStackTrace();
                            Toast.makeText(context, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(context, "Lost connection...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        queue.add(jsObjRequest);
    }

    public void createLocation(LocationModel location, final Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");

        RequestQueue queue;
        queue = Volley.newRequestQueue(context);
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/locations";
        JSONObject jsonBody = new JSONObject();
        if(location.getSSID() == null){
            try{
                jsonBody.put("location",location.getName());
                jsonBody.put("latitude",Double.parseDouble(location.getCoordinates().getLatitude()));
                jsonBody.put("longitude",Double.parseDouble(location.getCoordinates().getLongitude()));
                jsonBody.put("radius",Integer.parseInt(location.getCoordinates().getRadius()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            try{
                jsonBody.put("ssid",location.getSSID().split(" ")[1]);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                                // boa
                            }
                            else{
                                try{
                                    Toast.makeText(context, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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

    public void removeLocations(ArrayList<String> locations, final Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");

        RequestQueue queue;
        queue = Volley.newRequestQueue(context);
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/deleteLocation";
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("locations",new JSONArray(locations));
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
                                    Toast.makeText(context, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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

    public void removeMessages(ArrayList<String> ids, final Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");

        RequestQueue queue;
        queue = Volley.newRequestQueue(context);
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/deleteMessages";
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("ids",new JSONArray(ids));
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
                                    Toast.makeText(context, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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

    public void postMessage(Message message, final Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");

        RequestQueue queue;
        queue = Volley.newRequestQueue(context);
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/messages";
        JSONObject jsonBody = parseMsgToSend(message);
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
                                    Toast.makeText(context, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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

    public void listLocations (final String str, final View v){
        final ArrayList<LocationModel> locations = new ArrayList<LocationModel>();
        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");
        RequestQueue queue;
        queue = Volley.newRequestQueue(v.getContext());
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/locations";
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
                                        LocationModel location = new LocationModel(arr.get("location").toString(),coordinates);
                                        locations.add(location);
                                    }
                                    else{
                                        System.out.println(arr.get("ssid").toString());
                                        LocationModel ssid = new LocationModel(arr.get("ssid").toString());
                                        locations.add(ssid);
                                    }
                                }
                                if(str.equals("list")){
                                    Intent listLocationsIntent = new Intent(v.getContext(), ListLocationsActivity.class);
                                    listLocationsIntent.putExtra("serverIP", SERVER_IP);
                                    listLocationsIntent.putExtra("locations", locations);
                                    v.getContext().startActivity(listLocationsIntent);
                                }
                                else if (str.equals("post")){
                                    Intent postMessageIntent = new Intent(v.getContext(), PostMessageActivity.class);
                                    postMessageIntent.putExtra("serverIP", SERVER_IP);
                                    postMessageIntent.putExtra("locations", locations);
                                    ((Activity) v.getContext()).startActivityForResult(postMessageIntent,POST_MESSAGE_REQUEST_CODE);
                                }
                                else if (str.equals("remove")){
                                    Intent removeLocationsIntent = new Intent(v.getContext(), RemovableItemListActivity.class);
                                    removeLocationsIntent.putExtra("serverIP", SERVER_IP);
                                    removeLocationsIntent.putExtra("locations", locations);
                                    ((Activity) v.getContext()).startActivityForResult(removeLocationsIntent,REMOVE_LOCATIONS_REQUEST_CODE);
                                }
                            }
                            else{
                                try{
                                    Toast.makeText(v.getContext(), "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(v.getContext(), "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(v.getContext(), "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void removeMessages(final View v){
        final ArrayList<Message> messages = new ArrayList<Message>();
        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");
        RequestQueue queue;
        queue = Volley.newRequestQueue(v.getContext());
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/userMessages";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.get("status").toString().equals("ok")){
                        for (int i = 0; i < response.getJSONArray("messages").length(); i++) {
                            JSONObject arr = (JSONObject) response.getJSONArray("messages").get(i);
                            LocationModel location = new LocationModel(arr.get("location").toString(),(Coordinates) null);
                            int initHour = Integer.parseInt(arr.get("initTime").toString().split(":")[0]);
                            int initMinute = Integer.parseInt(arr.get("initTime").toString().split(":")[1].split("-")[0]);
                            int initDay = Integer.parseInt(arr.get("initTime").toString().split("/")[0].split("-")[1]);
                            int initMonth = Integer.parseInt(arr.get("initTime").toString().split("/")[1]);
                            int initYear = Integer.parseInt(arr.get("initTime").toString().split("/")[2]);
                            int endHour = Integer.parseInt(arr.get("endTime").toString().split(":")[0]);
                            int endMinute = Integer.parseInt(arr.get("endTime").toString().split(":")[1].split("-")[0]);
                            int endDay = Integer.parseInt(arr.get("endTime").toString().split("/")[0].split("-")[1]);
                            int endMonth = Integer.parseInt(arr.get("endTime").toString().split("/")[1]);
                            int endYear = Integer.parseInt(arr.get("endTime").toString().split("/")[2]);
                            TimeWindow timeWindow = new TimeWindow(initHour,initMinute,initDay,initMonth,
                                    initYear,endHour,endMinute,endDay,endMonth,endYear);

                            String id = arr.get("id").toString();
                            String msg = arr.get("body").toString();
                            String owner = arr.get("username").toString();
                            String title = arr.get("title").toString();

                            Message message = new Message(id,title,msg,owner,location,null,null,timeWindow);
                            messages.add(message);
                        }
                        Intent unpostMessageIntent = new Intent(v.getContext(), UnpostMessageActivity.class);
                        unpostMessageIntent.putExtra("serverIP", SERVER_IP);
                        unpostMessageIntent.putExtra("messages", messages);
                        ((Activity) v.getContext()).startActivityForResult(unpostMessageIntent,UNPOST_MESSAGE_REQUEST_CODE);
                    }
                    else{
                        try{
                            Toast.makeText(v.getContext(), "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(v.getContext(), "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void getKeys(final Context context, final boolean activity) {
        final HashMap<String, Set<String>> keys = new HashMap<String, Set<String>>();

        final SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token", "");
        RequestQueue queue;
        queue = Volley.newRequestQueue(context);
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/profile";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.get("status").toString().equals("ok")) {
                        Iterator<String> iter = response.keys();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Set<String> Keys = new HashSet<String>();
                        editor.putStringSet("Keys", Keys);
                        editor.apply();
                        Set<String> messagesSet = new HashSet<String>();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            Set<String> set = new HashSet<String>();
                            try {
                                for (int i = 0; i < response.getJSONArray(key).length(); i++) {
                                    set.add(response.getJSONArray(key).getString(i));
                                    messagesSet.add(key + " = " + response.getJSONArray(key).getString(i));
                                    editor = sharedPreferences.edit();
                                    editor.putStringSet("Keys", messagesSet);
                                    editor.apply();
                                }
                                keys.put(key, set);
                            } catch (JSONException e) {
                                // Something went wrong!
                            }
                        }
                    } else {
                        try {
                            Toast.makeText(context, "Status: " + response.get("status"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Toast.makeText(context, "Error: " + new String(error.networkResponse.data, "UTF-8"), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Lost connection...", Toast.LENGTH_LONG).show();
                }
            }
        }) {
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

    public void getKeys(final View v, final boolean activity) {
        final HashMap<String, Set<String>> keys = new HashMap<String, Set<String>>();

        final SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token", "");
        RequestQueue queue;
        queue = Volley.newRequestQueue(v.getContext());
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/profile";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.get("status").toString().equals("ok")) {
                        Iterator<String> iter = response.keys();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putStringSet("Keys", null);

                        Set<String> messagesSet = new HashSet<String>();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            Set<String> set = new HashSet<String>();
                            try {
                                for (int i = 0; i < response.getJSONArray(key).length(); i++) {
                                    set.add(response.getJSONArray(key).getString(i));
                                    messagesSet.add(key + " = " + response.getJSONArray(key).getString(i));
                                    editor = sharedPreferences.edit();
                                    editor.putStringSet("Keys", messagesSet);
                                    editor.apply();
                                }
                                keys.put(key, set);
                            } catch (JSONException e) {
                                // Something went wrong!
                            }
                        }
                        if(activity==true){
                            getAllKeys(keys,v);
                        }
                    } else {
                        try {
                            Toast.makeText(v.getContext(), "Status: " + response.get("status"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Toast.makeText(v.getContext(), "Error: " + new String(error.networkResponse.data, "UTF-8"), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "Lost connection...", Toast.LENGTH_LONG).show();
                }
            }
        }) {
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

    public void getAllKeys(final HashMap<String, Set<String>> keys, final View v) {
        final ArrayList<String> allKeys = new ArrayList<String>();

        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token", "");
        RequestQueue queue;
        queue = Volley.newRequestQueue(v.getContext());
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/keys";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.get("status").toString().equals("ok")) {
                        JSONArray keysJsonArray = response.getJSONArray("keys");
                        for (int i = 0; i < keysJsonArray.length(); i++) {
                            allKeys.add(keysJsonArray.getString(i));
                        }

                        Intent userProfileIntent = new Intent(v.getContext(), UserProfileActivity.class);
                        userProfileIntent.putExtra("serverIP", SERVER_IP);
                        userProfileIntent.putExtra("keys", keys);
                        userProfileIntent.putExtra("allKeys", allKeys);
                        ((Activity) v.getContext()).startActivityForResult(userProfileIntent, USER_PROFILE_REQUEST_CODE);
                    } else {
                        try {
                            Toast.makeText(v.getContext(), "Status: " + response.get("status"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Toast.makeText(v.getContext(), "Error: " + new String(error.networkResponse.data, "UTF-8"), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "Lost connection...", Toast.LENGTH_LONG).show();
                }
            }
        }) {
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

    public void deletedKeyPairs(JSONObject jsonBody, final Context context) {
        RequestQueue queue;
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token", "");
        queue = Volley.newRequestQueue(context);
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/removeKey";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.get("status").toString().equals("ok")) {
                                getKeys(context,false);
                            } else {
                                try {
                                    Toast.makeText(context, "Status: " + response.get("status"), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Toast.makeText(context, "Error: " + new String(error.networkResponse.data, "UTF-8"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Lost connection...", Toast.LENGTH_LONG).show();
                        }
                    }
                }) {
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

    public void addKeys(JSONObject jsonBody, final Context context) {
        RequestQueue queue;
        SharedPreferences sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token", "");
        queue = Volley.newRequestQueue(context);
        SecurityHandler.allowAllSSL();
        String url = "https://" + SERVER_IP + "/profile";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.get("status").toString().equals("ok")) {
                                new Http().getKeys(context,false);
                            } else {
                                try {
                                    Toast.makeText(context, "Status: " + response.get("status"), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Toast.makeText(context, "Error: " + new String(error.networkResponse.data, "UTF-8"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Lost connection...", Toast.LENGTH_LONG).show();
                        }
                    }
                }) {
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

    private JSONObject parseMsgToSend(Message message){
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
        return jsonBody;
    }
}
