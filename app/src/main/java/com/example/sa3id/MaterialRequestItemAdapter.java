package com.example.sa3id;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class MaterialRequestItemAdapter extends BaseAdapter implements ListAdapter {

    private Context context;
    private ArrayList<Uri> materialsList;

    public MaterialRequestItemAdapter(Context context, ArrayList<Uri> materialsList) {
        this.context = context;
        this.materialsList = materialsList;
    }

    @Override
    public int getCount() {
        return materialsList.size();
    }

    @Override
    public Object getItem(int position) {
        return materialsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.material_request_item_layout, null);
        }

        TextView tvMaterialName = convertView.findViewById(R.id.tvMaterialName);
        ImageView ivRemove = convertView.findViewById(R.id.ivRemove);

        // Display the file name (extracted from the URI)
        String fileName = materialsList.get(position).getLastPathSegment();
        tvMaterialName.setText(fileName != null ? fileName : "Unknown File");

        // Set a click listener for the remove icon
        ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialsList.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}


