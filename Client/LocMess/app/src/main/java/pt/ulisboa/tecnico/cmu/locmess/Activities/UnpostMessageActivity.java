package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Adapters.MyListViewAdapter;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class UnpostMessageActivity extends AppCompatActivity {

    ArrayList<Message> messages = new ArrayList<Message>();
    ArrayList<String> idsToRemove = new ArrayList<String>();
    ListView listView;
    Map<String,Boolean> checkedStatus = new LinkedHashMap<String,Boolean>();
    MyListViewAdapter adapter;
    List<String> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_removable_item_list);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle("Select locations to remove");

        messages = (ArrayList<Message>) getIntent().getSerializableExtra("messages");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        myList = new ArrayList<String>();

        for (Message msg : messages) {
            String coordinates = "Loc: " + msg.getLocation().getName() + ", Eding Date/Time: " +
                    msg.getTimeWindow().getEndingDay() + "/" +
                    msg.getTimeWindow().getEndingMonth() + "/" +
                    msg.getTimeWindow().getEndingYear() + " " +
                    msg.getTimeWindow().getEndingHour() + ":" +
                    msg.getTimeWindow().getEndingMinutes() + ", Id: " + msg.getId();
            myList.add(msg.getTitle() + " = " + coordinates);
        }

        listView = (ListView) findViewById(R.id.lvRemovableItemList);
        // Pass value to MyListViewAdapter  Class
        adapter = new MyListViewAdapter(this, R.layout.list_item, myList);
        // Binds the Adapter to the ListView
        listView.setAdapter(adapter);
        // define Choice mode for multiple  delete
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                // TODO  Auto-generated method stub
                final int checkedCount = listView.getCheckedItemCount();
                // Set the  CAB title according to total checked items
                mode.setTitle(checkedCount + "  Selected");
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
                                UnpostMessageActivity.this);
                        builder.setMessage("Do you  want to delete selected record(s)?");

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO  Auto-generated method stub

                            }
                        });
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

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
                                        idsToRemove.add(selecteditem.split("Id: ")[1]);
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
            returnIntent.putExtra("ids",idsToRemove);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
