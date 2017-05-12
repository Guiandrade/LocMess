package pt.ulisboa.tecnico.cmu.locmess.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.location.Location;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.cmu.locmess.Models.Coordinates;
import pt.ulisboa.tecnico.cmu.locmess.Models.LocationModel;
import pt.ulisboa.tecnico.cmu.locmess.Models.Message;
import pt.ulisboa.tecnico.cmu.locmess.Utils.Http;
import pt.ulisboa.tecnico.cmu.locmess.Utils.PermissionUtils;
import pt.ulisboa.tecnico.cmu.locmess.Services.NotificationService;
import pt.ulisboa.tecnico.cmu.locmess.R;
import pt.ulisboa.tecnico.cmu.locmess.Security.SecurityHandler;

public class UserAreaActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, LocationListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static final long ZOOM_LEVEL = 18; // Street level
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    // Keys for storing activity state in the Bundle.
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    protected static final String TAG = "UserAreaActivity";
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    String SERVER_IP;
    String username;
    String token;
    int MAIN_ACTIVITY_REQUEST_CODE = 1;
    int POST_MESSAGE_REQUEST_CODE = 3;
    int USER_PROFILE_REQUEST_CODE = 5;
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private int initialMap = 0;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Circle> circles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        new Http().getKeys(this,false);

        mRequestingLocationUpdates = true;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);


        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        Intent serviceIntent = new Intent(UserAreaActivity.this, NotificationService.class);
        startService(serviceIntent);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");
        username = sharedPreferences.getString("username", "");

        final ImageButton ibGridMenu = (ImageButton) findViewById(R.id.ibGridMenu);
        final Button btPostMessage = (Button) findViewById(R.id.btPostMessage);
        final ImageButton ibUserProfile = (ImageButton) findViewById(R.id.ibUserProfile);
        final Button btLogout = (Button) findViewById(R.id.btLogout);

        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(UserAreaActivity.this, NotificationService.class));
                SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("token", "");
                editor.putString("username","");
                editor.putStringSet("Keys", null);
                editor.apply();
                Intent logoutIntent = new Intent(UserAreaActivity.this, LoginActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(logoutIntent);
            }
        });

        ibGridMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainMenuIntent = new Intent(UserAreaActivity.this, MainMenuActivity.class);
                mainMenuIntent.putExtra("serverIP", SERVER_IP);
                startActivityForResult(mainMenuIntent, MAIN_ACTIVITY_REQUEST_CODE);
            }
        });

        btPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Http().listLocations("post", v);
            }
        });

        ibUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Http().getKeys(v, true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //
            }
        }
        else if (requestCode == REQUEST_CHECK_SETTINGS) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    // Nothing to do. startLocationupdates() gets called in onResume again.
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    mRequestingLocationUpdates = false;
                    break;
            }
        }
        else if (requestCode == POST_MESSAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Message message = (Message) data.getSerializableExtra("messagePosted");
                new Http().postMessage(message,this);
            }
        } else if (requestCode == USER_PROFILE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                HashMap<String, Set<String>> addedKeyPairs = (HashMap<String, Set<String>>) data.getSerializableExtra("addedKeys");
                HashMap<String, Set<String>> deletedKeyPairs = (HashMap<String, Set<String>>) data.getSerializableExtra("deletedKeys");

                JSONObject res = new JSONObject();
                JSONObject json = new JSONObject();
                for (Map.Entry<String, Set<String>> entry : addedKeyPairs.entrySet()) {
                    try {
                        String key = entry.getKey();
                        Set<String> val = entry.getValue();
                        json.put(key, new JSONArray(val));
                    } catch (Exception e) {

                    }
                }
                try {
                    res.put("keys", json);
                    new Http().addKeys(res,this);
                } catch (Exception e) {

                }


                JSONObject res1 = new JSONObject();
                JSONObject json1 = new JSONObject();
                for (Map.Entry<String, Set<String>> entry : deletedKeyPairs.entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue());
                    for (String str : entry.getValue()) {
                        try {
                            String key = entry.getKey();
                            Set<String> val = entry.getValue();
                            json1.put(key, new JSONArray(val));
                        } catch (Exception e) {

                        }
                    }
                }
                try {
                    res1.put("keys", json1);
                    new Http().deletedKeyPairs(res1,this);
                } catch (Exception e) {

                }
                System.out.println("CHEGOU AQUIIIIIIIIIIIIIII");
                new Http().getKeys(this, false);
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission to access the location is missing.
                PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                        Manifest.permission.ACCESS_FINE_LOCATION, true);
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
        if (mRequestingLocationUpdates) {
            Log.i(TAG, "in onConnected(), starting location updates");
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        // update to current position
        if (initialMap == 0) {
            updateUI();
            initialMap = 1;
        }
    }


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void addMarker(LatLng point, String title, String radius) {
        String snippet = "Radius(m):" + radius;
        Marker newMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(title)
                .snippet(snippet));
        markers.add(newMarker);
    }

    @Override
    public void onMapLongClick(final LatLng point) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(UserAreaActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.add_map_location_layout, null);
        final EditText locationName = (EditText) mView.findViewById(R.id.NewLocationName);
        final EditText latitude = (EditText) mView.findViewById(R.id.Latitude);
        final EditText longitude = (EditText) mView.findViewById(R.id.Longitude);
        final EditText radius = (EditText) mView.findViewById(R.id.radius);

        // Complete with coordinates
        latitude.setText(String.valueOf(point.latitude));
        longitude.setText(String.valueOf(point.longitude));

        mBuilder.setPositiveButton(R.string.add_location_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                addMarker(point, String.valueOf(locationName.getText()), String.valueOf(radius.getText()));
                createLocation(new LocationModel(locationName.getText().toString(),new Coordinates(latitude.getText().toString(),
                        longitude.getText().toString(),radius.getText().toString())));
                String message = locationName.getText() + " was sucessfully saved with lat = "
                        + latitude.getText() + " and long = " + longitude.getText() + " and radius of " + radius.getText() + " m !";
                Toast.makeText(UserAreaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
        mBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(UserAreaActivity.this, "Cancelled Action!", Toast.LENGTH_SHORT).show();
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //hide map toolbar
        mMap.getUiSettings().setMapToolbarEnabled(false);

        String[] parts = marker.getSnippet().split(":");
        Integer radius = Integer.valueOf(parts[1]);
        // Clicked on marker, create circle with radius defined by user and show title
        marker.showInfoWindow();

        // If circle exists, remove it
        for (Circle circle : circles) {
            if (circle.getCenter().longitude == marker.getPosition().longitude && circle.getCenter().latitude == marker.getPosition().latitude) {
                circles.remove(circle);
                circle.remove();
                return false;
            }
        }
        // If not, create it
        Circle newCircle = mMap.addCircle(new CircleOptions()
                .center(marker.getPosition())
                .radius(radius)
                .strokeWidth(10)
                .strokeColor(R.color.colorPrimaryDark)
                .clickable(false));

        circles.add(newCircle);
        // maintain zoom to marker
        return false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Moved to current location!", Toast.LENGTH_SHORT).show();
        updateUI();
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        enableMyLocation();
        listLocations();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission will be asked
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Updates all UI fields.
     */
    public void updateUI() {
        if (mCurrentLocation != null) {
            LatLng myCoordinates = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myCoordinates));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
        }
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        if (ActivityCompat.checkSelfPermission(UserAreaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            PermissionUtils.requestPermission(UserAreaActivity.this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, UserAreaActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            status.startResolutionForResult(UserAreaActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(UserAreaActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        mRequestingLocationUpdates = false;
                }
            }
        });
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        updateUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    public ArrayList<LocationModel> listLocations (){
        final ArrayList<LocationModel> locations = new ArrayList<LocationModel>();
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String token = sharedPreferences.getString("token","");
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        SecurityHandler.allowAllSSL();
        String url = "https://" + new Http().getServerIp() + "/locations";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")) {
                                for (int i = 0; i < response.getJSONArray("locations").length(); i++) {
                                    JSONObject arr = (JSONObject) response.getJSONArray("locations").get(i);
                                    if(!arr.has("ssid")){
                                        addMarker(new LatLng(arr.getDouble("latitude"),arr.getDouble("longitude")),
                                                arr.get("location").toString(),arr.get("radius").toString());
                                    }
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{
                            Toast.makeText(UserAreaActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(UserAreaActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Basic " + token);
                return headers;
            }
        };
        queue.add(jsObjRequest);
        return locations;
    }

    public void createLocation(LocationModel location){
        RequestQueue queue;
        queue = Volley.newRequestQueue(this);
        SecurityHandler.allowAllSSL();
        String url = "https://" + new Http().getServerIp() + "/locations";
        JSONObject jsonBody = new JSONObject();
        if(location.getSSID() == null){
            try{
                jsonBody.put("location",location.getName());
                jsonBody.put("latitude",Double.parseDouble(location.getCoordinates().getLatitude()));
                jsonBody.put("longitude",Double.parseDouble(location.getCoordinates().getLongitude()));
                jsonBody.put("radius",Integer.parseInt(location.getCoordinates().getRadius()));
            }catch (Exception e){

            }
        }
        else{
            try{
                jsonBody.put("ssid",location.getSSID().split(" ")[1]);
            }catch (Exception e) {

            }
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.get("status").toString().equals("ok")){
                                // boa puto
                            }
                            else{
                                try{
                                    Toast.makeText(UserAreaActivity.this, "Status: "+ response.get("status"), Toast.LENGTH_LONG).show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try{
                            Toast.makeText(UserAreaActivity.this, "Error: "+ new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(UserAreaActivity.this, "Lost connection...", Toast.LENGTH_LONG).show();
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Basic " + token);
                return headers;
            }
        };
        queue.add(jsObjRequest);
    }
}
