package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import java.util.List;
import java.util.Map;

public class MainMenuActivity extends AppCompatActivity {

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
        this.setTitle("Main Menu");

        final ImageButton bRemoveLocations = (ImageButton) findViewById(R.id.ibRemoveLocations);
        final ImageButton bListLocations = (ImageButton) findViewById(R.id.ibListLocations);
        final ImageButton bCreateLocations = (ImageButton) findViewById(R.id.ibCreateLocations);
        final Button bPostMessage = (Button) findViewById(R.id.ibPostMessage);
        final ImageButton bUnpostMessage = (ImageButton) findViewById(R.id.ibUnpostMessages);

        //Display back button on top
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bRemoveLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent removeLocationsIntent = new Intent(MainMenuActivity.this, RemovableItemListActivity.class);
                removeLocationsIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(removeLocationsIntent,REMOVE_LOCATIONS_REQUEST_CODE);
            }
        });

        bListLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listLocations();
            }
        });

        bCreateLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createLocationsIntent = new Intent(MainMenuActivity.this, CreateLocationActivity.class);
                createLocationsIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(createLocationsIntent,CREATE_LOCATIONS_REQUEST_CODE);
            }
        });

        bPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postMessageIntent = new Intent(MainMenuActivity.this, PostMessageActivity.class);
                postMessageIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(postMessageIntent,POST_MESSAGE_REQUEST_CODE);
            }
        });

        bUnpostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent unpostMessageIntent = new Intent(MainMenuActivity.this, UnpostMessageActivity.class);
                unpostMessageIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(unpostMessageIntent,UNPOST_MESSAGE_REQUEST_CODE);
            }
        });
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
                ArrayList<Location> locations = (ArrayList<Location>) data.getSerializableExtra("locationsRemoved");
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
                ArrayList<Message> messages = (ArrayList<Message>) data.getSerializableExtra("messagesRemoved");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                removeMessages(messages);
            }
        }
    }

    public void createLocation(Location location){
        /*RequestQueue queue;
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
        queue.add(jsObjRequest);*/
    }

    public void removeLocations(ArrayList<Location> locations){
        //to do
    }

    public void postMessage(Message message){
        //to do
    }

    public void removeMessages(ArrayList<Message> messages){
        //to do
    }

    public ArrayList<Location> listLocations (){
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
                                    Coordinates coordinates = new Coordinates(arr.get("latitude").toString().substring(0,7),arr.get("longitude").toString().substring(0,7));
                                    Location location = new Location(arr.get("location").toString(),coordinates);
                                    locations.add(location);
                                }
                                Intent listLocationsIntent = new Intent(MainMenuActivity.this, ListLocationsActivity.class);
                                listLocationsIntent.putExtra("serverIP", SERVER_IP);
                                listLocationsIntent.putExtra("locations", locations);
                                startActivity(listLocationsIntent);
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
}
