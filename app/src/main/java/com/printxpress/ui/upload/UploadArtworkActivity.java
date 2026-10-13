package com.printxpress.ui.upload;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import com.printxpress.util.ImageLoader;
import com.printxpress.MainActivity;
import com.printxpress.R;
import java.io.File;

public class UploadArtworkActivity extends AppCompatActivity {
    private TextView tvUploadStatus, tvProductSizeHint;
    private ImageView ivArtworkPreview;
    private Uri selectedFileUri = null;
    private Uri cameraImageUri = null;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String[]> documentLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_artwork);

        tvUploadStatus = findViewById(R.id.tvUploadStatus);
        tvProductSizeHint = findViewById(R.id.tvProductSizeHint);
        ivArtworkPreview = findViewById(R.id.ivArtworkPreview);
        Button btnPickGallery = findViewById(R.id.btnPickGallery);
        Button btnPickFiles = findViewById(R.id.btnPickFiles);
        Button btnPickCamera = findViewById(R.id.btnPickCamera);
        MaterialButton btnConfirmArtwork = findViewById(R.id.btnConfirmArtwork);

        String productName = getIntent().getStringExtra("productName");
        if (productName == null) productName = "Custom Print Product";
        tvProductSizeHint.setText("Product: " + productName);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                handleSelectedFile(uri);
            }
        });

        documentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                // Take persistable URI permission if needed
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                handleSelectedFile(uri);
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && cameraImageUri != null) {
                handleSelectedFile(cameraImageUri);
            } else {
                Toast.makeText(this, "Camera capture cancelled or failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnPickGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnPickFiles.setOnClickListener(v -> documentLauncher.launch(new String[]{"image/*", "application/pdf"}));

        btnPickCamera.setOnClickListener(v -> {
            try {
                File photoFile = new File(getFilesDir(), "camera_photos");
                if (!photoFile.exists()) {
                    boolean created = photoFile.mkdirs();
                    if (!created) {
                        Toast.makeText(this, "Could not create photo directory", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                File imageFile = new File(photoFile, "captured_" + System.currentTimeMillis() + ".jpg");
                cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
                cameraLauncher.launch(cameraImageUri);
            } catch (Exception e) {
                Toast.makeText(this, "Error launching camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        ivArtworkPreview.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                showFullSizePreview(selectedFileUri);
            }
        });

        btnConfirmArtwork.setOnClickListener(v -> {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select an artwork file first", Toast.LENGTH_SHORT).show();
                return;
            }
            long prodId = getIntent().getLongExtra("productId", 1L);
            double price = getIntent().getDoubleExtra("basePrice", 2500.0);
            int qty = getIntent().getIntExtra("quantity", 1);

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("navigateTo", "checkout");
            intent.putExtra("productId", prodId);
            intent.putExtra("basePrice", price);
            intent.putExtra("quantity", qty);
            intent.putExtra("designData", "File: " + selectedFileUri.toString());
            startActivity(intent);
            finish();
        });
    }

    private void handleSelectedFile(Uri uri) {
        ArtworkValidator.ValidationResult result = ArtworkValidator.validateFile(this, uri);
        if (!result.isValid) {
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            tvUploadStatus.setText("Error: " + result.message);
            ivArtworkPreview.setVisibility(View.GONE);
            selectedFileUri = null;
        } else {
            selectedFileUri = uri;
            tvUploadStatus.setText("Selected File: " + uri.getLastPathSegment() + "\nStatus: " + result.message + "\n(Tap preview to view fully)");
            ivArtworkPreview.setVisibility(View.VISIBLE);
            ImageLoader.load(this, uri.toString(), ivArtworkPreview, R.drawable.bg_order_gradient);
            if (result.isWarning) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showFullSizePreview(Uri uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView fullImageView = new ImageView(this);
        fullImageView.setAdjustViewBounds(true);
        fullImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int padding = 32;
        fullImageView.setPadding(padding, padding, padding, padding);
        ImageLoader.load(this, uri.toString(), fullImageView, 0);
        builder.setView(fullImageView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
