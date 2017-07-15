package wifipeertopeer.com.wifipeertopeer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements SalutDataCallback, View.OnClickListener {

    public static final String TAG = "SalutTestApp";
    public static final String CLIENT = "client";
    public static final String HOST = "host";
    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;
    public static Salut network;
    public Button hostingButton;
    public static Button sendingButton, resetingButton;
    public Button joiningButton;
    public TextView userView,receivedCountView;
    public EditText speedView,packetSizeView, noOfPacketView;
    public static TextView sentCountView,statusView;
    int id = 0;
    public static int sentCount = 0;
    public int receivedCount = 0;
    String user="Unknown";

    public static String speed;
    public static int packetSize,noOfPacket;

    private boolean isHostCreated = false;
    private boolean isRegisretedWithHost = false;

    private CustomBroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private RelativeLayout rLayout;

    public static TextView sentText;
    public static TextView receivedText;

    static boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        startApp();

    }

    private void startApp() {
        rLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        intentFilter = new IntentFilter();
        intentFilter.addAction("client");
        intentFilter.addAction("host");


        hostingButton = (Button) findViewById(R.id.hosting_button);
        joiningButton = (Button) findViewById(R.id.joining_button);
        sendingButton = (Button) findViewById(R.id.sending_button);
        resetingButton = (Button) findViewById(R.id.reseting_button);

        userView = (TextView) findViewById(R.id.userView);
        statusView = (TextView) findViewById(R.id.statusView);
        sentCountView = (TextView) findViewById(R.id.sentView);
        receivedCountView = (TextView) findViewById(R.id.receivedView);

        sentText = (TextView) findViewById(R.id.sent);
        receivedText = (TextView) findViewById(R.id.received);

        speedView = (EditText)findViewById(R.id.speed_editText);
        packetSizeView = (EditText)findViewById(R.id.packetSize_editText);
        noOfPacketView = (EditText)findViewById(R.id.noOfPacket_editText);


        hostingButton.setOnClickListener(this);
        joiningButton.setOnClickListener(this);
        sendingButton.setOnClickListener(this);
        resetingButton.setOnClickListener(this);
        rLayout.setOnClickListener(this);

        disableButton(sendingButton);
        //disableButton(resetingButton);


        //handle wifi
        if (Salut.isWiFiEnabled(getApplicationContext())) {
            Salut.disableWiFi(getApplicationContext());
            Salut.enableWiFi(getApplicationContext());
        }else{
            Salut.enableWiFi(getApplicationContext());
        }

        //BroadCastReceiver
        receiver = new CustomBroadcastReceiver();

        /*Create a data receiver object that will bind the callback
        with some instantiated object from our app. */
        dataReceiver = new SalutDataReceiver(this, this);

        id = new Random().nextInt(10) + 65;
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


    private void setupNetwork() {
        if (!network.isRunningAsHost) {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_LONG).show();
                    isHostCreated = true;
                    enableButton(sendingButton);
                    receivedText.setVisibility(View.GONE);
                    receivedCountView.setVisibility(View.GONE);
                }
            });

            userView.setText("Pedestrian DEVICE_" + id);
            statusView.setText("Service Started");

            disableButton(hostingButton);
            disableButton(joiningButton);

        }
    }



    private void discoverServices() {
        if (!network.isRunningAsHost && !network.isDiscovering) {
            statusView.setText("Started Dsicovering");
            userView.setText("Driver");
            network.discoverNetworkServices(new SalutCallback() {
                @Override
                public void call() {
                    Toast.makeText(getApplicationContext(), "Device: " + network.foundDevices.get(0).instanceName + " found. ", Toast.LENGTH_LONG).show();

                    statusView.setText("Host " + network.foundDevices.get(0).deviceName + " found");

                    //for now registered with the first host. Don't know what happens of there are multipe host
                    network.registerWithHost(network.foundDevices.get(0), new SalutCallback() {
                        @Override
                        public void call() {
                            isRegisretedWithHost = true;
                            Log.d(TAG, "We're now registered.");
                            statusView.setText("Registered with host: " + network.foundDevices.get(0).deviceName);
                            enableButton(sendingButton);
                            sendingButton.setText("Receive");
                            sentText.setVisibility(View.GONE);
                            sentCountView.setVisibility(View.GONE);
                        }
                    }, new SalutCallback() {
                        @Override
                        public void call() {
                            Log.d(TAG, "We failed to register.");
                        }
                    });
                }
            }, true);

            disableButton(hostingButton);
            disableButton(joiningButton);
        }
    }

    private void disableButton(Button b) {
        b.setAlpha(0.5f);
        b.setClickable(false);
    }

    public static void enableButton(Button b) {
        b.setAlpha(1f);
        b.setClickable(true);
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
            Log.d(TAG, newMessage.description.substring(0,2));  //See you on the other side!

            if(flag == false){
                receivedCount ++;
                receivedCountView.setText(String.valueOf(receivedCount));
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

        if (v.getId() == R.id.hosting_button) {
            setupNetwork();
        } else if (v.getId() == R.id.joining_button) {
            discoverServices();
        }else if(v.getId() == R.id.sending_button){
            send();
        }else if(v.getId() == R.id.reseting_button){
            saveAndReset();
        }
    }

    private void send() {
        flag = false;
//        disableButton(resetingButton);
        Log.d(TAG, "Sending packets");


        speed = speedView.getText().toString();
        String size  = packetSizeView.getText().toString();
        String no  = noOfPacketView.getText().toString();

        if(!size.equals("") && !speed.equals("") && !no.equals("")){
            packetSize = Integer.parseInt(size);
            noOfPacket = Integer.parseInt(no);

            disableButton(sendingButton);

            if(network.isRunningAsHost){
                user = HOST;
                Intent hostIntent = new Intent(HOST);
                sendBroadcast(hostIntent);
            }else{
                user = CLIENT;
                Intent hostIntent = new Intent(CLIENT);
                sendBroadcast(hostIntent);
            }
        }else{
            Toast.makeText(getBaseContext(),"Null input(s)",Toast.LENGTH_LONG).show();
        }


    }

    public static void updateCountandViews(){
        if(flag == false){
            sentCount ++;
            sentCountView.setText(String.valueOf(sentCount));
        }

    }

    public void saveAndReset(){
        flag = true;

        enableButton(MainActivity.sendingButton);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        String data = currentDateTimeString + ";" + user + ";" + speed  + ";" + noOfPacket + ";" + packetSize + ";" + sentCount + ";"
                + receivedCount + ";" + "\n";

        writeToFile(data);

        sentCount = 0;
        receivedCount = 0;

        sentCountView.setText("0");
        receivedCountView.setText("0");

        //disableButton(resetingButton);
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
        File file = new File(folder, "log.txt");

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


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
//
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