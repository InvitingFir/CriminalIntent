package com.bignerdranch.criminalintent.Util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class PictureUtils {
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcHeight = options.outHeight;
        float srcWidth = options.outWidth;

        int inSampleSize;
        if(srcHeight > destHeight || srcWidth > destWidth){
            float scaleWidth = srcWidth/destWidth;
            float scaleHeight = srcHeight/destHeight;
            inSampleSize = Math.round(Math.max(scaleHeight, scaleWidth));
        }
        else inSampleSize = 1;
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity){
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }
}
