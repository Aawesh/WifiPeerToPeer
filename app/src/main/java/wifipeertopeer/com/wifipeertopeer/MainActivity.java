package wifipeertopeer.com.wifipeertopeer;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    private final String TAG = "MainActivity";

    private LocationManager locationManager;


    boolean once = true;

    String filename;
    private Button initialize_btn;
    private Button start_btn;
    private Button stop_btn;
    private TextView textView;
    private RelativeLayout rLayout;


    double latitude;
    double longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.floating);

        initialize_btn = (Button) findViewById(R.id.initialize_btn);
        start_btn = (Button) findViewById(R.id.start_btn);
        stop_btn = (Button) findViewById(R.id.stop_btn);
        textView = (TextView) findViewById(R.id.textView);
       // rLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        showAlerDialog();


//        initialize_btn.setOnClickListener(this);
//        start_btn.setOnClickListener(this);
//        stop_btn.setOnClickListener(this);
        //rLayout.setOnClickListener(this);
    }

    private void showAlerDialog() {

        Dialog warning = new Dialog(this);
        warning.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        warning.setTitle("Alert !!!");
        warning.setContentView(getLayoutInflater().inflate(R.layout.image_layout
                , null));
        warning.show();

        /*
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Material_Dialog_Alert));

        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setMessage("Be careful !!!")
//                .setIcon(R.drawable.warning)
                .setTitle("Safety first")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                */


    }

    private void startLocationUpdates() {
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, this);
    }

    private void initializeGPS() {

        promptToEnableGPS();

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

        textView.setText("Initialized GPS");

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
            case R.id.initialize_btn:
                initializeGPS();
                break;
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
            if(once){
                textView.setText(location.getLatitude() + "-----" + location.getLongitude() + "---first");
                once = false;
            }else{
                textView.setText(location.getLatitude() + "-----" + location.getLongitude());
            }

            Log.d(TAG,location.getLatitude() + "-----" + location.getLongitude());
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