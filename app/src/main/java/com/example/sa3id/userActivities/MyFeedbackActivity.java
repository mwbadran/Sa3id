package com.example.sa3id.userActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.example.sa3id.models.FeedbackMsg;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MyFeedbackActivity extends BaseActivity {
    private RecyclerView recyclerViewMyFeedback;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<FeedbackMsg, FeedbackViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK).getReference();

        initViews();
        loadMyFeedback();
    }

    private void initViews() {
        recyclerViewMyFeedback = findViewById(R.id.recyclerViewMyFeedback);
        recyclerViewMyFeedback.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadMyFeedback() {
        String userId = mAuth.getCurrentUser().getUid();
        Query query = mDatabase.child("feedback").orderByChild("userId").equalTo(userId);

        FirebaseRecyclerOptions<FeedbackMsg> options =
                new FirebaseRecyclerOptions.Builder<FeedbackMsg>()
                        .setQuery(query, FeedbackMsg.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<FeedbackMsg, FeedbackViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position, @NonNull FeedbackMsg model) {
                // Set status text and color
                String statusText;
                int statusColor;
                switch (model.getStatus()) {
                    case "pending":
                        statusText = "قيد الانتظار";
                        statusColor = getResources().getColor(R.color.orange,null);
                        break;
                    case "in-progress":
                        statusText = "قيد المعالجة";
                        statusColor = getResources().getColor(R.color.blue,null);
                        break;
                    case "resolved":
                        statusText = "تم الرد";
                        statusColor = getResources().getColor(R.color.sa3id_green,null);
                        break;
                    default:
                        statusText = model.getStatus();
                        statusColor = getResources().getColor(R.color.gray,null);
                }
                holder.tvStatus.setText(statusText);
                holder.tvStatus.setTextColor(statusColor);

                // Set feedback details
                holder.tvSubject.setText(model.getSubject());
                holder.tvMessage.setText(model.getMessage());
                holder.tvDate.setText(model.getTimestamp());

                // Set response details if available
                if (!model.getResponse().isEmpty()) {
                    holder.tvResponse.setVisibility(View.VISIBLE);
                    holder.tvResponse.setText(model.getResponse());
                    holder.tvResponseDate.setText(model.getResponseTimestamp());
                } else {
                    holder.tvResponse.setVisibility(View.GONE);
                    holder.tvResponseDate.setVisibility(View.GONE);
                }
            }

            @NonNull
            @Override
            public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_my_feedback, parent, false);
                return new FeedbackViewHolder(view);
            }
        };

        recyclerViewMyFeedback.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    // ViewHolder for feedback items
    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvSubject, tvMessage, tvDate, tvResponse, tvResponseDate;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvResponse = itemView.findViewById(R.id.tvResponse);
            tvResponseDate = itemView.findViewById(R.id.tvResponseDate);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_my_feedback;
    }
} 