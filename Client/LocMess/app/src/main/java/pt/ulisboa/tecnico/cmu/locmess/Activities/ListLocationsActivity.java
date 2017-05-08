package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class ListLocationsActivity extends AppCompatActivity {

    ArrayList<LocationModel> locations = new ArrayList<LocationModel>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_locations);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        locations = (ArrayList<LocationModel>) getIntent().getSerializableExtra("locations");
        this.setTitle("List Locations");

        ListView locationsListView = (ListView) findViewById(R.id.lvListLocations);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        List<HashMap<String, String>> listItems = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1,R.id.text2});

        for(LocationModel loc : locations){
            HashMap<String, String> resultsMap = new HashMap<>();
            if(!(loc.getSSID() == null)){
                resultsMap.put("First Line", loc.getSSID());
                resultsMap.put("Second Line", "");
                listItems.add(resultsMap);
            }
            else{
                resultsMap.put("First Line", loc.getName());
                String coordinates = "Lat: " + loc.getCoordinates().getLatitude() + ", Lon: " +
                        loc.getCoordinates().getLongitude();
                resultsMap.put("Second Line", coordinates);
                listItems.add(resultsMap);
            }
        }

        locationsListView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
