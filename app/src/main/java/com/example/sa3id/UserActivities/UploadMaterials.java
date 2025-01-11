package com.example.sa3id.UserActivities;

import static com.example.sa3id.Constants.FIREBASE_REALTIME_LINK;
import static com.example.sa3id.UploadRequest.getCurrentTimestamp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.MaterialRequestItemAdapter;
import com.example.sa3id.R;
import com.example.sa3id.UploadRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class UploadMaterials extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private String requestTimeStamp;

    private EditText etDescription;
    private Button btnSendRequest;
    private ImageView btnChooseFiles, btnClearMaterialsList;

    private RecyclerView rvMaterialsList;
    private ArrayList<Uri> selectedFilesList;
    private MaterialRequestItemAdapter materialAdapter;

    private final int FILE_REQUEST_CODE = 10;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_upload_materials;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            //AlertDialog
            Toast.makeText(this, "No user logged In!!!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignIn.class));
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        etDescription = findViewById(R.id.etDescription);
        btnChooseFiles = findViewById(R.id.btnChooseFiles);
        btnClearMaterialsList = findViewById(R.id.btnClearMaterialsList);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        rvMaterialsList = findViewById(R.id.rvMaterialsList);
        rvMaterialsList.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns
        selectedFilesList = new ArrayList<>();
        materialAdapter = new MaterialRequestItemAdapter(this, selectedFilesList);
        rvMaterialsList.setAdapter(materialAdapter);

        btnChooseFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFiles();
            }
        });

        btnClearMaterialsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedFilesList.clear();
                materialAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "تم مسح جميع العناصر", Toast.LENGTH_SHORT).show();
            }
        });

        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUploadRequest();
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_REALTIME_LINK);
        storageReference = FirebaseStorage.getInstance().getReference("Materials");
        databaseReference = firebaseDatabase.getReference("UploadRequests");
    }

    private void chooseFiles() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Files"), FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        selectedFilesList.add(fileUri);
                    }
                } else if (data.getData() != null) { // Single file selected
                    Uri fileUri = data.getData();
                    selectedFilesList.add(fileUri);
                }
                materialAdapter.notifyDataSetChanged();
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void sendUploadRequest() {
        String senderEmail = firebaseUser.getEmail();
        requestTimeStamp = getCurrentTimestamp();
        if (senderEmail == null) {
            //AlertDialog
            Toast.makeText(this, "userEmail null", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!selectedFilesList.isEmpty()) {
            String description = etDescription.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(this, "الرجاء اضافة وصف", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> fileUrls = new ArrayList<>();
            for (Uri fileUri : selectedFilesList) {
                String fileName = System.currentTimeMillis() + "." + getFileExtension(fileUri);
                StorageReference fileReference = storageReference.child(senderEmail + " " + requestTimeStamp).child(fileName);
                fileReference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                fileUrls.add(uri.toString());
                                if (fileUrls.size() == selectedFilesList.size()) {
                                    saveRequestToDatabase(description, fileUrls);
                                }
                            }
                        });
                    }
                });
            }
        } else {
            Toast.makeText(this, "No files selected to upload.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRequestToDatabase(String description, ArrayList<String> fileUrls) {
        UploadRequest uploadRequest = new UploadRequest(description, fileUrls, firebaseUser.getEmail(), requestTimeStamp);

        DatabaseReference newRequestRef = databaseReference.push();
        newRequestRef.setValue(uploadRequest);
        Toast.makeText(this, "Request sent successfully.", Toast.LENGTH_SHORT).show();
        selectedFilesList.clear();
        materialAdapter.notifyDataSetChanged();
        etDescription.setText("");
    }
}
