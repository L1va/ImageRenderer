package com.example.l1va.imagerenderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Matrix3f;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.util.AttributeSet;
import android.view.View;

public class CustomView extends View {

    private Bitmap initialBitmap;
    private Bitmap bitmap;
    private Rect rect = new Rect();
    private int brightness = 50;
    private int blur = 0;
    private int extractRed = 0;
    private int width;
    private int height;

    private RenderScript renderScript;
    private ScriptIntrinsicColorMatrix scriptIntrinsicColorMatrix;
    private ScriptIntrinsicBlur scriptIntrinsicBlur;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        renderScript = RenderScript.create(context);
        scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicColorMatrix = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, rect, null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        updateRect();
    }

    private void invalidateBitmap() {
        invalidate();
    }

    private void updateRect() {

        if (bitmap == null) {
            return;
        }
        float ws = width / ((float) bitmap.getWidth());
        float hs = height / ((float) bitmap.getHeight());

        if (ws > hs) {
            int dx = (int) (width - hs * bitmap.getWidth()) / 2;
            rect.left = dx;
            rect.right = width - dx;
            rect.top = 0;
            rect.bottom = height;
        } else {
            int dy = (int) (height - ws * bitmap.getHeight()) / 2;
            rect.left = 0;
            rect.right = width;
            rect.top = dy;
            rect.bottom = height - dy;
        }
    }


    public void initBitmap(Bitmap bitmap) {
        this.initialBitmap = bitmap.copy(bitmap.getConfig(), true);
        this.bitmap = bitmap;
        updateRect();
        brightness = 50;
        blur = 0;
        extractRed = 0;
        invalidateBitmap();
    }

    public void saveStep() {
        this.initialBitmap = bitmap.copy(bitmap.getConfig(), true);
        brightness = 50;
        blur = 0;
        extractRed = 0;
    }

    public void setBrightness(int value) {
        if (brightness == value) {
            return;
        }
        brightness = value;
        (new BrightnessTask()).execute((value - 50) / 100f);
    }

    public void setBlur(int value) {
        if (blur == value) {
            return;
        }
        blur = value;
        if (value == 0) {
            bitmap = initialBitmap;
            return;
        }
        (new BlurTask()).execute(value / 4f);
    }

    public void setExtractRed(int value) {
        if (extractRed == value) {
            return;
        }
        extractRed = value;
        (new ExtractRedTask()).execute((100 - value) / 100f);
    }

    public int getBrightness() {
        return brightness;
    }

    public int getBlur() {
        return blur;
    }

    public int getExtractRed() {
        return extractRed;
    }


    private class BrightnessTask extends AsyncTask<Float, Void, Void> {

        protected Void doInBackground(Float... values) {
            float v = values[0];
            scriptIntrinsicColorMatrix.setColorMatrix(new Matrix3f(new float[]{1 + v, 0, 0, 0, 1 + v, 0, 0, 0, 1 + v}));

            Allocation inAlloc = Allocation.createFromBitmap(renderScript, initialBitmap);
            Allocation outAlloc = Allocation.createFromBitmap(renderScript, bitmap);

            scriptIntrinsicColorMatrix.forEach(inAlloc, outAlloc);

            outAlloc.copyTo(bitmap);
            return null;
        }

        protected void onPostExecute(Void result) {
            invalidateBitmap();
        }
    }

    private class BlurTask extends AsyncTask<Float, Void, Void> {

        protected Void doInBackground(Float... values) {
            float v = values[0];
            scriptIntrinsicBlur.setRadius(v);

            Allocation inAlloc = Allocation.createFromBitmap(renderScript, initialBitmap);
            Allocation outAlloc = Allocation.createFromBitmap(renderScript, bitmap);

            scriptIntrinsicBlur.setInput(inAlloc);
            scriptIntrinsicBlur.forEach(outAlloc);

            outAlloc.copyTo(bitmap);
            return null;
        }

        protected void onPostExecute(Void result) {
            invalidateBitmap();
        }
    }

    private class ExtractRedTask extends AsyncTask<Float, Void, Void> {

        protected Void doInBackground(Float... values) {

            float v = values[0];
            scriptIntrinsicColorMatrix.setColorMatrix(new Matrix3f(new float[]{v, 0, 0, 0, 1, 0, 0, 0, 1}));

            Allocation inAlloc = Allocation.createFromBitmap(renderScript, initialBitmap);
            Allocation outAlloc = Allocation.createFromBitmap(renderScript, bitmap);

            scriptIntrinsicColorMatrix.forEach(inAlloc, outAlloc);

            outAlloc.copyTo(bitmap);
            return null;
        }

        protected void onPostExecute(Void result) {
            invalidateBitmap();
        }
    }
}
