package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.R;

public class SSIDActivity extends AppCompatActivity {

    Spinner spSSID;
    BroadcastReceiver bReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssid);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle("Create location");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spSSID = (Spinner) findViewById(R.id.spSSID);
        final Button bAddSSID = (Button) findViewById(R.id.bAddSSID);
        getSSIDs();

        bAddSSID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationModel ssid = new LocationModel(spSSID.getSelectedItem().toString());
                Intent returnIntent = new Intent();
                returnIntent.putExtra("locationCreated",ssid);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }

    public void getSSIDs() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {

            final WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi.isWifiEnabled() == false) {
                wifi.setWifiEnabled(true);
            }

            bReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    List<ScanResult> results = wifi.getScanResults();
                    int size = results.size();
                    ArrayList<String> ssids = new ArrayList<String>();
                    for (int i = 0; i < size; i++) {
                        if(!ssids.contains("SSID: " + results.get(i).SSID)) {
                            ssids.add("SSID: " + results.get(i).SSID);
                        }
                    }
                    showSSIDs(ssids.toArray(new String[0]));
                }
            };

            this.registerReceiver(bReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            wifi.startScan();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
            }, 0);

            showSSIDs(new String[]{});
        }
    }

    private void showSSIDs(String[] ssids){
        this.unregisterReceiver(bReciever);
        ArrayAdapter<CharSequence> ssids_adapter = new ArrayAdapter<CharSequence>
                (this, android.R.layout.simple_spinner_dropdown_item,ssids);
        spSSID.setAdapter(ssids_adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
