package com.mobile.helpers;

import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RSBlurProcessor {

    private RenderScript rs;

    private static final boolean IS_BLUR_SUPPORTED = Build.VERSION.SDK_INT >= 17;
    private static final int MAX_RADIUS = 25;

    public RSBlurProcessor(RenderScript rs) {
        this.rs = rs;
    }

    @Nullable
    public Bitmap blur(@NonNull Bitmap bitmap, float radius, int repeat) {

        if (!IS_BLUR_SUPPORTED) {
            return null;
        }

        if (radius > MAX_RADIUS) {
            radius = MAX_RADIUS;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Type bitmapType = new Type.Builder(rs, Element.RGBA_8888(rs))
                .setX(width)
                .setY(height)
                .setMipmaps(false)
                .create();

        Allocation allocation = Allocation.createTyped(rs, bitmapType);

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blurScript.setRadius(radius);

        allocation.copyFrom(bitmap);

        blurScript.setInput(allocation);

        blurScript.forEach(allocation);

        for (int i=0; i<repeat; i++) {
            blurScript.forEach(allocation);
        }

        allocation.copyTo(bitmap);

        allocation.destroy();
        blurScript.destroy();
        allocation = null;
        blurScript = null;

        return bitmap;
    }
}