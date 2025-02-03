package com.example.test4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.Interpreter;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveDetectionActivity extends AppCompatActivity {

    private static final String TAG = "LiveDetectionActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int MODEL_INPUT_SIZE = 640;
    private PreviewView previewView;
    private BoundingBoxOverlay overlayView;
    private Interpreter tflite;
    private String[] labels;
    private ExecutorService executorService;
    private ProcessCameraProvider cameraProvider;
    private volatile boolean isAnalyzing = false;

    // Khai báo TextView
    private TextView tvMessage;

    private HandlerThread analysisThread;
    private Handler analysisHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_detection);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);

        tvMessage = findViewById(R.id.tv_message);

        // Tạo HandlerThread cho phân tích hình ảnh
        analysisThread = new HandlerThread("ImageAnalysisThread");
        analysisThread.start();
        analysisHandler = new Handler(analysisThread.getLooper());

        executorService = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            loadModel();
            loadLabels();
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadModel();
            loadLabels();
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadModel() {
        try {
            MappedByteBuffer modelFile = loadModelFile("best_float32.tflite");
            tflite = new Interpreter(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLabels() {
        try {
            InputStream is = getAssets().open("labels.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            labels = reader.lines().toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load labels", Toast.LENGTH_SHORT).show();
        }
    }

    private MappedByteBuffer loadModelFile(String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreviewAndAnalysis(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreviewAndAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(new android.util.Size(MODEL_INPUT_SIZE, MODEL_INPUT_SIZE))
                .build();

        imageAnalysis.setAnalyzer(executorService, this::analyzeImage);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void analyzeImage(ImageProxy imageProxy) {
        if (isAnalyzing) {
            imageProxy.close();
            return;
        }

        isAnalyzing = true;
        analysisHandler.post(() -> {
            Bitmap bitmap = ImageUtil.imageProxyToBitmap(imageProxy);
            if (bitmap != null) {
                detectObjects(bitmap);
            }
            imageProxy.close();
            isAnalyzing = false;
        });
    }


    private void detectObjects(Bitmap bitmap) {
        // Resize bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);

        // Chuẩn bị buffer đầu vào
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        for (int y = 0; y < MODEL_INPUT_SIZE; y++) {
            for (int x = 0; x < MODEL_INPUT_SIZE; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                inputBuffer.putFloat(Color.red(pixel) / 255.0f);
                inputBuffer.putFloat(Color.green(pixel) / 255.0f);
                inputBuffer.putFloat(Color.blue(pixel) / 255.0f);
            }
        }

        // Chuẩn bị đầu ra
        float[][][] output = new float[1][300][6];
        tflite.run(inputBuffer, output);

        // Cập nhật UI
        runOnUiThread(() -> processDetections(output));
    }

    private void processDetections(float[][][] output) {
        List<BoundingBoxOverlay.DetectionResult> detections = new ArrayList<>();

        float scaleX = (float) previewView.getWidth() / MODEL_INPUT_SIZE;
        float scaleY = (float) previewView.getHeight() / MODEL_INPUT_SIZE;

        for (float[] box : output[0]) {
            float confidence = box[4];
            if (confidence > 0.5) { // Ngưỡng confidence
                int classId = (int) box[5];
                float left = box[0] * previewView.getWidth();
                float top = box[1] * previewView.getHeight();
                float right = box[2] * previewView.getWidth();
                float bottom = box[3] * previewView.getHeight();

                String label = labels[classId];
                String text = label + String.format(" (%.2f)", confidence);

                BoundingBoxOverlay.DetectionResult detectionResult = new BoundingBoxOverlay.DetectionResult(
                        new RectF(left, top, right, bottom),
                        text
                );
                detections.add(detectionResult);
            }
        }

        // Kiểm tra danh sách phát hiện
        if (detections.isEmpty()) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("No objects detected");
        } else {
            tvMessage.setVisibility(View.GONE); // Ẩn TextView khi có đối tượng
        }

        // Vẽ bounding box
        overlayView.setDetections(detections);
    }




    private void drawBoundingBoxes(float[][][] output) {
        List<BoundingBoxOverlay.DetectionResult> detections = new ArrayList<>();

        for (float[] box : output[0]) {
            float confidence = box[4];
            if (confidence > 0.5) {  // Thay đổi ngưỡng nếu cần
                int classId = (int) box[5];
                float left = box[0] * previewView.getWidth();
                float top = box[1] * previewView.getHeight();
                float right = box[2] * previewView.getWidth();
                float bottom = box[3] * previewView.getHeight();

                BoundingBoxOverlay.DetectionResult detectionResult = new BoundingBoxOverlay.DetectionResult(
                        new RectF(left, top, right, bottom),
                        labels[classId] + " " + String.format("%.2f", confidence)
                );
                detections.add(detectionResult);
            }
        }

        overlayView.setDetections(detections);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
        executorService.shutdown();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    public void onBackPressed() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        super.onBackPressed();  // Gọi phương thức super để xử lý hành động quay lại mặc định
    }

}