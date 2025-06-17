package com.example.sa3id.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sa3id.R;
import com.example.sa3id.models.Announcement;
import com.example.sa3id.userActivities.AnnouncementViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AnnouncementAdapter extends ArrayAdapter<Announcement> {
    private Context context;
    private List<Announcement> objects;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.announcement_layout, parent, false);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        ImageView announcementImage = view.findViewById(R.id.announcementImage);
        Announcement announcement = objects.get(position);
        
        tvTitle.setText(announcement.getTitle());
        tvDescription.setText(announcement.getDescription());
        
        if (announcement.isLocal()) {
            announcementImage.setImageResource(announcement.getImageResource());
        } else {
            // Load image from Firebase Storage using Picasso
            Picasso.get()
                .load(announcement.getImageUrl())
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .into(announcementImage);
        }

        return view;
    }

    public AnnouncementAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Announcement> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.objects = objects;
    }

    public static void openAnnouncementAsActivity(Context context, String title, String description, int imageResource) {
        Intent intent = new Intent(context, AnnouncementViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("imageResource", String.valueOf(imageResource));
        context.startActivity(intent);
    }

    public static void openAnnouncementAsActivity(Context context, String title, String description, String imageUrl) {
        Intent intent = new Intent(context, AnnouncementViewActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("imageUrl", imageUrl);
        context.startActivity(intent);
    }
}
