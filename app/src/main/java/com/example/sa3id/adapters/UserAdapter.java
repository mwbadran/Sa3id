package com.example.sa3id.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.sa3id.R;
import com.example.sa3id.models.User;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private Context context;
    private FirebaseFirestore firestore;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());
        holder.switchAdmin.setChecked(user.isAdmin());

        // Load profile picture if available
        String profilePicUrl = user.getProfilePicUrl();
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Glide.with(context)
                .load(profilePicUrl)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .into(holder.ivUserIcon);
        } else {
            holder.ivUserIcon.setImageResource(R.drawable.baseline_person_24);
        }

        holder.switchAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            user.setAdmin(isChecked);
            firestore.collection("Users")
                    .document(user.getEmail())
                    .update("isAdmin", isChecked)
                    .addOnFailureListener(e -> {
                        // Revert the switch if update fails
                        holder.switchAdmin.setChecked(!isChecked);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail;
        SwitchMaterial switchAdmin;
        ImageView ivUserIcon;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            switchAdmin = itemView.findViewById(R.id.switchAdmin);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
        }
    }
} 