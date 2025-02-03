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

public class OverlayView extends View {
    private List<BoundingBox> boundingBoxes = new ArrayList<>();
    private Paint paint;

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setColor(Color.RED);
    }

    public void setBoundingBoxes(List<BoundingBox> boxes) {
        this.boundingBoxes = boxes;
        invalidate(); // Yêu cầu vẽ lại
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (BoundingBox box : boundingBoxes) {
            RectF rectF = new RectF(box.left, box.top, box.right, box.bottom);
            canvas.drawRect(rectF, paint);
            canvas.drawText(box.label, box.left, box.top, paint); // Vẽ nhãn
        }
    }

    // Lớp con để lưu trữ thông tin bounding box
    public static class BoundingBox {
        public float left, top, right, bottom;
        public String label;

        public BoundingBox(float left, float top, float right, float bottom, String label) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.label = label;
        }
    }
}
