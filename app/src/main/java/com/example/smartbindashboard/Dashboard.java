package com.example.smartbindashboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class Dashboard extends AppCompatActivity {

    CardView cardMap;
    CardView cardDevices;
    CardView cardStatistics;
    CardView cardProfile;
    CardView cardSettings;
    CardView cardLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        cardMap = findViewById(R.id.map);
        cardDevices = findViewById(R.id.devices);
        cardStatistics = findViewById(R.id.statistics);
        cardProfile = findViewById(R.id.profile);
        cardSettings = findViewById(R.id.settings);
        cardLogout = findViewById(R.id.logout);

        // When the user clicks the map icon
        cardMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, Map.class);
                startActivity(intent);
                finish();
            }
        });

        // When the user clicks the logout icon
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getApplicationContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                editor.putBoolean("LoggedIn", false);
                editor.commit();

                Intent intent = new Intent(Dashboard.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}