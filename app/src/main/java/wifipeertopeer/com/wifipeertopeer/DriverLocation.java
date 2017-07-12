package wifipeertopeer.com.wifipeertopeer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by aawesh on 7/7/17.
 */

public class DriverLocation implements LocationListener {

    protected static final String TAG = "AlertZone";
    public LocationManager locationManager;

    Location crossingLocation;
    double d_c = 0.0; //distance to crossing
    double t_c = 0.0; //time for pedestrian to reach the crossing
    double v_c = 0.0; //speed of vehicle

    Location loc;

    float speed;


    public DriverLocation() {

        crossingLocation = new Location("crossingLocation");

        String[] loc = readFromFile().split("\\s+");

        crossingLocation.setLatitude(Double.parseDouble(loc[0]));
        crossingLocation.setLongitude(Double.parseDouble(loc[1]));

        locationManager = (LocationManager) UserSelectionActivity.context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(UserSelectionActivity.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserSelectionActivity.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            loc = location;

            d_c = location.distanceTo(crossingLocation);
            speed = location.getSpeed();
            v_c = speed == 0.0?0.001:speed;

            t_c = d_c/v_c;

        /*    Log.d(TAG, "distance from vehicle to crossig: "+d_c);
            Log.d(TAG, "velocity of a vehile: "+v_c);
            Log.d(TAG, "time for vehicle to reach the crossing: "+t_c);*/
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

    public Double get_t_c(){
        return t_c;
    }

    public Double get_v_c(){
        return v_c;
    }

    public String readFromFile(){
        String path = Environment.getExternalStorageDirectory() + File.separator + "wifip2p";

        File file = new File(path,"location_data.txt");

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            Log.e("read failure",e.toString());
        }

        return text.toString();
    }

    public void removeUpdates(){
        locationManager.removeUpdates(this);
//        Log.d(TAG, "driver location updates removed");
    }

    public double getLatitude(){
        return loc.getLatitude();
    }

    public double getLongitude(){
        return loc.getLongitude();
    }

    public float getSpeed(){
        return speed;
    }
}
