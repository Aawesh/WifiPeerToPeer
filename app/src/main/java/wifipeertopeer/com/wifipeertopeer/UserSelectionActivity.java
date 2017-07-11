package wifipeertopeer.com.wifipeertopeer;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.peak.salut.Salut;

/**
 * Created by aawesh on 7/6/17.
 */

public class UserSelectionActivity extends AppCompatActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {
    Button walkingButton;
    Button drivingButton;
    Button exitButton;
    Button setButton;

    ToggleButton toggle;

    static TextView infoview;
    static TextView infoview2;
    static TextView infoview3;

    EditText n;
    EditText p;

    static Context context;
    Intent intent;

    RelativeLayout relativeLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.first_screen);

        context = this;
        intent = new Intent(this,CommunicationService.class);

        relativeLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        walkingButton = (Button) findViewById(R.id.pedestrianButton);
        drivingButton = (Button) findViewById(R.id.driverButton);
        exitButton = (Button) findViewById(R.id.exitButton);
        setButton = (Button) findViewById(R.id.setButton);

         toggle= (ToggleButton) findViewById(R.id.walkingButton);

        infoview = (TextView)findViewById(R.id.infoView);
        infoview2 = (TextView)findViewById(R.id.infoView2);
        infoview3 = (TextView)findViewById(R.id.infoView3);

        walkingButton.setOnClickListener(this);
        drivingButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        setButton.setOnClickListener(this);
        relativeLayout.setOnClickListener(this);

        n = (EditText)findViewById(R.id.alertCount);
        p = (EditText)findViewById(R.id.probability);

        exitButton.setVisibility(View.GONE);
        setButton.setVisibility(View.GONE);

        n.setVisibility(View.GONE);
        p.setVisibility(View.GONE);
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

        if (v.getId() == R.id.pedestrianButton) {
            arrangePedestrianViews();
            intent.putExtra("user",Constants.HOST);
            startCommunicationService();
        } else if (v.getId() == R.id.driverButton) {
            arrangeDriverViews();
            intent.putExtra("user",Constants.CLIENT);
            startCommunicationService();
        }else if(v.getId() == R.id.exitButton){
            Toast.makeText(getBaseContext(), "Service Stopped", Toast.LENGTH_LONG).show();

            stopService(intent);

            walkingButton.setVisibility(View.VISIBLE);
            drivingButton.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.GONE);
        }else if(v.getId() == R.id.setButton){
            String N = n.getText().toString();
            String P  = p.getText().toString();

            if(!N.equals("") && !P.equals("")){
                CommunicationService.N = Integer.parseInt(N);
                CommunicationService.P = Double.parseDouble(P)/100;
                CommunicationService.alertCount = 0;
            }else{
                Toast.makeText(getBaseContext(),"Null input(s)",Toast.LENGTH_LONG).show();
            }
        }
    }



    private void startCommunicationService() {
        startService(intent);
    }

    public void arrangePedestrianViews(){
        walkingButton.setVisibility(View.GONE);
        drivingButton.setVisibility(View.GONE);

        exitButton.setVisibility(View.VISIBLE);
        setButton.setVisibility(View.VISIBLE);

        n.setVisibility(View.VISIBLE);
        p.setVisibility(View.VISIBLE);
    }

    public void arrangeDriverViews(){
        walkingButton.setVisibility(View.GONE);
        drivingButton.setVisibility(View.GONE);

        exitButton.setVisibility(View.VISIBLE);
    }


    @Override
    public void onBackPressed() {
        //do nothing
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.walkingButton){
            if (isChecked) {
                // The toggle is enabled
                CommunicationService.isPedestrianWalking = true;
            } else {
                // The toggle is disabled
                CommunicationService.isPedestrianWalking = false;
            }
        }
    }
}
