package wifipeertopeer.com.wifipeertopeer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.peak.salut.Callbacks.SalutCallback;

/**
 * Created by aawesh on 6/8/17.
 */

public class CustomBroadcastReceiver extends BroadcastReceiver {

    private String speed;

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        //TODO do this in a background service because it is a long running tasks

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                speed = "Location = "+String.valueOf(location.getLatitude()) + String.valueOf(location.getLongitude());
                speed += "\n speed = " + (location.hasSpeed()?String.valueOf(location.getSpeed())+" m/s":"no speed");


                sendMessage(speed,action);
                Log.d(action + " is sedning : ", speed);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"Enable GPS", Toast.LENGTH_SHORT).show();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

    }

    private void sendMessage(String speed,String action) {

        if(action != null){

            Message myMessage = new Message();
            myMessage.description = speed ;

            if(action.equals(MainActivity.CLIENT)){
                MainActivity.network.sendToHost(myMessage, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(MainActivity.TAG, "Oh no! The data failed to send.");
                    }
                });
            }else if(action.equals(MainActivity.HOST)){
                Log.d(MainActivity.TAG, myMessage.description);

                MainActivity.network.sendToAllDevices(myMessage, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(MainActivity.TAG, "Oh no! The data failed to send.");
                    }
                });
            }

            MainActivity.updateCountandViews();
        }
    }
}
