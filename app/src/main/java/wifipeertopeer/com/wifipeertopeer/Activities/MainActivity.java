package wifipeertopeer.com.wifipeertopeer.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.io.IOException;
import java.util.Random;

import wifipeertopeer.com.wifipeertopeer.BroadcastReceivers.CustomBroadcastReceiver;
import wifipeertopeer.com.wifipeertopeer.Interfaces.UserTypeListener;
import wifipeertopeer.com.wifipeertopeer.PlainClasses.DefaultValueConstants;
import wifipeertopeer.com.wifipeertopeer.PlainClasses.Message;
import wifipeertopeer.com.wifipeertopeer.PlainClasses.UserType;
import wifipeertopeer.com.wifipeertopeer.R;


public class MainActivity extends AppCompatActivity implements SalutDataCallback, View.OnClickListener {

    public static final String TAG = "SalutTestApp";

    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;
    public static Salut network;
    int id = 0;

    private boolean isHostCreated = false;
    private boolean isRegisretedWithHost = false;

    private CustomBroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private RelativeLayout rLayout;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        handleWifi();

        initializeVariables();

        registerOnClickListeners();



        UserType userType = new UserType();
        userType.setUserTypeListener(new UserTypeListener() {
            @Override
            public void onUserDetected(String user) {
                if(user.equals(DefaultValueConstants.HOST)){
                    setupNetwork();
                }else{
                    discoverServices();

                }
                tv.setText(user);
                System.out.println("user = " + user);
            }
        });


    }


    private void setupNetwork() {
        if (!network.isRunningAsHost) {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_LONG).show();
                    isHostCreated = true;
                    send();
                }
            });
        }
    }



    private void discoverServices() {
        if (!network.isRunningAsHost && !network.isDiscovering) {
            network.discoverNetworkServices(new SalutCallback() {
                @Override
                public void call() {
                    Toast.makeText(getApplicationContext(), "Device: " + network.foundDevices.get(0).instanceName + " found. ", Toast.LENGTH_LONG).show();


                    //for now registered with the first host. Don't know what happens of there are multipe host
                    network.registerWithHost(network.foundDevices.get(0), new SalutCallback() {
                        @Override
                        public void call() {
                            isRegisretedWithHost = true;
//                            send();
                            Log.d(TAG, "We're now registered.");
                        }
                    }, new SalutCallback() {
                        @Override
                        public void call() {
                            Log.d(TAG, "We failed to register.");
                        }
                    });
                }
            }, true);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /*Create a callback where we will actually process the data.*/
    @Override
    public void onDataReceived(Object o) {
        Log.d(TAG, "Received network data.");

        try {
            Message newMessage = LoganSquare.parse(o.toString(), Message.class);
            Log.d(TAG, newMessage.description.substring(0,2));

            if(network.isRunningAsHost){
              //TODO
                System.out.println("newMessage.description = " + newMessage.description + " from client");

            }else{
                //TODO
                System.out.println("newMessage.description = " + newMessage.description + " from host");

            }
        } catch (IOException ex) {
            Log.e(TAG, "Failed to parse network data.");
        }
    }

    @Override
    public void onClick(View v) {

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        if (!Salut.isWiFiEnabled(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Please enable WiFi first.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void send() {
        Log.d(TAG, "Sending packets");

            if(network.isRunningAsHost){
                Intent hostIntent = new Intent(DefaultValueConstants.HOST);
                sendBroadcast(hostIntent);
            }else{
                Intent hostIntent = new Intent(DefaultValueConstants.CLIENT);
                sendBroadcast(hostIntent);
            }



    }

    private void registerOnClickListeners() {
        rLayout.setOnClickListener(this);
    }

    private void initializeVariables() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(DefaultValueConstants.HOST);
        intentFilter.addAction(DefaultValueConstants.CLIENT);

        rLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        tv = (TextView)findViewById(R.id.textView);

        receiver = new CustomBroadcastReceiver();

        /*Create a data receiver object that will bind the callback
        with some instantiated object from our app. */
        dataReceiver = new SalutDataReceiver(this, this);

        id = new Random().nextInt(10) + 70;
        System.out.println("id ============== " + id);

        /*Populate the details for our awesome service. */
        serviceData = new SalutServiceData("PedestrianService", 8888, "DEVICE_" + id);

        /*Create an instance of the Salut class, with all of the necessary data from before.
        * We'll also provide a callback just in case a device doesn't support WiFi Direct, which
        * Salut will tell us about before we start trying to use methods.*/
        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                // wiFiFailureDiag.show(); TODO
                // OR
                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
            }
        });
    }

    private void handleWifi() {
        //handle wifi TODO optimize this
        if (Salut.isWiFiEnabled(getApplicationContext())) {
            Salut.disableWiFi(getApplicationContext());
            Salut.enableWiFi(getApplicationContext());
        }else{
            Salut.enableWiFi(getApplicationContext());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (network.isRunningAsHost) {
            if (isHostCreated) {
                network.stopNetworkService(true);
                isHostCreated = false;
            }
        } else {
            if(isRegisretedWithHost) {
                network.unregisterClient(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}