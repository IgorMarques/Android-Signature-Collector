package com.samsung.android.sdk.pen.pg.example5_6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenContextMenuItemInfo;
import com.samsung.android.sdk.pen.engine.SpenControlListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.recognition.SpenCreationFailureException;
import com.samsung.android.sdk.pen.recognition.SpenRecognitionBase.ResultListener;
import com.samsung.android.sdk.pen.recognition.SpenRecognitionInfo;
import com.samsung.android.sdk.pen.recognition.SpenTextRecognition;
import com.samsung.android.sdk.pen.recognition.SpenTextRecognitionManager;
import com.samsung.spensdk3.example.R;

public class PenSample5_6_TextRecognition extends Activity {

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    RelativeLayout mSpenViewLayout;

    private SpenTextRecognition mTextRecognition = null;
    private SpenTextRecognitionManager mSpenTextRecognitionManager = null;
    private boolean mIsProcessingRecognition = false;

    private ImageView mSelectionBtn;
    private ImageView mPenBtn;

    private Rect mScreenRect;
    private int mToolType = SpenSurfaceView.TOOL_SPEN;
	
	private ProgressDialog mProgressDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);
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

        mSpenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);

        // Create SpenSurfaceView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenSurfaceView.",
                Toast.LENGTH_SHORT).show();
            finish();
        }
        mSpenViewLayout.addView(mSpenSurfaceView);

		// Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        mScreenRect = new Rect();
        display.getRectSize(mScreenRect);
		// Create SpenNoteDoc
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext,
                mScreenRect.width(), mScreenRect.height());
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
		// Set PageDoc to View
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        initPenSettingInfo();
		// Register the listener
        mSpenSurfaceView.setControlListener(mControlListener);

		// Set a button
        mSelectionBtn = (ImageView) findViewById(R.id.selectionBtn);
        mSelectionBtn.setOnClickListener(mSelectionBtnClickListener);

        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        selectButton(mPenBtn);

        setTextRecognition();

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

    private void setTextRecognition() {
		// Set TextRecognition
        mSpenTextRecognitionManager = new SpenTextRecognitionManager(mContext);

        List<SpenRecognitionInfo> textRecognitionList = mSpenTextRecognitionManager.getInfoList(
                SpenObjectBase.TYPE_STROKE, SpenObjectBase.TYPE_CONTAINER);

        try {
            if (textRecognitionList.size() > 0) {
                for (SpenRecognitionInfo info : textRecognitionList) {
                    if (info.name.equalsIgnoreCase("SpenText")) {
                        mTextRecognition = mSpenTextRecognitionManager
                            .createRecognition(info);
                        break;
                    }
                }
            } else {
                finish();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "SpenTextRecognitionManager class not found.",
                Toast.LENGTH_SHORT).show();
            return;
        } catch (InstantiationException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Failed to access the SpenTextRecognitionManager constructor.",
                Toast.LENGTH_SHORT).show();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Failed to access the SpenTextRecognitionManager field or method.",
                Toast.LENGTH_SHORT).show();
            return;
        } catch (SpenCreationFailureException e) {
			// Exit the application if the device does not support Recognition feature.
            e.printStackTrace();
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setIcon(this.getResources().getDrawable(
                android.R.drawable.ic_dialog_alert));
            ad.setTitle(this.getResources().getString(R.string.app_name))
                .setMessage(
                    "This device does not support Recognition.")
                .setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                            int which) {
							// Close the dialog.
                            dismissDlg();
                            finish();
                        }
                    }).show();
            ad = null;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "SpenTextRecognitionManager engine not loaded.",
                Toast.LENGTH_SHORT).show();
            return;
        }

		// Set the language to recognize Korean, English, or Chinese.
        List<String> languageList = mTextRecognition.getSupportedLanguage();
        if (textRecognitionList.size() > 0) {
            for (String language : languageList) {
                if (language.equalsIgnoreCase("kor")) {
                    mTextRecognition.setLanguage(language);
                    break;
                }
            }
        }

        try {
            mTextRecognition.setResultListener(new ResultListener() {
                @Override
                public void onResult(List<SpenObjectBase> input,
                    List<SpenObjectBase> output) {
					// Get the rect of the selected objects and set the rect for text.
					// Remove the selected objects and append the recognized objects to pageDoc.
                    RectF rect = new RectF(mScreenRect.width(),
                        mScreenRect.height(), 0, 0);

                    for (SpenObjectBase obj : input) {
                        if (rect.contains(obj.getRect()) == false) {
                            RectF objRect = obj.getRect();
                            if(objRect == null) continue;
                            rect.left = rect.left < objRect.left
                                ? rect.left : objRect.left;
                            rect.top = rect.top < objRect.top
                                ? rect.top : objRect.top;
                            rect.right = rect.right > objRect.right
                                ? rect.right : objRect.right;
                            rect.bottom = rect.bottom > objRect.bottom
                                ? rect.bottom : objRect.bottom;
                        }
                        mSpenPageDoc.removeObject(obj);
                    }
                    mIsProcessingRecognition = false;
                    if(output == null)
                    {
                        mSpenSurfaceView.closeControl();
                        mSpenSurfaceView.update();
                        dismissDlg();
                    	return;
                    }

                    for (SpenObjectBase obj : output) {
                        if (obj instanceof SpenObjectTextBox) {
                            obj.setRect(rect, false);
                            mSpenPageDoc.appendObject(obj);
                        }
                    }
                    //mIsProcessingRecognition = false;
                    mSpenSurfaceView.closeControl();
                    mSpenSurfaceView.update();
                    dismissDlg();
                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "SpenTextRecognition is not loaded.",
                Toast.LENGTH_SHORT).show();
            dismissDlg();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "SpenTextRecognition is not loaded.",
                Toast.LENGTH_SHORT).show();
            dismissDlg();
            return;
        }
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

    private final SpenControlListener mControlListener = new SpenControlListener() {

        @Override
        public boolean onCreated(ArrayList<SpenObjectBase> selectedList,
                ArrayList<Rect> arg1,
                ArrayList<SpenContextMenuItemInfo> arg2,
                ArrayList<Integer> arg3, int arg4, PointF arg5) {
            if (selectedList.size() > 0 && !mIsProcessingRecognition) {
				// List the selected strokes and send the list as a request.
                ArrayList<SpenObjectBase> inputList = new ArrayList<SpenObjectBase>();
                for (int i = 0; i < selectedList.size(); i++) {
                    if (selectedList.get(i).getType() == SpenObjectBase.TYPE_STROKE) {
                        inputList.add(selectedList.get(i));
                    }
                }

                if (inputList.size() <= 0) {
                    return false;
                }
                mIsProcessingRecognition = true;
                try {
					mProgressDlg = ProgressDialog.show(mContext, "", "Recognizing ...", true);
                    mTextRecognition.request(inputList);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "SpenTextRecognition is not loaded.",
                        Toast.LENGTH_SHORT).show();
                    dismissDlg();	
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "SpenTextRecognition engine not loaded.",
                        Toast.LENGTH_SHORT).show();
                    dismissDlg();
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onClosed(ArrayList<SpenObjectBase> arg0) {
            return false;
        }

        @Override
        public boolean onMenuSelected(ArrayList<SpenObjectBase> arg0,
                int arg1) {
            return false;
        }

        @Override
        public void onObjectChanged(ArrayList<SpenObjectBase> arg0) {
        }

        @Override
        public void onRectChanged(RectF arg0, SpenObjectBase arg1) {
        }

        @Override
        public void onRotationChanged(float arg0, SpenObjectBase arg1) {
        }
    };

    private void selectButton(View v) {
		// Enable or disable the button according to the current mode.
        mSelectionBtn.setSelected(false);
        mPenBtn.setSelected(false);

        v.setSelected(true);
    }

    private void dismissDlg() {
    	if(mProgressDlg != null){
    		if (mProgressDlg.isShowing()) {
                mProgressDlg.dismiss();
            }
    	}
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDlg();
        if(mIsProcessingRecognition){
            return;
        }
        if (mTextRecognition != null) {
            mSpenTextRecognitionManager.destroyRecognition(mTextRecognition);
            mSpenTextRecognitionManager.close();
        }

        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.closeControl();
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