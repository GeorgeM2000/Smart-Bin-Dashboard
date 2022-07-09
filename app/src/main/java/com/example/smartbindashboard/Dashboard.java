package com.example.smartbindashboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

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
        cardMap.setOnClickListener(view -> {
            Intent intent = new Intent(Dashboard.this, Map.class);
            startActivity(intent);
            finish();
        });

        // When the user clicks the logout icon
        cardLogout.setOnClickListener(view -> {
            SharedPreferences userPreferences = getApplicationContext().getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            SharedPreferences routePreferences = getApplicationContext().getSharedPreferences("Route_Preferences", Context.MODE_PRIVATE);

            // Get the User ID(UID)
            String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            // Sign out
            FirebaseAuth.getInstance().signOut();

            // If there is route data stored in shared preferences
            if(!routePreferences.getString("Routes", "").equals("")) {
                // Delete route data
                FirebaseDatabase.getInstance().getReference("Routes")
                        .child("Kastoria")
                        .child(userUid)
                        .removeValue()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                Toast.makeText(this, "Route data deleted", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Dashboard.this, "Failed to delete route data", Toast.LENGTH_LONG).show();
                            }
                        });
            }



            SharedPreferences.Editor routeEditor = routePreferences.edit();
            SharedPreferences.Editor userEditor = userPreferences.edit();

            // Delete shared preferences values
            routeEditor.putString("Routes", "");
            routeEditor.putString("Regions", "");
            routeEditor.putInt("Current_Route_Index", -1);
            routeEditor.putString("Base_Location_Longitude", "");
            routeEditor.putString("Base_Location_Latitude", "");
            routeEditor.putBoolean("Base_Location_Set", false);

            userEditor.putBoolean("Log_In_State", false);
            userEditor.apply();
            routeEditor.apply();

            startActivity(new Intent(Dashboard.this, MainActivity.class));
            finish();
        });
    }
}