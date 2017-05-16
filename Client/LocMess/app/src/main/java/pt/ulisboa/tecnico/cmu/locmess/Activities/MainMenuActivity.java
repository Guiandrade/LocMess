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
import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Models.TimeWindow;
import pt.ulisboa.tecnico.cmu.locmess.R;
import pt.ulisboa.tecnico.cmu.locmess.Security.SecurityHandler;
import pt.ulisboa.tecnico.cmu.locmess.Utils.Http;

public class MainMenuActivity extends AppCompatActivity {

    String token;
    String SERVER_IP;
    int CREATE_LOCATIONS_REQUEST_CODE = 1;
    int REMOVE_LOCATIONS_REQUEST_CODE = 2;
    int POST_MESSAGE_REQUEST_CODE = 3;
    int UNPOST_MESSAGE_REQUEST_CODE = 4;
    ArrayList<LocationModel> locations = new ArrayList<LocationModel>();
    ArrayList<Message> messages = new ArrayList<Message>();
    private Http http;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        http= new Http(this.getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
                http.listLocations("list",v);
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
               http.listLocations("post",v);
            }
        });

        bUnpostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                http.removeMessages(v);
            }
        });

        bRemoveLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                http.listLocations("remove",v);
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
                LocationModel location = (LocationModel) data.getSerializableExtra("locationCreated");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                http.createLocation(location,this);
            }
        }

        else if (requestCode == REMOVE_LOCATIONS_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<String> locations = (ArrayList<String>) data.getSerializableExtra("locationsRemoved");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                http.removeLocations(locations,this);
            }
        }

        else if (requestCode == POST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Message message = (Message) data.getSerializableExtra("messagePosted");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                http.postMessage(message,this);
            }
        }

        else if (requestCode == UNPOST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<String> ids = (ArrayList<String>) data.getSerializableExtra("ids");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                http.removeMessages(ids,this);
            }
        }
    }


    public ArrayList<LocationModel> returnLocations(ArrayList<LocationModel> locations){
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
