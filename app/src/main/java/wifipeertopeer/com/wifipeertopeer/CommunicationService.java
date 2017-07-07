package wifipeertopeer.com.wifipeertopeer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.io.IOException;
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

    boolean isMoving = false;



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

    public long sentTime = 0;
    public long receivedTime = 0;
    public long t_delay = 0;

///////////////////////////////////////////////


    @Override
    public int onStartCommand(Intent intent,int flags, int startID){

        //initialize location usage classes
        AlertZone alertZone = new AlertZone();//initialize GPS as soon as possible
        driverLocation = new DriverLocation();

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
                .setSmallIcon(R.drawable.car);
        Notification notification = builder.build();
        nm.notify(R.string.service_started,notification);

        startForeground(ONGOING_NOTIFICATION_ID, notification);
        Toast.makeText(getBaseContext(), "Service Started", Toast.LENGTH_SHORT).show();

        establishCommunication(intent.getStringExtra("user"));

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
                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
            }
        });

        if(user.equalsIgnoreCase(Constants.CLIENT)){
            discoverServices();
        }else{
            // if pedestrian reaches the alert zone, then start network service if host is not created and send message. If not in the alert zone then stop sending messges.
            // Drive only sends if the pedestrian sends
            //alertZonelistener
            alertZone.setAlertZoneListener(new AlertZoneListener() {
                @Override
                public void onAlerZoneEntered(double t_p) {
                    if(!isHostCreated){
                        isHostCreated = true;
                        setupNetwork();
                        Log.d(TAG, "Host created");
                    }

                    //host sends t_p
                    Message message_t_p = new Message();
                    message_t_p.description = String.valueOf(t_p);

                    sentTime = System.currentTimeMillis();
                    network.sendToAllDevices(message_t_p, new SalutCallback() {
                        @Override
                        public void call() {
                            Log.e(MainActivity.TAG, "Oh no! The data failed to send.");
                        }
                    });

                }

                @Override
                public void onAlerZoneExited() {
                    Log.d(TAG,"Pedestrian is outside of alert zone");
//                    TODO 5 maybe later on
//                    do not send mesages
//                    maybe we can stop the host
                }
            });


        }

    }

    private void receiveMessage(Object o) {
        Log.d(TAG, "Received network data.");

        try {
            Message newMessage = LoganSquare.parse(o.toString(), Message.class);
            Log.d(TAG, "Message received: " + newMessage.description);


            if(network.isRunningAsHost){ //pedestrian
                String[] payload = newMessage.description.split("\\s+");
                t_c = Double.parseDouble(payload[0]);
                t_p = Double.parseDouble(payload[1]);
                v_c = Double.parseDouble(payload[2]);

                receivedTime = System.currentTimeMillis();
                t_delay = receivedTime-sentTime;

                runAlertAlgorithm(t_c,t_p,v_c,t_delay);

            }else{ //vehicle
                t_p = Double.parseDouble(newMessage.description);
                t_c = driverLocation.get_t_c();
                v_c = driverLocation.get_v_c();

                Message payload = new Message();
                payload.description = String.valueOf(t_c) + " " + String.valueOf(t_p) + " " + String.valueOf(v_c);


                MainActivity.network.sendToHost(payload, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(MainActivity.TAG, "Oh no! The data failed to send.");
                    }
                });
            }
        } catch (IOException ex) {
            Log.e(TAG, "Failed to parse network data.");
        }
    }

    private void runAlertAlgorithm(double tc, double tp, double vc, long delay) {

        //TODO getUserContext

//        case1:
        if(vc > 0 && isMoving && getProbabolityOfCollistion(t_c,t_p,v_c,delay) >= 0.9 ){
            Log.d(TAG, "Alert must be fired");
            fireAlert();

        }else{
            Log.d(TAG, "Pedestrian is safe according to algorithm");
        }


//        case2:
//        case3:
    }

    private void fireAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(dialogView);
        final AlertDialog alert = builder.create();
        alert.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
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
                    Toast.makeText(getApplicationContext(), "Device: " + network.foundDevices.get(0).instanceName + " found. ", Toast.LENGTH_LONG).show();

                    //for now registered with the first host. Don't know what happens of there are multipe host
                    network.registerWithHost(network.foundDevices.get(0), new SalutCallback() {
                        @Override
                        public void call() {
                            isRegisretedWithHost = true;
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopForeground(false);
        nm.cancel(R.string.service_started);

        //TODO stop all the communication related tasks 1.

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

    public double getProbabolityOfCollistion(double tc, double tp, double vc, long delay) {


        //vr is used as vc as of now

        double f = ((Constants.mew_k * Constants.mass * Constants.g) + ((Constants.row*Constants.a*Constants.cd*vc*vc)/2) + Constants.f0);
        double d_skid = (Constants.mass*vc*vc*f)/2;

        double t_skid = d_skid/vc;
        double t_all = tc-tp-t_delay-t_skid;

        LogNormalDistribution logNormalDistribution = new LogNormalDistribution();

        return logNormalDistribution.cumulativeProbability(t_all);
    }
}
