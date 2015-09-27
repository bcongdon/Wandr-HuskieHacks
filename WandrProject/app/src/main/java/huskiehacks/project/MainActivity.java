package huskiehacks.project;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceFragment;
import android.service.carrier.CarrierMessagingService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Button imOKButton,homeButton;
    TextView helloWorld;
    Location lastKnownCoords;
    Location homeCoords = new Location("");


    protected static final String TAG = "creating-and-monitoring-geofences";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The list of geofences used in this sample.
     */
    protected ArrayList<Geofence> mGeofenceList;

    /**
     * Used to keep track of whether geofences were added.
     */
    private boolean mGeofencesAdded;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    /**
     * Used to persist application state about whether geofences were added.
     */
    private SharedPreferences mSharedPreferences;

    // Buttons for kicking off the process of adding or removing geofences.
    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;

    public class homeOnClickListener implements Button.OnClickListener {
        //Do stuff
        public void onClick(View v){
            homeCoords = lastKnownCoords;
            helloWorld.setText("Home Coords are: " + homeCoords.getLongitude() + " " + homeCoords.getLatitude());
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(getString(R.string.homeLatitude), (float)homeCoords.getLatitude());
            editor.putFloat(getString(R.string.homeLongitute), (float)homeCoords.getLongitude());
            editor.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imOKButton = (Button) findViewById(R.id.button);
        helloWorld = (TextView) findViewById(R.id.textView);
        homeButton = (Button) findViewById(R.id.homeButton);
        imOKButton.setOnClickListener(this);
        homeButton.setOnClickListener(new homeOnClickListener());
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        homeCoords.setLatitude(sharedPref.getFloat(getString(R.string.homeLatitude), 0f));
        homeCoords.setLongitude(sharedPref.getFloat(getString(R.string.homeLongitute), 0f));
        helloWorld.setText("Saved home Coords are: " + homeCoords.getLongitude() + " " + homeCoords.getLatitude());



        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                lastKnownCoords = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };


        try{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            //helloWorld.setText("Successful");
        }
        catch (SecurityException e){
            helloWorld.setText("Fail");
        }




        Geofence fence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("geofence")

                .setCircularRegion(
                        homeCoords.getLatitude(),
                        homeCoords.getLongitude(),
                        100
                )
                .setExpirationDuration(10000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        mGeofenceList.add(fence);

        super.onCreate(savedInstanceState);



        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Retrieve an instance of the SharedPreferences object.
        String sharedName = "huskiehacks.project.SHARED_PREFERENCES_NAME";
        mSharedPreferences = getSharedPreferences(sharedName,
                MODE_PRIVATE);

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mGeofencesAdded = mSharedPreferences.getBoolean("huskiehacks.project.GEOFENCES_ADDED_KEY", false);


        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Open a settings menu
            Intent i = new Intent(this, MyPreferenceActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onClick(View v){
        helloWorld.setText("I am OKAY");
        if(helloWorld.getText().equals("I am OKAY")){
            helloWorld.setText("Test");
        }

    }

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        //Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        //Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        //Log.i(TAG, "Connected to GoogleApiClient");
    }

}


