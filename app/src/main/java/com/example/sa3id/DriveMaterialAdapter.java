package com.example.sa3id;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.UserActivities.MaterialsPage;
import com.google.api.services.drive.model.File;

import java.util.List;

public class DriveMaterialAdapter extends RecyclerView.Adapter<DriveMaterialAdapter.DriveMaterialViewHolder> {

    private List<DriveMaterial> materialItems;
    Context context;
    public DriveMaterialAdapter(List<DriveMaterial> materialItems) {
        this.materialItems = materialItems;
    }

    @NonNull
    @Override
    public DriveMaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drive_material_layout, parent, false);
        return new DriveMaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriveMaterialViewHolder holder, int position) {
        DriveMaterial item = materialItems.get(position);
        holder.title.setText(item.getTitle());
        holder.type.setText(item.getArabicType());
        holder.icon.setImageResource(item.getIconResId());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context = view.getContext();
                String type = item.getType();
                 if ("Image".equals(type)) {
                    openImage(item);
                } else if ("Folder".equals(type)) {
                    openFolder(item);
                } else if ("FolderShortcut".equals(type)) {
                     openFolderShortCut(item);
                 } else openGenericFile(item);


                ObjectAnimator colorFade = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), Color.parseColor("#000000"), Color.parseColor("#2b2a2a"));
                colorFade.setDuration(400);
                colorFade.start();
            }
        });



//        // Add hover effect on each item
//        holder.itemView.setOnHoverListener(new View.OnHoverListener() {
//            @Override
//            public boolean onHover(View view, MotionEvent motionEvent) {
//                Log.d("MaterialAdapter", "onHover called");
//                if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
//                    view.setBackgroundColor(Color.parseColor("#000000"));
//                    view.animate().alpha(0.7f).setDuration(300); // Fade-in effect
//                } else if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
//                    view.setBackgroundColor(Color.parseColor("#2b2a2a"));
//                    view.animate().alpha(1f).setDuration(300); // Fade-out effect
//                }
//                return false;
//            }
//        });

    }

    private void openFolderShortCut(DriveMaterial item) {
        String shortcutId = item.getId();

        new Thread(() -> {
            try {
                File file = ((MaterialsPage) context).getGoogleDriveService().files()
                        .get(shortcutId)
                        .setFields("id,shortcutDetails(targetId)") // Request shortcut details
                        .execute();

                String actualFolderId = file.getShortcutDetails().getTargetId();

                Log.d("MaterialAdapter", "Resolved folder shortcut: " + actualFolderId);

                ((MaterialsPage) context).runOnUiThread(() ->
                        ((MaterialsPage) context).listFilesInFolder(actualFolderId)
                );
            } catch (Exception e) {
                Log.e("MaterialAdapter", "Error resolving shortcut", e);
                ((MaterialsPage) context).runOnUiThread(() ->
                        Toast.makeText(context, "Failed to open folder shortcut", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }




    private void openGenericFile(DriveMaterial item) {
        String fileId = item.getId();
        String pdfUrl = "https://drive.google.com/uc?id=" + fileId;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Optional
        context.startActivity(intent);
    }


    private void openImage(DriveMaterial item) {
        // Open Image file using an Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(item.getUrl()), "image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }


    private void openFolder(DriveMaterial item) {
        String folderId = item.getId();
        MaterialsPage activity = (MaterialsPage) context;
        activity.listFilesInFolder(folderId);
    }


    @Override
    public int getItemCount() {
        return materialItems.size();
    }

    static class DriveMaterialViewHolder extends RecyclerView.ViewHolder {
        TextView title, type;
        ImageView icon;

        public DriveMaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.materialTitle);
            type = itemView.findViewById(R.id.materialType);
            icon = itemView.findViewById(R.id.materialIcon);
        }
    }


}
