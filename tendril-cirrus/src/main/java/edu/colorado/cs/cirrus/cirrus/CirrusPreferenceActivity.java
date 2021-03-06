package edu.colorado.cs.cirrus.cirrus;

import android.content.Context;
import edu.colorado.cs.cirrus.android.*;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class CirrusPreferenceActivity extends SherlockPreferenceActivity {

    private PreferenceUtils cirrusPrefs;

    private Preference setHomeLocPref;
    private Preference gpsFreqPref;
    private Preference smartHeatPref;
    private Preference logoutPref;


    private EditTextPreference homeTempPref;
    private EditTextPreference awayTempPref;
    private EditTextPreference radiusPref;

    private LocationManager locManager;
    private LocationListener locListener;

    private simplePreferenceClickListener myPreferenceClickListener;

    private double latitude = 0.0;
    private double longitude = 0.0;

    private String bestProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Setup layout
        addPreferencesFromResource(R.xml.preferences);

        //On click listener
        myPreferenceClickListener = new simplePreferenceClickListener();
        
        //Attach to the Preferences from the layout
        setHomeLocPref = (Preference) findPreference("setHomeLocPref");
        gpsFreqPref = (Preference) findPreference("gpsFrequency");
        smartHeatPref = (Preference) findPreference("smartHeat");
        homeTempPref = (EditTextPreference) findPreference("homeTemp");
        awayTempPref = (EditTextPreference) findPreference("awayTemp");
        radiusPref = (EditTextPreference) findPreference("gpsRadius");
        
        logoutPref = (Preference) findPreference("logout");

        //Set on click listener
        setHomeLocPref.setOnPreferenceClickListener(myPreferenceClickListener);
        gpsFreqPref.setOnPreferenceClickListener(myPreferenceClickListener);
        smartHeatPref.setOnPreferenceClickListener(myPreferenceClickListener);
        homeTempPref.setOnPreferenceClickListener(myPreferenceClickListener);
        awayTempPref.setOnPreferenceClickListener(myPreferenceClickListener);
        logoutPref.setOnPreferenceClickListener(myPreferenceClickListener);
        radiusPref.setOnPreferenceClickListener(myPreferenceClickListener);

        // Force homeTempPref and awayTempPref to take numbers
        EditText homeTempEditText = (EditText)homeTempPref.getEditText();
        homeTempEditText.setKeyListener(DigitsKeyListener.
                getInstance(false,true));

        EditText awayTempEditText = (EditText)awayTempPref.getEditText();
        awayTempEditText.setKeyListener(DigitsKeyListener.
                getInstance(false,true));
        
        EditText radiusEditText = (EditText)radiusPref.getEditText();
        radiusEditText.setKeyListener(DigitsKeyListener.
                getInstance(false,true));
        
        // Grab custom prefs
        cirrusPrefs = new PreferenceUtils(this);
        
        // Start Location stuff
        startLocationStuff();

    }


    @Override
    protected void onResume() {
        super.onResume();

        //Start polling for GPS information every 15 seconds
        locManager.requestLocationUpdates(
                bestProvider, 15000, 0, locListener);


    }

    @Override
    protected void onPause() {
        //Stop polling for GPS info
        locManager.removeUpdates(locListener);

        super.onPause();

    }



    class simplePreferenceClickListener implements OnPreferenceClickListener{

        public boolean onPreferenceClick(Preference preference) {

            if ( preference.equals(setHomeLocPref) ){
                Toast.makeText(getBaseContext(), "Setting home location",
                    Toast.LENGTH_SHORT).show();

                if( latitude == 0.0 || longitude == 0.0 ){
                    Toast.makeText(getBaseContext(), "Error getting GPS info",
                        Toast.LENGTH_LONG).show();
                    return false;
                }

                cirrusPrefs.setHomeLatitude( (float) latitude);
                cirrusPrefs.setHomeLongitude( (float) longitude);
    
                Toast.makeText(getBaseContext(), "Latitude: " + latitude
                        + ", Longitude: " + longitude,
                        Toast.LENGTH_SHORT).show();

            } else if (preference.equals(logoutPref)) {
                cirrusPrefs.removeAccessToken();
                TendrilTemplate.logOut();
                setResult(-1, null);
                finish();
            }

            return true;

        }

    }

    class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            //Just update latitude and longitude
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getBaseContext(), "GPS Disabled", 
                Toast.LENGTH_SHORT).show();

        }

        
        public void onProviderEnabled(String provider) {
            Toast.makeText(getBaseContext(), "GPS Enabled", 
                Toast.LENGTH_SHORT).show();

        }

        public void onStatusChanged(String provider, int status,
                Bundle extras) {


        }
    }
    
    private void startLocationStuff() {


        // Setup the location stuff
        locManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        locListener = new MyLocationListener();


        final Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        bestProvider = locManager.getBestProvider(criteria, true);

    }


}

