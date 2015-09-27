package huskiehacks.project;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import com.google.android.gms.location.Geofence;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button imOKButton,homeButton;
    TextView helloWorld;
    Location lastKnownCoords;
    Location homeCoords = new Location("");

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
                        1.1,
                        1.1,
                        100
                )
                .setExpirationDuration(10000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

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
}
