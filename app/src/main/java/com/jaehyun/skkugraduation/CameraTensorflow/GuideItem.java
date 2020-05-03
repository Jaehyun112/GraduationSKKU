package com.jaehyun.skkugraduation.CameraTensorflow;

import android.graphics.Bitmap;


import com.jaehyun.skkugraduation.R;

import java.util.ArrayList;
import java.util.List;

public class GuideItem {

    private static final int GUIDE_WIDTH_FRONT = 200;
    private static final int GUIDE_WIDTH_SIDEL = 200;
    private static final int GUIDE_WIDTH_UP =    200;
    private static final int GUIDE_WIDTH_BACK =  200;
    private static final int GUIDE_WIDTH_SIDER = 200;
    private static final int GUIDE_WIDTH_DOWN =  200;
    private static final int GUIDE_WIDTH_WHITE = 200;
    private static final int GUIDE_WIDTH_BLUE =  200;
    private static final int GUIDE_WIDTH_BLACK = 200;
    private static final int GUIDE_WIDTH_FREE =  200;

    private static final int GUIDE_HEIGHT_FRONT = 200;
    private static final int GUIDE_HEIGHT_SIDEL = 200;
    private static final int GUIDE_HEIGHT_UP =    200;
    private static final int GUIDE_HEIGHT_BACK =  200;
    private static final int GUIDE_HEIGHT_SIDER = 200;
    private static final int GUIDE_HEIGHT_DOWN =  200;
    private static final int GUIDE_HEIGHT_WHITE = 200;
    private static final int GUIDE_HEIGHT_BLUE =  200;
    private static final int GUIDE_HEIGHT_BLACK = 200;
    private static final int GUIDE_HEIGHT_FREE =  200;

    private static final String GUIDE_TEXT_FRONT = " ";
    private static final String GUIDE_TEXT_SIDEL = " ";
    private static final String GUIDE_TEXT_UP = " ";
    private static final String GUIDE_TEXT_BACK = " ";
    private static final String GUIDE_TEXT_SIDER = " ";
    private static final String GUIDE_TEXT_DOWN = " ";
    private static final String GUIDE_TEXT_WHITE = " ";
    private static final String GUIDE_TEXT_BLUE = " ";
    private static final String GUIDE_TEXT_BLACK = " ";
    private static final String GUIDE_TEXT_FREE = " ";

    public static int[] guideWidts ={
            GUIDE_WIDTH_FRONT,
            GUIDE_WIDTH_SIDEL,
            GUIDE_WIDTH_UP,
            GUIDE_WIDTH_BACK,
            GUIDE_WIDTH_SIDER,
            GUIDE_WIDTH_DOWN,
            GUIDE_WIDTH_WHITE,
            GUIDE_WIDTH_BLUE,
            GUIDE_WIDTH_BLACK,
            GUIDE_WIDTH_FREE
    };
    public static int[] guideheights ={
            GUIDE_HEIGHT_FRONT,
            GUIDE_HEIGHT_SIDEL,
            GUIDE_HEIGHT_UP,
            GUIDE_HEIGHT_BACK,
            GUIDE_HEIGHT_SIDER,
            GUIDE_HEIGHT_DOWN,
            GUIDE_HEIGHT_WHITE,
            GUIDE_HEIGHT_BLUE,
            GUIDE_HEIGHT_BLACK,
            GUIDE_HEIGHT_FREE
    };

    public static String[] guideInfos = {
            GUIDE_TEXT_FRONT,
            GUIDE_TEXT_SIDEL,
            GUIDE_TEXT_UP,
            GUIDE_TEXT_BACK,
            GUIDE_TEXT_SIDER,
            GUIDE_TEXT_DOWN,
            GUIDE_TEXT_WHITE,
            GUIDE_TEXT_BLUE,
            GUIDE_TEXT_BLACK,
            GUIDE_TEXT_FREE
    };


    public static int currentStep = 0;

    public static int phoneLike = 0;

    public static final int SAMSUNGnETC = 0;
    public static final int LG = 1;
    public static final int IPHONE = 2;

    public static int inpectLike = 0;

    public static final int AUTO_INSPECT = 0;
    public static final int SINGLE_INSPECT = 1;

    public static int captureLike = 0;

    public static final int CAPTURE_AUTO = 0;
    public static final int CAPTURE_BUTTON = 1;

    public static List<Bitmap> cropBitmapList = new ArrayList<>();

    public GuideItem(){
    }

    public static void clear(){
        currentStep = 0;
        inpectLike = AUTO_INSPECT;
        phoneLike = SAMSUNGnETC;
        cropBitmapList.clear();
    }

    public static  void addCropBitmapList(Bitmap bm){
        cropBitmapList.add(bm);
    }

    public static  void setCropBitmapList(int currentStep,Bitmap bm){
        cropBitmapList.set(currentStep,bm);
    }
}
