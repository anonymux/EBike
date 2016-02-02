package com.mohnishkumar.ebike;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.text.DecimalFormat;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

public class MainActivity extends IOIOActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private double speed;
    private TextView textView_, textView2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView_ = (TextView) findViewById(R.id.Volts_In);
        textView2 = (TextView) findViewById(R.id.Speed);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        textView2.setText(String.valueOf(speed));
    }

    class MyIOIOLooper extends BaseIOIOLooper {
        AnalogInput input;
        private float volts=0;

        @Override
        public void setup() throws ConnectionLostException {
            Log.d("Setup Called", "Setup Called");
            input = ioio_.openAnalogInput(39);
            input.setBuffer(10);

           }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            for (int i=0; i<=10; i++){
                volts = volts + input.getVoltageBuffered();
            }
            volts = volts/10;
            final DecimalFormat twoDForm = new DecimalFormat("#.##");
            runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView_.setText(String.valueOf(twoDForm.format(volts)));
                    }
                });
                Thread.sleep(100);
        }

        @Override
        public void incompatible() {
        }

        @Override
        public void disconnected() {
            input.close();
        }
    }

    @Override
    protected IOIOLooper createIOIOLooper() {
        Log.d("Called","createIOIOLooper");
        return new MyIOIOLooper();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(500); // Update location twice every second

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.hasSpeed()) {
            speed = (double) location.getSpeed() * 3.6;
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            textView2.setText(String.valueOf(twoDForm.format(speed)));
        }
    }
}