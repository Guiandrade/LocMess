package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RemovableItemListActivity extends AppCompatActivity {
    ArrayList<Location> locations = new ArrayList<Location>();
    ListView listView;
    HashMap<String,Boolean> checkedStatus = new HashMap<String,Boolean>();
    SimpleAdapter adapter;
    List<HashMap<String, String>> listItems;
    int bColor = Color.TRANSPARENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_removable_item_list);
        this.setTitle("Select locations to remove");

        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listView = (ListView) findViewById(R.id.lvRemovableItemList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        Drawable background = listView.getBackground();
        if (background instanceof ColorDrawable)
            bColor = ((ColorDrawable) background).getColor();

        listItems = new ArrayList<>();
        adapter = new SimpleAdapter(this, listItems, R.layout.delete_row_layout,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.tvLocationName,R.id.tvCoordinates});

        for(Location loc : locations){
            HashMap<String, String> resultsMap = new HashMap<>();
            checkedStatus.put(loc.getName(),false);
            resultsMap.put("First Line", loc.getName());
            String coordinates = loc.getCoordinates().getLatitude() + ", " +
                    loc.getCoordinates().getLongitude() + ", " +
                    loc.getCoordinates().getRadius();
            resultsMap.put("Second Line", coordinates);
            listItems.add(resultsMap);
        }

        listView.setAdapter(adapter);
        final Button bDeleteItems = (Button) findViewById(R.id.bDeleteItems);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
                String locName = item.get("First Line");

                if (checkedStatus.get(locName).equals(true)){
                    parent.getChildAt(position).setBackgroundColor(bColor);
                    checkedStatus.put(locName,false);
                }
                else{
                    parent.getChildAt(position).setBackgroundColor(Color.RED);
                    checkedStatus.put(locName,true);
                }
            }
        });

        bDeleteItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedItems();
            }
        });
    }

    public void deleteSelectedItems() {
        int i = checkedStatus.size()-1;
        for (HashMap.Entry<String, Boolean> entry : checkedStatus.entrySet()) {
            if(entry.getValue().equals(true)){
                listItems.remove(i);
                listView.getChildAt(i).setBackgroundColor(bColor);
                locations.remove(i);
            }
            i--;
        }
        checkedStatus.values().removeAll(Collections.singleton(true));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("locationsRemoved",locations);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("locationsRemoved",locations);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}