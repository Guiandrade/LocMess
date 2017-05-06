package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.cmu.locmess.MyListViewAdapter;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class UserProfileActivity extends AppCompatActivity {

    String SERVER_IP;
    String username;
    HashMap<String, Set<String>> keyPairs = new HashMap<String, Set<String>>();
    HashMap<String, Set<String>> addedKeyPairs = new HashMap<String, Set<String>>();
    HashMap<String, Set<String>> deletedKeyPairs = new HashMap<String, Set<String>>();
    ArrayList<String> allKeys = new ArrayList<String>();
    List<String> pairs = new ArrayList<String>();
    ListView lvKeyPairs;
    Map<String,Boolean> checkedStatus = new LinkedHashMap<String,Boolean>();
    ArrayAdapter<String> adapterAutoComplete;
    int bColor = Color.TRANSPARENT;
    MyListViewAdapter adapter;
    ListView listView;
    List<String> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle("User Profile");
        SERVER_IP = (String) getIntent().getSerializableExtra("serverIP");
        keyPairs = (HashMap<String, Set<String>>) getIntent().getSerializableExtra("keys");
        allKeys = (ArrayList<String>) getIntent().getSerializableExtra("allKeys");

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username","");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final TextView tvUsername = (TextView) findViewById(R.id.tvUsername);
        final Button bAddKeyPair = (Button) findViewById(R.id.bAddKeyPair);

        tvUsername.setText(username);
        myList = new  ArrayList<String>();

        for (Map.Entry<String, Set<String>> entry : keyPairs.entrySet()) {
            for (String str : entry.getValue()) {
                String pair = entry.getKey() + " = " + str;
                myList.add(pair);
            }
        }

        listView = (ListView) findViewById(R.id.lvKeyPairs);
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
                                UserProfileActivity.this);
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
                                        addToDeletedPairs(selecteditem);
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

        bAddKeyPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog keyPairsDialog = new Dialog(UserProfileActivity.this);
                keyPairsDialog.setContentView(R.layout.add_key_pair_layout);
                keyPairsDialog.show();

                final AutoCompleteTextView acKey = (AutoCompleteTextView) keyPairsDialog.findViewById(R.id.acKey);
                final EditText etPair = (EditText) keyPairsDialog.findViewById(R.id.etPair);
                final Button bAddPair = (Button) keyPairsDialog.findViewById(R.id.bAddPair);


                adapterAutoComplete = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_dropdown_item_1line, allKeys);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                acKey.setAdapter(adapterAutoComplete);

                acKey.setOnTouchListener(new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event){
                        acKey.showDropDown();
                        return false;
                    }
                });

                bAddPair.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key = acKey.getText().toString();
                        String value = etPair.getText().toString();
                        String pair = key + " = " + value;
                        addKey(key,value);
                        myList.add(pair);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(UserProfileActivity.this, "Added Key Pair: " + pair, Toast.LENGTH_LONG).show();
                        keyPairsDialog.cancel();
                    }
                });
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("addedKeys",addedKeyPairs);
            returnIntent.putExtra("deletedKeys",deletedKeyPairs);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addKey(String key, String value){
        if(keyPairs.containsKey(key)){
            keyPairs.get(key).add(value);
        }
        else{
            Set<String> val = new HashSet<String>();
            val.add(value);
            keyPairs.put(key,val);
        }

        if(addedKeyPairs.containsKey(key)){
            addedKeyPairs.get(key).add(value);
        }
        else{
            Set<String> val = new HashSet<String>();
            val.add(value);
            addedKeyPairs.put(key,val);
        }
    }

    public void addToDeletedPairs(String pair){
        String key = pair.split(" =")[0];
        String value = pair.split("= ")[1];

        if(deletedKeyPairs.containsKey(key)){
            deletedKeyPairs.get(key).add(value);
        }
        else{
            Set<String> val = new HashSet<String>();
            val.add(value);
            deletedKeyPairs.put(key,val);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
