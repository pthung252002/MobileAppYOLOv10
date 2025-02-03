package com.example.test4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private Interpreter tflite;

    private ImageView imageView;
    private TextView txt_diceas;
    private TextView txt_conf;
    private TextView txt_des;

    // RecyclerView cho Causes và Preventions
    private RecyclerView recyclerViewCauses;
    private RecyclerView recyclerViewPreventions;

    private List<String> labels;

    // Khai báo DatabaseHelper
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView);
        txt_diceas = findViewById(R.id.txt_dicease);
        txt_conf = findViewById(R.id.txt_conf);
        txt_des = findViewById(R.id.txt_des);

        // Khai báo RecyclerView
        recyclerViewCauses = findViewById(R.id.recyclerViewCauses);
        recyclerViewPreventions = findViewById(R.id.recyclerViewPreventions);

        // Thiết lập LayoutManager cho RecyclerView
        recyclerViewCauses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPreventions.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Nhận Uri của hình ảnh đã cắt từ ProcessImageActivity
        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("croppedImageUri");
        Uri imageUri = Uri.parse(imageUriString);

        // Hiển thị hình ảnh được cắt
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));

            // Tải file nhãn từ assets
            loadLabels();

            // Tải mô hình YOLOv10
            Interpreter.Options options = new Interpreter.Options();
            tflite = new Interpreter(loadModelFile("best_float32.tflite"), options);

            // Chuẩn bị ảnh để làm đầu vào cho mô hình
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true);
            float[][][][] input = bitmapToInputArray(resizedBitmap);

            // Dự đoán kết quả
            float[][][] output = new float[1][300][6];  // Mảng chứa kết quả [1, 300, 6]
            tflite.run(input, output);

            // Vẽ bounding box lên ảnh
            Bitmap bitmapWithBoxes = drawBoundingBoxes(bitmap, output);
            imageView.setImageBitmap(bitmapWithBoxes);

            // Hiển thị kết quả dự đoán
            displayResults(output);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float[][][][] bitmapToInputArray(Bitmap bitmap) {
        int batchSize = 1;
        int imageHeight = 640;
        int imageWidth = 640;
        int channels = 3;
        float[][][][] input = new float[batchSize][imageHeight][imageWidth][channels];

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int pixel = bitmap.getPixel(x, y);
                input[0][y][x][0] = (pixel >> 16 & 0xFF) / 255.0f;  // Red
                input[0][y][x][1] = (pixel >> 8 & 0xFF) / 255.0f;   // Green
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;        // Blue
            }
        }
        return input;
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(getAssets().openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = getAssets().openFd(modelPath).getStartOffset();
        long declaredLength = getAssets().openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void loadLabels() {
        labels = new ArrayList<>();
        try (InputStream inputStream = getAssets().open("labels.txt")) {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            String[] labelArray = new String(buffer).split("\n");
            for (String label : labelArray) {
                labels.add(label.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayResults(float[][][] output) {
        float maxConfidence = 0;
        int bestClassIndex = -1;

        for (int i = 0; i < output[0].length; i++) {
            float confidence = output[0][i][4];
            if (confidence > maxConfidence) {
                maxConfidence = confidence;
                bestClassIndex = (int) output[0][i][5];
            }
        }

        if (bestClassIndex != -1 && maxConfidence > 0.5) {
            if (bestClassIndex < labels.size()) {
                txt_diceas.setText(labels.get(bestClassIndex));

                String description = databaseHelper.getDiseaseDescription(bestClassIndex);
                if (description != null && description.length() > 0) {
                    txt_des.setText(String.join("\n", description));
                } else {
                    txt_des.setText("No description available");
                }

                String[] causes = databaseHelper.getCauseDescriptions(bestClassIndex);
                String[] preventions = databaseHelper.getPreventDescriptions(bestClassIndex);

                // Kiểm tra và thay đổi causesList và preventionsList nếu cần
                List<String> causesList = (causes != null && causes.length > 0) ? Arrays.asList(causes) : Arrays.asList("No causes available");
                List<String> preventionsList = (preventions != null && preventions.length > 0) ? Arrays.asList(preventions) : Arrays.asList("No preventions available");

                // Tạo adapter cho RecyclerView
                CustomRecyclerAdapter causesAdapter = new CustomRecyclerAdapter(this, causesList, ContextCompat.getColor(this, R.color.black));
                recyclerViewCauses.setAdapter(causesAdapter);
                causesAdapter.notifyDataSetChanged(); // Gọi notifyDataSetChanged() khi dữ liệu thay đổi

                CustomRecyclerAdapter preventionsAdapter = new CustomRecyclerAdapter(this, preventionsList, ContextCompat.getColor(this, R.color.black));
                recyclerViewPreventions.setAdapter(preventionsAdapter);
                preventionsAdapter.notifyDataSetChanged(); // Gọi notifyDataSetChanged() khi dữ liệu thay đổi

            } else {
                txt_diceas.setText("Unknown");
            }
            txt_conf.setText(String.format("%.2f", maxConfidence));
        } else {
            txt_diceas.setText("Oop!! Nothing detected");
            txt_conf.setText("");

            // Tạo adapter cho RecyclerView
            CustomRecyclerAdapter causesAdapter = new CustomRecyclerAdapter(this, Arrays.asList("No causes available"), ContextCompat.getColor(this, R.color.black));
            recyclerViewCauses.setAdapter(causesAdapter);
            causesAdapter.notifyDataSetChanged(); // Gọi notifyDataSetChanged() khi dữ liệu thay đổi

            CustomRecyclerAdapter preventionsAdapter = new CustomRecyclerAdapter(this, Arrays.asList("No preventions available"), ContextCompat.getColor(this, R.color.black));
            recyclerViewPreventions.setAdapter(preventionsAdapter);
            preventionsAdapter.notifyDataSetChanged(); // Gọi notifyDataSetChanged() khi dữ liệu thay đổi

        }

    }

    private Bitmap drawBoundingBoxes(Bitmap bitmap, float[][][] output) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);

        float maxConfidence = 0f;
        int maxIndex = -1;

        // Tìm đối tượng có độ tin cậy cao nhất
        for (int i = 0; i < output[0].length; i++) {
            float confidence = output[0][i][4];
            if (confidence > maxConfidence) {
                maxConfidence = confidence;
                maxIndex = i;
            }
        }

        // Vẽ bounding box cho đối tượng có độ tin cậy cao nhất
        if (maxIndex != -1 && maxConfidence > 0.5) {
            int classIndex = (int) output[0][maxIndex][5];
            float xMin = output[0][maxIndex][0] * bitmap.getWidth();
            float yMin = output[0][maxIndex][1] * bitmap.getHeight();
            float xMax = output[0][maxIndex][2] * bitmap.getWidth();
            float yMax = output[0][maxIndex][3] * bitmap.getHeight();

            canvas.drawRect(xMin, yMin, xMax, yMax, paint);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.GREEN);
            textPaint.setTextSize(40f);
            canvas.drawText(labels.get(classIndex) + " " + maxConfidence, xMin, yMin - 10, textPaint);
        }

        return mutableBitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Gán giá trị rỗng cho các TextView và RecyclerView khi Activity bị ẩn hoặc khi thoát
        txt_diceas.setText("");
        txt_conf.setText("");

    }

}
