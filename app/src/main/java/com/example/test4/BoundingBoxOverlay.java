package com.example.test4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxOverlay extends View {

    private final Paint boxPaint;
    private final Paint textPaint;
    private List<DetectionResult> detections = new ArrayList<>();

    public BoundingBoxOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(5f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    public void setDetections(List<DetectionResult> detections) {
        this.detections = detections;
        invalidate();  // Refresh view to draw new bounding boxes
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (DetectionResult detection : detections) {
            // Draw bounding box
            canvas.drawRect(detection.getBoundingBox(), boxPaint);
            // Draw label with confidence score
            canvas.drawText(detection.getLabel(), detection.getBoundingBox().left, detection.getBoundingBox().top, textPaint);
        }
    }

    // Inner class to store detection results
    public static class DetectionResult {
        private final RectF boundingBox;
        private final String label;

        public DetectionResult(RectF boundingBox, String label) {
            this.boundingBox = boundingBox;
            this.label = label;
        }

        public RectF getBoundingBox() {
            return boundingBox;
        }

        public String getLabel() {
            return label;
        }
    }
}
