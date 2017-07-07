package wifipeertopeer.com.wifipeertopeer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;

/**
 * Created by aawesh on 7/6/17.
 */

public class UserSelectionActivity extends AppCompatActivity {
    Button walkingButton;
    Button drivingButton;
    Button exitButton;

    static Context context;
    Intent intent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.first_screen);

        context = this;
        intent = new Intent(this,CommunicationService.class);

        walkingButton = (Button) findViewById(R.id.pedestrianButton);
        drivingButton = (Button) findViewById(R.id.driverButton);
        exitButton = (Button) findViewById(R.id.exitButton);

        exitButton.setVisibility(View.GONE);


        walkingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                arrangeViews();

                intent.putExtra("user",Constants.HOST);
                startCommunicationService();
            }
        });

        drivingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                arrangeViews();

                intent.putExtra("user",Constants.CLIENT);
                startCommunicationService();


            }
        });


        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Service Stopped", Toast.LENGTH_LONG).show();

                stopService(intent);

                walkingButton.setVisibility(View.VISIBLE);
                drivingButton.setVisibility(View.VISIBLE);
                exitButton.setVisibility(View.GONE);

            }
        });
    }

    private void startCommunicationService() {
        startService(intent);
    }

    public void arrangeViews(){
        walkingButton.setVisibility(View.GONE);
        drivingButton.setVisibility(View.GONE);

        exitButton.setVisibility(View.VISIBLE);
    }
}
