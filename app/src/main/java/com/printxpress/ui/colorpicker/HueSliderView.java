package com.printxpress.ui.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

public class HueSliderView extends View {

    private float hue = 0f; // 0..360
    private Paint sliderPaint;
    private Paint thumbPaint;
    private Paint thumbBorderPaint;
    private LinearGradient rainbowShader;

    private OnHueChangedListener listener;

    public interface OnHueChangedListener {
        void onHueChanged(float hue);
    }

    public HueSliderView(Context context) {
        super(context);
        init();
    }

    public HueSliderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HueSliderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        sliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(Color.WHITE);

        thumbBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbBorderPaint.setStyle(Paint.Style.STROKE);
        thumbBorderPaint.setStrokeWidth(4f);
        thumbBorderPaint.setColor(Color.BLACK);
    }

    public void setOnHueChangedListener(OnHueChangedListener listener) {
        this.listener = listener;
    }

    public void setHue(float hue) {
        this.hue = Math.max(0f, Math.min(360f, hue));
        invalidate();
    }

    private void updateShader() {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        int[] colors = new int[]{
                Color.RED, Color.YELLOW, Color.GREEN,
                Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
        };
        rainbowShader = new LinearGradient(0, 0, width, 0, colors, null, Shader.TileMode.CLAMP);
        sliderPaint.setShader(rainbowShader);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateShader();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (sliderPaint.getShader() == null) {
            updateShader();
        }

        float width = getWidth();
        float height = getHeight();
        float cornerRadius = height / 2f;

        canvas.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius, sliderPaint);

        float cx = (hue / 360f) * width;
        float cy = height / 2f;
        float thumbRadius = height * 0.6f;

        canvas.drawCircle(cx, cy, thumbRadius, thumbBorderPaint);
        canvas.drawCircle(cx, cy, thumbRadius - 3f, thumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float width = getWidth();
        if (width <= 0) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                // fall through
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                float x = Math.max(0f, Math.min(event.getX(), width));
                hue = (x / width) * 360f;
                invalidate();
                if (listener != null) {
                    listener.onHueChanged(hue);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
