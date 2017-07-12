package wifipeertopeer.com.wifipeertopeer;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

import org.apache.commons.math3.distribution.LogNormalDistribution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;

/**
 * Created by aawesh on 7/6/17.
 */

public class CommunicationService extends Service {

    NotificationManager nm = null;
    int ONGOING_NOTIFICATION_ID = 0;

    double t_p = 0.0; //distance to crossing
    double t_c = 0.0; //time for pedestrian to reach the crossing
    double v_c = 0.0; //speed of vehicle

    //more specific activity
    static boolean isPedestrianWalking = false;


    /////////////////////////////////////////////
    public static final String TAG = "SalutTestApp";
    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;
    public static Salut network;
    int id = 0;

    private boolean isHostCreated = false;
    private boolean isRegisretedWithHost = false;

    private AlertZone alertZone;
    private DriverLocation driverLocation;
//    public ActivityType activityType;

    public long sentTime = 0;
    public long receivedTime = 0;
    public long t_delay = 0;

    public String current_user;
    public String alertSignal;


    static int N = 3; //alert count default is 5
    static double P = 0.8; //probability threshold default 0.9

    static int alertCount = 0;

    double firstLatitude = 0.0;
    double firstLongitude= 0.0;



///////////////////////////////////////////////


    @Override
    public int onStartCommand(Intent intent,int flags, int startID){

        current_user = intent.getStringExtra("user");

        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this,UserSelectionActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentText("Tracking on progress")
                .setContentTitle("WiFiP2p")
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setTicker("Notification")
                //.setWhen(System.currentTimeMillis())
                .setSmallIcon(current_user.equalsIgnoreCase(Constants.CLIENT)?R.drawable.car:R.drawable.walking100);
        Notification notification = builder.build();
        nm.notify(R.string.service_started,notification);

        startForeground(ONGOING_NOTIFICATION_ID, notification);
        Toast.makeText(getBaseContext(), "Service Started", Toast.LENGTH_SHORT).show();

        establishCommunication(current_user);

        return START_STICKY;
    }

    private void establishCommunication(String user) {


        dataReceiver = new SalutDataReceiver((Activity) UserSelectionActivity.context, new SalutDataCallback() {
            @Override
            public void onDataReceived(Object o) {
                receiveMessage(o);
            }
        });

        id = new Random().nextInt(10) + 65;

        serviceData = new SalutServiceData("PedestrianService", 8888, "DEVICE_" + id);

        /*Create an instance of the Salut class, with all of the necessary data from before.
        * We'll also provide a callback just in case a device doesn't support WiFi Direct, which
        * Salut will tell us about before we start trying to use methods.*/
        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                // wiFiFailureDiag.show();
                // OR
//                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
            }
        });

        if(user.equalsIgnoreCase(Constants.CLIENT)){

            driverLocation = new DriverLocation();
            discoverServices();
        }else{

            alertZone = new AlertZone();//initialize GPS as soon as possible
//            activityType = new ActivityType(getApplicationContext()); //initialize activity as soon as possibl


            // if pedestrian reaches the alert zone, then start network service if host is not created and send message. If not in the alert zone then stop sending messges.
            // Drive only sends if the pedestrian sends
            //alertZonelistener
            alertZone.setAlertZoneListener(new AlertZoneListener() {
                @Override
                public void onAlerZoneEntered(double t_p) {
//                    Log.d(TAG,"Pedestrian is inside the alert zone");
                    //UserSelectionActivity.infoview.setText("inside alert zone");
                    if(!isHostCreated){
                        isHostCreated = true;
                        setupNetwork();
//                        Log.d(TAG, "Host created");
                    }

                    //host sends t_p
                    Message message_t_p = new Message();
                    message_t_p.description = String.valueOf(t_p)+" "+"no"; //t_p and no alert from here

                    sentTime = System.currentTimeMillis();

                    if(network.isRunningAsHost){
                        network.sendToAllDevices(message_t_p, new SalutCallback() {
                            @Override
                            public void call() {
//                                Log.e(TAG, "Oh no! The data failed to send.");
                            }
                        });
                    }


                }

                @Override
                public void onAlerZoneExited() {
//                    Log.d(TAG,"Pedestrian is outside of alert zone");
//                    UserSelectionActivity.infoview.setText("outside alert zone");

                    //todo hack
                    if(!isHostCreated){
                        isHostCreated = true;
                        setupNetwork();
//                        Log.d(TAG, "Host created");
                    }

//                    TODO 5 maybe later on
//                    do not send mesages
//                    maybe we can stop the host
                }
            });


        }

    }

    private void receiveMessage(Object o) {
//        Log.d(TAG, "Received network data.");

        try {
            Message newMessage = LoganSquare.parse(o.toString(), Message.class);
//            Log.d(TAG, "Message received: " + newMessage.description);


            if(network.isRunningAsHost){ //pedestrian
                String[] pedestrianPayload = newMessage.description.split("\\s+");
                t_c = Double.parseDouble(pedestrianPayload[0]);
                t_p = Double.parseDouble(pedestrianPayload[1]);
                v_c = Double.parseDouble(pedestrianPayload[2]);

                receivedTime = System.currentTimeMillis();
                t_delay = (receivedTime-sentTime);

                UserSelectionActivity.infoview.setText("tp = "+t_p+" tc = "+t_c+" vc = "+v_c);
                runAlertAlgorithm(t_c,t_p,v_c,t_delay);

            }else{ //vehicle
                if(current_user.equalsIgnoreCase(Constants.CLIENT)){
                    //check if we need t apert or not

                    String[] vehiclePayload = newMessage.description.split("\\s+");

                    t_p = Double.parseDouble(vehiclePayload[0]);
                    alertSignal= vehiclePayload[1];

                    t_c = driverLocation.get_t_c();
                    v_c = driverLocation.get_v_c();

                    //vehicle is alerted here
                    //save the data here
                    if(alertSignal.equalsIgnoreCase("yes")){
                        fireAlert();
                        writeToFile(v_c + "," + t_c + "," + driverLocation.getSpeed() + "," + N +"," + P + "," + driverLocation.getLatitude() + "," + driverLocation.getLongitude() + "\n");
                    }

                    Message payload = new Message();
                    payload.description = String.valueOf(t_c) + " " + String.valueOf(t_p) + " " + String.valueOf(v_c);

                    network.sendToHost(payload, new SalutCallback() {
                        @Override
                        public void call() {
//                            Log.e(TAG, "Oh no! The data failed to send.");
                        }
                    });
                }

            }
        } catch (IOException ex) {
//            Log.e(TAG, "Failed to parse network data.");
        }
    }

    private void runAlertAlgorithm(double tc, double tp, double vc, long delay) {

        //todo remove hardcoded values
        /*tc = 10;
        tp = 5.21;
        vc=7;
*/
//        Log.d(TAG, "vehicle speed: "+vc);
//        Log.d(TAG, "cumulative probability: "+getProbabolityOfCollistion(tc,tp,vc,delay));

        if((tp-tc) >= 5){
//            Log.d(TAG, "All the vehicles pass before the pedestrian reach the crossing");
            UserSelectionActivity.infoview3.setText("Vehicle will pass");
//            alertCount = 0;
        }
        else if(tc > tp){
            if(vc > 0.001 && isPedestrianWalking && getProbabolityOfCollistion(tc,tp,vc,delay) >= P ){
//                Log.d(TAG, "Alert must be fired");
                UserSelectionActivity.infoview3.setText("You are not safe. Be careful");
                fireAlert();
                alertCount ++;

                if(alertCount == 1){
                    firstLatitude = alertZone.getLatitude();
                    firstLongitude = alertZone.getLongitude();
                }

                if(alertCount == N){ //otherwise dont send anything because we are already sending data from alert zone entered
                    //host sends t_p
                    Message message_t_p = new Message();
                    message_t_p.description = String.valueOf(t_p)+" "+"yes"; //t_p and yes alert from here

                    sentTime = System.currentTimeMillis();

                    if(network.isRunningAsHost){
                        network.sendToAllDevices(message_t_p, new SalutCallback() {
                            @Override
                            public void call() {
//                                Log.e(TAG, "Oh no! The data failed to send.");
                            }
                        });
                    }
                     isPedestrianWalking = false;
                    UserSelectionActivity.toggle.toggle();

                    writeToFile(t_c + "," + t_p + "," + P + "," + firstLatitude + "," + firstLongitude + "," + alertZone.getLatitude() + "," + alertZone.getLongitude() + "\n");

                    //alertCount = 0; //reset it to 0 because once it reaches the N we want to notify the driver again of pedestrian still ignores the message
                }

            }else{
//                Log.d(TAG, "Pedestrian is safe according to algorithm");
                UserSelectionActivity.infoview3.setText("Safe acc to algorithm");

                //alertCount = 0;
            }
        }
    }

    private void fireAlert() {
//        Log.d(TAG, "Alert Fired");
        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.warning)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_poi))
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle("Be careful a vehicle is appeaching.")
//                .setContentIntent(notificationPendingIntent)
//                .setContentText(String.format(getString(R.string.notification), viewObject.getTitle()))
                .setDefaults(Notification.DEFAULT_ALL)
//                .setStyle(bigText)
                .setPriority(Notification.PRIORITY_HIGH);

        builder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(5, builder.build());
    }


    private void setupNetwork() {
        if (!network.isRunningAsHost) {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_LONG).show();
                }
            });

        }
    }



    private void discoverServices() {
        if (!network.isRunningAsHost && !network.isDiscovering) {
            network.discoverNetworkServices(new SalutCallback() {
                @Override
                public void call() {
                    //todo uncomment hack
                    //Toast.makeText(getApplicationContext(), "Device: " + network.foundDevices.get(0).instanceName + " found. ", Toast.LENGTH_LONG).show();

                    //for now registered with the first host. Don't know what happens of there are multipe host
                    network.registerWithHost(network.foundDevices.get(0), new SalutCallback() {
                        @Override
                        public void call() {
                            isRegisretedWithHost = true;
//                            Log.d(TAG, "We're now registered.");
                        }
                    }, new SalutCallback() {
                        @Override
                        public void call() {
//                            Log.d(TAG, "We failed to register.");
                        }
                    });
                }
            }, true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public double getProbabolityOfCollistion(double tc, double tp, double vc, long t_delay) {

        double delay = Double.parseDouble(String.valueOf(t_delay))/1000;


        double f = ((Constants.mew_k * Constants.mass * Constants.g) + ((Constants.row*Constants.a*Constants.cd*vc*vc)/2));
        double d_skid = (Constants.mass*vc*vc)/(f*2);


        double t_skid = (d_skid/vc);
        double t_all = (tc - tp - delay- t_skid);

    /*    Log.d(TAG,"t_delay: "+delay);
        Log.d(TAG,"f = "+String.valueOf(f));
        Log.d(TAG,"tc = "+String.valueOf(tc));
        Log.d(TAG,"tp = "+String.valueOf(tp));
        Log.d(TAG,"d_skid = " + String.valueOf(d_skid));
        Log.d(TAG,"t_skid = " + String.valueOf(t_skid));
        Log.d(TAG,"t_all = " + String.valueOf(t_all));*/



        LogNormalDistribution logNormalDistribution = new LogNormalDistribution();
        Double probablityOfCollision = logNormalDistribution.cumulativeProbability(t_all);

//        Log.d(TAG,String.valueOf(probablityOfCollision));
        UserSelectionActivity.infoview3.setText(String.valueOf(probablityOfCollision));

        return probablityOfCollision;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopForeground(false);
        nm.cancel(R.string.service_started);

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


        if(current_user.equalsIgnoreCase(Constants.HOST)){
//            activityType.removeActivityUpdates();
            alertZone.removeUpdates();

        }else{
            driverLocation.removeUpdates();
        }
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
        File file = new File(folder, "alertEngineData.txt");

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
//            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


}
