package com.example.test4;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageUtil {

    // Chuyển đổi ImageProxy từ YUV_420_888 sang NV21
    public static Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        // Truy xuất các plane từ ImageProxy
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        // Lấy kích thước của các buffer
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        // Mảng để chứa dữ liệu NV21
        byte[] nv21 = new byte[ySize + uSize + vSize];

        // Sao chép dữ liệu từ các planes vào mảng NV21
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize); // Lưu V trước
        uBuffer.get(nv21, ySize + vSize, uSize); // Sau đó là U

        // Tạo YuvImage với định dạng NV21
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);

        // Chuyển YuvImage thành JPEG
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, imageProxy.getWidth(), imageProxy.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();

        // Chuyển đổi byte[] thành Bitmap
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
