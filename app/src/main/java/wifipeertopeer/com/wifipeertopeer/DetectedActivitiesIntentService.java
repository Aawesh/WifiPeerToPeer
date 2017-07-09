/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wifipeertopeer.com.wifipeertopeer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    public int walkingConfidence = 0;
    public int runningConfidence = 0;
    public int stillConfidence = 0;

    protected static final String TAG = "DetectedActivitiesIS";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        // Log each activity.
        Log.i(TAG, "activities detected");
        for (DetectedActivity da: detectedActivities) {
            if(da.getType() == Constants.MONITORED_ACTIVITIES[0]){ //still
                stillConfidence = da.getConfidence();
            }else if(da.getType() == Constants.MONITORED_ACTIVITIES[2]) { //walking
                walkingConfidence = da.getConfidence();
            }else if(da.getType() == Constants.MONITORED_ACTIVITIES[3]){ //running
                runningConfidence = da.getConfidence();
            }

            Log.d(TAG, "still_confidence: "+stillConfidence);
            Log.d(TAG, "walking_confidence: "+walkingConfidence);
            Log.d(TAG, "running_confidence: "+runningConfidence);
        }



        if(stillConfidence < 30){
            if(walkingConfidence >= 30 && runningConfidence < 30){
                CommunicationService.isPedestrianWalking = true;
                CommunicationService.isPedestrianMoving = true;

                CommunicationService.isPedestrianRunning = false;
                Log.i(TAG, "walking: "+ CommunicationService.isPedestrianWalking );

            }else if(runningConfidence >= 30 && walkingConfidence <30){
                CommunicationService.isPedestrianRunning = true;
                CommunicationService.isPedestrianMoving = true;

                CommunicationService.isPedestrianWalking = false;
                Log.i(TAG, "running: "+ CommunicationService.isPedestrianRunning );
            }
        }else{
            CommunicationService.isPedestrianMoving = false ;
            CommunicationService.isPedestrianWalking = false ;
            CommunicationService.isPedestrianRunning = false ;
            Log.i(TAG, "moving: "+ CommunicationService.isPedestrianMoving );
    }
    }
}
