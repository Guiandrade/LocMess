package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity {

    int CREATE_LOCATIONS_REQUEST_CODE = 1;
    int REMOVE_LOCATIONS_REQUEST_CODE = 2;
    int POST_MESSAGE_REQUEST_CODE = 3;
    ArrayList<Location> locations = new ArrayList<Location>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");
        this.setTitle("Main Menu");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final ImageButton bRemoveLocations = (ImageButton) findViewById(R.id.ibRemoveLocations);
        final ImageButton bListLocations = (ImageButton) findViewById(R.id.ibListLocations);
        final ImageButton bCreateLocations = (ImageButton) findViewById(R.id.ibCreateLocations);
        final Button bPostMessage = (Button) findViewById(R.id.ibPostMessage);

        bRemoveLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent removeLocationsIntent = new Intent(MainMenuActivity.this, RemovableItemListActivity.class);
                removeLocationsIntent.putExtra("locations", locations);
                startActivityForResult(removeLocationsIntent,REMOVE_LOCATIONS_REQUEST_CODE);
            }
        });

        bListLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listLocationsIntent = new Intent(MainMenuActivity.this, ListLocationsActivity.class);
                listLocationsIntent.putExtra("locations", locations);
                startActivity(listLocationsIntent);
            }
        });

        bCreateLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createLocationsIntent = new Intent(MainMenuActivity.this, CreateLocationActivity.class);
                startActivityForResult(createLocationsIntent,CREATE_LOCATIONS_REQUEST_CODE);
            }
        });

        bPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postMessageIntent = new Intent(MainMenuActivity.this, PostMessageActivity.class);
                postMessageIntent.putExtra("locations", locations);
                startActivityForResult(postMessageIntent,POST_MESSAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CREATE_LOCATIONS_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Location location = (Location) data.getSerializableExtra("locationCreated");
                locations.add(location);
            }
        }

        else if (requestCode == REMOVE_LOCATIONS_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Location> locationsRemoved = (ArrayList<Location>) data.getSerializableExtra("locationsRemoved");
                locations = locationsRemoved;
            }
        }
    }

    @Override
    public void onBackPressed() {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("locationsUpdated",locations);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
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
