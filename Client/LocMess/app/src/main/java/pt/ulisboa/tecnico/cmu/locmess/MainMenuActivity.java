package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;

public class MainMenuActivity extends AppCompatActivity {

    String SERVER_IP;
    String username;
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
        username = (String) getIntent().getSerializableExtra("username");
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
                removeLocationsIntent.putExtra("username", username);
                removeLocationsIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(removeLocationsIntent,REMOVE_LOCATIONS_REQUEST_CODE);
            }
        });

        bListLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listLocationsIntent = new Intent(MainMenuActivity.this, ListLocationsActivity.class);
                listLocationsIntent.putExtra("username", username);
                listLocationsIntent.putExtra("serverIP", SERVER_IP);
                startActivity(listLocationsIntent);
            }
        });

        bCreateLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createLocationsIntent = new Intent(MainMenuActivity.this, CreateLocationActivity.class);
                createLocationsIntent.putExtra("username", username);
                createLocationsIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(createLocationsIntent,CREATE_LOCATIONS_REQUEST_CODE);
            }
        });

        bPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postMessageIntent = new Intent(MainMenuActivity.this, PostMessageActivity.class);
                postMessageIntent.putExtra("username", username);
                postMessageIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(postMessageIntent,POST_MESSAGE_REQUEST_CODE);
            }
        });

        bUnpostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent unpostMessageIntent = new Intent(MainMenuActivity.this, UnpostMessageActivity.class);
                unpostMessageIntent.putExtra("username", username);
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
                username = (String) getIntent().getSerializableExtra("username");
                createLocation(location);
            }
        }

        else if (requestCode == REMOVE_LOCATIONS_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Location> locations = (ArrayList<Location>) data.getSerializableExtra("locationsRemoved");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                username = (String) getIntent().getSerializableExtra("username");
                removeLocations(locations);
            }
        }

        else if (requestCode == POST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Message message = (Message) data.getSerializableExtra("messagePosted");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                username = (String) getIntent().getSerializableExtra("username");
                postMessage(message);
            }
        }

        else if (requestCode == UNPOST_MESSAGE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Message> messages = (ArrayList<Message>) data.getSerializableExtra("messagesRemoved");
                SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
                username = (String) getIntent().getSerializableExtra("username");
                removeMessages(messages);
            }
        }
    }

    public void createLocation(Location location){
        //to do
    }

    public void removeLocations(ArrayList<Location> locations){
        //to do
    }

    public void postMessage(Message message){
        //to do
    }

    public void removeMessages(ArrayList<Message> messages){

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
