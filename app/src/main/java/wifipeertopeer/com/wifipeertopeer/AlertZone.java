package wifipeertopeer.com.wifipeertopeer;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by aawesh on 6/18/17.
 */

public class AlertZone implements LocationListener {

    protected static final String TAG = "AlertZone";
    protected LocationManager locationManager;

    AlertZoneListener listener;

    //all the units are in meter
    Location crossingLocation;
    Location loc;
    double d_p; //distance to crossing
    double t_p; //time for pedestrian to reach the crossing


    public AlertZone() {

        crossingLocation = new Location("crossingLocation");

        String[] loc = readFromFile().split("\\s+");

        crossingLocation.setLatitude(Double.parseDouble(loc[0]));
        crossingLocation.setLongitude(Double.parseDouble(loc[1]));

        listener = null;
        locationManager = (LocationManager) UserSelectionActivity.context.getSystemService(Context.LOCATION_SERVICE);

    }

    public void setAlertZoneListener(AlertZoneListener listener) {
        this.listener = listener;
        if (ActivityCompat.checkSelfPermission(UserSelectionActivity.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserSelectionActivity.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        //calculate the distance between
        if(location != null){

            loc = location;
            d_p = location.distanceTo(crossingLocation);
//            UserSelectionActivity.infoview2.setText(String.valueOf(d_p));
            Log.d(TAG,"ped to cross distance: " + d_p);

            if(d_p <= Constants.MAX_ALERT_ZONE_DISTANCE_FROM_CROSSING){ //pedestrian is in the alert zone
                double max_speed = 0.001; // if the user is still then set the minimum velocity so that we can specify that the user is not walking at all.
                if(CommunicationService.isPedestrianWalking){
                    max_speed = Constants.MAXIMUM_WALKING_SPEED;
                    Log.d(TAG, "user is walking");
                    UserSelectionActivity.infoview2.setText("walking");
                }else if(!CommunicationService.isPedestrianMoving){
                    UserSelectionActivity.infoview2.setText("still");
                }

                t_p = d_p / max_speed;

                listener.onAlerZoneEntered(t_p);
            }else{
                listener.onAlerZoneExited();
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

    public void removeUpdates(){
        locationManager.removeUpdates(this);
        Log.d(TAG, "pedestrian location updates removed");

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


    public double getLatitude(){
        return loc.getLatitude();
    }

    public double getLongitude(){
        return loc.getLongitude();
    }
}
