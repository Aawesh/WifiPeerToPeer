package wifipeertopeer.com.wifipeertopeer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by aawesh on 7/6/17.
 */

public class UserSelectionActivity extends AppCompatActivity {
    Button walkingButton;
    Button drivingButton;
    Button exitButton;

    static TextView infoview;
    static TextView infoview2;

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

        infoview = (TextView)findViewById(R.id.infoView);
        infoview2 = (TextView)findViewById(R.id.infoView2);

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

    @Override
    public void onBackPressed() {
        //do nothing
    }
}
