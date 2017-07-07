package wifipeertopeer.com.wifipeertopeer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by aawesh on 6/8/17.
 */

public class CustomBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        char[] chars = new char[MainActivity.packetSize/2]; //524288 = 1MB/2 = 1024 KB , 2621440 = 5MB/2
        Arrays.fill(chars,'a');
        String message = new String(chars);

        sendMessageRepeatedly(message,action);

    }

    private void sendMessageRepeatedly(final String message, final String action) {
        final Handler handler = new Handler();
        final Timer timer = new Timer();
        TimerTask sendMessageTask = new TimerTask() {

            private int count = 0;

            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            sendMessage(message,action);
                        } catch (Exception e) {
                            Log.d(MainActivity.TAG,"Failed to send message due to TimerTask");
                        }
                    }
                });

                if(++count == MainActivity.noOfPacket) {
                    timer.cancel();
                    Log.d(MainActivity.TAG, "All packets sent successfully");
                    MainActivity.enableButton(MainActivity.resetingButton);
                }
            }
        };
        timer.schedule(sendMessageTask, 0, 100); //execute in every 100 ms

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
