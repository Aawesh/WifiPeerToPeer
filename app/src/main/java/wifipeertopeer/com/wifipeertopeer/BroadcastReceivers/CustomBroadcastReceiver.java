package wifipeertopeer.com.wifipeertopeer.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;

import java.util.Arrays;

import wifipeertopeer.com.wifipeertopeer.Activities.MainActivity;
import wifipeertopeer.com.wifipeertopeer.PlainClasses.DefaultValueConstants;
import wifipeertopeer.com.wifipeertopeer.PlainClasses.Message;

/**
 * Created by aawesh on 6/8/17.
 */

public class CustomBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        char[] chars = new char[10/2]; //524288 = 1MB/2 = 1024 KB , 2621440 = 5MB/2
        Arrays.fill(chars,'a');
        String message = new String(chars);

        sendMessage(message,action);

    }

    public static void sendMessage(String message,String action) {

        if(action != null){

            Message myMessage = new Message();
            myMessage.description = message ;

            if(action.equals(DefaultValueConstants.CLIENT)){
                MainActivity.network.sendToHost(myMessage, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(MainActivity.TAG, "Oh no! The data failed to send.");
                    }
                });
            }else if(action.equals(DefaultValueConstants.HOST)){
                Log.d(MainActivity.TAG, myMessage.description);

                MainActivity.network.sendToAllDevices(myMessage, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e(MainActivity.TAG, "Oh no! The data failed to send.");
                    }
                });
            }
        }
    }
}
