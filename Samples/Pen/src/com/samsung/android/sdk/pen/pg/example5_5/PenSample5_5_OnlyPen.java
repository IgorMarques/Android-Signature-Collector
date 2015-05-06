package com.samsung.android.sdk.pen.pg.example5_5;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.pen.SpenPen;
import com.samsung.android.sdk.pen.pen.SpenPenInfo;
import com.samsung.android.sdk.pen.pen.SpenPenManager;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;

public class PenSample5_5_OnlyPen extends Activity {

    private SpenPen mPen;
    private SpenPenManager mPenManager;
    private Bitmap mBitmap;
    private boolean isDrawing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = this;

        // Initialize Spen
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
        } catch (SsdkUnsupportedException e) {
            if (SDKUtils.processUnsupportedException(this, e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(context, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get Pen settings from Pen Manager and set Pen accordingly.
        mPenManager = new SpenPenManager(context);
        SpenPenInfo penInfo = new SpenPenInfo();
        List<SpenPenInfo> penInfoList = mPenManager.getPenInfoList();
        for (SpenPenInfo info : penInfoList) {
            if (info.name.equalsIgnoreCase("InkPen")) {
                penInfo = info;
                break;
            }
        }
        try {
            mPen = mPenManager.createPen(penInfo);
        } catch (ClassNotFoundException e) {
            Toast.makeText(context, "SpenPenManager class not found.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (InstantiationException e) {
            Toast.makeText(context, "Failed to access the SpenPenManager constructor.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Toast.makeText(context, "Failed to access the SpenPenManager field or method.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "SpenPenManager is not loaded.", Toast.LENGTH_SHORT).show();
        }
        mPen.setSize(10);
        mPen.setColor(Color.BLUE);
        isDrawing = false;
        // Get the dimension of the device screen and define that of a view.
        Display display = getWindowManager().getDefaultDisplay();
        Rect mScreenSize = new Rect();
        display.getRectSize(mScreenSize);

        View view = new MyView(context, mScreenSize.width(), mScreenSize.height());
        setContentView(view);
    }

    protected class MyView extends View {

        private final RectF bitmapRect = new RectF();

        public MyView(Context context, int w, int h) {
            super(context);
            createBitmap(w, h);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            RectF tempRect = new RectF();
            // Get touch events and draw a pen.
            if (mBitmap != null && mBitmap.isRecycled() == false) {
                mBitmap.setPixel(0, 0, 0);
            }
            isDrawing = true;
            mPen.draw(event, tempRect);
            invalidate(convertRect(tempRect));
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // Draw the bitmap of a pen on the canvas.
            canvas.drawBitmap(mBitmap, null, bitmapRect, null);
            isDrawing = false;
            super.onDraw(canvas);
        }

        private void createBitmap(int w, int h) {
            // Create a bitmap and set the bitmap to Pen to draw a pen.
            mBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            bitmapRect.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            mPen.setBitmap(mBitmap);
        }

        private Rect convertRect(RectF src) {
            // Convert RectF of a refreshed bitmap into Rect.
            Rect dst = new Rect();
            dst.left = (int) src.left;
            dst.right = (int) src.right;
            dst.top = (int) src.top;
            dst.bottom = (int) src.bottom;

            return dst;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isDrawing == true) return;
        if (mPenManager != null) {
            mPenManager.destroyPen(mPen);
        }
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }
}