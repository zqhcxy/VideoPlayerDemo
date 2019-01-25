package com.zqh.videoplayerdemo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.animation.TranslateAnimation;

public class CommonUtil {


    /**
     * 获取进入于退出动画
     *
     * @param fromYDelta
     * @param toYDelta
     * @return
     */
    public static TranslateAnimation getYAnimation(float fromYDelta, float toYDelta) {
        TranslateAnimation animation = new TranslateAnimation(0, 0, fromYDelta, toYDelta);
        animation.setDuration(150);
        return animation;
    }

    //获取虚拟按键的高度
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        try {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        } catch (Exception e) {
            Log.i("GalleryUtil", "getNavigationBarHeight fail  msg:" + e.getMessage());
        }
        return result;
    }
}
