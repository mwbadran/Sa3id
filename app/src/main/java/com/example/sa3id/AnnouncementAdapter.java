package com.example.sa3id;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AnnouncementAdapter extends ArrayAdapter<Announcement> {
    Context context;
    List<Announcement> objects;

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
        announcementImage.setImageResource(announcement.getImageResource());

        return view;
    }

    public AnnouncementAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Announcement> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.objects = objects;

    }
}
