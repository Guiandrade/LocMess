package pt.ulisboa.tecnico.cmu.locmess;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.List;

public class PostMessageActivity extends AppCompatActivity {

    ArrayList<Location> locations = new ArrayList<Location>();
    private String time = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_message);

        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");

        final EditText etTitle = (EditText) findViewById(R.id.etTitle);
        final EditText etMessage = (EditText) findViewById(R.id.etMessage);
        final Spinner sSelectLocation = (Spinner) findViewById(R.id.sSelectLocation);
        final Spinner sSelectPolicy = (Spinner) findViewById(R.id.sSelectPolicy);
        final Button bSetTime = (Button) findViewById(R.id.bSetTime);
        final Button bPostMessage = (Button) findViewById(R.id.bPostMessage);

        List<String> spinnerPolicyArray =  new ArrayList<String>();
        spinnerPolicyArray.add("SELECT POLICY");
        spinnerPolicyArray.add("Whitelist");
        spinnerPolicyArray.add("Blacklist");

        ArrayAdapter<String> policyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerPolicyArray);
        policyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSelectPolicy.setAdapter(policyAdapter);

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
                Log.d("TEST", sSelectPolicy.getSelectedItem().toString());
                Log.d("TEST", "" + time);
            }
        });

        bSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog timeDialog = new Dialog(PostMessageActivity.this);
                timeDialog.setContentView(R.layout.time_dialog_layout);
                timeDialog.show();

                final Button btpSetTime = (Button) timeDialog.findViewById(R.id.btpSetTime);
                final TimePicker tpSetTime = (TimePicker) timeDialog.findViewById(R.id.tpSetTime);

                btpSetTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        time = tpSetTime.getCurrentHour() + ":" + tpSetTime.getCurrentMinute();
                        timeDialog.cancel();
                    }
                });
            }
        });

    }
}
