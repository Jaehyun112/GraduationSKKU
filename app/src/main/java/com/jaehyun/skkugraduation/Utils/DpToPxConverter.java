package com.jaehyun.skkugraduation.Utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DpToPxConverter {

    private Context mContext;

    public DpToPxConverter(Context context){
        this.mContext = context;
    }

    public int convert(int dp){
        int dpToPx = 0;
        dpToPx = (int) (dp * ((float) mContext.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return dpToPx;
    }

}
