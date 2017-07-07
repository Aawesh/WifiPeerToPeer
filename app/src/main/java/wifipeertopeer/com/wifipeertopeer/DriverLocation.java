package wifipeertopeer.com.wifipeertopeer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by aawesh on 7/7/17.
 */

public class DriverLocation implements LocationListener {

    protected static final String TAG = "AlertZone";
    protected LocationManager locationManager;

    protected GoogleApiClient mGoogleApiClient;
    AlertZoneListener listener;

    private double mLatitude = 0.0;
    private double mLongitude = 0.0;


    Location crossingLocation;
    double d_c = 0.0; //distance to crossing
    double t_c = 0.0; //time for pedestrian to reach the crossing
    double v_c = 0.0; //speed of vehicle


    public DriverLocation() {

        crossingLocation = new Location("crossingLocation");
        crossingLocation.setLatitude(Constants.CROSSINGLATITUDE);
        crossingLocation.setLongitude(Constants.CROSSINGLONGITUDE);

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


    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            d_c = location.distanceTo(crossingLocation);

            v_c = location.getSpeed();

            t_c = d_c/v_c;
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
}
