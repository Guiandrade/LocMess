package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.cmu.locmess.Models.Location;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Models.TimeWindow;
import pt.ulisboa.tecnico.cmu.locmess.MyListViewAdapter;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class PostMessageActivity extends AppCompatActivity {

    ArrayList<Location> locations = new ArrayList<Location>();
    ArrayAdapter<String> adapterList;
    ArrayAdapter<String> adapterAutoComplete;
    Map<String,Boolean> checkedStatus = new LinkedHashMap<String,Boolean>();
    private ArrayList<String> keyPairsWhitelist = new ArrayList<String>();
    private ArrayList<String> keyPairsBlacklist = new ArrayList<String>();
    MyListViewAdapter adapter;
    ListView listView;
    List<String> myList;
    private String endTime = "";
    private String endDate = "";
    private String beginTime = "";
    private String beginDate = "";
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_message);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username","");

        final EditText etTitle = (EditText) findViewById(R.id.etTitle);
        final EditText etMessage = (EditText) findViewById(R.id.etMessage);
        final Spinner sSelectLocation = (Spinner) findViewById(R.id.sSelectLocation);
        final Button bSetTimeEnd = (Button) findViewById(R.id.bSetTimeEnd);
        final Button bSetDateEnd = (Button) findViewById(R.id.bSetDateEnd);
        final Button bSetTimeBegin = (Button) findViewById(R.id.bSetTimeBegin);
        final Button bSetDateBegin = (Button) findViewById(R.id.bSetDateBegin);
        final Button bKeyPairsWhiteList = (Button) findViewById(R.id.bKeyPairsWhiteList);
        final Button bKeyPairsBlackList = (Button) findViewById(R.id.bKeyPairsBlackList);
        final Button bPostMessage = (Button) findViewById(R.id.bPostMessage);
        final TextView tvTimeEnd = (TextView) findViewById(R.id.tvTimeEnd);
        final TextView tvDateEnd = (TextView) findViewById(R.id.tvDateEnd);
        final TextView tvTimeBegin = (TextView) findViewById(R.id.tvTimeBegin);
        final TextView tvDateBegin = (TextView) findViewById(R.id.tvDateBegin);

        tvTimeEnd.setText(setDefaultTime());
        endTime = setDefaultTime();
        tvDateEnd.setText(setDefaultDate("End"));
        endDate = setDefaultDate("End");

        tvTimeBegin.setText(setDefaultTime());
        beginTime = setDefaultTime();
        tvDateBegin.setText(setDefaultDate("Begin"));
        beginDate = setDefaultDate("Begin");

        List<String> spinnerLocationsArray =  new ArrayList<String>();
        spinnerLocationsArray.clear();
        spinnerLocationsArray.add("SELECT LOCATION");
        for(Location loc : locations){
            if(!(loc.getSSID() == null)){
                spinnerLocationsArray.add(loc.getSSID());
            }
            else{
                spinnerLocationsArray.add(loc.getName());
            }
        }

        ArrayAdapter<String> locationsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerLocationsArray);
        locationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSelectLocation.setAdapter(locationsAdapter);

        bPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString();
                String message = etMessage.getText().toString();
                String locationSelected = sSelectLocation.getSelectedItem().toString();
                Location location = null;
                for(Location loc : locations){
                    if(!(loc.getSSID() == null)) {
                        if ((loc.getSSID()).equals(locationSelected)) {
                            location = loc;
                        }
                    }
                    else{
                        if(loc.getName().equals(locationSelected)){
                            location = loc;
                        }
                    }
                }

                String owner = username;
                HashMap<String, Set<String>> whitelistKeyPairs = new HashMap<String, Set<String>>();
                HashMap<String, Set<String>> blacklistKeyPairs = new HashMap<String, Set<String>>();
                for (String entry: keyPairsWhitelist) {
                    if(whitelistKeyPairs.containsKey(entry.split("=")[0])){
                        whitelistKeyPairs.get(entry.split(" =")[0]).add(entry.split("= ")[1]);
                    }
                    else{
                        Set<String> val = new HashSet<String>();
                        val.add(entry.split("= ")[1]);
                        whitelistKeyPairs.put(entry.split(" =")[0],val);
                    }
                }

                for (String entry: keyPairsBlacklist) {
                    if(blacklistKeyPairs.containsKey(entry.split(" =")[0])){
                        blacklistKeyPairs.get(entry.split(" =")[0]).add(entry.split("= ")[1]);
                    }
                    else{
                        Set<String> val = new HashSet<String>();
                        val.add(entry.split("= ")[1]);
                        blacklistKeyPairs.put(entry.split(" =")[0],val);
                    }
                }
                TimeWindow timeWindow = new TimeWindow(Integer.parseInt(beginTime.split(":")[0]),
                        Integer.parseInt(beginTime.split(":")[1]),
                        Integer.parseInt(beginDate.split("/")[0]),
                        Integer.parseInt(beginDate.split("/")[1]),
                        Integer.parseInt(beginDate.split("/")[2]),
                        Integer.parseInt(endTime.split(":")[0]),
                        Integer.parseInt(endTime.split(":")[1]),
                        Integer.parseInt(endDate.split("/")[0]),
                        Integer.parseInt(endDate.split("/")[1]),
                        Integer.parseInt(endDate.split("/")[2]));

                Message msg = new Message(title,message,owner,location,whitelistKeyPairs,
                        blacklistKeyPairs,timeWindow);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("messagePosted",msg);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        bSetTimeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog timeDialog = new Dialog(PostMessageActivity.this);
                timeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                timeDialog.setContentView(R.layout.time_dialog_layout);
                timeDialog.show();

                final Button btpSetTime = (Button) timeDialog.findViewById(R.id.btpSetTime);
                final TimePicker tpSetTime = (TimePicker) timeDialog.findViewById(R.id.tpSetTime);

                btpSetTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvTimeEnd.setText(setTime(tpSetTime.getCurrentHour(),
                                tpSetTime.getCurrentMinute()));
                        endTime = tpSetTime.getCurrentHour() + ":" + tpSetTime.getCurrentMinute();
                        timeDialog.cancel();
                    }
                });
            }
        });

        bSetTimeBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog timeDialog = new Dialog(PostMessageActivity.this);
                timeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                timeDialog.setContentView(R.layout.time_dialog_layout);
                timeDialog.show();

                final Button btpSetTime = (Button) timeDialog.findViewById(R.id.btpSetTime);
                final TimePicker tpSetTime = (TimePicker) timeDialog.findViewById(R.id.tpSetTime);

                btpSetTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvTimeBegin.setText(setTime(tpSetTime.getCurrentHour(),
                                tpSetTime.getCurrentMinute()));
                        beginTime = tpSetTime.getCurrentHour() + ":" + tpSetTime.getCurrentMinute();
                        timeDialog.cancel();
                    }
                });
            }
        });

        bSetDateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dateDialog = new Dialog(PostMessageActivity.this);
                dateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dateDialog.setContentView(R.layout.date_dialog_layout);
                dateDialog.show();

                final Button bdpSetDate = (Button) dateDialog.findViewById(R.id.bdpSetDate);
                final DatePicker dpSetDate = (DatePicker) dateDialog.findViewById(R.id.dpSetDate);

                Date dateNow = new Date();
                dpSetDate.setMinDate(System.currentTimeMillis());

                bdpSetDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvDateEnd.setText(setDate(dpSetDate.getDayOfMonth(),dpSetDate.getMonth(),
                                dpSetDate.getYear()));
                        endDate = dpSetDate.getDayOfMonth() + "/" +
                                dpSetDate.getMonth() + "/" +
                                dpSetDate.getYear();
                        dateDialog.cancel();
                    }
                });
            }
        });

        bSetDateBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dateDialog = new Dialog(PostMessageActivity.this);
                dateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dateDialog.setContentView(R.layout.date_dialog_layout);
                dateDialog.show();

                final Button bdpSetDate = (Button) dateDialog.findViewById(R.id.bdpSetDate);
                final DatePicker dpSetDate = (DatePicker) dateDialog.findViewById(R.id.dpSetDate);

                Date dateNow = new Date();
                dpSetDate.setMinDate(System.currentTimeMillis());

                bdpSetDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvDateBegin.setText(setDate(dpSetDate.getDayOfMonth(),dpSetDate.getMonth(),
                                dpSetDate.getYear()));
                        beginDate = dpSetDate.getDayOfMonth() + "/" +
                                dpSetDate.getMonth() + "/" +
                                dpSetDate.getYear();
                        dateDialog.cancel();
                    }
                });
            }
        });

        bKeyPairsBlackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog keyPairsDialog = new Dialog(PostMessageActivity.this);
                keyPairsDialog.setContentView(R.layout.keypairs_dialog_layout);
                keyPairsDialog.show();

                final AutoCompleteTextView acKey = (AutoCompleteTextView) keyPairsDialog.findViewById(R.id.acKey);
                final EditText etPair = (EditText) keyPairsDialog.findViewById(R.id.etPair);
                final Button bAddPair = (Button) keyPairsDialog.findViewById(R.id.bAddPair);
                final Button bDeletePair = (Button) keyPairsDialog.findViewById(R.id.bDeletePair);

                myList = new  ArrayList<String>();
                listView = (ListView) keyPairsDialog.findViewById(R.id.lvPairs);

                adapter = new MyListViewAdapter(v.getContext(), R.layout.list_item, myList);
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
                                        PostMessageActivity.this);
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
                                                keyPairsBlacklist.remove(selecteditem);
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

                ArrayList<String> keys = new ArrayList<String>();
                keys.add("Club");
                keys.add("Favourite Colour");
                keys.add("Relationship Status");

                adapterAutoComplete = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_dropdown_item_1line, keys);
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
                        String pair = acKey.getText().toString() + " = " + etPair.getText().toString();
                        keyPairsBlacklist.add(pair);
                        myList.add(pair);
                        adapter.notifyDataSetChanged();
                        /*String res = "";
                        for(String str : keyPairsList){
                            res = res + "||" + str;
                            Log.d("ADD PAIR",res);
                        }
                        res = "";
                        Iterator it = checkedStatus.entrySet().iterator();
                        while(it.hasNext()){
                            Map.Entry entry = (LinkedHashMap.Entry)it.next();
                            res = res + "||" + entry.getKey() + "-" + entry.getValue();
                            Log.d("ADD",res);
                        }*/
                    }
                });
            }
        });

        bKeyPairsWhiteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog keyPairsDialog = new Dialog(PostMessageActivity.this);
                keyPairsDialog.setContentView(R.layout.keypairs_dialog_layout);
                keyPairsDialog.show();

                final AutoCompleteTextView acKey = (AutoCompleteTextView) keyPairsDialog.findViewById(R.id.acKey);
                final EditText etPair = (EditText) keyPairsDialog.findViewById(R.id.etPair);
                final Button bAddPair = (Button) keyPairsDialog.findViewById(R.id.bAddPair);
                final Button bDeletePair = (Button) keyPairsDialog.findViewById(R.id.bDeletePair);

                myList = new  ArrayList<String>();
                listView = (ListView) keyPairsDialog.findViewById(R.id.lvPairs);

                adapter = new MyListViewAdapter(v.getContext(), R.layout.list_item, myList);
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
                                        PostMessageActivity.this);
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
                                                keyPairsWhitelist.remove(selecteditem);
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

                ArrayList<String> keys = new ArrayList<String>();
                keys.add("Club");
                keys.add("Favourite Colour");
                keys.add("Relationship Status");

                adapterAutoComplete = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_dropdown_item_1line, keys);
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
                        String pair = acKey.getText().toString() + " = " + etPair.getText().toString();
                        keyPairsWhitelist.add(pair);
                        checkedStatus.put(pair,false);
                        adapterList.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public String setDefaultTime(){
        Date date = new Date();
        int hours = date.getHours();
        int minutes = date.getMinutes();
        String finalHour = "";
        String finalMinutes = "";

        if(String.valueOf(hours).length() == 1){
            finalHour = "0" + date.getHours();
        }
        else{
            finalHour = String.valueOf(date.getHours());
        }

        if(String.valueOf(minutes).length() == 1){
            finalMinutes = "0" + date.getMinutes();
        }
        else{
            finalMinutes = String.valueOf(date.getMinutes());
        }

        return finalHour + ":" + finalMinutes;
    }

    public String setDefaultDate(String name){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH,1);
        int year = cal.get(Calendar.YEAR);
        int month;
        if (name.equals("End")){
            month = cal.get(Calendar.MONTH) + 1;
        }
        else{
            month = cal.get(Calendar.MONTH);
        }

        int day = cal.get(Calendar.DAY_OF_MONTH);

        return "" + day + "/" + month + "/" + year;
    }

    public String setTime(int hours, int minutes){
        Date date = new Date();
        date.setHours(hours);
        date.setMinutes(minutes);
        String finalHour = "";
        String finalMinutes = "";
        if(String.valueOf(hours).length() == 1){
            finalHour = "0" + date.getHours();
        }
        else{
            finalHour = String.valueOf(date.getHours());
        }

        if(String.valueOf(minutes).length() == 1){
            finalMinutes = "0" + date.getMinutes();
        }
        else{
            finalMinutes = String.valueOf(date.getMinutes());
        }

        return finalHour + ":" + finalMinutes;
    }

    public String setDate(int day, int month, int year){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(year,month,day);
        int finalYear = cal.get(Calendar.YEAR);
        int finalMonth = cal.get(Calendar.MONTH) + 1;
        int finalDay = cal.get(Calendar.DAY_OF_MONTH);

        return "" + finalDay + "/" + finalMonth + "/" + finalYear;
    }
}
