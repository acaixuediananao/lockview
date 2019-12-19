package com.wxl.lockview;

import android.content.res.Resources;

public class Utils {
    public static float dp2px(int dpValue){
        return (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int upInt(float value){
        return (int) Math.ceil(value);
    }
}
