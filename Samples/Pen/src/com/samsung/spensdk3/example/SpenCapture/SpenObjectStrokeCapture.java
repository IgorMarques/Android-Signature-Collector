package com.samsung.spensdk3.example.SpenCapture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.content.Context;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectImage;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.spensdk3.example.R;

import java.io.IOException;

/**
 * Created by igormarquessilva on 11/05/15.
 */
public class SpenObjectStrokeCapture extends Activity {

//    1. To begin with, initialize the Pen APIs
//    2. Create a SpenPageDoc and attach it to a SpenSurfaceView.
//    3. To register a signature, capture all user strokes (SpenObjectStroke) in an arraylist and call then register on the SpenSignatureVerification object. Register as many times until the required count is reached.
//    4. During verification, call the request on the SpenSignatureVerification object by passing the SpenObjectStroke arraylist.

    private final int MODE_PEN = 0;
    private final int MODE_IMG_OBJ = 1;
    private final int MODE_STROKE_OBJ = 3;

    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    private SpenSettingPenLayout mPenSettingView;
    private Context mContext;
    private int mMode = MODE_PEN;
    private int mToolType = SpenSurfaceView.TOOL_SPEN;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_pen);
        mContext = this;

        // Initialize Spen
        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if (processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Spen.",
                    Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }

        // Create Spen View
        RelativeLayout spenViewLayout =
                (RelativeLayout) findViewById(R.id.spenViewLayout);
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenView.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewLayout.addView(mSpenSurfaceView);

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

        if(isSpenFeatureEnabled == false) {
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext,
                    "Device does not support Spen. \n You can draw stroke by finger.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {

        e.printStackTrace();
        int errType = e.getType();
        // If the device is not a Samsung device or if the device does not support Pen.
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
                || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            Toast.makeText(mContext, "This device does not support Spen.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            // If SpenSDK APK is not installed.
            showAlertDialog( "You need to install additional Spen software"
                    +" to use this application."
                    + "You will be taken to the installation screen."
                    + "Restart this application after the software has been installed."
                    , true);
        } else if (errType
                == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            // SpenSDK APK must be updated.
            showAlertDialog( "You need to update your Spen software "
                    + "to use this application."
                    + " You will be taken to the installation screen."
                    + " Restart this application after the software has been updated."
                    , true);
        } else if (errType
                == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            // Update of SpenSDK APK to an available new version is recommended.
            showAlertDialog( "We recommend that you update your Spen software"
                    +" before using this application."
                    + " You will be taken to the installation screen."
                    + " Restart this application after the software has been updated."
                    , false);
            return false;
        }
        return true;
    }
    private void showAlertDialog(String msg, final boolean closeActivity) {

        AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
        dlg.setIcon(getResources().getDrawable(
                android.R.drawable.ic_dialog_alert));
        dlg.setTitle("Upgrade Notification")
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                // Go to the market website and install/update APK.
                                Uri uri = Uri.parse("market://details?id="
                                        + Spen.SPEN_NATIVE_PACKAGE_NAME);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                                dialog.dismiss();
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                if(closeActivity == true) {
                                    // Terminate the activity if APK is not installed.
                                    finish();
                                }
                                dialog.dismiss();
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if(closeActivity == true) {
                            // Terminate the activity if APK is not installed.
                            finish();
                        }
                    }
                })
                .show();
        dlg = null;
    }

    private final SpenTouchListener mPenTouchListener = new SpenTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getToolType(0) == mToolType) {
                // Check if the control is created.
                SpenControlBase control = mSpenSurfaceView.getControl();
                if (control == null) {
                    // When Pen touches the display while it is in Add ObjectImage mode
                    if (mMode == MODE_IMG_OBJ) {
                        // Set a bitmap file to ObjectImage.
                        SpenObjectImage imgObj = new SpenObjectImage();
                        Bitmap imageBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                R.drawable.ic_launcher);
                        imgObj.setImage(imageBitmap);

                        // Set the location to insert ObjectImage and add it to PageDoc.
                        float imgWidth = imageBitmap.getWidth();
                        float imgHeight = imageBitmap.getHeight();
                        RectF rect = getRealPoint(event.getX(), event.getY(), imgWidth, imgHeight);
                        imgObj.setRect(rect, true);
                        mSpenPageDoc.appendObject(imgObj);
                        mSpenSurfaceView.update();

                        imageBitmap.recycle();
                        return true;
                        // When Pen touches the display while it is in Add ObjectTextBox mode
                    } else if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_TEXT) {
                        // Set the location to insert ObjectTextBox and add it to PageDoc.
                        SpenObjectTextBox textObj = new SpenObjectTextBox();
                        RectF rect = getRealPoint(event.getX(), event.getY(), 0, 0);
                        rect.right += 350;
                        rect.bottom += 50;
                        textObj.setRect(rect, true);
                        mSpenPageDoc.appendObject(textObj);
                        mSpenPageDoc.selectObject(textObj);
                        mSpenSurfaceView.update();
                        // When Pen touches the display while it is in Add ObjectStroke mode
                    } else if (mMode == MODE_STROKE_OBJ) {
                        // Set the location to insert ObjectStroke and add it to PageDoc.
                        RectF rect = getRealPoint(event.getX(), event.getY(), 0, 0);
                        float rectX = rect.centerX();
                        float rectY = rect.centerY();
                        int pointSize = 157;
                        float[][] strokePoint = new float[pointSize][2];
                        for (int i = 0; i < pointSize; i++) {
                            strokePoint[i][0] = rectX++;
                            strokePoint[i][1] = (float) (rectY + Math.sin(.04 * i) * 50);
                        }
                        PointF[] points = new PointF[pointSize];
                        float   [] pressures = new float[pointSize];
                        int[] timestamps = new int[pointSize];

                        for (int i = 0; i < pointSize; i++) {
                            points[i] = new PointF();
                            points[i].x = strokePoint[i][0];
                            points[i].y = strokePoint[i][1];
                            pressures[i] = 1;
                            timestamps[i] = (int) android.os.SystemClock.uptimeMillis();
                        }

                        SpenObjectStroke strokeObj = new SpenObjectStroke(mPenSettingView.getInfo().name, points,
                                pressures, timestamps);
                        strokeObj.setPenSize(mPenSettingView.getInfo().size);
                        strokeObj.setColor(mPenSettingView.getInfo().color);
                        mSpenPageDoc.appendObject(strokeObj);
                        mSpenSurfaceView.update();
                    }
                }
            }
            return false;
        }
    };

    private RectF getRealPoint(float x, float y, float width, float height) {
        float panX = mSpenSurfaceView.getPan().x;
        float panY = mSpenSurfaceView.getPan().y;
        float zoom = mSpenSurfaceView.getZoomRatio();
        width *= zoom;
        height *= zoom;
        RectF realRect = new RectF();
        realRect.set((x - width / 2) / zoom + panX, (y - height / 2) / zoom + panY, (x + width / 2) / zoom + panX,
                (y + height / 2) / zoom + panY);
        return realRect;
    }

}