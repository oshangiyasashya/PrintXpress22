package com.printxpress.ui.editor;

import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class DesignElement {
    public String id;
    public float x;
    public float y;
    public float width;
    public float height;
    public float rotation;
    public float scale = 1.0f;
    public boolean isLocked = false;
    public boolean isVisible = true;

    public DesignElement(String id, float x, float y, float width, float height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void draw(Canvas canvas, Paint paint);

    public boolean contains(float px, float py) {
        return px >= x && px <= (x + width) && py >= y && py <= (y + height);
    }
}
