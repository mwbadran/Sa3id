package com.example.sa3id.adminActivities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.example.sa3id.adapters.UserAdapter;
import com.example.sa3id.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends BaseActivity {
    private RecyclerView rvUsers;
    private UserAdapter userAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadUsers();
    }

    private void initViews() {
        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, new ArrayList<>());
        rvUsers.setAdapter(userAdapter);
    }

    private void loadUsers() {
        firestore.collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    String currentUserEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        // Set the email from the document ID if it's not set
                        if (user.getEmail() == null || user.getEmail().isEmpty()) {
                            user.setEmail(document.getId());
                        }
                        // Don't show the current user in the list
                        if (!user.getEmail().equals(currentUserEmail)) {
                            users.add(user);
                        }
                    }
                    userAdapter.updateUsers(users);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في تحميل المستخدمين", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_manage_users;
    }
}