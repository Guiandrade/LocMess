package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostMessageActivity extends AppCompatActivity {

    ArrayList<Location> locations = new ArrayList<Location>();
    ArrayAdapter<String> adapterList;
    ArrayAdapter<String> adapterAutoComplete;
    Map<String,Boolean> checkedStatus = new LinkedHashMap<String,Boolean>();
    private ArrayList<String> keyPairsWhitelist = new ArrayList<String>();
    private ArrayList<String> keyPairsBlacklist = new ArrayList<String>();
    int bColor = Color.TRANSPARENT;
    ListView lvPairs;
    private String time = "";
    private String date = "";


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

        final EditText etTitle = (EditText) findViewById(R.id.etTitle);
        final EditText etMessage = (EditText) findViewById(R.id.etMessage);
        final Spinner sSelectLocation = (Spinner) findViewById(R.id.sSelectLocation);
        final Button bSetTime = (Button) findViewById(R.id.bSetTime);
        final Button bSetDate = (Button) findViewById(R.id.bSetDate);
        final Button bKeyPairsWhiteList = (Button) findViewById(R.id.bKeyPairsWhiteList);
        final Button bKeyPairsBlackList = (Button) findViewById(R.id.bKeyPairsBlackList);
        final Button bPostMessage = (Button) findViewById(R.id.bPostMessage);
        final TextView tvTime = (TextView) findViewById(R.id.tvTime);
        final TextView tvDate = (TextView) findViewById(R.id.tvDate);

        tvTime.setText(setDefaultTime());
        time = setDefaultTime();
        tvDate.setText(setDefaultDate());
        date = setDefaultDate();

        List<String> spinnerLocationsArray =  new ArrayList<String>();
        spinnerLocationsArray.clear();
        spinnerLocationsArray.add("SELECT LOCATION");
        for(Location loc : locations){
            spinnerLocationsArray.add(loc.getName());
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
                    if(loc.getName().equals(locationSelected)){
                        location = loc;
                    }
                }

                String owner = "not done yet";
                HashMap<String, Set<String>> whitelistKeyPairs = new HashMap<String, Set<String>>();
                HashMap<String, Set<String>> blacklistKeyPairs = new HashMap<String, Set<String>>();
                for (String entry: keyPairsWhitelist) {
                    if(whitelistKeyPairs.containsKey(entry.split("=")[0])){
                        whitelistKeyPairs.get(entry.split("=")[0]).add(entry.split("=")[1]);
                    }
                    else{
                        Set<String> val = new HashSet<String>();
                        val.add(entry.split("=")[1]);
                        whitelistKeyPairs.put(entry.split("=")[0],val);
                    }
                }

                for (String entry: keyPairsBlacklist) {
                    if(blacklistKeyPairs.containsKey(entry.split("=")[0])){
                        blacklistKeyPairs.get(entry.split("=")[0]).add(entry.split("=")[1]);
                    }
                    else{
                        Set<String> val = new HashSet<String>();
                        val.add(entry.split("=")[1]);
                        blacklistKeyPairs.put(entry.split("=")[0],val);
                    }
                }
                TimeWindow timeWindow = new TimeWindow(Integer.parseInt(time.split(":")[0]),
                        Integer.parseInt(time.split(":")[1]),
                        Integer.parseInt(date.split("/")[0]),
                        Integer.parseInt(date.split("/")[1]),
                        Integer.parseInt(date.split("/")[2]));

                Message msg = new Message(title,message,owner,location,whitelistKeyPairs,
                        blacklistKeyPairs,timeWindow);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("messagePosted",msg);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        bSetTime.setOnClickListener(new View.OnClickListener() {
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
                        tvTime.setText(setTime(tpSetTime.getCurrentHour(),
                                tpSetTime.getCurrentMinute()));
                        time = tpSetTime.getCurrentHour() + ":" + tpSetTime.getCurrentMinute();
                        timeDialog.cancel();
                    }
                });
            }
        });

        bSetDate.setOnClickListener(new View.OnClickListener() {
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
                        tvDate.setText(setDate(dpSetDate.getDayOfMonth(),dpSetDate.getMonth(),
                                dpSetDate.getYear()));
                        date = dpSetDate.getDayOfMonth() + "/" +
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
                lvPairs = (ListView) keyPairsDialog.findViewById(R.id.lvPairs);

                ArrayList<String> keys = new ArrayList<String>();
                keys.add("Club");
                keys.add("Favourite Colour");
                keys.add("Relationship Status");

                adapterAutoComplete = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_dropdown_item_1line, keys);
                acKey.setAdapter(adapterAutoComplete);

                adapterList = new ArrayAdapter<String>(v.getContext(),android.R.layout.simple_list_item_1, keyPairsBlacklist);
                lvPairs.setAdapter(adapterList);

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
                        checkedStatus.put(pair,false);
                        adapterList.notifyDataSetChanged();
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

                lvPairs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String keyPair = (String) parent.getItemAtPosition(position);

                        if (checkedStatus.get(keyPair).equals(true)){
                            parent.getChildAt(position).setBackgroundColor(bColor);
                            checkedStatus.put(keyPair,false);
                        }
                        else{
                            parent.getChildAt(position).setBackgroundColor(Color.RED);
                            checkedStatus.put(keyPair,true);
                        }
                        String res = "";
                        /*for (LinkedHashMap.Entry<String, Boolean> entry : checkedStatus.entrySet()) {
                            res = res + "||" + entry.getKey() + "-" + entry.getValue();
                            Log.d("CLICK LIST",res);
                        }*/
                    }
                });

                bDeletePair.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*String res = "";
                        for (LinkedHashMap.Entry<String, Boolean> entry : checkedStatus.entrySet()) {
                            res = res + "||" + entry.getKey() + "-" + entry.getValue();
                            Log.d("DELETE BEGIN",res);
                        }*/


                        int i = 0;
                        int e = 0;
                        for (LinkedHashMap.Entry<String, Boolean> entry : checkedStatus.entrySet()) {
                            if(entry.getValue().equals(true)){
                                keyPairsBlacklist.remove(i-e);
                                lvPairs.getChildAt(i).setBackgroundColor(bColor);
                                e++;
                            }
                            i++;
                        }
                        checkedStatus.values().removeAll(Collections.singleton(true));
                        adapterList.notifyDataSetChanged();

                        /*res = "";
                        for (LinkedHashMap.Entry<String, Boolean> entry : checkedStatus.entrySet()) {
                            res = res + "||" + entry.getKey() + "-" + entry.getValue();
                            Log.d("DELETE END",res);
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
                lvPairs = (ListView) keyPairsDialog.findViewById(R.id.lvPairs);

                ArrayList<String> keys = new ArrayList<String>();
                keys.add("Club");
                keys.add("Favourite Colour");
                keys.add("Relationship Status");

                adapterAutoComplete = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_dropdown_item_1line, keys);
                acKey.setAdapter(adapterAutoComplete);

                adapterList = new ArrayAdapter<String>(v.getContext(),android.R.layout.simple_list_item_1, keyPairsWhitelist);
                lvPairs.setAdapter(adapterList);

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

                lvPairs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String keyPair = (String) parent.getItemAtPosition(position);

                        if (checkedStatus.get(keyPair).equals(true)){
                            parent.getChildAt(position).setBackgroundColor(bColor);
                            checkedStatus.put(keyPair,false);
                        }
                        else{
                            parent.getChildAt(position).setBackgroundColor(Color.RED);
                            checkedStatus.put(keyPair,true);
                        }
                    }
                });

                bDeletePair.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = 0;
                        int e = 0;
                        for (LinkedHashMap.Entry<String, Boolean> entry : checkedStatus.entrySet()) {
                            if(entry.getValue().equals(true)){
                                keyPairsWhitelist.remove(i-e);
                                lvPairs.getChildAt(i).setBackgroundColor(bColor);
                                e++;
                            }
                            i++;
                        }
                        checkedStatus.values().removeAll(Collections.singleton(true));
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

    public String setDefaultDate(){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH,1);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
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
