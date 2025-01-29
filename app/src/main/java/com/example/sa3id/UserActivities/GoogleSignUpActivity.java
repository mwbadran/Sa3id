package com.example.sa3id.UserActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sa3id.R;
import com.example.sa3id.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class GoogleSignUpActivity extends AppCompatActivity {

    private EditText etUsername;
    private FirebaseFirestore firestore;
    private String userId, email, displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_up);

        firestore = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");
        displayName = getIntent().getStringExtra("displayName");

        etUsername = findViewById(R.id.etUsername);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        // Set default value if available
        if (displayName != null && !displayName.isEmpty()) {
            etUsername.setText(displayName);
        }

        btnSubmit.setOnClickListener(v -> validateAndSaveUsername());
    }

    private void validateAndSaveUsername() {
        String username = etUsername.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, R.string.username_required, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(username, email);

        firestore.collection("Users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "خطأ في الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {
        // Force user to complete registration
        super.onBackPressed();
        Toast.makeText(this, "يرجى إكمال عملية التسجيل", Toast.LENGTH_SHORT).show();
    }
}