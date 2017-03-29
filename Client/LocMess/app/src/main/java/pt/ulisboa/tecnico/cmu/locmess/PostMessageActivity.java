package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
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
import java.util.Date;
import java.util.List;

public class PostMessageActivity extends AppCompatActivity {

    ArrayList<Location> locations = new ArrayList<Location>();
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
        final Spinner sSelectPolicy2 = (Spinner) findViewById(R.id.sSelectPolicy2);
        final Spinner sSelectPolicy1 = (Spinner) findViewById(R.id.sSelectPolicy1);
        final Button bSetTime = (Button) findViewById(R.id.bSetTime);
        final Button bSetDate = (Button) findViewById(R.id.bSetDate);
        final Button bKeyPairs2 = (Button) findViewById(R.id.bKeyPairs2);
        final Button bPostMessage = (Button) findViewById(R.id.bPostMessage);
        final TextView tvTime = (TextView) findViewById(R.id.tvTime);
        final TextView tvDate = (TextView) findViewById(R.id.tvDate);

        tvTime.setText(setDefaultTime());
        tvDate.setText(setDefaultDate());

        List<String> spinnerPolicyArray =  new ArrayList<String>();
        spinnerPolicyArray.add("SELECT POLICY");
        spinnerPolicyArray.add("Whitelist");
        spinnerPolicyArray.add("Blacklist");

        ArrayAdapter<String> policyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerPolicyArray);
        policyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSelectPolicy2.setAdapter(policyAdapter);
        sSelectPolicy1.setAdapter(policyAdapter);

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
                Log.d("TEST", etTitle.getText().toString());
                Log.d("TEST", sSelectPolicy2.getSelectedItem().toString());
                Log.d("TEST", "" + time);
                Log.d("TEST", "" + date);
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

        bKeyPairs2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog keyPairsDialog = new Dialog(PostMessageActivity.this);
                final EditText etKey = (EditText) keyPairsDialog.findViewById(R.id.etKey);
                final EditText etPair = (EditText) keyPairsDialog.findViewById(R.id.etPair);
                final Button bAddPair = (Button) keyPairsDialog.findViewById(R.id.bAddPair);
                final Button bDeletePair = (Button) keyPairsDialog.findViewById(R.id.bDeletePair);
                final Button bCheckPairs = (Button) keyPairsDialog.findViewById(R.id.bCheckPairs);
                final ListView lvPairs = (ListView) keyPairsDialog.findViewById(R.id.lvPairs);

                //keyPairsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                keyPairsDialog.setContentView(R.layout.keypairs_dialog_layout);
                keyPairsDialog.show();
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

        return hours + ":" + minutes;
    }

    public String setDefaultDate(){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
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
