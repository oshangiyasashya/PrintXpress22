package com.printxpress.ui.admin.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AdminDonutChartView extends View {
    public static class SliceData {
        public String label;
        public float value;
        public int color;

        public SliceData(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final List<SliceData> slices = new ArrayList<>();
    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    public AdminDonutChartView(Context context) {
        super(context);
        init();
    }

    public AdminDonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        slicePaint.setStyle(Paint.Style.STROKE);
        slicePaint.setStrokeWidth(50f);

        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<SliceData> data) {
        slices.clear();
        if (data != null) {
            slices.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (slices.isEmpty()) {
            textPaint.setTextSize(32f);
            textPaint.setColor(0xFF64748B);
            canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float total = 0f;
        for (SliceData s : slices) {
            total += s.value;
        }

        if (total <= 0f) total = 1f;

        float width = getWidth();
        float height = getHeight();

        float size = Math.min(width, height) * 0.7f;
        float strokeWidth = size * 0.18f;
        slicePaint.setStrokeWidth(strokeWidth);

        float left = (width - size) / 2f;
        float top = (height - size) / 2f;
        oval.set(left, top, left + size, top + size);

        float startAngle = -90f;
        for (SliceData s : slices) {
            float sweep = (s.value / total) * 360f;
            slicePaint.setColor(s.color);
            canvas.drawArc(oval, startAngle, sweep, false, slicePaint);
            startAngle += sweep;
        }

        textPaint.setTextSize(36f);
        textPaint.setColor(0xFF0F172A);
        canvas.drawText(String.valueOf((int) total), width / 2f, (height / 2f) + 10f, textPaint);

        textPaint.setTextSize(22f);
        textPaint.setColor(0xFF64748B);
        canvas.drawText("Total Items", width / 2f, (height / 2f) + 38f, textPaint);
    }
}
