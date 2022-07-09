package com.example.smartbindashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class Login extends AppCompatActivity {

    TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;


    public static class Region {
        @SuppressWarnings("unused")
        private String name;

        public String getName() {
            return name;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewSignUp = findViewById(R.id.signUpText);
        progressBar = findViewById(R.id.progress);
        mAuth = FirebaseAuth.getInstance();

        // When user clicks the Sign Up text
        textViewSignUp.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, SignUp.class));
            finish();
        });

        // When user clicks the login button
        buttonLogin.setOnClickListener(view -> login());
    }

    // Save the regions to the shared preferences
    private void saveRegions(List<Region> regions) {

        SharedPreferences routePreferences = getApplicationContext().getSharedPreferences("Route_Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = routePreferences.edit();

        Gson gson = new Gson();

        String json = gson.toJson(regions);

        editor.putString("Regions", json);
        editor.apply();

    }

    // Save the Base coordinates to the shared preferences
    private void saveBase(String[] baseCoord) {
        SharedPreferences routePreferences = getApplicationContext().getSharedPreferences("Route_Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = routePreferences.edit();

        editor.putString("Base_Location_Latitude", baseCoord[0]);
        editor.putString("Base_Location_Longitude", baseCoord[1]);
        editor.putBoolean("Base_Location_Set", true);
        editor.apply();

    }

    // Get the regions from the database
    private void getRegions() {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("regions")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        List<Region> regions;
                        GenericTypeIndicator<List<Region>> t = new GenericTypeIndicator<List<Region>>() {};
                        regions = task.getResult().getValue(t);

                        // Save user regions to shared preferences
                        saveRegions(regions);
                    } else {
                        Toast.makeText(this, "Failed to acquire the regions.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Get the base coordinates
    private void getBase() {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("base")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        String[] baseCoordinates = new String[]{Objects.requireNonNull(task.getResult().child("latitude").getValue()).toString(), Objects.requireNonNull(task.getResult().child("longitude").getValue()).toString()};

                        saveBase(baseCoordinates);
                    } else {
                        Toast.makeText(this, "Failed to acquire the location of the base.", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void login() {

        // Get values
        String email = String.valueOf(textInputEditTextEmail.getText()).replaceAll(" ", "");
        String password = String.valueOf(textInputEditTextPassword.getText()).replaceAll(" ", "");

        // Check if password and email fields are filled
        if(!email.equals("") && !password.equals("")) {
            // Start progressBar first
            progressBar.setVisibility(View.VISIBLE);

            // Sign in
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, task -> {
                if(task.isSuccessful()) {
                    // Get current time and date
                    String currentTime = LocalTime.now().toString();
                    String currentDate = LocalDate.now().toString();

                    // If login is successful, change the login status
                    SharedPreferences userPreferences = getApplicationContext().getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPreferences.edit();

                    // Set logged in status to true
                    editor.putBoolean("Log_In_State", true);

                    // Store current time in shared preferences
                    editor.putString("Log_In_Time", currentTime);
                    editor.putString("Log_In_Date", currentDate);
                    editor.apply();

                    // Get regions
                    getRegions();

                    // Get Base location
                    getBase();

                    startActivity(new Intent(Login.this, Dashboard.class));
                    finish();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(Login.this, "All fields are required.", Toast.LENGTH_SHORT).show();
        }
    }

}