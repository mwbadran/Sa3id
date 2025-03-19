package com.example.sa3id.UserActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.FeedbackMsg;
import com.example.sa3id.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class FeedbackActivity extends BaseActivity {
    private TextInputEditText etName, etEmail, etSubject, etMessage;
    private MaterialButton btnSubmitFeedback;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference();
        mFirestore = FirebaseFirestore.getInstance();

        initViews();

        prefillUserInfo();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etSubject = findViewById(R.id.etSubject);
        etMessage = findViewById(R.id.etMessage);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        btnSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });

    }


    private void prefillUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mFirestore.collection("Users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                etName.setText(name);
                            }
                            // User's email already exists in FirebaseAuth
                            etEmail.setText(currentUser.getEmail());
                        }
                    });
        }
    }

    private void submitFeedback() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("الرجاء إدخال الاسم");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("الرجاء إدخال البريد الإلكتروني");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(subject)) {
            etSubject.setError("الرجاء إدخال الموضوع");
            etSubject.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(message)) {
            etMessage.setError("الرجاء إدخال الرسالة");
            etMessage.requestFocus();
            return;
        }

        //showProgressDialog("جاري إرسال الرسالة...");

        // Create feedback object
        String feedbackId = UUID.randomUUID().toString();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "guest";

        FeedbackMsg feedback = new FeedbackMsg(feedbackId, userId, name, email, subject, message, timestamp, "pending", "", "", "");

        // Save to Firebase Realtime Database
        mDatabase.child("feedback").child(feedbackId)
                .setValue(feedback)
                .addOnCompleteListener(task -> {
                    //hideProgressDialog();
                    if (task.isSuccessful()) {
                        // Clear form
                        etSubject.setText("");
                        etMessage.setText("");

                        Toast.makeText(FeedbackActivity.this, "تم إرسال رسالتك بنجاح", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(FeedbackActivity.this, "حدث خطأ أثناء إرسال الرسالة، الرجاء المحاولة مرة أخرى", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_feedback;
    }


}