package com.example.parseuber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

    Button getstartBtn;

    public void redirectActivity(){

        if(ParseUser.getCurrentUser().get("driverOrRider").equals("rider")){

            Intent intent = new Intent(getApplicationContext(),RiderActivity.class);
            startActivity(intent);
        }else {

            Intent intent = new Intent(getApplicationContext(),ViewRequestActivity.class);
            startActivity(intent);
        }

    }


    public void getStarted(){

        Switch userType = (Switch)findViewById(R.id.driverOrRider);
        Log.i ("UserType ", String.valueOf(userType.isChecked()));

        String person = "driver";
        if (userType.isChecked()){
            person = "rider";

        }

        ParseUser.getCurrentUser().put("driverOrRider",person);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                redirectActivity();

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getstartBtn = findViewById(R.id.getStartedBtn_ID);

        getstartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStarted();
            }
        });

        if(ParseUser.getCurrentUser() == null ){

            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {

                    if(e==null){

                        Log.i("LogIn","successful");

                    }else {

                        Log.i("LogIn","failed");

                    }
                }
            });

        }else {

            if(ParseUser.getCurrentUser().get("driverOrRider") !=null){

                Log.i("info",ParseUser.getCurrentUser().get("driverOrRider").toString());

                redirectActivity();

            }

        }



        ParseAnalytics.trackAppOpenedInBackground(getIntent());

    }
}
