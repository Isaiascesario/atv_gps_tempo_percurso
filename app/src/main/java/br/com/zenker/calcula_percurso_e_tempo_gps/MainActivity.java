package br.com.zenker.calcula_percurso_e_tempo_gps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.SystemClock;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static android.net.Uri.parse;

public class MainActivity extends AppCompatActivity {

    private TextView distanceTextView;
    private Chronometer timeChronometer;
    private TextInputLayout searchTextInputLayout;
    private double latitude;
    private double longitude;
    private boolean isGpsActive = false;
    private float distance = 0;

    private Location oldLocation = new Location(Context.LOCATION_SERVICE);
//    private Location newLocation = new Location(Context.LOCATION_SERVICE);
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_PERMISSION_CODE_GPS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        distanceTextView = findViewById(R.id.distanceTextView);
        timeChronometer = findViewById(R.id.timeChronometer);
        searchTextInputLayout = findViewById(R.id.searchTextInputLayout);
        distanceTextView.setText("0.00 m");
//        oldLocation.setLatitude(latitude);
//        oldLocation.setLongitude(longitude);
//        newLocation.setLatitude(latitude);
//        newLocation.setLongitude(longitude);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((v) -> {
            //Uri uri = Uri.parse("geo:%f, %f?q=%s");
            String search = searchTextInputLayout.getEditText().toString();
            searchTextInputLayout.getEditText().setText("");
            Uri uri = parse(getString(R.string.uri_map, latitude, longitude, search));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                distance += location.distanceTo(oldLocation);
                oldLocation = location;
                distanceTextView.setText(getString(R.string.distance, distance));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void grantGpsPermission(View view) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, getString(R.string.permission_already_granted), Toast.LENGTH_SHORT).show();
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_CODE_GPS);
            Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_CODE_GPS)
        {
            if(grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, getString(R.string.no_gps_no_app), Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void activateGps(View view) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(isGpsActive) {
                Toast.makeText(this, getString(R.string.active_gps), Toast.LENGTH_SHORT).show();
            }
            else {
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 2000, 1, locationListener);
                Toast.makeText(this, getString(R.string.gps_on), Toast.LENGTH_SHORT).show();
                isGpsActive = true;
            }
        }
        else
        {
            Toast.makeText(this, getString(R.string.no_gps_no_app), Toast.LENGTH_SHORT).show();
        }
    }

    public void deactivateGps(View view) {
        if(isGpsActive) {
            locationManager.removeUpdates(locationListener);
            Toast.makeText(this, getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show();
            isGpsActive = false;
        }
        else {
            Toast.makeText(this, getString(R.string.gps_already_off), Toast.LENGTH_SHORT).show();
        }
    }

    public void startRoute(View view) {
        if(!isGpsActive) {
            Toast.makeText(this, getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show();
        }
        else {
            timeChronometer.setBase(SystemClock.elapsedRealtime());
            onChronometerTick(timeChronometer);
        }
    }

    public void onChronometerTick (Chronometer chronometer) {
        distanceTextView.setText(getString(R.string.distance, distance));
        timeChronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    public void stopRoute(View view) {
        timeChronometer.stop();
        locationManager.removeUpdates(locationListener);
    }
}
