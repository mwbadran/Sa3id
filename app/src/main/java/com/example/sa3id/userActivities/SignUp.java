package com.example.sa3id.userActivities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class SignUp extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient mGoogleSignInClient;
    private CustomAlertDialog customAlertDialog;

    private EditText etUsername, etEmail, etPassword;
    private TextView tvAlreadyHaveAnAccount;
    private Button signupButton;
    private Button btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        customAlertDialog = new CustomAlertDialog(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();

        initViews();
        setupBackNavigation();
    }

    private void setupBackNavigation() {
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
        signupButton = findViewById(R.id.signup_button);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        Intent comeIntent = getIntent();
        if (comeIntent != null) {
            etEmail.setText(comeIntent.getStringExtra("userEmail") != null ?
                    comeIntent.getStringExtra("userEmail") : "");
            etPassword.setText(comeIntent.getStringExtra("userPassword") != null ?
                    comeIntent.getStringExtra("userPassword") : "");
        }

        signupButton.setOnClickListener(v -> registerUser());
        tvAlreadyHaveAnAccount.setOnClickListener(v -> navigateToSignIn());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
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
                customAlertDialog.show(
                    "فشل تسجيل الدخول عبر Google: " + e.getMessage(),
                    R.drawable.baseline_error_24
                );
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
                        customAlertDialog.show(
                            "فشل المصادقة",
                            R.drawable.baseline_error_24
                        );
                    }
                });
    }

    private void saveGoogleUserToFirestore(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Uri profilePicUri = account.getPhotoUrl();
        String profilePicUrl = profilePicUri != null ? profilePicUri.toString() : null;
        firestore.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Intent intent = new Intent(this, GoogleSignUpActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("email", firebaseUser.getEmail());
                        intent.putExtra("displayName", firebaseUser.getDisplayName());
                        intent.putExtra("profilePicUrl", profilePicUrl);
                        startActivity(intent);
                        finish();
                    } else {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    customAlertDialog.show(
                        "فشل في التحقق من الحساب",
                        R.drawable.baseline_error_24
                    );
                });
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(SignUp.this, SignIn.class);
        intent.putExtra("userEmail", etEmail.getText().toString().trim());
        intent.putExtra("userPassword", etPassword.getText().toString().trim());
        startActivity(intent);
        finish();
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        // Validate username
        if (username.isEmpty()) {
            customAlertDialog.show("الرجاء إدخال اسم المستخدم", R.drawable.baseline_error_24);
            return;
        }

        // Validate email
        if (email.isEmpty()) {
            customAlertDialog.show("الرجاء إدخال البريد الإلكتروني", R.drawable.baseline_error_24);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            customAlertDialog.show("الرجاء إدخال بريد إلكتروني صالح", R.drawable.baseline_error_24);
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            customAlertDialog.show("الرجاء إدخال كلمة المرور", R.drawable.baseline_error_24);
            return;
        }

        if (password.length() < 6) {
            customAlertDialog.show("كلمة المرور يجب أن تكون 6 أحرف على الأقل", R.drawable.baseline_error_24);
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), username, email);
                        }
                    } else {
                        customAlertDialog.show(
                            "فشل إنشاء الحساب. " + task.getException().getMessage(),
                            R.drawable.baseline_error_24
                        );
                    }
                });
    }

    private void saveUserToFirestore(String userId, String username, String email) {
        User user = new User(username, email);

        firestore.collection("Users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(SignUp.this, GoogleSignUpActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("email", email);
                    intent.putExtra("displayName", username);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                    customAlertDialog.show(
                        "فشل في حفظ بيانات المستخدم",
                        R.drawable.baseline_error_24
                    )
                );
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mAuth.signOut();
        }
    }
}