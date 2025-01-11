package com.example.sa3id.UserActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sa3id.R;
import com.example.sa3id.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private EditText etUsername, etEmail, etPassword;
    private Button loginButton, signupButton;
    private TextView tvAlreadyHaveAnAccount;
    private String email, password, username;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private Intent comeIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        comeIntent = getIntent();
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        sharedPreferences = getSharedPreferences("UserDetailsSP", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference("Users");

        initViews();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(SignUp.this, MainActivity.class));
                finish();
            }
        });

    }

    private void initViews() {
        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.password);
        tvAlreadyHaveAnAccount = findViewById(R.id.tvAlreadyHaveAnAccount);

        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);


        if (comeIntent != null) {
            etEmail.setText((comeIntent.getStringExtra("userEmail") == null) ? "" : comeIntent.getStringExtra("userEmail"));
            etPassword.setText((comeIntent.getStringExtra("userPassword")==null)?"":comeIntent.getStringExtra("userPassword"));
        }



        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register(view);
            }
        });

        tvAlreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                intent.putExtra("userEmail", email);
                intent.putExtra("userPassword", password);
                startActivity(intent);
                finish();
            }
        });
    }

    public void register(View view) {
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();
        username = etUsername.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUserCurrent = mAuth.getCurrentUser();
                    if (firebaseUserCurrent != null) {
                        String userId = firebaseUserCurrent.getUid();
                        User theUser = new User(username, email);
                        databaseReference.child(userId).setValue(theUser);
//                        databaseReference.child(userId).child("username").setValue(username);
//                        databaseReference.child(userId).child("email").setValue(email);
                        Toast.makeText(SignUp.this, "Authentication Successful.", Toast.LENGTH_SHORT).show();
                        editor.putString("userEmail", email);
                        editor.putString("username", username);
                        editor.apply();
                        startActivity(new Intent(SignUp.this, SignIn.class));
                        finish();
                    }
                } else {
                    Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(SignUp.this, "No user logged in", Toast.LENGTH_SHORT).show();
        } else {
            String userId = firebaseUser.getUid();
            databaseReference.child(userId).child("username").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String username = task.getResult().getValue(String.class);
                        if (username != null) {
                            editor.putString("userEmail", firebaseUser.getEmail());
                            editor.putString("username", username);
                            etEmail.setText(firebaseUser.getEmail());
                            etUsername.setText(username);
                            editor.apply();
                            startActivity(new Intent(SignUp.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUp.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }




}