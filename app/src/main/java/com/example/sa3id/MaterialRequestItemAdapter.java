package com.example.sa3id;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MaterialRequestItemAdapter extends RecyclerView.Adapter<MaterialRequestItemAdapter.MaterialViewHolder> {

    private Context context;
    private ArrayList<Uri> materialsList;

    public MaterialRequestItemAdapter(Context context, ArrayList<Uri> materialsList) {
        this.context = context;
        this.materialsList = materialsList;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.material_request_item_layout, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        Uri fileUri = materialsList.get(position);
        String fileName = fileUri.getLastPathSegment();

        if (fileName == null) {
            fileName = "ملف";
        }

        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(fileUri.toString()));

        if (mimeType != null) {
            if (mimeType.contains("pdf") || fileName.endsWith("pdf")) {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_pdf);
            } else if (fileName.endsWith("doc") || fileName.endsWith("docx")) {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_word);
            } else if (fileName.endsWith("pptx") || fileName.endsWith("ppt")) {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_powerpoint);
            } else if (fileName.endsWith("xls") || fileName.endsWith("xlsx")) {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_excel);
            } else if (mimeType.contains("image")) {
                if (fileName.endsWith(".gif")) {
                    Glide.with(context)
                            .asGif()
                            .load(fileUri)
                            .into(holder.ivMaterialIcon);
                } else {
                    holder.ivMaterialIcon.setImageURI(fileUri);
                }
                fileName = "صورة";
            } else {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_generic_file);
            }
        } else {
            holder.ivMaterialIcon.setImageResource(R.drawable.ic_generic_file);
        }

        // trim the filename
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            fileName = fileName.substring(0, dotIndex);
        }

        holder.tvMaterialName.setText(fileName);

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int positionToDelete = holder.getAdapterPosition();
                if (positionToDelete != RecyclerView.NO_POSITION) {
                    materialsList.remove(positionToDelete);
                    notifyItemRemoved(positionToDelete);
                    notifyItemRangeChanged(positionToDelete, materialsList.size());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return materialsList.size();
    }

    public static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaterialName;
        ImageView ivMaterialIcon;
        ImageView ivDelete;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaterialName = itemView.findViewById(R.id.tvMaterialName);
            ivMaterialIcon = itemView.findViewById(R.id.ivMaterialIcon);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
