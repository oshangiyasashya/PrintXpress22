package com.printxpress.ui.admin.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AdminLineChartView extends View {
    public static class LinePoint {
        public String label;
        public float value;

        public LinePoint(String label, float value) {
            this.label = label;
            this.value = value;
        }
    }

    private final List<LinePoint> points = new ArrayList<>();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();

    public AdminLineChartView(Context context) {
        super(context);
        init();
    }

    public AdminLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setColor(0xFFEA580C);

        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(0xFFEA580C);

        textPaint.setColor(0xFF64748B);
        textPaint.setTextSize(22f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint.setColor(0xFFE2E8F0);
        gridPaint.setStrokeWidth(2f);
    }

    public void setData(List<LinePoint> data) {
        points.clear();
        if (data != null) {
            points.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.isEmpty()) {
            textPaint.setTextSize(32f);
            canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float paddingBottom = 60f;
        float paddingTop = 40f;
        float paddingLeft = 40f;
        float paddingRight = 40f;

        float chartHeight = height - paddingTop - paddingBottom;
        float chartWidth = width - paddingLeft - paddingRight;

        float maxValue = 1f;
        for (LinePoint p : points) {
            if (p.value > maxValue) maxValue = p.value;
        }

        int count = points.size();
        float step = count > 1 ? (chartWidth / (count - 1)) : chartWidth;

        linePath.reset();
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, gridPaint);

        for (int i = 0; i < count; i++) {
            LinePoint p = points.get(i);
            float x = paddingLeft + (i * step);
            float y = height - paddingBottom - ((p.value / maxValue) * chartHeight);

            if (i == 0) {
                linePath.moveTo(x, y);
            } else {
                linePath.lineTo(x, y);
            }

            canvas.drawCircle(x, y, 8f, pointPaint);

            textPaint.setTextSize(22f);
            textPaint.setColor(0xFF64748B);
            canvas.drawText(p.label, x, height - 16f, textPaint);
        }

        canvas.drawPath(linePath, linePaint);
    }
}
