package com.example.sa3id.userActivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.dialogs.CustomAlertDialog;
import com.example.sa3id.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileEditActivity extends BaseActivity {

    private ImageView profilePicture;
    private FloatingActionButton fabEditPicture;
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private Button btnSaveChanges;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri selectedImageUri = null;
    private String currentProfilePicUrl = null;
    private CustomAlertDialog customAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        customAlertDialog = new CustomAlertDialog(this);

        initViews();
        setupClickListeners();
        loadCurrentUserProfile();
    }

    private void initViews() {
        profilePicture = findViewById(R.id.profile_pic);
        fabEditPicture = findViewById(R.id.edit_profile_pic);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
    }

    private void setupClickListeners() {
        fabEditPicture.setOnClickListener(v -> showImageSelectionDialog());
        btnSaveChanges.setOnClickListener(v -> validateAndSaveChanges());
    }

    private void validateAndSaveChanges() {
        String username = etUsername.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            customAlertDialog.show("يرجى إدخال اسم المستخدم", R.drawable.baseline_error_24);
            return;
        }

        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("جاري حفظ التغييرات...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // If there's a new image selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSaveUser(username, loadingDialog);
        } else {
            // Just update the user details
            updateUserDetails(username, currentProfilePicUrl, loadingDialog);
        }
    }

    private void showImageSelectionDialog() {
        String[] options = {"الكاميرا", "المعرض", "إلغاء"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اختر صورة الملف الشخصي");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    openCamera();
                    break;
                case 1:
                    openGallery();
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Profile Picture", null);
        return Uri.parse(path);
    }

    private void loadCurrentUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            customAlertDialog.show("يرجى تسجيل الدخول أولاً", R.drawable.baseline_error_24);
            finish();
            return;
        }

        // Set email (non-editable)
        etEmail.setText(currentUser.getEmail());

        firestore.collection("Users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load username
                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            etUsername.setText(username);
                        }

                        // Load profile picture
                        currentProfilePicUrl = documentSnapshot.getString("profilePic");
                        if (currentProfilePicUrl != null && !currentProfilePicUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(currentProfilePicUrl)
                                    .placeholder(R.drawable.profile_pic)
                                    .error(R.drawable.profile_pic)
                                    .into(profilePicture);
                        }
                    }
                })
                .addOnFailureListener(e -> customAlertDialog.show("فشل في تحميل الملف الشخصي", R.drawable.baseline_error_24));
    }

    private void uploadImageAndSaveUser(String username, AlertDialog loadingDialog) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadingDialog.dismiss();
            return;
        }

        String filename = "profile_images/" + currentUser.getUid() + "_" + UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(filename);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        updateUserDetails(username, imageUrl, loadingDialog);
                    }).addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        customAlertDialog.show("فشل في الحصول على رابط الصورة", R.drawable.baseline_error_24);
                    });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    customAlertDialog.show("فشل في رفع الصورة: " + e.getMessage(), R.drawable.baseline_error_24);
                });
    }

    private void updateUserDetails(String username, String profilePicUrl, AlertDialog loadingDialog) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadingDialog.dismiss();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        if (profilePicUrl != null) {
            updates.put("profilePic", profilePicUrl);
        }

        firestore.collection("Users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismiss();
                    customAlertDialog.show("تم تحديث الملف الشخصي بنجاح", R.drawable.baseline_check_circle_24);
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    customAlertDialog.show("فشل في تحديث الملف الشخصي: " + e.getMessage(), R.drawable.baseline_error_24);
                });
    }

    // Activity result launchers for camera and gallery
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    profilePicture.setImageBitmap(imageBitmap);
                    selectedImageUri = getImageUri(imageBitmap);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        profilePicture.setImageBitmap(bitmap);
                        selectedImageUri = imageUri;
                    } catch (IOException e) {
                        e.printStackTrace();
                        customAlertDialog.show("فشل في تحميل الصورة", R.drawable.baseline_error_24);
                    }
                }
            });

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile_edit;
    }
}