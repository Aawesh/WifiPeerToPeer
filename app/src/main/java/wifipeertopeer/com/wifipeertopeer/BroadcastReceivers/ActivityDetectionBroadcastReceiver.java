package wifipeertopeer.com.wifipeertopeer.BroadcastReceivers;

/**
 * Created by aawesh on 6/21/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.DetectedActivity;

import wifipeertopeer.com.wifipeertopeer.Interfaces.UserTypeListener;
import wifipeertopeer.com.wifipeertopeer.PlainClasses.Constants;

import java.util.ArrayList;

/**
 * Receiver for intents sent by DetectedActivitiesIntentService via a sendBroadcast().
 * Receives a list of one or more DetectedActivity objects associated with the current state of
 * the device.
 */
public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

    private UserTypeListener listener;

    public void setUserTypeListener(UserTypeListener listener){
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<DetectedActivity> updatedActivities =
                intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);

        int drivingConidenceLevel=0;
        int walkingConidenceLevel=0;

        //TODO detect user
        for (DetectedActivity activity : updatedActivities) {

            if(activity.getType() == Constants.MONITORED_ACTIVITIES[2]){
                walkingConidenceLevel = activity.getConfidence();
            }else if(activity.getType() == Constants.MONITORED_ACTIVITIES[5]) {
                drivingConidenceLevel = activity.getConfidence();
            }
        }

        String user = (drivingConidenceLevel > walkingConidenceLevel) ? Constants.CLIENT : Constants.HOST;
        System.out.println(" detected====== " + user);

        listener.onUserDetected(user);

    }
}