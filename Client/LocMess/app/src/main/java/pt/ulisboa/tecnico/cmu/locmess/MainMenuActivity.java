package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity {

    int CREATE_LOCATIONS_REQUEST_CODE = 1;
    int REMOVE_LOCATIONS_REQUEST_CODE = 2;
    ArrayList<Location> locations = new ArrayList<Location>();

    public ArrayList<Location> populateLocations (){
        Coordinates coordinates1 = new Coordinates("123","456","10m");
        Location location1 = new Location("Arco do Cego", coordinates1);

        Coordinates coordinates2 = new Coordinates("789","987","20m");
        Location location2 = new Location("Taguspark", coordinates2);

        Coordinates coordinates3 = new Coordinates("654","321","30m");
        Location location3 = new Location("Estádio da Luz", coordinates3);

        locations.add(location1);
        locations.add(location2);
        locations.add(location3);

        return locations;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        populateLocations();

        final ImageButton bRemoveLocations = (ImageButton) findViewById(R.id.ibRemoveLocations);
        final ImageButton bListLocations = (ImageButton) findViewById(R.id.ibListLocations);
        final ImageButton bCreateLocations = (ImageButton) findViewById(R.id.ibCreateLocations);

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
}
