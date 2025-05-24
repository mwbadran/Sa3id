package com.example.sa3id.userActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sa3id.R;
import com.example.sa3id.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignIn extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient mGoogleSignInClient;

    private EditText etEmail, etPassword;
    private TextView tvDontHaveAnAccount;
    private Button loginButton;
    private Button btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        setupBackNavigation();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(SignIn.this, MainActivity.class));
                finish();
            }
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.password);
        tvDontHaveAnAccount = findViewById(R.id.tvDontHaveAnAccount);
        loginButton = findViewById(R.id.login_button);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        Intent comeIntent = getIntent();
        if (comeIntent != null) {
            etEmail.setText(comeIntent.getStringExtra("userEmail") != null ?
                    comeIntent.getStringExtra("userEmail") : "");
            etPassword.setText(comeIntent.getStringExtra("userPassword") != null ?
                    comeIntent.getStringExtra("userPassword") : "");
        }

        loginButton.setOnClickListener(v -> loginUser());
        tvDontHaveAnAccount.setOnClickListener(v -> navigateToSignUp());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(SignIn.this, "Google sign in failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveGoogleUserToFirestore(user);
                        }
                    } else {
                        Toast.makeText(SignIn.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // In both classes replace the saveGoogleUserToFirestore method with:

    private void saveGoogleUserToFirestore(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();

        firestore.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // New user - require username
                        Intent intent = new Intent(this, GoogleSignUpActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("email", firebaseUser.getEmail());
                        intent.putExtra("displayName", firebaseUser.getDisplayName());
                        startActivity(intent);
                        finish();
                    } else {
                        // Existing user - direct login
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في التحقق من الحساب", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(SignIn.this, SignUp.class);
        intent.putExtra("userEmail", etEmail.getText().toString().trim());
        intent.putExtra("userPassword", etPassword.getText().toString().trim());
        startActivity(intent);
        finish();
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserDetails(firebaseUser.getUid());
                        }
                    } else {
                        Toast.makeText(SignIn.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserDetails(String userId) {
        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        startActivity(new Intent(SignIn.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignIn.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show());
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserDetails(currentUser.getUid());
        }
    }
}