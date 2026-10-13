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

public class AdminBarChartView extends View {
    public static class BarData {
        public String label;
        public float value;
        public BarData(String label, float value) {
            this.label = label;
            this.value = value;
        }
    }

    private final List<BarData> dataList = new ArrayList<>();
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();
    private int barColor = 0xFF2563EB; // Primary Blue

    public AdminBarChartView(Context context) {
        super(context);
        init();
    }

    public AdminBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

        textPaint.setColor(0xFF64748B);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint.setColor(0xFFE2E8F0);
        gridPaint.setStrokeWidth(2f);
    }

    public void setBarColor(int color) {
        this.barColor = color;
        barPaint.setColor(color);
        invalidate();
    }

    public void setData(List<BarData> data) {
        dataList.clear();
        if (data != null) {
            dataList.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataList.isEmpty()) {
            textPaint.setTextSize(32f);
            canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float paddingBottom = 60f;
        float paddingTop = 40f;
        float paddingLeft = 30f;
        float paddingRight = 30f;

        float chartHeight = height - paddingTop - paddingBottom;
        float chartWidth = width - paddingLeft - paddingRight;

        float maxValue = 1f;
        for (BarData b : dataList) {
            if (b.value > maxValue) maxValue = b.value;
        }

        int count = dataList.size();
        float barWidth = (chartWidth / count) * 0.55f;
        float step = chartWidth / count;

        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, gridPaint);

        for (int i = 0; i < count; i++) {
            BarData d = dataList.get(i);
            float xCenter = paddingLeft + (i * step) + (step / 2f);
            float barHeight = (d.value / maxValue) * chartHeight;

            barRect.left = xCenter - (barWidth / 2f);
            barRect.right = xCenter + (barWidth / 2f);
            barRect.top = height - paddingBottom - barHeight;
            barRect.bottom = height - paddingBottom;

            canvas.drawRoundRect(barRect, 12f, 12f, barPaint);

            if (d.value > 0) {
                textPaint.setTextSize(22f);
                textPaint.setColor(0xFF1E293B);
                canvas.drawText(String.valueOf((int) d.value), xCenter, Math.max(barRect.top - 8f, paddingTop), textPaint);
            }

            textPaint.setTextSize(22f);
            textPaint.setColor(0xFF64748B);
            canvas.drawText(d.label, xCenter, height - 16f, textPaint);
        }
    }
}
