package com.odm.tptest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class TouchPanelActivity extends Activity {

    int mHightPix = 0, mWidthPix = 0, mRadius = 20, mStep = 0;
    float w = 0, h = 0;
    private final int X_MAX_POINTS = 16;
    private int mButtonX = 0;
    private int mButtonY = 0;
    private String mTouchTraceFile;
    StringBuffer mFileData = new StringBuffer();

    //added by caigaopeng
    private int mDisableUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowManager.LayoutParams mLayoutParams;
        mLayoutParams = getWindow().getAttributes();
        mLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(mLayoutParams);

        Display mDisplay = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        //modified by caigaopeng, get full screen width and height
        //mDisplay.getSize(size);
        mDisplay.getRealSize(size);
        mWidthPix = size.x;
        mHightPix = size.y;
    }

    @Override
    protected void onResume() {
        getWindow().getDecorView().setSystemUiVisibility(mDisableUIFlag);
        super.onResume();
        setContentView(new TPTestView(this, mWidthPix, mHightPix));
    }
}
