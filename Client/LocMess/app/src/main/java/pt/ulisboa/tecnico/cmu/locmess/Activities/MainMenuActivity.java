package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;
import pt.ulisboa.tecnico.cmu.locmess.Models.Location;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Models.TimeWindow;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class MainMenuActivity extends AppCompatActivity {

    String token;
    String SERVER_IP;
    int CREATE_LOCATIONS_REQUEST_CODE = 1;
    int REMOVE_LOCATIONS_REQUEST_CODE = 2;
    int POST_MESSAGE_REQUEST_CODE = 3;
    int UNPOST_MESSAGE_REQUEST_CODE = 4;
    ArrayList<Location> locations = new ArrayList<Location>();
    ArrayList<Message> messages = new ArrayList<Message>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token","");
        this.setTitle("Main Menu");

        final ImageButton bRemoveLocations = (ImageButton) findViewById(R.id.ibRemoveLocations);
        final ImageButton bListLocations = (ImageButton) findViewById(R.id.ibListLocations);
        final ImageButton bCreateLocations = (ImageButton) findViewById(R.id.ibCreateLocations);
        final Button bPostMessage = (Button) findViewById(R.id.ibPostMessage);
        final ImageButton bUnpostMessage = (ImageButton) findViewById(R.id.ibUnpostMessages);
        final ImageButton bReadMessages = (ImageButton) findViewById(R.id.ibReadMessages);

        //Display back button on top
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bListLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listLocations("list");
            }
        });

        bCreateLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence [] items = {"Coordinates","SSID"};
                new AlertDialog.Builder(MainMenuActivity.this)
                .setSingleChoiceItems(items, 0, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        if(selectedPosition == 0){
                            Intent createLocationsIntent = new Intent(MainMenuActivity.this, CreateLocationActivity.class);
                            createLocationsIntent.putExtra("serverIP", SERVER_IP);
                            startActivityForResult(createLocationsIntent,CREATE_LOCATIONS_REQUEST_CODE);
                        }
                        else{
                            Intent createLocationsSSIDIntent = new Intent(MainMenuActivity.this, SSIDActivity.class);
                            createLocationsSSIDIntent.putExtra("serverIP", SERVER_IP);
                            startActivityForResult(createLocationsSSIDIntent,CREATE_LOCATIONS_REQUEST_CODE);
                        }
                    }
                })
                .show();
            }
        });

        bPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listLocations("post");
            }
        });

        bUnpostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMessages();
            }
        });

        bRemoveLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listLocations("remove");
            }
        });

        bReadMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent readMessagesIntent = new Intent(MainMenuActivity.this, ReadMessagesActivity.class);
                startActivity(readMessagesIntent);
            }
        });

    }

    public void dimiss(DialogInterface dialog){
        dialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CREATE_LOCATIONS_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Location location = (Location) data.getSerializableExtra("locationCreated");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                createLocation(location);
            }
        }

        else if (requestCode == REMOVE_LOCATIONS_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<String> locations = (ArrayList<String>) data.getSerializableExtra("locationsRemoved");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                removeLocations(locations);
            }
        }

        else if (requestCode == POST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Message message = (Message) data.getSerializableExtra("messagePosted");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                postMessage(message);
            }
        }

        else if (requestCode == UNPOST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<String> ids = (ArrayList<String>) data.getSerializableExtra("ids");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                removeMessages(ids);
            }
        }
    }

    public void createLocation(Location location){
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/locations";
        JSONObject jsonBody = new JSONObject();
        if(location.getSSID() == null){
            try{
                jsonBody.put("location",location.getName());
                jsonBody.put("latitude",Double.parseDouble(location.getCoordinates().getLatitude()));
                jsonBody.put("longitude",Double.parseDouble(location.getCoordinates().getLongitude()));
                jsonBody.put("radius",Integer.parseInt(location.getCoordinates().getRadius()));
            }catch (Exception e){

            }
        }
        else{
            try{
                jsonBody.put("ssid",location.getSSID().split(" ")[1]);
            }catch (Exception e) {

            }
        }

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
                                    Toast.makeText(MainMenuActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(MainMenuActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(MainMenuActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void removeLocations(ArrayList<String> locations){
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/deleteLocation";
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
                                    Toast.makeText(MainMenuActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(MainMenuActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(MainMenuActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void removeMessages(ArrayList<String> ids){
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/deleteMessages";
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
                                    Toast.makeText(MainMenuActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(MainMenuActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(MainMenuActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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

    public void postMessage(Message message){
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/messages";
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("title",message.getTitle());
            System.out.println("asjdnaskjdnaksjnd" + message.getLocation().getSSID());
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
                                    Toast.makeText(MainMenuActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(MainMenuActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(MainMenuActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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
                                        System.out.println(arr.get("ssid").toString());
                                        Location ssid = new Location(arr.get("ssid").toString());
                                        locations.add(ssid);
                                    }
                                }
                                if(str.equals("list")){
                                    Intent listLocationsIntent = new Intent(MainMenuActivity.this, ListLocationsActivity.class);
                                    listLocationsIntent.putExtra("serverIP", SERVER_IP);
                                    listLocationsIntent.putExtra("locations", locations);
                                    startActivity(listLocationsIntent);
                                }
                                else if (str.equals("post")){
                                    Intent postMessageIntent = new Intent(MainMenuActivity.this, PostMessageActivity.class);
                                    postMessageIntent.putExtra("serverIP", SERVER_IP);
                                    postMessageIntent.putExtra("locations", locations);
                                    startActivityForResult(postMessageIntent,POST_MESSAGE_REQUEST_CODE);
                                }
                                else if (str.equals("remove")){
                                    Intent removeLocationsIntent = new Intent(MainMenuActivity.this, RemovableItemListActivity.class);
                                    removeLocationsIntent.putExtra("serverIP", SERVER_IP);
                                    removeLocationsIntent.putExtra("locations", locations);
                                    startActivityForResult(removeLocationsIntent,REMOVE_LOCATIONS_REQUEST_CODE);
                                }
                            }
                            else{
                                try{
                                    Toast.makeText(MainMenuActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainMenuActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainMenuActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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

    public ArrayList<Location> returnLocations(ArrayList<Location> locations){
        return locations;
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("locationsUpdated",locations);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeMessages(){
        final ArrayList<Message> messages = new ArrayList<Message>();

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        String url = "http://" + SERVER_IP + "/userMessages";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.get("status").toString().equals("ok")){
                        for (int i = 0; i < response.getJSONArray("messages").length(); i++) {
                            JSONObject arr = (JSONObject) response.getJSONArray("messages").get(i);
                            Location location = new Location(arr.get("location").toString(),(Coordinates) null);
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
                        Intent unpostMessageIntent = new Intent(MainMenuActivity.this, UnpostMessageActivity.class);
                        unpostMessageIntent.putExtra("serverIP", SERVER_IP);
                        unpostMessageIntent.putExtra("messages", messages);
                        startActivityForResult(unpostMessageIntent,UNPOST_MESSAGE_REQUEST_CODE);
                    }
                    else{
                        try{
                            Toast.makeText(MainMenuActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainMenuActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainMenuActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
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
