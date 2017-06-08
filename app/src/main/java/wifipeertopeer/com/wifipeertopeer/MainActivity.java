package wifipeertopeer.com.wifipeertopeer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements SalutDataCallback, View.OnClickListener{

    public static final String TAG = "SalutTestApp";
    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;
    public Salut network;
    public Button hostingBtn;
    public Button discoverBtn;
    public TextView tv1,tv2,tv3;
    int id = 0;

    private boolean hostCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hostingBtn = (Button) findViewById(R.id.hosting_button);
        discoverBtn = (Button) findViewById(R.id.discover_services);

        tv1 = (TextView)findViewById(R.id.textView);
        tv2 = (TextView)findViewById(R.id.textView2);
        tv3 = (TextView)findViewById(R.id.textView3);

        hostingBtn.setOnClickListener(this);
        discoverBtn.setOnClickListener(this);


        /*Create a data receiver object that will bind the callback
        with some instantiated object from our app. */
        dataReceiver = new SalutDataReceiver(this, this);

        id = new Random().nextInt(10)+65;
        /*Populate the details for our awesome service. */
        serviceData = new SalutServiceData("PedestrianService", 8888, "DEVICE_"+id);

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

    private void setupNetwork() {
        if(!network.isRunningAsHost){
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_LONG).show();
                    hostCreated = true;
                }
            });

            tv1.setText("Pedestrian DEVICE_"+id);
            tv2.setText("Service Started");

            hostingBtn.setText("Stop Service");
            discoverBtn.setAlpha(0.5f);
            discoverBtn.setClickable(false);
        }
        else {
            tv1.setText("Pedestrian");
            tv2.setText("Service Stoppped");
            tv3.setText("");
            network.stopNetworkService(false);
            hostingBtn.setText("Start Service");
            discoverBtn.setAlpha(1f);
            discoverBtn.setClickable(true);
            if(hostCreated) {
                network.stopNetworkService(true);
                hostCreated = false;
            }
        }
    }

    private void discoverServices()
    {
        if(!network.isRunningAsHost && !network.isDiscovering)
        {
            tv2.setText("Started Dsicovering");
            network.discoverNetworkServices(new SalutCallback() {
                @Override
                public void call() {
                    Toast.makeText(getApplicationContext(), "Device: " + network.foundDevices.get(0).instanceName + " found. ", Toast.LENGTH_SHORT).show();

                    tv1.setText("Driver");
                    tv2.setText("Host " + network.foundDevices.get(0).deviceName + " found");

                    //TODO remove debug
                    for(int i = 0;i<network.foundDevices.size();i++){
                        Log.d("Host_"+i+1,network.foundDevices.get(i).instanceName);
                    }

                    //for now registered with the first host. Don't know what happens of there are multipe host
                    network.registerWithHost(network.foundDevices.get(0), new SalutCallback() {
                        @Override
                        public void call() {
                            Log.d(TAG, "We're now registered.");
                            tv2.setText("Registered with host: "+network.foundDevices.get(0).deviceName);
                            //send message
                            Message myMessage = new Message();
                            myMessage.description = "Hello pedestrian !!!"+ " from driver: " + network.thisDevice.deviceName;

                            network.sendToHost(myMessage, new SalutCallback() {
                                @Override
                                public void call() {
                                    Log.e(TAG, "Oh no! The data failed to send.");
                                }
                            });
//                            tv3.setText("Message sent from Client: "+myMessage.description);
                        }
                    }, new SalutCallback() {
                        @Override
                        public void call() {
                            Log.d(TAG, "We failed to register.");
                        }
                    });
                }
            }, true);
            discoverBtn.setText("Stop Discovery");
            hostingBtn.setAlpha(0.5f);
            hostingBtn.setClickable(false);
        }
        else
        {
            if(!network.isRunningAsHost){
                network.stopServiceDiscovery(true);
            }
            discoverBtn.setText("Discover Services");
            hostingBtn.setAlpha(1f);
            hostingBtn.setClickable(false);

            tv1.setText("Driver");
            tv2.setText("Stopped Discovering");
            tv3.setText("");
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
        //Data Is Received
        Log.d(TAG, "Received network data.");
        try
        {
            Message newMessage = LoganSquare.parse(o.toString(), Message.class);
            Log.d(TAG, newMessage.description);  //See you on the other side!
            tv3.setText(newMessage.description);
            //Do other stuff with data.
        }
        catch (IOException ex)
        {
            Log.e(TAG, "Failed to parse network data.");
        }

        //this means host has recieved at least one message from clients which means that at least one client is registered with host
        if(network.isRunningAsHost){ // if ishost or issome boolean true for first receipt from client then host can start sending data regardless of any message received from the clients TODO
            Message myMessage = new Message();
            myMessage.description = "Hello driver !!! from pedestrian: "+network.thisDevice.deviceName;
            Log.d(TAG,myMessage.description);

            network.sendToAllDevices(myMessage, new SalutCallback() {
                @Override
                public void call() {
                    Log.e(TAG, "Oh no! The data failed to send.");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {

        if(!Salut.isWiFiEnabled(getApplicationContext()))
        {
            Toast.makeText(getApplicationContext(), "Please enable WiFi first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(v.getId() == R.id.hosting_button)
        {
            setupNetwork();
        }
        else if(v.getId() == R.id.discover_services)
        {
            discoverServices();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(network.isRunningAsHost)
            network.stopNetworkService(true);
        else
            network.unregisterClient(false);
    }
}