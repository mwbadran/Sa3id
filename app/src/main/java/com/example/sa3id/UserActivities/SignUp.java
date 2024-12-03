package com.example.sa3id.UserActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sa3id.R;
import com.example.sa3id.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    EditText etUsername,etEmail, etPassword;
    Button loginButton, signupButton;
    TextView tvAlreadyHaveAnAccount;
    String email, password, username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize SharedPreferences and editor
        sharedPreferences = getSharedPreferences("UserDetailsSP", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mAuth = FirebaseAuth.getInstance();

        initViews();

    }

    private void initViews() {
        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.password);
        tvAlreadyHaveAnAccount = findViewById(R.id.tvAlreadyHaveAnAccount);

        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);





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
                intent.putExtra("userEmail", email);
                intent.putExtra("userPassword", password);
                startActivity(intent);
            }
        });
    }

    public void register(View view) {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        username = etUsername.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(new User(username, email))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUp.this, "Authentication Successful.", Toast.LENGTH_SHORT).show();
                                        editor.putString("userEmail", email);
                                        editor.putString("username", username);
                                        editor.apply();
                                        startActivity(new Intent(SignUp.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUp.this, "Failed to save username.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(SignUp.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser==null) {
            Toast.makeText(SignUp.this, "no user logged in", Toast.LENGTH_SHORT).show();
        } else {
            editor.putString("userEmail",firebaseUser.getDisplayName());
            startActivity(new Intent(SignUp.this, MainActivity.class));
            finish();
        }
    }


}