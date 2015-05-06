package com.samsung.android.sdk.pen.pg.example1_2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.settingui.SpenPenPresetInfo;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout.PresetListener;
import com.samsung.spensdk3.example.R;

public class PenSample1_2_PenSetting extends Activity {

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    private SpenSettingPenLayout mPenSettingView;

    private Button mDefaultPenListBtn;
    private Button mCustomPenListBtn;

    private ArrayList<String> mDefaultPenList;
    private ArrayList<String> mCustomPenList;

    private static final String OBLIQUE_PEN_NAME = "com.samsung.android.sdk.pen.pen.preload.ObliquePen";
    private static final String FOUNTAIN_PEN_NAME = "com.samsung.android.sdk.pen.pen.preload.FountainPen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pen_setting);
        mContext = this;

        // Initialize Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if (SDKUtils.processUnsupportedException(this, e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        FrameLayout spenViewContainer = (FrameLayout) findViewById(R.id.spenViewContainer);
        RelativeLayout spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // Create PenSettingView
        if (android.os.Build.VERSION.SDK_INT > 19) {
            mPenSettingView = new SpenSettingPenLayout(mContext, new String(), spenViewLayout);
        } else {
            mPenSettingView = new SpenSettingPenLayout(getApplicationContext(), new String(), spenViewLayout);
        }
        if (mPenSettingView == null) {
            Toast.makeText(mContext, "Cannot create new PenSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewContainer.addView(mPenSettingView);

        // Create SpenView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewLayout.addView(mSpenSurfaceView);
        mPenSettingView.setCanvasView(mSpenSurfaceView);

        // Get the dimension of the device screen
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        // Add a Page to NoteDoc, get an instance, and set it to the member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.clearHistory();
        // Set PageDoc to View
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        initPenSettingInfo();
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);

        if (isSpenFeatureEnabled == false) {
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger",
                    Toast.LENGTH_SHORT).show();
        }

        mDefaultPenListBtn = (Button) findViewById(R.id.penBtn);
        mDefaultPenListBtn.setOnClickListener(mPenBtnClickListener);

        mCustomPenListBtn = (Button) findViewById(R.id.customPenBtn);
        mCustomPenListBtn.setOnClickListener(mCustomPenBtnClickListener);

        mDefaultPenList = mPenSettingView.getPenList();

        mCustomPenList = new ArrayList<String>();
        mCustomPenList.add(FOUNTAIN_PEN_NAME);
        mCustomPenList.add(OBLIQUE_PEN_NAME);

        mPenSettingView.setPresetListener(mPresetListener);
    }

    private void initPenSettingInfo() {
        // Initialize Pen settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.BLUE;
        penInfo.size = 10;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);
    }

    private final OnClickListener mPenBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // If PenSettingView is open, close it.
            if (mPenSettingView.isShown()) {
                mPenSettingView.setVisibility(View.GONE);
                // If PenSettingView is not open, open it.
            } else {
                mPenSettingView.setPenList(mDefaultPenList);

                // Switch to default preset list
                mPenSettingView.setExtendedPresetEnable(false);
                List<SpenPenPresetInfo> presetInfoList = mPenSettingView.getPenPresetInfoList();
                Toast.makeText(mContext, "The number of preset items: " + presetInfoList.size(), Toast.LENGTH_SHORT)
                        .show();

                // Restore to default images of FountainPen
                mPenSettingView.setPenImage(FOUNTAIN_PEN_NAME, null, null, null, null);

                mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_EXTENSION);
                mPenSettingView.setVisibility(View.VISIBLE);
            }
        }
    };

    private final OnClickListener mCustomPenBtnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // If PenSettingView is open, close it.
            if (mPenSettingView.isShown()) {
                mPenSettingView.setVisibility(View.GONE);
                // If PenSettingView is not open, open it.
            } else {
                mPenSettingView.setPenList(mCustomPenList);

                // Switch to extended preset list
                mPenSettingView.setExtendedPresetEnable(true);
                List<SpenPenPresetInfo> presetInfoList = mPenSettingView.getPenPresetInfoList();
                Toast.makeText(mContext, "The number of preset items: " + presetInfoList.size(), Toast.LENGTH_SHORT)
                        .show();

                // Custom images of FountainPen
                Drawable mNormalImage = mContext.getResources().getDrawable(
                        R.drawable.snote_popup_pensetting_montblanc_marker);
                Drawable mSelectedImage = mContext.getResources().getDrawable(
                        R.drawable.snote_popup_pensetting_montblanc_marker_select);
                Drawable mFocusImage = mContext.getResources().getDrawable(
                        R.drawable.snote_popup_pensetting_montblanc_marker_focus);
                Drawable mPresetImage = mContext.getResources().getDrawable(R.drawable.pen_preset_montblanc_marker);
                mPenSettingView.setPenImage(FOUNTAIN_PEN_NAME, mNormalImage, mSelectedImage, mFocusImage, mPresetImage);

                mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_EXTENSION);
                mPenSettingView.setVisibility(View.VISIBLE);
            }
        }
    };

    private final PresetListener mPresetListener = new PresetListener() {

        @Override
        public void onAdded(SpenSettingPenInfo arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onDeleted(int arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSelected(int arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onChanged(int index) {
            // TODO Auto-generated method stub

        }

    };

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mPenSettingView != null) {
                SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                penInfo.color = color;
                mPenSettingView.setInfo(penInfo);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPenSettingView != null) {
            mPenSettingView.close();
        }
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