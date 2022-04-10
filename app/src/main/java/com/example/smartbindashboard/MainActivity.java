package com.example.smartbindashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;



public class MainActivity extends AppCompatActivity {

    Button buttonLogin, buttonSignUp;
    SharedPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLogin = findViewById(R.id.login);
        buttonSignUp = findViewById(R.id.signUp);
        userPreferences = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);


        // If the user has already logged in
        if(userPreferences.getBoolean("Log_In_State", false)) {

            // If user has logged in before, get the hours since user's last log in
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            long timeDifference = 0;

            try {
                // Get the current time
                Date currentTime = timeFormat.parse(LocalTime.now().toString());

                // Get the stored time
                Date storedTime = timeFormat.parse(userPreferences.getString("Log_In_Time", ""));

                // Calculate the time difference in hours
                timeDifference = ((Math.abs(currentTime.getTime() - storedTime.getTime())) / (60 * 60 * 1000)) % 24;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // If five hours have passed since user's last log in or the log in date is not the same as the current date
            if(timeDifference >= 5 || !userPreferences.getString("Log_In_Date", "").equals(LocalDate.now().toString())) {
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.putBoolean("Log_In_State", false);
                editor.apply();
            } else {
                startActivity(new Intent(MainActivity.this, Dashboard.class));
                finish();
            }
        }


        // When user clicks the login button
        buttonLogin.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });

        // When user clicks the sign up button
        buttonSignUp.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SignUp.class));
            finish();
        });
    }

}