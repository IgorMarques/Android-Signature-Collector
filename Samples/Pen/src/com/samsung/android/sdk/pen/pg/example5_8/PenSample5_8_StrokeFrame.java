package com.samsung.android.sdk.pen.pg.example5_8;

import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectContainer;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenContextMenuItemInfo;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenControlListener;
import com.samsung.android.sdk.pen.engine.SpenStrokeFrameListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.spensdk3.example.R;

public class PenSample5_8_StrokeFrame extends Activity {

    private final int CONTEXT_MENU_DELETE = 0;
    private final int CONTEXT_MENU_RETAKE = 1;

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    RelativeLayout mSpenViewLayout;

    private ImageView mStrokeFrameBtn;

    private SpenObjectContainer mStrokeFrameContainer;

    boolean mStrokeFrameStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stroke_frame);
        mContext = this;

		// Initialize Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if( SDKUtils.processUnsupportedException(this, e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.",
                Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        mSpenViewLayout =
            (RelativeLayout) findViewById(R.id.spenViewLayout);

		// Create SpenView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenSurfaceView.",
                Toast.LENGTH_SHORT).show();
            finish();
        }
        mSpenViewLayout.addView(mSpenSurfaceView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);
		// Create SpenNoteDoc
        try {
            mSpenNoteDoc =
                new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.",
                Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
		// Add a Page to NoteDoc and get an instance and set it to the member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.clearHistory();
		// Set PageDoc to View
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        initPenSettingInfo();
		// Register the listener
        mSpenSurfaceView.setControlListener(mControlListener);

		// Set a button
        mStrokeFrameBtn = (ImageView) findViewById(R.id.videoBtn);
        mStrokeFrameBtn.setOnClickListener(mFrameBtnClickListener);

        if(isSpenFeatureEnabled == false) {
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext,
                "Device does not support Spen. \n You can draw stroke by finger",
                Toast.LENGTH_SHORT).show();
        }
    }

    private void initPenSettingInfo() {
		// Initialize Pen settings
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.BLUE;
        penInfo.size = 10;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
    }

    private final OnClickListener mFrameBtnClickListener =
        new OnClickListener() {
            @Override
            public void onClick(View v) {
				// If StrokeFrame is recording, cancel it.
                if (mStrokeFrameStarted) {
                    mStrokeFrameBtn.setImageResource(R.drawable.selector_video);
                    mStrokeFrameStarted = false;

                mSpenSurfaceView.cancelStrokeFrame();
                } else {
					// Create a frame with the objects and start recording with StrokFrame.
                    ArrayList<SpenObjectBase> oList =
                        mSpenPageDoc.getObjectList(SpenPageDoc.FIND_TYPE_STROKE);

                    if (oList.size() != 0) {
                        mStrokeFrameBtn.setImageResource(R.drawable.tool_ic_stop);
                        mStrokeFrameStarted = true;

                        ArrayList<SpenObjectStroke> osList =
                            new ArrayList<SpenObjectStroke>();
                        for (SpenObjectBase o : oList) {
                            osList.add((SpenObjectStroke) o);
                        }

                    mSpenSurfaceView.update();
                    mSpenSurfaceView
                            .takeStrokeFrame((Activity) mContext,
                            mSpenViewLayout, osList, mStrokeFrameListener);
                    } else {
                        Toast.makeText(mContext,
                            "It doesn't work.\nPlease draw the stroke.",
                            Toast.LENGTH_SHORT).show();
                    }
                }

            }
        };

    SpenControlListener mControlListener = new SpenControlListener() {

        @Override
        public boolean onClosed(ArrayList<SpenObjectBase> objectList) {
            return false;
        }

        @Override
        public boolean onCreated(ArrayList<SpenObjectBase> objectList,
            ArrayList<Rect> relativeRectList,
            ArrayList<SpenContextMenuItemInfo> menu,
            ArrayList<Integer> styleList,  int pressType, PointF point ) {
            if (objectList == null) {
                return false;
            }
            SpenControlBase control = mSpenSurfaceView.getControl();
            if(control != null) {
                control.setContextMenuVisible(true);
            }
			// Set the Context menu
            menu.add(new SpenContextMenuItemInfo(CONTEXT_MENU_DELETE, "Delete", true));
			// If the selected object is container type, add Retake to the menu.
            if(objectList.get(0).getType() == SpenObjectBase.TYPE_CONTAINER) {
                menu.add(new SpenContextMenuItemInfo(CONTEXT_MENU_RETAKE, "Re Take", true));
                mStrokeFrameContainer = (SpenObjectContainer) objectList.get(0);
            }
            return true;
        }

        @Override
        public boolean onMenuSelected(
            ArrayList<SpenObjectBase> objectList, int itemId) {
            if (objectList == null) {
                return true;
            }
			// Remove the selected object (StrokeFrame)
            if (itemId == CONTEXT_MENU_DELETE) {
                mSpenPageDoc.removeSelectedObject();
                mSpenSurfaceView.closeControl();
                mSpenSurfaceView.update();
            // Retake StrokeFrame
            } else if(itemId == CONTEXT_MENU_RETAKE) {
                SpenControlBase control = mSpenSurfaceView.getControl();
                if(control != null) {
                    control.setContextMenuVisible(false);
                }

                mSpenSurfaceView.retakeStrokeFrame((Activity) mContext, mSpenViewLayout,
                        mStrokeFrameContainer, mStrokeFrameListener);
                mStrokeFrameBtn.setImageResource(R.drawable.tool_ic_stop);
                mStrokeFrameStarted = true;
            }
            return false;
        }

        @Override
        public void onObjectChanged(ArrayList<SpenObjectBase> object) {
        }

        @Override
        public void onRectChanged(RectF rect, SpenObjectBase object) {
        }

        @Override
        public void onRotationChanged(float angle,
            SpenObjectBase objectBase) {
        }
    };

    private final SpenStrokeFrameListener mStrokeFrameListener = new SpenStrokeFrameListener() {
        @Override
        public void onCompleted(int frameType, SpenObjectContainer o) {
			// On the completion of recording, select the object, and prompt the context menu.
            mSpenPageDoc.selectObject(o);
            mSpenSurfaceView.update();
            mStrokeFrameContainer = o;

            SpenControlBase control = mSpenSurfaceView.getControl();
            if(control != null) {
                control.setContextMenuVisible(true);
            }
            mStrokeFrameBtn.setImageResource(R.drawable.selector_video);
            mStrokeFrameStarted = false;
			control.invalidate();
        }

        @Override
        public void onCanceled(int state, SpenObjectContainer o) {
        	SpenControlBase control = mSpenSurfaceView.getControl();
            if(control != null) {
            	mSpenSurfaceView.closeControl();           	
            	Toast.makeText(mContext, "Cancel because object is out of view",
                        Toast.LENGTH_SHORT).show();          	
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }

        if(mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mSpenSurfaceView.cancelStrokeFrame();
        mStrokeFrameBtn.setImageResource(R.drawable.selector_video);
        mStrokeFrameStarted = false;
        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.closeControl();
        }
    }


}