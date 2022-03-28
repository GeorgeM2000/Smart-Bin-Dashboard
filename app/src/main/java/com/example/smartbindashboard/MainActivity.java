package com.example.smartbindashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Button buttonLogin, buttonSignUp;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLogin = findViewById(R.id.login);
        buttonSignUp = findViewById(R.id.signUp);
        sp = getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        Date currentTime = Calendar.getInstance().getTime();
        long differenceInMinutes = 0;


        // If the user has already logged in
        if(sp.getBoolean("LoggedIn", false)) {

            // If user has logged in before, get the hours since user's last log in
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
            try {
                Date storedTime = timeFormat.parse(sp.getString("Time", ""));
                differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime.getTime() - storedTime.getTime()) % 60;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(differenceInMinutes > 50) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("LoggedIn", false);
            } else {
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        }


        // When user clicks the login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        // When user clicks the sign up button
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });
    }
}