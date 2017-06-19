package wifipeertopeer.com.wifipeertopeer.PlainClasses;

import java.util.Random;

import wifipeertopeer.com.wifipeertopeer.Interfaces.UserTypeListener;

/**
 * Created by aawesh on 6/18/17.
 */

public class UserType {
    private UserTypeListener listener;

    // Constructor where listener events are ignored
    public UserType() {
        this.listener = null; // set null listener
    }

    // Assign the listener implementing events interface that will receive the events (passed in by the owner)
    public void setUserTypeListener(UserTypeListener listener) {
        this.listener = listener;
        findUserType();
    }

    //TODO Find the user type in the realtime, this will be an asynchronous task
    private void findUserType() {
        int id = 1; //0 for host and 1 for client change and run for testing purpose
        String userType = (id == 0)? DefaultValueConstants.HOST : DefaultValueConstants.CLIENT;

        listener.onUserDetected(userType);
    }


}
