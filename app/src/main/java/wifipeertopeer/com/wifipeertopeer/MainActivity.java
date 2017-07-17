package wifipeertopeer.com.wifipeertopeer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    private final String TAG = "MainActivity";

    private LocationManager locationManager;

    private double speed = 0.0;


    boolean once = true;

    String filename;
    private Button start_btn;
    private Button stop_btn;
    private TextView textView,speedView;
    private RelativeLayout rLayout;


    double latitude;
    double longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_btn = (Button) findViewById(R.id.start_btn);
        stop_btn = (Button) findViewById(R.id.stop_btn);
        textView = (TextView) findViewById(R.id.textView);
        speedView = (TextView) findViewById(R.id.speed);
        rLayout = (RelativeLayout)findViewById(R.id.mainLayout);


        start_btn.setOnClickListener(this);
        stop_btn.setOnClickListener(this);
        rLayout.setOnClickListener(this);
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


    private void promptToEnableGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
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
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            textView.setText(location.getLatitude() + "-----" + location.getLongitude());
            speedView.setText(String.valueOf(location.getSpeed()));

            Log.d(TAG,location.getLatitude() + "-----" + location.getLongitude());
            Log.d(TAG,location.getLatitude() + "-----" + location.getSpeed());

            speed = location.getSpeed();
            if(location.getSpeed()!= 0.0){
                writeToFile(String.valueOf(speed) + "\n");
            }
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
        File file = new File(folder, "walking_speed.txt");

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