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

/**
 * Created by aawesh on 6/18/17.
 */

public class AlertZone implements LocationListener {

    protected static final String TAG = "AlertZone";
    protected LocationManager locationManager;

    protected GoogleApiClient mGoogleApiClient;
    AlertZoneListener listener;

    private double mLatitude = 0.0;
    private double mLongitude = 0.0;

    private double crossingLatitude = 0.0;
    private double crossingLongitude = 0.0;

    //all the units are in meter
    private final double maximumWalkingSpeed = 1.34112;
    private final double maximumRunningSpeed = 12.5171;
    private final double maxAlertZoneDistanceFromCrossing = 7;

    Location crossingLocation;
    double d_p; //distance to crossing
    double t_p; //time for pedestrian to reach the crossing


    public AlertZone() {

        crossingLocation = new Location("crossingLocation");
        crossingLocation.setLatitude(crossingLatitude);
        crossingLocation.setLongitude(crossingLongitude);

        listener = null;
        locationManager = (LocationManager) UserSelectionActivity.context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(UserSelectionActivity.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserSelectionActivity.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        //initialize locaiton manager and all TODO 4
    }

    public void setAlertZoneListener(AlertZoneListener listener) {
        this.listener = listener;
    }


    @Override
    public void onLocationChanged(Location location) {
        //calculate the distance between
        if(location != null){

            d_p = location.distanceTo(crossingLocation);

            if(d_p <= maximumWalkingSpeed){ //pedestrian is in the alert zone
                t_p = d_p / maximumWalkingSpeed;
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
}
