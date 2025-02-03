// ProcessImageActivity.java
package com.example.test4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

import java.io.File;

public class ProcessImageActivity extends AppCompatActivity {

    private Uri croppedImageUri;
    private Uri originalImageUri; // Lưu Uri ảnh gốc
    private ImageView imageView; // Hiển thị ảnh đã cắt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);

        // Khởi tạo ImageView
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.VISIBLE);

        // Lấy Uri từ Intent
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            originalImageUri = Uri.parse(imageUriString);

            // Khởi động UCrop để cắt ảnh
            croppedImageUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.jpg"));
            UCrop.of(originalImageUri, croppedImageUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(640, 640)
                    .start(ProcessImageActivity.this);
        }

        // Xác nhận ảnh đã cắt và chuyển sang MainActivity2
        Button cropButton = findViewById(R.id.btn_crop);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem có Uri cắt ảnh không
                if (croppedImageUri != null) {
                    Intent resultIntent = new Intent(ProcessImageActivity.this, MainActivity2.class);
                    resultIntent.putExtra("croppedImageUri", croppedImageUri.toString());
                    startActivity(resultIntent);
                    finish();
                } else {
                    Toast.makeText(ProcessImageActivity.this, "Please crop the image first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Kiểm tra kết quả từ UCrop
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            croppedImageUri = UCrop.getOutput(data);
            // Hiển thị ảnh đã cắt
            if (croppedImageUri != null) {
                imageView.setImageURI(croppedImageUri);
                imageView.setVisibility(View.VISIBLE); // Hiện ImageView khi có ảnh cắt
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                cropError.printStackTrace();
            }
            Toast.makeText(this, "Error while cropping image.", Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_CANCELED) {
            // Nếu người dùng hủy bỏ việc cắt ảnh, quay lại MainActivity
            Intent intent = new Intent(ProcessImageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
