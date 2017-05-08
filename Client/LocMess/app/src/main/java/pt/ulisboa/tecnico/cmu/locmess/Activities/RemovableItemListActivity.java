package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.widget.AbsListView.MultiChoiceModeListener;

import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.MyListViewAdapter;
import pt.ulisboa.tecnico.cmu.locmess.R;


import java.util.ArrayList;
import java.util.List;

public class RemovableItemListActivity extends AppCompatActivity {
    ArrayList<LocationModel> locations = new ArrayList<LocationModel>();
    ArrayList<String> locationsToRemove= new ArrayList<String>();
    ListView listView;
    MyListViewAdapter adapter;
    List<String> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_removable_item_list);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle("Select locations to remove");

        locations = (ArrayList<LocationModel>) getIntent().getSerializableExtra("locations");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        myList = new  ArrayList<String>();

        for(LocationModel loc : locations){
            if(!(loc.getSSID() == null)){
                myList.add(loc.getSSID()+" =  ");
            }
            else{
                String coordinates = "Lat: " + loc.getCoordinates().getLatitude() + ", Lon: " +
                        loc.getCoordinates().getLongitude();
                myList.add(loc.getName()+" = "+coordinates);
            }
        }

        listView = (ListView)  findViewById(R.id.lvRemovableItemList);
        // Pass value to MyListViewAdapter  Class
        adapter = new MyListViewAdapter(this, R.layout.list_item, myList);
        // Binds the Adapter to the ListView
        listView.setAdapter(adapter);
        // define Choice mode for multiple  delete
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                // TODO  Auto-generated method stub
                final int checkedCount  = listView.getCheckedItemCount();
                // Set the  CAB title according to total checked items
                mode.setTitle(checkedCount  + "  Selected");
                // Calls  toggleSelection method from ListViewAdapter Class
                adapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.delete_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final android.view.ActionMode mode, MenuItem item) {
                // TODO  Auto-generated method stub
                switch (item.getItemId()) {
                    case R.id.selectAll:
                        //
                        final int checkedCount = myList.size();
                        // If item  is already selected or checked then remove or
                        // unchecked  and again select all
                        adapter.removeSelection();
                        for (int i = 0; i < checkedCount; i++) {
                            listView.setItemChecked(i, true);
                            //  listviewadapter.toggleSelection(i);
                        }
                        // Set the  CAB title according to total checked items

                        // Calls  toggleSelection method from ListViewAdapter Class

                        // Count no.  of selected item and print it
                        mode.setTitle(checkedCount + "  Selected");
                        return true;
                    case R.id.delete:
                        // Add  dialog for confirmation to delete selected item
                        // record.
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                RemovableItemListActivity.this);
                        builder.setMessage("Do you  want to delete selected record(s)?");

                        builder.setNegativeButton("No", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO  Auto-generated method stub

                            }
                        });
                        builder.setPositiveButton("Yes", new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO  Auto-generated method stub
                                SparseBooleanArray selected = adapter
                                        .getSelectedIds();
                                for (int i = (selected.size() - 1); i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        String selecteditem = adapter
                                                .getItem(selected.keyAt(i));
                                        // Remove  selected items following the ids
                                        locationsToRemove.add(selecteditem.split(" = ")[0]);
                                        adapter.remove(selecteditem);
                                    }
                                }

                                // Close CAB
                                mode.finish();
                                selected.clear();

                            }
                        });
                        AlertDialog alert = builder.create();
                        //alert.setIcon(R.drawable.questionicon);// dialog  Icon
                        alert.setTitle("Confirmation"); // dialog  Title
                        alert.show();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {

            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("locationsRemoved",locationsToRemove);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}