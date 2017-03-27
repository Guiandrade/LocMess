package pt.ulisboa.tecnico.cmu.locmess;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListLocationsActivity extends AppCompatActivity {

    ArrayList<Location> locations = new ArrayList<Location>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_locations);
        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");

        ListView locationsListView = (ListView) findViewById(R.id.lvListLocations);

        List<HashMap<String, String>> listItems = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1,R.id.text2});

        for(Location loc : locations){
            HashMap<String, String> resultsMap = new HashMap<>();
            resultsMap.put("First Line", loc.getName());
            String coordinates = loc.getCoordinates().getLatitude() + ", " +
                    loc.getCoordinates().getLongitude() + ", " +
                    loc.getCoordinates().getRadius();
            resultsMap.put("Second Line", coordinates);
            listItems.add(resultsMap);
        }

        locationsListView.setAdapter(adapter);
    }
}