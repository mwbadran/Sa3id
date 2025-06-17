package com.example.sa3id.userActivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sa3id.R;
import com.example.sa3id.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class GoogleSignUpActivity extends AppCompatActivity {
    private EditText etUsername;
    private ImageView profileImageView;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String userId, email, displayName;
    private Uri selectedImageUri = null;
    private String googleProfilePicUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_up);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");
        displayName = getIntent().getStringExtra("displayName");
        googleProfilePicUrl = getIntent().getStringExtra("profilePicUrl");

        etUsername = findViewById(R.id.etUsername);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        profileImageView = findViewById(R.id.profile_image);

        if (displayName != null && !displayName.isEmpty()) {
            etUsername.setText(displayName);
        }

        if (googleProfilePicUrl != null && !googleProfilePicUrl.isEmpty()) {
            Picasso.get()
                    .load(googleProfilePicUrl)
                    .placeholder(R.drawable.profile_pic)
                    .error(R.drawable.profile_pic)
                    .into(profileImageView);
        }

        profileImageView.setOnClickListener(v -> showImageSelectionDialog());

        btnSubmit.setOnClickListener(v -> validateAndSaveUser());
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
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void validateAndSaveUser() {
        String username = etUsername.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, R.string.username_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("جاري الحفظ...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        if (selectedImageUri != null) {
            // User selected a new image, upload it first, then save user data
            uploadImageAndSaveUser(username, loadingDialog);
        } else if (googleProfilePicUrl != null && !googleProfilePicUrl.isEmpty()) {
            // No new image selected, but Google profile pic is available
            saveUserToFirestore(username, googleProfilePicUrl, loadingDialog);
        } else {
            // Save user without image
            saveUserToFirestore(username, null, loadingDialog);
        }
    }

    private void uploadImageAndSaveUser(String username, AlertDialog loadingDialog) {
        String filename = "profile_images/" + userId + "_" + UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child(filename);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveUserToFirestore(username, imageUrl, loadingDialog);
                    }).addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        Toast.makeText(GoogleSignUpActivity.this, "فشل في الحصول على رابط الصورة", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(GoogleSignUpActivity.this, "فشل في رفع الصورة: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserToFirestore(String username, String profilePicUrl, AlertDialog loadingDialog) {
        User user = new User(username, email, profilePicUrl);

        if (profilePicUrl != null) {
            user.setProfilePicUrl(profilePicUrl);
        }

        firestore.collection("Users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismiss();
                    Intent intent = new Intent(this, ChooseBagrutsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "خطأ في الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // for camera and gallery
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    profileImageView.setImageBitmap(imageBitmap);

                    selectedImageUri = getImageUri(imageBitmap);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), imageUri);
                        profileImageView.setImageBitmap(bitmap);
                        selectedImageUri = imageUri;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "فشل في تحميل الصورة", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "يرجى إكمال عملية التسجيل", Toast.LENGTH_SHORT).show();
    }
}