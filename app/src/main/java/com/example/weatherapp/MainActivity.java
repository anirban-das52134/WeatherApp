package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.validation.Validator;

public class MainActivity extends AppCompatActivity {

    EditText email,pass;
    FirebaseAuth auth;
    ProgressBar progressBar;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        auth = FirebaseAuth.getInstance();
        submitBtn  = findViewById(R.id.submit);
        progressBar = findViewById(R.id.progressBarRegister);

        //If user is already logged in skip the validation part
        if(auth.getCurrentUser()!=null){
            goToDashboard(auth.getCurrentUser().getEmail());
            finish();
        }
    }

    public void VallidateUser(View v){

        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String emailStr = email.getText().toString().trim();
        final String passStr = pass.getText().toString().trim();
        //Handling trivial error cases
        if(TextUtils.isEmpty(emailStr)){
            email.setError("Email is required!");
            return;
        }
        else {
            email.setError(null);
        }
        if(TextUtils.isEmpty(passStr)){
            pass.setError("Password is required!");
            return;
        }
        else {
            pass.setError(null);
        }
        if(passStr.length() < 6 ){
            pass.setError("Password must be greater or equal to 6 characters!");
            return;
        }
        else {
            pass.setError(null);
        }

        //Show the progressbar
        progressBar.setVisibility(View.VISIBLE);
        //Disable the submit button to not allow further request
        submitBtn.setEnabled(false);
        //Authenticate the user
        auth.signInWithEmailAndPassword(emailStr,passStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Welcome!",Toast.LENGTH_SHORT).show();
                    goToDashboard(emailStr);
                }
                else {
                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                    //If the user does not exist we automatically create the user
                    if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                        registerUser(emailStr,passStr);
                        return;
                    }
                    Toast.makeText(MainActivity.this,"Error : " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    submitBtn.setEnabled(true);
                }
            }
        });
    }

    private void registerUser(final String emailStr, String passStr){
        Log.i("Content","New User!");
        submitBtn.setEnabled(false);
        auth.createUserWithEmailAndPassword(emailStr,passStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Registered!",Toast.LENGTH_SHORT).show();
                    goToDashboard(emailStr);
                }
                else {
                    Toast.makeText(MainActivity.this,"Error : " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    submitBtn.setEnabled(true);
                }
            }
        });
    }

    //User verified so go to next activity
    private void goToDashboard(String emailStr){
        Intent intent = new Intent(MainActivity.this, Dashboard.class);
        intent.putExtra("email", emailStr);
        startActivity(intent);
        finish();
    }
}