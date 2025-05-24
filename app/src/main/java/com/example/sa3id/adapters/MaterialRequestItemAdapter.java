package com.example.sa3id.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
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
import com.example.sa3id.R;

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
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(cR.getType(fileUri));

        if (fileName == null) {
            fileName = "ملف";
        }

        //Toast.makeText(context, type, Toast.LENGTH_SHORT).show();
        if (type != null) {
            if (type.contains("pdf") || fileName.endsWith("pdf")) {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_pdf);
            } else if (fileName.endsWith("doc") || fileName.endsWith("docx")) {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_word);
            } else if (fileName.endsWith("pptx") || fileName.endsWith("ppt")) {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_powerpoint);
            } else if (fileName.endsWith("xls") || fileName.endsWith("xlsx")) {
                holder.ivMaterialIcon.setImageResource(R.drawable.ic_excel);
            } else if (type.contains("image") || type.contains("png") || type.contains("jpg") || type.contains("jpeg") || type.contains("jfif") || type.contains("gif") || fileName.endsWith(".gif")) {
                if (type.contains("gif") || fileName.endsWith(".gif")) {
                    Glide.with(context).asGif().load(fileUri).into(holder.ivMaterialIcon);
                } else {
                    holder.ivMaterialIcon.setImageURI(fileUri);
                }
                fileName = "صورة";
            } else if (type.contains("video") || type.contains("mp4") || type.contains("avi") || type.contains("mov") || type.contains("mkv")) {
                // Load video thumbnail
                Bitmap videoThumbnail = getVideoThumbnail(fileUri);
                if (videoThumbnail != null) {
                    holder.ivMaterialIcon.setImageBitmap(videoThumbnail);
                } else {
                    holder.ivMaterialIcon.setImageResource(R.drawable.ic_generic_file); // Fallback icon
                }
                fileName = "فيديو";
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

    private Bitmap getVideoThumbnail(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            return retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC); // Get frame at 1 second
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
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
