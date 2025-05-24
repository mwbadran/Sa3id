package com.example.sa3id.userActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.models.DriveMaterial;
import com.example.sa3id.adapters.DriveMaterialAdapter;
import com.example.sa3id.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


public class MaterialsPage extends BaseActivity {

    private String rootFolder = "1b6dJPYVzU0ZGO9Zlj7De2GDRwzpNf4NT";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private Drive googleDriveService;
    private RecyclerView recyclerView;
    private DriveMaterialAdapter adapter;
    private List<DriveMaterial> materialItemList;
    private Stack<String> folderStack;
    Intent backIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backIntent = getIntent();
        rootFolder = backIntent.getStringExtra("rootFolderId");

        requestSignIn();

        folderStack = new Stack<>();
        initViews();
    }

    @Override
    protected boolean handleChildBackPress() {
        //Toast.makeText(this, String.valueOf(folderStack.size()), Toast.LENGTH_SHORT).show();
        if (!folderStack.isEmpty()) {
            folderStack.pop();

            if (!folderStack.isEmpty()) {
                String previousFolderId = folderStack.pop();
                listFilesInFolder(previousFolderId);
                return true;
            }

        }
        finish();
        return true;
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);

        recyclerView.addItemDecoration(dividerItemDecoration);

        materialItemList = new ArrayList<>();
        //populateMaterials();

        adapter = new DriveMaterialAdapter(materialItemList);
        recyclerView.setAdapter(adapter);
    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_READONLY))
                        .requestEmail()
                        .build();

        //GoogleSignIn.getClient(this, signInOptions).signOut(); // Clear previous sign-in

        startActivityForResult( GoogleSignIn.getClient(this, signInOptions).getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this, Collections.singleton(DriveScopes.DRIVE_READONLY));
            credential.setSelectedAccount(account.getAccount());

            googleDriveService = new Drive.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    new com.google.api.client.json.gson.GsonFactory(),
                    credential)
                    .setApplicationName("Sa3id")
                    .build();

            listFilesInFolder(rootFolder);
        } catch (Exception e) {
            Log.e("MaterialsPage", "Sign-in failed", e);
        }
    }


    public void listFilesInFolder(String folderId) {

            folderStack.push(folderId);

        new Thread(() -> {
            try {
                // Query Google Drive for files in the folder
                FileList result = googleDriveService.files().list()
                        .setQ("'" + folderId + "' in parents and trashed=false")
                        .setFields("files(id, name, mimeType, webViewLink)")
                        .execute();

                List<File> files = result.getFiles();


                runOnUiThread(() -> {
                    materialItemList.clear();


                    for (File file : files) {

                        int iconResDrawable = R.drawable.ic_generic_file; // default icon

                        String mimeType = file.getMimeType();
                        String fileId = file.getId();
                        String fileName = file.getName();
                        String fileUrl = file.getWebViewLink();

                        String type = mimeType; //default name
                        String arabicType = type;

                        if (mimeType.contains("pdf") || fileName.endsWith(".pdf")) {
                            type = "PDF";
                            arabicType = "ملف PDF";
                            iconResDrawable = R.drawable.ic_pdf;
                        } else if (mimeType.contains("msword") || mimeType.contains("formats-officedocument.wordprocessingml.document")) {
                            type = "Word";
                            arabicType = "مستند Word";
                            iconResDrawable = R.drawable.ic_word;
                        }
                        else if (mimeType.contains("powerpoint") || mimeType.contains("formats-officedocument.presentationml.presentation")) {
                            type = "PowerPoint";
                            arabicType = "عارضة PowerPoint";
                            iconResDrawable = R.drawable.ic_powerpoint;
                        }
                        else if (mimeType.contains("excel") || mimeType.contains("formats-officedocument.spreadsheetml.sheet")) {
                            type = "Excel";
                            arabicType = "Excel";
                            iconResDrawable = R.drawable.ic_excel;
                        }
                        else if (mimeType.contains("image")) {
                            type = "Image";
                            arabicType = "صورة";
                            iconResDrawable = R.drawable.ic_image;
                        } else if (mimeType.contains("folder")) {
                            type = "Folder";
                            arabicType = "مجلد";
                            iconResDrawable = R.drawable.ic_directory;
                        } else if (mimeType.contains("vnd.google-apps.shortcut")) {
                            type = "FolderShortcut";
                            arabicType = "مجلد";
                            iconResDrawable = R.drawable.ic_directory;
                        }


                        DriveMaterial material = new DriveMaterial(fileName, type, arabicType, iconResDrawable, fileUrl,fileId);
                        //Toast.makeText(this, fileUrl, Toast.LENGTH_SHORT).show();

                        materialItemList.add(material);
                    }

                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                //network errors...
                Log.e("MaterialsPage", "Error listing files in folder", e);
            }
        }).start();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_materials_page;
    }

    private void populateMaterials() {
        //example for testing
        materialItemList.add(new DriveMaterial("Introduction to Math", "PDF", R.drawable.ic_pdf));
        materialItemList.add(new DriveMaterial("Chemistry Notes", "Document", R.drawable.ic_generic_file));
        materialItemList.add(new DriveMaterial("Biology Diagrams", "Image", R.drawable.ic_image));
    }

    public Drive getGoogleDriveService() {
        return googleDriveService;
    }
}

