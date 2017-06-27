package wifipeertopeer.com.wifipeertopeer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    private final String TAG = "MainActivity";

    private LocationManager locationManager;


    boolean start = false;
    boolean once = true;

    String filename;
    private Button button;
    private EditText editText;
    private TextView textView;
    private RelativeLayout rLayout;


    double latitude;
    double longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button1);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        rLayout = (RelativeLayout)findViewById(R.id.mainLayout);


        button.setOnClickListener(this);
        rLayout.setOnClickListener(this);

        button.setClickable(false);
        button.setAlpha(0.5f);

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
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);

    }

    @Override
    public void onClick(View v) {

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        switch (v.getId()) {
            case R.id.button1:
                if (!start) {
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                    filename = editText.getText().toString();
                    filename = filename.equals("")?"location":filename;
                    start = true;
                    button.setText("Stop");
                }else{
                    locationManager.removeUpdates(this);
                    start = false ;
                    button.setText("Start");
                    textView.setText("Stopped listening GPS data");
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (once) {
                once = false;
                button.setClickable(true);
                button.setAlpha(1f);
                textView.setText("GPS connected");
            } else {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                String data = latitude + "," + longitude + "\n";

                Log.d(TAG,data);

                textView.setText(latitude + "\t\t\t"+longitude);
                writeToFile(data,filename);
            }
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        textView.setText("GPS state: ON");
        button.setAlpha(1f);
        button.setClickable(true);
    }

    @Override
    public void onProviderDisabled(String provider) {
        textView.setText("GPS state: OFF");
        button.setAlpha(0.5f);
        button.setClickable(false);

    }

    public void writeToFile(String data,String filename) {

        String path = Environment.getExternalStorageDirectory() + File.separator + "LocationData";
        // Create the folder.
        File folder = new File(path);
        if (!folder.exists()) {
            // Make it, if it doesn't exit
            folder.mkdirs();
        }

        // Create the file.
        File file = new File(folder, filename+".csv");

        try {
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);


            Log.d(TAG," Successful data write ");
            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}