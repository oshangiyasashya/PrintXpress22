package com.printxpress.ui.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SaturationBrightnessView extends View {

    private float hue = 0f; // 0..360
    private float saturation = 1f; // 0..1
    private float brightness = 1f; // 0..1

    private Paint boxPaint;
    private Paint thumbPaint;
    private Paint thumbBorderPaint;
    private ComposeShader composeShader;

    private OnSaturationBrightnessChangedListener listener;

    public interface OnSaturationBrightnessChangedListener {
        void onColorChanged(float saturation, float brightness);
    }

    public SaturationBrightnessView(Context context) {
        super(context);
        init();
    }

    public SaturationBrightnessView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SaturationBrightnessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(Color.WHITE);

        thumbBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbBorderPaint.setStyle(Paint.Style.STROKE);
        thumbBorderPaint.setStrokeWidth(4f);
        thumbBorderPaint.setColor(Color.BLACK);
    }

    public void setOnSaturationBrightnessChangedListener(OnSaturationBrightnessChangedListener listener) {
        this.listener = listener;
    }

    public void setHue(float hue) {
        this.hue = hue;
        updateShaders();
        invalidate();
    }

    public void setSaturationBrightness(float saturation, float brightness) {
        this.saturation = Math.max(0f, Math.min(1f, saturation));
        this.brightness = Math.max(0f, Math.min(1f, brightness));
        invalidate();
    }

    private void updateShaders() {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        int pureHueColor = Color.HSVToColor(new float[]{hue, 1f, 1f});

        LinearGradient horizontalShader = new LinearGradient(0, 0, width, 0, Color.WHITE, pureHueColor, Shader.TileMode.CLAMP);
        LinearGradient verticalShader = new LinearGradient(0, 0, 0, height, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);

        composeShader = new ComposeShader(verticalShader, horizontalShader, PorterDuff.Mode.MULTIPLY);
        boxPaint.setShader(composeShader);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateShaders();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (boxPaint.getShader() == null) {
            updateShaders();
        }

        float width = getWidth();
        float height = getHeight();

        canvas.drawRect(0, 0, width, height, boxPaint);

        float cx = saturation * width;
        float cy = (1f - brightness) * height;

        canvas.drawCircle(cx, cy, 20f, thumbBorderPaint);
        canvas.drawCircle(cx, cy, 16f, thumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                // fall through
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                float x = Math.max(0f, Math.min(event.getX(), width));
                float y = Math.max(0f, Math.min(event.getY(), height));

                saturation = x / width;
                brightness = 1f - (y / height);

                invalidate();
                if (listener != null) {
                    listener.onColorChanged(saturation, brightness);
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
