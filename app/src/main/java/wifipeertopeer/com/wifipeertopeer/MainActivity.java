package wifipeertopeer.com.wifipeertopeer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    private final String TAG = "MainActivity";

    private LocationManager locationManager;

    boolean once = true;

    String filename;
    private Button start_btn;
    private Button stop_btn;
    private Button capture_btn;
    private TextView textView,speedView,distanceView;
    private RelativeLayout rLayout;

    private EditText speedInput;

    private Location alertZoneLocation;
    private double cLat = 44.313357;
    private double cLong = -96.784856;
//    private double cLat = 44.313081;
//    private double cLong = -96.776430;

    private double mLat,mLong;

    private double walkingSpeed = 0.0;
    private double distance = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_btn = (Button) findViewById(R.id.start_btn);
        stop_btn = (Button) findViewById(R.id.stop_btn);
        capture_btn = (Button) findViewById(R.id.capture_btn);
        textView = (TextView) findViewById(R.id.textView);
        speedView = (TextView) findViewById(R.id.speed);
        distanceView = (TextView) findViewById(R.id.distance);
        speedInput = (EditText) findViewById(R.id.editText);
        rLayout = (RelativeLayout)findViewById(R.id.mainLayout);


        start_btn.setOnClickListener(this);
        stop_btn.setOnClickListener(this);
        capture_btn.setOnClickListener(this);
        rLayout.setOnClickListener(this);

        alertZoneLocation = new Location("alertZoneLocation");
        alertZoneLocation.setLatitude(cLat);
        alertZoneLocation.setLongitude(cLong);
    }

    private void startLocationUpdates() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }




    @Override
    public void onClick(View v) {

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        switch (v.getId()) {
            case R.id.start_btn:
                startLocationUpdates();
                break;
            case R.id.stop_btn:
                locationManager.removeUpdates(this);
                break;
            case R.id.capture_btn:
                captureLocation();
                break;
        }
    }

    private void captureLocation() {

        walkingSpeed = Double.parseDouble(speedInput.getText().toString());
        System.out.println("distance = " + distance);
        System.out.println("walkingSpeed = " + walkingSpeed);

        final long time = Math.round((distance/walkingSpeed)*1000);

        final Handler handler = new Handler();
        final Timer timer = new Timer();
        TimerTask capture = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            String data = mLat+","+mLong+","+distance+","+time+","+walkingSpeed+"\n";
                            writeToFile(data);

                            Toast.makeText(MainActivity.this,"Captured",Toast.LENGTH_LONG).show();
                            Log.v(TAG,data);

                            timer.cancel();
                        } catch (Exception e) {
                            Log.d(TAG,"Failed to write due to TimerTask");
                        }
                    }
                });
            }
        };
        timer.schedule(capture, time);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLat = location.getLatitude();
            mLong = location.getLongitude();
            distance = location.distanceTo(alertZoneLocation);

            textView.setText(mLat + " ----- " + mLong);
            speedView.setText(String.valueOf(location.getSpeed()));
            distanceView.setText(String.valueOf(distance));

            Log.d(TAG,location.getLatitude() + " ----- " + location.getLongitude());
            Log.d(TAG,String.valueOf(distance));
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    public void writeToFile(String data) {

        String path = Environment.getExternalStorageDirectory() + File.separator + "wifip2p";
        // Create the folder.
        File folder = new File(path);
        if (!folder.exists()) {
            // Make it, if it doesn't exit
            folder.mkdirs();
        }

        // Create the file.
        File file = new File(folder, "energy.csv");

        try {
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);


            System.out.println(" Successful ");
            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}