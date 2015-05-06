package com.samsung.android.sdk.pen.pg.example4_1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
import com.samsung.android.sdk.pen.document.SpenObjectImage;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenContextMenuItemInfo;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenControlListener;
import com.samsung.android.sdk.pen.engine.SpenObjectRuntime;
import com.samsung.android.sdk.pen.engine.SpenObjectRuntimeInfo;
import com.samsung.android.sdk.pen.engine.SpenObjectRuntimeManager;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.plugin.interfaces.SpenObjectRuntimeInterface;
import com.samsung.spensdk3.example.R;

public class PenSample4_1_SOR extends Activity {

    private final int CONTEXT_MENU_RUN_ID = 0;

    private Context mContext;
    private Activity mActivity;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    RelativeLayout mSpenViewLayout;

    private ImageView mSelectionBtn;
    private ImageView mPenBtn;
    private ImageView mVideoBtn;

    private SpenObjectRuntimeManager mSpenObjectRuntimeManager;
    private List<SpenObjectRuntimeInfo> mSpenObjectRuntimeInfoList;
    private SpenObjectRuntimeInfo mObjectRuntimeInfo;
    private SpenObjectRuntime mVideoRuntime;

    private int mToolType = SpenSurfaceView.TOOL_SPEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sor);
        mContext = this;
        mActivity = this;

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

        // Create SpenSurfaceView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenSurfaceView.",
                Toast.LENGTH_SHORT).show();
            finish();
        }
        mSpenSurfaceView.setZoomable(false);
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
		// Add a Page to NoteDoc, get an instance, and set it to the member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
        mSpenPageDoc.clearHistory();
		// Set PageDoc to View.
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        initPenSettingInfo();
		// Register the listener
        mSpenSurfaceView.setControlListener(mControlListener);

		// Set a button
        mSelectionBtn = (ImageView) findViewById(R.id.selectionBtn);
        mSelectionBtn.setOnClickListener(mSelectionBtnClickListener);

        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mVideoBtn = (ImageView) findViewById(R.id.videoBtn);
        mVideoBtn.setOnClickListener(mVideoBtnClickListener);

        selectButton(mPenBtn);

		// Set ObjectRuntimeManager
        mSpenObjectRuntimeManager = new SpenObjectRuntimeManager(mActivity);
        mSpenObjectRuntimeInfoList =
            new ArrayList<SpenObjectRuntimeInfo>();
        mSpenObjectRuntimeInfoList = mSpenObjectRuntimeManager.getObjectRuntimeInfoList();

        if(isSpenFeatureEnabled == false) {
            mToolType = SpenSurfaceView.TOOL_FINGER;
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
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

    private final OnClickListener mSelectionBtnClickListener =
        new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButton(mSelectionBtn);
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_SELECTION);
            }
        };

    private final OnClickListener mPenBtnClickListener =
        new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectButton(mPenBtn);
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
            }
        };

    private final OnClickListener mVideoBtnClickListener =
        new OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoBtn.setClickable(false);
            mSpenSurfaceView.closeControl();
                createObjectRuntime();
            }
        };

    SpenControlListener mControlListener = new SpenControlListener() {

        @Override
        public boolean onCreated(ArrayList<SpenObjectBase> objectList,
            ArrayList<Rect> relativeRectList,
            ArrayList<SpenContextMenuItemInfo> menu,
            ArrayList<Integer> styleList, int pressType, PointF point) {
            if (objectList == null) {
                return false;
            }
			// If SOR is available, display the context menu.
            if (objectList.get(0).getSorInfo() != null) {
                menu.add(new SpenContextMenuItemInfo(
                    CONTEXT_MENU_RUN_ID, "Run", true));
                return true;
            }
            return true;
        }

        @Override
        public boolean onMenuSelected(
            ArrayList<SpenObjectBase> objectList, int itemId) {
            if (objectList == null) {
                return true;
            }

            if (itemId == CONTEXT_MENU_RUN_ID) {
                SpenObjectBase object = objectList.get(0);
                mSpenSurfaceView.getControl().setContextMenuVisible(false);
                mSpenSurfaceView.getControl().setStyle(SpenControlBase.STYLE_BORDER_STATIC);
                mSpenSurfaceView.getControl().setActivated(true);
                
                tuningRect(object);
				// Set the listener and play object.
                mVideoRuntime.setListener(objectRuntimelistener);
                mVideoRuntime.start(object, getRealRect(object.getRect()),
 mSpenSurfaceView.getPan(),
                        mSpenSurfaceView.getZoomRatio(), mSpenSurfaceView.getFrameStartPosition(), mSpenViewLayout);
                mSpenSurfaceView.update();
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

        @Override
        public boolean onClosed(ArrayList<SpenObjectBase> objectList) {
            if(mVideoRuntime != null) {
                mVideoRuntime.stop(true);
            }
            return false;
        }
    };

    void createObjectRuntime() {
        if (mSpenObjectRuntimeInfoList == null
            || mSpenObjectRuntimeInfoList.size() == 0) {
            return;
        }

        try {
            for(SpenObjectRuntimeInfo info : mSpenObjectRuntimeInfoList) {
                if (info.name.equalsIgnoreCase("Video")) {
                    mVideoRuntime =
                        mSpenObjectRuntimeManager.createObjectRuntime(info);

                    mObjectRuntimeInfo = info;
                    startObjectRuntime();

                    return;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "ObjectRuntimeInfo class not found.",
                Toast.LENGTH_SHORT).show();
        } catch (InstantiationException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Failed to access the ObjectRuntimeInfo constructor.",
                Toast.LENGTH_SHORT).show();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Failed to access the ObjectRuntimeInfo field or method.",
                Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "ObjectRuntimeInfo is not loaded.",
                Toast.LENGTH_SHORT).show();
        }
    }

    void startObjectRuntime() {
        if (mVideoRuntime == null) {
            Toast.makeText(mContext,
                "ObjectRuntime is not loaded \n Load Plugin First !!",
                Toast.LENGTH_SHORT).show();
            return;
        }

        SpenObjectBase objectBase = null;
        switch (mVideoRuntime.getType()) {
        case SpenObjectRuntimeInterface.TYPE_NONE:
            return;
        case SpenObjectRuntimeInterface.TYPE_IMAGE:
            objectBase = new SpenObjectImage();
            break;
        case SpenObjectRuntimeInterface.TYPE_STROKE:
            objectBase = new SpenObjectStroke();
            break;
        case SpenObjectRuntimeInterface.TYPE_CONTAINER:
            objectBase = new SpenObjectContainer();
            break;
        default:
            break;
        }
        if(objectBase == null) {
            Toast.makeText(mContext, "Has no selected object.",
                Toast.LENGTH_SHORT).show();
            return;
        }
        objectBase.setSorInfo(mObjectRuntimeInfo.className);
        objectBase.setOutOfViewEnabled(false);

        mVideoRuntime.setListener(objectRuntimelistener);
        mSpenPageDoc.appendObject(objectBase);
        mSpenPageDoc.selectObject(objectBase);
        mSpenSurfaceView.update();
        mSpenSurfaceView.getControl().setContextMenuVisible(false);
        mVideoRuntime.start(objectBase,
            new RectF(0, 0, mSpenPageDoc.getWidth(), mSpenPageDoc.getHeight()),
                mSpenSurfaceView.getPan(), mSpenSurfaceView.getZoomRatio(), mSpenSurfaceView.getFrameStartPosition(),
                mSpenViewLayout);
    }

    SpenObjectRuntime.UpdateListener objectRuntimelistener =
        new SpenObjectRuntime.UpdateListener() {

            @Override
            public void onCompleted(Object objectBase) {
            if (mSpenSurfaceView != null) {
                SpenControlBase control = mSpenSurfaceView.getControl();
                    if (control != null) {
                        control.setContextMenuVisible(true);
                    mSpenSurfaceView.updateScreenFrameBuffer();
                    mSpenSurfaceView.update();
                    }
                }
                mVideoBtn.setClickable(true);
            }

            @Override
            public void onObjectUpdated(RectF rect, Object objectBase) {
            if (mSpenSurfaceView != null) {
                SpenControlBase control = mSpenSurfaceView.getControl();
                    if(control != null) {
                        control.fit();
                        control.invalidate();
                    mSpenSurfaceView.update();
                    }
                }
            }

            @Override
            public void onCanceled(int state, Object objectBase) {
                if (state == SpenObjectRuntimeInterface.CANCEL_STATE_INSERT) {
                    mSpenPageDoc.removeObject((SpenObjectBase) objectBase);
                    mSpenPageDoc.removeSelectedObject();
                mSpenSurfaceView.closeControl();
                mSpenSurfaceView.update();
                } else if (state == SpenObjectRuntimeInterface.CANCEL_STATE_RUN) {
                mSpenSurfaceView.closeControl();
                mSpenSurfaceView.update();
                }
                mVideoBtn.setClickable(true);
            }
        };

    private void selectButton(View v) {
		// Enable or disable the button according to the current mode.
        mSelectionBtn.setSelected(false);
        mPenBtn.setSelected(false);
        v.setSelected(true);
    }

    private RectF getRealRect(RectF rect) {
        float panX = mSpenSurfaceView.getPan().x;
        float panY = mSpenSurfaceView.getPan().y;
        float zoom = mSpenSurfaceView.getZoomRatio();
        PointF startPoint = mSpenSurfaceView.getFrameStartPosition();
        RectF realRect = new RectF();
        realRect.set(
            (rect.left - panX) * zoom + startPoint.x,
            (rect.top - panY) * zoom + startPoint.y,
            (rect.right - panX) * zoom + startPoint.x,
            (rect.bottom - panY) * zoom + startPoint.y
        );
        return realRect;
    }

	private void tuningRect(SpenObjectBase objectBase) {
		RectF objectRect = objectBase.getRect();
		RectF rectAbsolute = getAbsoluteCoordinate(objectRect);
		rectAbsolute.left = (int) rectAbsolute.left;
		rectAbsolute.top = (int) rectAbsolute.top;
		rectAbsolute.right = (int) rectAbsolute.left + (int)rectAbsolute.width();
		rectAbsolute.bottom = (int) rectAbsolute.top + (int)rectAbsolute.height();

		RectF rectRelative = getRealRect(rectAbsolute);
		objectBase.setRect(rectRelative, false);
	}

	protected RectF getAbsoluteCoordinate(RectF rect) {
		RectF dstRect = new RectF();
		float panX = mSpenSurfaceView.getPan().x;
		float panY = mSpenSurfaceView.getPan().y;
		float zoom = mSpenSurfaceView.getZoomRatio();
		PointF startPoint = mSpenSurfaceView.getFrameStartPosition();
		dstRect.left = (rect.left - startPoint.x) / zoom + panX;
		dstRect.right = (rect.right - startPoint.x) / zoom + panX;
		dstRect.top = (rect.top - startPoint.y) / zoom + panY;
		dstRect.bottom = (rect.bottom - startPoint.y) / zoom + panY;
		return dstRect;
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mSpenObjectRuntimeManager != null) {
            if(mVideoRuntime != null) {
                mVideoRuntime.stop(true);
                mSpenObjectRuntimeManager.unload(mVideoRuntime);
            }
            mSpenObjectRuntimeManager.close();
        }

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
}