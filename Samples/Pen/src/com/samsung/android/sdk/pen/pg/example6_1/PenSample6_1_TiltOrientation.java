package com.samsung.android.sdk.pen.pg.example6_1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Display;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.spensdk3.example.R;

public class PenSample6_1_TiltOrientation extends Activity {

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;

    TextView txtTilt, txtOrientation;
    SeekBar seekTilt, seekOrientation;

    int tiltValue = 0;
    int orientationValue = 0;
    private int mToolType = SpenSurfaceView.TOOL_SPEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pen_tilts_orientations);
        mContext = this;

        // Initialize Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage
                    .isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if (SDKUtils.processUnsupportedException(this, e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.",
                    Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        RelativeLayout spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // Create SpenView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenView.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewLayout.addView(mSpenSurfaceView);

        // Get the dimension of the device screen
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(),
                    rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        // Add a Page to NoteDoc, get an instance, and set it to the member
        // variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.clearHistory();
        // Set PageDoc to View
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        txtTilt = (TextView) findViewById(R.id.txtTilt);
        txtOrientation = (TextView) findViewById(R.id.txtOrientation);
        seekTilt = (SeekBar) findViewById(R.id.seekTilt);
        seekOrientation = (SeekBar) findViewById(R.id.seekOrientation);

        seekTilt.setOnSeekBarChangeListener(mOnSeekTiltChangeListener);
        seekOrientation
                .setOnSeekBarChangeListener(mOnSeekOrientationChangeListener);

        if (isSpenFeatureEnabled == false) {
            mToolType = SpenSurfaceView.TOOL_SPEN;
            Toast.makeText(
                    mContext,
                    "Device does not support Spen. \n You can draw stroke by finger",
                    Toast.LENGTH_SHORT).show();
        } else {
            mSpenSurfaceView.setToolTypeAction(mToolType,
                    SpenSurfaceView.ACTION_NONE);
        }

        addStrokeObject("com.samsung.android.sdk.pen.pen.preload.MontblancCalligraphyPen", rect.width()/2 + 100, rect.height()/2 + 100);
        addStrokeObject("com.samsung.android.sdk.pen.pen.preload.FountainPen",400, 400);
    }

    private OnSeekBarChangeListener mOnSeekTiltChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            updateStrokeProperties((float) tiltValue, (float) orientationValue);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            // TODO Auto-generated method stub
            txtTilt.setText("Tilt(" + progress + getResources().getString(R.string.degrees) + ")");
            tiltValue = progress;
        }
    };

    private OnSeekBarChangeListener mOnSeekOrientationChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            updateStrokeProperties((float) tiltValue, (float) orientationValue);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            // TODO Auto-generated method stub
            txtOrientation.setText("Orientation(" + (progress - 180) + getResources().getString(R.string.degrees) + ")");
            orientationValue = progress - 180;
        }
    };

    private void addStrokeObject(String name, float x, float y) {
        // Set the location to insert ObjectStroke and add it to PageDoc.
        RectF rect = getRealPoint(x, y, 0, 0);
        float rectX = rect.centerX();
        float rectY = rect.centerY();
        int pointSize = 157;
        float[][] strokePoint = new float[pointSize][2];
        for (int i = 0; i < pointSize; i++) {
            strokePoint[i][0] = rectX++;
            strokePoint[i][1] = (float) (rectY + Math.sin(.04 * i) * 300);
        }
        PointF[] points = new PointF[pointSize];
        float[] pressures = new float[pointSize];
        int[] timestamps = new int[pointSize];

        for (int i = 0; i < pointSize; i++) {
            points[i] = new PointF();
            points[i].x = strokePoint[i][0];
            points[i].y = strokePoint[i][1];
            pressures[i] = 1;
            timestamps[i] = (int) android.os.SystemClock.uptimeMillis();
        }

        SpenObjectStroke strokeObj = new SpenObjectStroke(name, points,
                pressures, timestamps);
        strokeObj.setPenSize(40);
        strokeObj.setColor(Color.RED);
        mSpenPageDoc.appendObject(strokeObj);
        mSpenSurfaceView.update();
    }

    private RectF getRealPoint(float x, float y, float width, float height) {
        float panX = mSpenSurfaceView.getPan().x;
        float panY = mSpenSurfaceView.getPan().y;
        float zoom = mSpenSurfaceView.getZoomRatio();
        width *= zoom;
        height *= zoom;
        RectF realRect = new RectF();
        realRect.set((x - width / 2) / zoom + panX, (y - height / 2) / zoom
                + panY, (x + width / 2) / zoom + panX, (y + height / 2) / zoom
                + panY);
        return realRect;
    }

    private void updateStrokeProperties(float _tilt, float _orientaion) {

        ArrayList<SpenObjectBase> objectList = mSpenPageDoc.getObjectList();
        _tilt = _tilt / 90;
        _orientaion = _orientaion / 360;

        int pointCount = 157;
        float[] tilts = new float[pointCount];
        float[] orientations = new float[pointCount];
        Arrays.fill(tilts, _tilt);
        Arrays.fill(orientations, _orientaion);
        
        for (SpenObjectBase obj : objectList) {
            if (obj.getType() == SpenObjectBase.TYPE_STROKE) {
                SpenObjectStroke stroke = (SpenObjectStroke) obj;
                stroke.setPoints(stroke.getPoints(), stroke.getPressures(),
                        stroke.getTimeStamps(), tilts, orientations);
            }
        }
        mSpenSurfaceView.update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }

        if (mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    };
}