package com.lzx.applock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzx
 * @date 2018/2/28
 */

public class BlurUtil {

    public static Bitmap drawableToBitmap(Drawable drawable, ImageView mUnLockLayout) {
        int w = 20;
        int h = 20;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        int[] pixex = new int[w * h];
        List<Integer> trIndexs = new ArrayList<Integer>();
        for (int i = 0; i < bitmap.getHeight(); i++) {
            for (int j = 0; j < bitmap.getWidth(); j++) {
                int color = bitmap.getPixel(j, i);
                int alpha = Color.alpha(color);
                if (alpha < 200) {
                    trIndexs.add(i * h + j);
                } else if (trIndexs.size() > 0) {
                    for (Integer tr : trIndexs) {
                        pixex[tr] = color;
                    }
                    trIndexs.clear();
                    pixex[i * h + j] = color;
                } else {
                    pixex[i * h + j] = color;
                }
            }
        }
        Bitmap bitmap2 = Bitmap.createBitmap(mUnLockLayout.getWidth(), mUnLockLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap2);
        RectF rectF = new RectF(0, 0, mUnLockLayout.getWidth(), mUnLockLayout.getHeight());
        canvas2.drawBitmap(Bitmap.createBitmap(pixex, w, h, Bitmap.Config.ARGB_8888), null, rectF, null);
        return bitmap2;
    }

    public static void blur(Context mContent, Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();
        float radius = 50;
        float scaleFactor = 8;
        Bitmap overlay = Bitmap.createBitmap(
                (int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);
        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        view.setBackground(new BitmapDrawable(mContent.getResources(), overlay));
    }

    public static Bitmap big(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(2.5f, 2.5f);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 4,
                bitmap.getHeight() / 4, bitmap.getWidth() / 2 - 1,
                bitmap.getHeight() / 2 - 1, matrix, true);
        return handleImage(resizeBmp, 85);
    }

    private static Bitmap handleImage(Bitmap bm, int hue) {
        Bitmap bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        ColorMatrix mHueMatrix = new ColorMatrix();
        ColorMatrix mAllMatrix = new ColorMatrix();
        float mHueValue = hue * 1.0F / 127;
        mHueMatrix.reset();
        mHueMatrix.setScale(mHueValue, mHueValue, mHueValue, 1);

        mAllMatrix.reset();
        mAllMatrix.postConcat(mHueMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(mAllMatrix));
        canvas.drawBitmap(bm, 0, 0, paint);
        return bmp;
    }

}
