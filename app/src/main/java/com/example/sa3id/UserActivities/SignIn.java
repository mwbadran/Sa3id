package com.example.sa3id.UserActivities;

import static com.example.sa3id.Constants.userSP;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.sa3id.Constants;

public class SignIn extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    EditText usernameEditText, passwordEditText;
    Button loginButton, signupButton;
    String email, password;
    Intent comeIntent = getIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_sign_in);

        // Initialize SharedPreferences and editor
        sharedPreferences = getSharedPreferences(userSP, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mAuth = FirebaseAuth.getInstance();

        initViews();


    }

    private void initViews() {
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);


        if (comeIntent != null) {
            usernameEditText.setText((comeIntent.getStringExtra("userEmail") == null) ? "" : comeIntent.getStringExtra("userEmail"));
            passwordEditText.setText((comeIntent.getStringExtra("userPassword")==null)?"":comeIntent.getStringExtra("userPassword"));
        }

        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);




        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(view);
            }
        });
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });

}

public void login(View view) {
    email = usernameEditText.getText().toString();
    password = passwordEditText.getText().toString();


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
            startActivity(new Intent(SignIn.this, MainActivity.class));
            finish();
        }
    }


    @Override
protected int getLayoutResourceId() {
    return R.layout.activity_sign_in;
}

}