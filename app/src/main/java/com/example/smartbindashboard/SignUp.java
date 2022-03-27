package com.example.smartbindashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

class SignUpData {
    private String fullName;
    private String username;
    private String email;
    private String password;
    private String state;

    public SignUpData(String email, String password, String username, String fullName) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.fullName = fullName;
    }

    public String getState() {
        return state;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

public class SignUp extends AppCompatActivity {


    TextInputEditText textInputEditTextFullname, textInputEditTextUsername, textInputEditTextPassword, textInputEditTextEmail;
    Button buttonSignUp;
    TextView textViewLogin;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextFullname = findViewById(R.id.fullname);
        textInputEditTextPassword = findViewById(R.id.password);
        textInputEditTextUsername = findViewById(R.id.username);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLogin = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progress);

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullname, username, password, email;
                fullname = String.valueOf(textInputEditTextFullname.getText());
                username = String.valueOf(textInputEditTextUsername.getText()).replaceAll(" ", "");
                email = String.valueOf(textInputEditTextEmail.getText()).replaceAll(" ", "");
                password = String.valueOf(textInputEditTextPassword.getText());

                if(!fullname.equals("") && !email.equals("") && !password.equals("") && !username.equals("")) {
                    // Start progressBar first (Set visibility VISIBLE)
                    progressBar.setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            sendData(email, password, username, fullname);
                        }
                    });
                } else {
                    Toast.makeText(SignUp.this, "All fields are required.", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }


    private interface postInterface {
        // Post Request
        @POST("/sign_up")
        // Initialize Login data call
        Call<SignUpData> getSignUpData(@Body SignUpData signUpData);
    }

    // Send sign up data
    private void sendData(String email, String password, String username, String fullName) {
        // Initialize retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.2.56:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Initialize interface
        SignUp.postInterface inter = retrofit.create(SignUp.postInterface.class);

        // Pass input values
        SignUpData signUpData = new SignUpData(email, password, username, fullName);
        Call<SignUpData> call = inter.getSignUpData(signUpData);
        call.enqueue(new Callback<SignUpData>() {
            @Override
            public void onResponse(Call<SignUpData> call, Response<SignUpData> response) {
                // Check if response is successful
                if(response.isSuccessful() && response.body() != null) {
                    if(response.body().getState().equals("Success")) {
                        Toast.makeText(SignUp.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUp.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                    // Check if there is an error with the sign up
                    else if(response.body().getState().equals("Error")) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignUp.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                    }
                    // If the response isn't successful and there is no error then user is invalid
                    else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignUp.this, "User is invalid", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<SignUpData> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SignUp.this, "Sign up failed", Toast.LENGTH_SHORT).show();
            }
        });

    }
}