package com.example.sa3id.UserActivities;

import static com.example.sa3id.Constants.userSP;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    EditText etEmail, etPassword;
    Button loginButton;
    String email, password;
    Intent comeIntent = getIntent();
    TextView tvDontHaveAnAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize SharedPreferences and editor
        sharedPreferences = getSharedPreferences(userSP, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mAuth = FirebaseAuth.getInstance();

        initViews();


    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.password);
        tvDontHaveAnAccount = findViewById(R.id.tvDontHaveAnAccount);

        if (comeIntent != null) {
            etEmail.setText((comeIntent.getStringExtra("userEmail") == null) ? "" : comeIntent.getStringExtra("userEmail"));
            etPassword.setText((comeIntent.getStringExtra("userPassword")==null)?"":comeIntent.getStringExtra("userPassword"));
        }

        loginButton = findViewById(R.id.login_button);





        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(view);
            }
        });

        tvDontHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                intent.putExtra("userEmail", email);
                intent.putExtra("userPassword", password);
                startActivity(intent);
                finish();
            }
        });

}

public void login(View view) {
    email = etEmail.getText().toString().trim();
    password = etPassword.getText().toString().trim();


    mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignIn.this, "Authentication Successful.", Toast.LENGTH_SHORT).show();
                        editor.putString("userEmail", email);
                        editor.commit();
                        startActivity(new Intent(SignIn.this, MainActivity.class));
                        finish();

                    } else {
                        Toast.makeText(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
}

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser==null) {
            Toast.makeText(SignIn.this, "no user logged in", Toast.LENGTH_SHORT).show();
        } else {
            editor.putString("userEmail", firebaseUser.getEmail());
            //editor.putString("username", firebaseUser.getDisplayName());
            editor.apply();
            startActivity(new Intent(SignIn.this, MainActivity.class));
            finish();
        }
    }



}