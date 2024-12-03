package com.example.sa3id.UserActivities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.sa3id.Material;

public class UploadMaterials extends BaseActivity {

    FirebaseDatabase firebaseDatabase;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    EditText etMatName, etMatDescription;
    Uri filePathUri;
    Button btnChooseFile, btnSend;
    ImageView imgMaterial;
    final int IMAGE_REQUEST_CODE = 7;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_upload_materials;
    }

    //I have to add new material type only for firebase suggestions to admins seperate from drive material class


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        //setContentView(R.layout.activity_upload_materials);
//
//
//        Button btnShrek = findViewById(R.id.btnShrek);
//        btnShrek.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(UploadMaterials.this,"i love shrek", Toast.LENGTH_SHORT).show();
//                databaseReference = FirebaseDatabase.getInstance().getReference();
//                databaseReference.child("garis").setValue("shrek");
//
//            }
//        });
//
//
//
//        initViews();
//
//
//    }
//
//    private void initViews() {
//        etMatDescription = findViewById(R.id.etMatDescription);
//        etMatName = findViewById(R.id.etMatName);
//        imgMaterial = findViewById(R.id.image_material);
//        btnChooseFile = findViewById(R.id.btnChooseFile);
//        btnSend = findViewById(R.id.btnSend);
//        btnChooseFile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                chooseImage();
//            }
//        });
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                uploadFile();
//                Toast.makeText(getApplicationContext(), "saving", Toast.LENGTH_SHORT).show();
//            }
//        });
//        firebaseDatabase = FirebaseDatabase.getInstance();
//        storageReference = FirebaseStorage.getInstance().getReference("Materials");
//        databaseReference = FirebaseDatabase.getInstance().getReference("MaterialsFolder");
//
//
//    }
//    private void chooseImage(){
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent,"SelectImage"), IMAGE_REQUEST_CODE);
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
//            filePathUri = data.getData();
//            imgMaterial.setImageURI(filePathUri);
//        }
//    }
//
//    public String getFileExtension(Uri uri) {
//        ContentResolver contentResolver = UploadMaterials.this.getContentResolver();
//        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
//    }
//
//    public void uploadFile() {
//        if (filePathUri != null) {
//            String fileName = System.currentTimeMillis() + "." + getFileExtension(filePathUri);
//            StorageReference fileReference = storageReference.child(fileName);
//            fileReference.putFile(filePathUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            String url = uri.toString();
//                            String matName = etMatName.getText().toString().trim();
//                            String matDescription = etMatDescription.getText().toString().trim();
//
//                            // Create a Material object with name, description, and download URL
//                            Material material = new Material(matName, matDescription, url);
//
//                            // Store the material object in the Firebase Realtime Database
//                            databaseReference = firebaseDatabase.getReference("MaterialsFolder").push();
//                            material.setKey(databaseReference.getKey());
//                            databaseReference.setValue(material);
//                        }
//                    });
//                }
//            });
//        }
//    }


}