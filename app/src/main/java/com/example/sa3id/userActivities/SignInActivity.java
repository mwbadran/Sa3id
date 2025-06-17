package com.example.sa3id.userActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sa3id.dialogs.CustomAlertDialog;
import com.example.sa3id.dialogs.EmailInputDialog;
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

public class SignInActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient mGoogleSignInClient;
    private CustomAlertDialog customAlertDialog;

    private EditText etEmail, etPassword;
    private TextView tvDontHaveAnAccount, tvForgotPassword;
    private Button loginButton;
    private Button btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        customAlertDialog = new CustomAlertDialog(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.client_id)).requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        setupBackNavigation();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.password);
        tvDontHaveAnAccount = findViewById(R.id.tvDontHaveAnAccount);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        loginButton = findViewById(R.id.login_button);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // prefill from signup if available
        Intent comeIntent = getIntent();
        if (comeIntent != null) {
            etEmail.setText(comeIntent.getStringExtra("userEmail") != null ? comeIntent.getStringExtra("userEmail") : "");
            etPassword.setText(comeIntent.getStringExtra("userPassword") != null ? comeIntent.getStringExtra("userPassword") : "");
        }

        // click handlers
        loginButton.setOnClickListener(v -> loginUser());
        tvDontHaveAnAccount.setOnClickListener(v -> navigateToSignUp());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        tvForgotPassword.setOnClickListener(v -> sendPasswordReset());
    }

    private void sendPasswordReset() {
        EmailInputDialog emailInputDialog = new EmailInputDialog(this) {
            @Override
            protected void onEmailSubmit(String email) {
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        customAlertDialog.show("تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني.", R.drawable.baseline_check_circle_24);
                    } else {
                        customAlertDialog.show("فشل في إرسال رابط إعادة التعيين. تأكد من البريد الإلكتروني وحاول مرة أخرى.", R.drawable.baseline_error_24);
                    }
                });
            }
        };
        emailInputDialog.show();
    }

    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.client_id)).requestEmail().build();

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
                customAlertDialog.show("Google sign in failed: " + e.getMessage(), R.drawable.baseline_error_24);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveGoogleUserToFirestore(user);
                }
            } else {
                customAlertDialog.show("Authentication failed.", R.drawable.baseline_error_24);
            }
        });
    }

    private void saveGoogleUserToFirestore(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();

        firestore.collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Intent intent = new Intent(this, GoogleSignUpActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("email", firebaseUser.getEmail());
                intent.putExtra("displayName", firebaseUser.getDisplayName());
                startActivity(intent);
                finish();
            } else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(e -> {
            customAlertDialog.show("فشل في التحقق من الحساب", R.drawable.baseline_error_24);
        });
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        intent.putExtra("userEmail", etEmail.getText().toString().trim());
        intent.putExtra("userPassword", etPassword.getText().toString().trim());
        startActivity(intent);
        finish();
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate email
        if (email.isEmpty()) {
            customAlertDialog.show("الرجاء إدخال البريد الإلكتروني", R.drawable.baseline_error_24);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    fetchUserDetails(firebaseUser.getUid());
                }
            } else {
                customAlertDialog.show("فشل تسجيل الدخول. تأكد من البريد الإلكتروني وكلمة المرور.", R.drawable.baseline_error_24);
            }
        });
    }

    private void fetchUserDetails(String userId) {
        firestore.collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(e -> customAlertDialog.show("Failed to fetch user details", R.drawable.baseline_error_24));
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
