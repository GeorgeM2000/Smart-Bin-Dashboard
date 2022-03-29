package com.example.smartbindashboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.utils.ViewState;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

// Login class for POST request
class LoginData {
    private String email;
    private String password;
    private String state;

    public LoginData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getState() {
        return state;
    }
}

public class Login extends AppCompatActivity {

    TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    Button buttonLogin;
    TextView textViewSignUp;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.signUpText);
        progressBar = findViewById(R.id.progress);

        // When user clicks the Sign Up text
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        // When user clicks the login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }


    private interface postInterface {
        // Post Request
        @POST("/login")
        // Initialize Login data call
        Call<LoginData> getLoginData(@Body LoginData loginData);
    }

    // Send login data
    private void sendData(String email, String password) {
        // Initialize retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.2.7:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Initialize interface
        postInterface inter = retrofit.create(postInterface.class);

        // Pass input values
        LoginData loginData = new LoginData(email, password);
        Call<LoginData> call = inter.getLoginData(loginData);
        call.enqueue(new Callback<LoginData>() {
            @Override
            public void onResponse(Call<LoginData> call, Response<LoginData> response) {
                // Check if response is successful
                if(response.isSuccessful() && response.body() != null) {
                    if(response.body().getState().equals("Success")) {
                        Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Get current time and date
                        String currentTime = LocalTime.now().toString();
                        String currentDate = LocalDate.now().toString();

                        // If login is successful, change the login status
                        SharedPreferences sp = getApplicationContext().getSharedPreferences("userPreferences", Context.MODE_PRIVATE);Toast.makeText(Login.this, "Login successful.", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sp.edit();

                        // Set logged in status to true
                        editor.putBoolean("LoggedIn", true);

                        // Store current time in shared preferences
                        editor.putString("Time", currentTime);
                        editor.putString("Date", currentDate);
                        editor.commit();

                        Intent intent = new Intent(Login.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                    // Check if there is an error with the login
                    else if(response.body().getState().equals("Error")) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                    // If the response isn't successful and there is no error then user was not found
                    else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "User not found", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<LoginData> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login() {

        // Get values
        String email = String.valueOf(textInputEditTextEmail.getText()).replaceAll(" ", "");
        String password = String.valueOf(textInputEditTextPassword.getText()).replaceAll(" ", "");

        // Check password and email fields
        if(!email.equals("") && !password.equals("")) {
            // Start progressBar first (Set visibility VISIBLE)
            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sendData(email, password);
                }
            });
        } else {
            Toast.makeText(Login.this, "All fields are required.", Toast.LENGTH_SHORT).show();
        }
    }

}