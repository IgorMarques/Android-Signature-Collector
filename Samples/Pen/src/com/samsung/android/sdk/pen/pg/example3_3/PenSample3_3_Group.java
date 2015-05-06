package com.samsung.android.sdk.pen.pg.example3_3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingSelectionInfo;
import com.samsung.android.sdk.pen.SpenSettingTextInfo;
import com.samsung.android.sdk.pen.document.SpenInvalidPasswordException;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectBase;
import com.samsung.android.sdk.pen.document.SpenObjectContainer;
import com.samsung.android.sdk.pen.document.SpenObjectImage;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.document.SpenUnsupportedTypeException;
import com.samsung.android.sdk.pen.document.SpenUnsupportedVersionException;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenContextMenuItemInfo;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenControlList;
import com.samsung.android.sdk.pen.engine.SpenControlListener;
import com.samsung.android.sdk.pen.engine.SpenFlickListener;
import com.samsung.android.sdk.pen.engine.SpenPageEffectListener;
import com.samsung.android.sdk.pen.engine.SpenSelectionChangeListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTextChangeListener;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.pen.SpenPenInfo;
import com.samsung.android.sdk.pen.pen.SpenPenManager;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingSelectionLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingTextLayout;
import com.samsung.spensdk3.example.R;

public class PenSample3_3_Group extends Activity {

    private final int CONTEXT_MENU_DELETE_ID = 10;
    private final int CONTEXT_MENU_GROUP_ID = 20;
    private final int CONTEXT_MENU_UNGROUP_ID = 21;

    private final int REQUEST_CODE_ATTACH_IMAGE = 100;

    private final String ATTACH_IMAGE_KEY = "Attach Image Key";

    private final int MODE_SELECTION = 0;
    private final int MODE_PEN = 1;
    private final int MODE_IMG_OBJ = 2;
    private final int MODE_TEXT_OBJ = 3;
    private final int MODE_STROKE_OBJ = 4;

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingTextLayout mTextSettingView;
    private SpenSettingSelectionLayout mSelectionSettingView;

    private ImageView mSelectionBtn;
    private ImageView mPenBtn;
    private ImageView mImgObjBtn;
    private ImageView mTextObjBtn;
    private ImageView mStrokeObjBtn;
    private ImageView mSaveFileBtn;
    private ImageView mLoadFileBtn;
    private ImageView mAddPageBtn;
    private TextView mTxtView;

    private int mMode = MODE_PEN;
    private Rect mScreenRect;
    private File mFilePath;
    private boolean isDiscard = false;
    private int mToolType = SpenSurfaceView.TOOL_SPEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
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
            mPenSettingView = new SpenSettingPenLayout(mContext, "", spenViewLayout);
        } else {
            mPenSettingView = new SpenSettingPenLayout(getApplicationContext(), "", spenViewLayout);
        }
        if (mPenSettingView == null) {
            Toast.makeText(mContext, "Cannot create new PenSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Create TextSettingView
        mTextSettingView = new SpenSettingTextLayout(getApplicationContext(), "", new HashMap<String, String>(),
                spenViewLayout);
        if (mTextSettingView == null) {
            Toast.makeText(mContext, "Cannot create new TextSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Create SelectionSettingView
        mSelectionSettingView = new SpenSettingSelectionLayout(getApplicationContext(), "", spenViewLayout);
        if (mSelectionSettingView == null) {
            Toast.makeText(mContext, "Cannot create new SelectionSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewContainer.addView(mPenSettingView);
        spenViewContainer.addView(mTextSettingView);
        spenViewContainer.addView(mSelectionSettingView);

        // Create SpenSurfaceView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        mSpenSurfaceView.setToolTipEnabled(true);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenSurfaceView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewLayout.addView(mSpenSurfaceView);
        mPenSettingView.setCanvasView(mSpenSurfaceView);
        mTextSettingView.setCanvasView(mSpenSurfaceView);
        mSelectionSettingView.setCanvasView(mSpenSurfaceView);

        // Get the dimension of the device screen.
        Display display = getWindowManager().getDefaultDisplay();
        mScreenRect = new Rect();
        display.getRectSize(mScreenRect);
        // Create SpenNoteDoc
        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, mScreenRect.width(), mScreenRect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show();
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

        initSettingInfo();
        // Register the listener
        mSpenSurfaceView.setTouchListener(mPenTouchListener);
        mSpenSurfaceView.setColorPickerListener(mColorPickerListener);
        mSpenSurfaceView.setTextChangeListener(mTextChangeListener);
        mSpenSurfaceView.setFlickListener(mFlickListener);
        mSpenSurfaceView.setControlListener(mControlListener);
        mSpenSurfaceView.setSelectionChangeListener(mSelectionListener);

        // Set a button
        mSelectionBtn = (ImageView) findViewById(R.id.selectionBtn);
        mSelectionBtn.setOnClickListener(mSelectionBtnClickListener);

        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mImgObjBtn = (ImageView) findViewById(R.id.imgObjBtn);
        mImgObjBtn.setOnClickListener(mImgObjBtnClickListener);

        mTextObjBtn = (ImageView) findViewById(R.id.textObjBtn);
        mTextObjBtn.setOnClickListener(mTextObjBtnClickListener);

        mStrokeObjBtn = (ImageView) findViewById(R.id.strokeObjBtn);
        mStrokeObjBtn.setOnClickListener(mStrokeObjBtnClickListener);

        mSaveFileBtn = (ImageView) findViewById(R.id.saveFileBtn);
        mSaveFileBtn.setOnClickListener(mSaveFileBtnClickListener);

        mLoadFileBtn = (ImageView) findViewById(R.id.loadFileBtn);
        mLoadFileBtn.setOnClickListener(mLoadFileBtnClickListener);

        mAddPageBtn = (ImageView) findViewById(R.id.addPageBtn);
        mAddPageBtn.setOnClickListener(mAddPageBtnClickListener);

        mTxtView = (TextView) findViewById(R.id.spen_page);
        mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));

        selectButton(mPenBtn);

        addImgObject(200, 200);
        addTextObject(300, 200, "test");
        addStrokeObject(400, 200);

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SPen/";
        mFilePath = new File(filePath);
        if (!mFilePath.exists()) {
            if (!mFilePath.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isSpenFeatureEnabled == false) {
            mToolType = SpenSurfaceView.TOOL_FINGER;
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void initSettingInfo() {
        // Initialize Pen settings
        List<SpenPenInfo> penList = new ArrayList<SpenPenInfo>();
        SpenPenManager penManager = new SpenPenManager(mContext);
        penList = penManager.getPenInfoList();
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        for (SpenPenInfo info : penList) {
            if (info.name.equalsIgnoreCase("Brush")) {
                penInfo.name = info.className;
                break;
            }
        }
        penInfo.color = Color.BLUE;
        penInfo.size = 10;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
        mPenSettingView.setInfo(penInfo);

        // Initialize text settings
        SpenSettingTextInfo textInfo = new SpenSettingTextInfo();
        int mCanvasWidth = 1080;

        if (mSpenSurfaceView != null) {
            if (mSpenSurfaceView.getCanvasWidth() < mSpenSurfaceView.getCanvasHeight()) {
                mCanvasWidth = mSpenSurfaceView.getCanvasWidth();
            } else {
                mCanvasWidth = mSpenSurfaceView.getCanvasHeight();
            }
            if (mCanvasWidth == 0) {
                mCanvasWidth = 1080;
            }
        }
        textInfo.size = Math.round(8.5 * mCanvasWidth / 360);
        mSpenSurfaceView.setTextSettingInfo(textInfo);
        mTextSettingView.setInfo(textInfo);

        SpenSettingSelectionInfo mSelectionInfo = mSpenSurfaceView.getSelectionSettingInfo();
        mSelectionInfo.type = SpenSettingSelectionInfo.TYPE_LASSO;
        mSelectionSettingView.setInfo(mSelectionInfo);
        mSpenSurfaceView.setSelectionSettingInfo(mSelectionInfo);
    }

    private final SpenTouchListener mPenTouchListener = new SpenTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getToolType(0) == mToolType) {
                // Check if the control is created.
                SpenControlBase control = mSpenSurfaceView.getControl();
                if (control == null) {
                    // When Pen touches the display while it is in Add
                    // ObjectImage mode
                    if (mMode == MODE_IMG_OBJ) {
                        addImgObject(event.getX(), event.getY());

                        return true;

                        // When Pen touches the display while it is in Add
                        // ObjectTextBox mode
                    } else if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_TEXT) {
                        SpenObjectTextBox obj = addTextObject(event.getX(), event.getY(), null);
                        mSpenPageDoc.selectObject(obj);
                        mSpenSurfaceView.update();

                        return true;

                        // When Pen touches the display while it is in Add
                        // ObjectStroke mode
                    } else if (mMode == MODE_STROKE_OBJ) {
                        addStrokeObject(event.getX(), event.getY());

                        return true;
                    }
                }
            }
            return false;
        }
    };

    private final OnClickListener mSelectionBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            // When Spen is in selection mode
            if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_SELECTION) {
                // If SelectionSettingView is open, close it.
                if (mSelectionSettingView.isShown()) {
                    mSelectionSettingView.setVisibility(View.GONE);
                    // If SelectionSettingView is not open, open it.
                } else {
                    mSelectionSettingView.setViewMode(SpenSettingSelectionLayout.VIEW_MODE_NORMAL);
                    mSelectionSettingView.setVisibility(View.VISIBLE);
                }
                // If Spen is not in selection mode, change it to selection
                // mode.
            } else {
                mMode = MODE_SELECTION;
                selectButton(mSelectionBtn);
                mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_SELECTION);
            }
        }
    };

    private final OnClickListener mPenBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            // When Spen is in stroke (pen) mode
            if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_STROKE) {
                // If PenSettingView is open, close it.
                if (mPenSettingView.isShown()) {
                    mPenSettingView.setVisibility(View.GONE);
                    // If PenSettingView is not open, open it.
                } else {
                    mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_EXTENSION);
                    mPenSettingView.setVisibility(View.VISIBLE);
                }
                // If Spen is not in stroke (pen) mode, change it to stroke
                // mode.
            } else {
                mMode = MODE_PEN;
                selectButton(mPenBtn);
                mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_STROKE);
            }
        }
    };

    private final OnClickListener mImgObjBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            if (mMode == MODE_IMG_OBJ) {
                closeSettingView();
                AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
                dlg.setIcon(mContext.getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                dlg.setTitle(mContext.getResources().getString(R.string.app_name))
                        .setMessage("Change the object image. Continue?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                changeImgObj();
                                // Close the dialog.
                                dialog.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                dlg = null;
            } else {
                mMode = MODE_IMG_OBJ;
                selectButton(mImgObjBtn);
                mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_NONE);
            }
        }
    };

    private final OnClickListener mTextObjBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            // When Spen is in text mode
            if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_TEXT) {
                // If TextSettingView is open, close it.
                if (mTextSettingView.isShown()) {
                    mTextSettingView.setVisibility(View.GONE);
                    // If TextSettingView is not open, open it.
                } else {
                    mTextSettingView.setViewMode(SpenSettingTextLayout.VIEW_MODE_NORMAL);
                    mTextSettingView.setVisibility(View.VISIBLE);
                }
                // If Spen is not in text mode, change it to text mode.
            } else {
                mMode = MODE_TEXT_OBJ;
                selectButton(mTextObjBtn);
                mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_TEXT);
            }
        }
    };

    private final OnClickListener mStrokeObjBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            mMode = MODE_STROKE_OBJ;
            selectButton(mStrokeObjBtn);
            mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_NONE);
        }
    };

    private final OnClickListener mSaveFileBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            closeSettingView();
            saveNoteFile(false);
        }
    };

    private final OnClickListener mLoadFileBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            closeSettingView();
            loadNoteFile();
        }
    };

    private final OnClickListener mAddPageBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.setPageEffectListener(new SpenPageEffectListener() {

                @Override
                public void onFinish() {
                    mAddPageBtn.setClickable(true);

                }
            });

            mSpenSurfaceView.closeControl();

            closeSettingView();
            // Create a page next to the current page.
            mSpenPageDoc = mSpenNoteDoc.insertPage(mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()) + 1);
            mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
            mSpenPageDoc.clearHistory();
            v.setClickable(false);
            mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                    SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);

            mTxtView = (TextView) findViewById(R.id.spen_page);
            mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));
        }
    };

    private void changeImgObj() {
        // Set warning messages.
        AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
        dlg.setIcon(mContext.getResources().getDrawable(android.R.drawable.ic_dialog_alert));
        dlg.setTitle(mContext.getResources().getString(R.string.app_name))
                .setMessage(
                        "When you select an image, copy the image in NoteDoc data. \n" + "If the image is large,"
                                + " the function is slow and it takes a long time to save/load.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        callGalleryForInputImage(REQUEST_CODE_ATTACH_IMAGE);
                        // Close the dialog.
                        dialog.dismiss();
                    }
                }).show();
        dlg = null;
    }

    private void callGalleryForInputImage(int nRequestCode) {
        // Get an image from Gallery.
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, nRequestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "Cannot find gallery.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void addImgObject(float x, float y) {
        SpenObjectImage imgObj = new SpenObjectImage();
        Bitmap imageBitmap;
        // Set a bitmap file to ObjectImage.
        // If there is a file attached, set it to ObjectImage.
        if (mSpenNoteDoc.hasAttachedFile(ATTACH_IMAGE_KEY)) {
            imageBitmap = BitmapFactory.decodeFile(mSpenNoteDoc.getAttachedFile(ATTACH_IMAGE_KEY));
            // If there is no file attached, set the launcher icon to
            // ObjectImage.
        } else {
            imageBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        }
        imgObj.setImage(imageBitmap);

        // Set the location to insert ObjectImage and add it to PageDoc.
        float imgWidth = imageBitmap.getWidth();
        float imgHeight = imageBitmap.getHeight();
        RectF rect = getRealPoint(x, y, imgWidth, imgHeight);
        imgObj.setRect(rect, true);
        mSpenPageDoc.appendObject(imgObj);
        mSpenSurfaceView.update();

        imageBitmap.recycle();
    }

    private SpenObjectTextBox addTextObject(float x, float y, String str) {
        // Set the location to insert ObjectTextBox and add it to PageDoc.
        SpenObjectTextBox textObj = new SpenObjectTextBox();
        RectF rect = getRealPoint(x, y, 0, 0);
        rect.right += 350;
        rect.bottom += 50;
        textObj.setRect(rect, true);
        textObj.setText(str);
        mSpenPageDoc.appendObject(textObj);
        mSpenSurfaceView.update();

        return textObj;
    }

    private void addStrokeObject(float x, float y) {
        // Set the location to insert ObjectStroke and add it to PageDoc.
        RectF rect = getRealPoint(x, y, 0, 0);
        float rectX = rect.centerX();
        float rectY = rect.centerY();
        int pointSize = 157;
        float[][] strokePoint = new float[pointSize][2];
        for (int i = 0; i < pointSize; i++) {
            strokePoint[i][0] = rectX++;
            strokePoint[i][1] = (float) (rectY + Math.sin(.04 * i) * 50);
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

        SpenObjectStroke strokeObj = new SpenObjectStroke(mPenSettingView.getInfo().name, points, pressures, timestamps);
        strokeObj.setPenSize(mPenSettingView.getInfo().size);
        strokeObj.setColor(mPenSettingView.getInfo().color);
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
        realRect.set((x - width / 2) / zoom + panX, (y - height / 2) / zoom + panY, (x + width / 2) / zoom + panX,
                (y + height / 2) / zoom + panY);
        return realRect;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(mContext, "Cannot find the image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Process a request to attach an image.
            if (requestCode == REQUEST_CODE_ATTACH_IMAGE) {
                // Get the image's URI and get the file path to attach it.
                Uri imageFileUri = data.getData();
                Cursor cursor = getContentResolver().query(Uri.parse(imageFileUri.toString()), null, null, null, null);
                cursor.moveToNext();
                String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));

                mSpenNoteDoc.attachFile(ATTACH_IMAGE_KEY, imagePath);
            }
        }
    }

    private boolean saveNoteFile(final boolean isClose) {
        // Prompt Save File dialog to get the file name
        // and get its save format option (note file or image).
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.save_file_dialog, (ViewGroup) findViewById(R.id.layout_root));

        AlertDialog.Builder builderSave = new AlertDialog.Builder(mContext);
        builderSave.setTitle("Enter file name");
        builderSave.setView(layout);

        final EditText inputPath = (EditText) layout.findViewById(R.id.input_path);
        inputPath.setText("Note");

        builderSave.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final RadioGroup selectFileExt = (RadioGroup) layout.findViewById(R.id.radioGroup);

                // Set the save directory for the file.
                String saveFilePath = mFilePath.getPath() + '/';
                String fileName = inputPath.getText().toString();
                if (!fileName.equals("")) {
                    saveFilePath += fileName;
                    switch (selectFileExt.getCheckedRadioButtonId()) {
                    // Save it as a note file.
                    case R.id.radioNote:
                        saveFilePath += ".spd";
                        saveNoteFile(saveFilePath);

                        break;

                    // Save it as an image.
                    case R.id.radioImage:
                        saveFilePath += ".png";
                        captureSpenSurfaceView(saveFilePath);

                        break;
                    default:
                        break;
                    }
                    if (isClose) {
                        finish();
                    }
                } else {
                    Toast.makeText(mContext, "Invalid filename !!!", Toast.LENGTH_LONG).show();
                }
            }
        });
        builderSave.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isClose) {
                    finish();
                }
            }
        });

        AlertDialog dlgSave = builderSave.create();
        dlgSave.show();

        return true;
    }

    boolean saveNoteFile(String strFileName) {
        try {
            // Save NoteDoc
            mSpenNoteDoc.save(strFileName);
            Toast.makeText(mContext, "Save success to " + strFileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot save NoteDoc file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void captureSpenSurfaceView(String strFileName) {

        // Capture the view
        Bitmap imgBitmap = mSpenSurfaceView.captureCurrentView(true);
        if (imgBitmap == null) {
            Toast.makeText(mContext, "Capture failed." + strFileName, Toast.LENGTH_SHORT).show();
            return;
        }

        OutputStream out = null;
        try {
            // Create FileOutputStream and save the captured image.
            out = new FileOutputStream(strFileName);
            imgBitmap.compress(CompressFormat.PNG, 100, out);
            // Save the note information.
            mSpenNoteDoc.save(out);
            out.close();
            Toast.makeText(mContext, "Captured images were stored in the file" + strFileName, Toast.LENGTH_SHORT)
                    .show();
        } catch (IOException e) {
            File tmpFile = new File(strFileName);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            Toast.makeText(mContext, "Failed to save the file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            File tmpFile = new File(strFileName);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            Toast.makeText(mContext, "Failed to save the file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        imgBitmap.recycle();
    }

    private void loadNoteFile() {
        // Load the file list.
        final String fileList[] = setFileList();
        if (fileList == null) {
            return;
        }

        // Prompt Load File dialog.
        new AlertDialog.Builder(mContext).setTitle("Select file")
                .setItems(fileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strFilePath = mFilePath.getPath() + '/' + fileList[which];

                        try {
                            // Create NoteDoc with the selected file.
                            SpenNoteDoc tmpSpenNoteDoc = new SpenNoteDoc(mContext, strFilePath, mScreenRect.width(),
                                    SpenNoteDoc.MODE_WRITABLE);
                            mSpenNoteDoc.close();
                            mSpenNoteDoc = tmpSpenNoteDoc;
                            if (mSpenNoteDoc.getPageCount() == 0) {
                                mSpenPageDoc = mSpenNoteDoc.appendPage();
                            } else {
                                mSpenPageDoc = mSpenNoteDoc.getPage(mSpenNoteDoc.getLastEditedPageIndex());
                            }
                            mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
                            mTxtView = (TextView) findViewById(R.id.spen_page);
                            mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));

                            mSpenSurfaceView.update();
                            Toast.makeText(mContext, "Successfully loaded noteFile.", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(mContext, "Cannot open this file.", Toast.LENGTH_LONG).show();
                        } catch (SpenUnsupportedTypeException e) {
                            Toast.makeText(mContext, "This file is not supported.", Toast.LENGTH_LONG).show();
                        } catch (SpenInvalidPasswordException e) {
                            Toast.makeText(mContext, "This file is locked by a password.", Toast.LENGTH_LONG).show();
                        } catch (SpenUnsupportedVersionException e) {
                            Toast.makeText(mContext, "This file is the version that does not support.",
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Failed to load noteDoc.", Toast.LENGTH_LONG).show();
                        }
                    }
                }).show();
    }

    private String[] setFileList() {
        // Call the file list under the directory in mFilePath.
        if (!mFilePath.exists()) {
            if (!mFilePath.mkdirs()) {
                Toast.makeText(mContext, "Save Path Creation Error", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        // Filter in spd and png files.
        File[] fileList = mFilePath.listFiles(new txtFileFilter());
        if (fileList == null) {
            Toast.makeText(mContext, "File does not exist.", Toast.LENGTH_SHORT).show();
            return null;
        }

        int i = 0;
        String[] strFileList = new String[fileList.length];
        for (File file : fileList) {
            strFileList[i++] = file.getName();
        }

        return strFileList;
    }

    static class txtFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(".spd") || name.endsWith(".png"));
        }
    }

    private final SpenColorPickerListener mColorPickerListener = new SpenColorPickerListener() {
        @Override
        public void onChanged(int color, int x, int y) {
            // Set the color from the Color Picker to the setting view.
            if (mPenSettingView != null) {
                if (mMode == MODE_PEN) {
                    SpenSettingPenInfo penInfo = mPenSettingView.getInfo();
                    penInfo.color = color;
                    mPenSettingView.setInfo(penInfo);
                } else if (mMode == MODE_TEXT_OBJ) {
                    SpenSettingTextInfo textInfo = mSpenSurfaceView.getTextSettingInfo();
                    textInfo.color = color;
                    mTextSettingView.setInfo(textInfo);
                }
            }
        }
    };

    SpenTextChangeListener mTextChangeListener = new SpenTextChangeListener() {

        @Override
        public boolean onSelectionChanged(int arg0, int arg1) {
            return false;
        }

        @Override
        public void onMoreButtonDown(SpenObjectTextBox arg0) {
        }

        @Override
        public void onChanged(SpenSettingTextInfo info, int state) {
            if (mTextSettingView != null) {
                if (state == CONTROL_STATE_SELECTED) {
                    mTextSettingView.setInfo(info);
                }
            }
        }

        @Override
        public void onFocusChanged(boolean arg0) {

        }
    };

    private final SpenFlickListener mFlickListener = new SpenFlickListener() {

        @Override
        public boolean onFlick(int direction) {
            int pageIndex = mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId());
            int pageCount = mSpenNoteDoc.getPageCount();
            if (pageCount > 1) {
                // Flick left and turn to the previous page.
                if (direction == DIRECTION_LEFT) {
                    mSpenPageDoc = mSpenNoteDoc.getPage((pageIndex + pageCount - 1) % pageCount);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_LEFT,
                            SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);

                    // Flick right and turn to the next page.
                } else if (direction == DIRECTION_RIGHT) {
                    mSpenPageDoc = mSpenNoteDoc.getPage((pageIndex + 1) % pageCount);
                    mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                            SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);
                }
                mTxtView = (TextView) findViewById(R.id.spen_page);
                mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));
                return true;
            }
            return false;
        }
    };

    private final SpenControlListener mControlListener = new SpenControlListener() {

        @Override
        public void onRotationChanged(float arg0, SpenObjectBase arg1) {
        }

        @Override
        public void onRectChanged(RectF arg0, SpenObjectBase arg1) {
        }

        @Override
        public void onObjectChanged(ArrayList<SpenObjectBase> arg0) {
        }

        @Override
        public boolean onMenuSelected(ArrayList<SpenObjectBase> objectList, int itemId) {
            SpenObjectContainer objContainer;
            switch (itemId) {
            // Remove the selected object.
            case CONTEXT_MENU_DELETE_ID:
                // mSpenPageDoc.removeSelectedObject();
                for (SpenObjectBase obj : objectList) {
                    mSpenPageDoc.removeObject(obj);
                }
                mSpenSurfaceView.closeControl();
                mSpenSurfaceView.update();
                break;

            // Group the objects.
            case CONTEXT_MENU_GROUP_ID:
                objContainer = mSpenPageDoc.groupObject(objectList, false);
                mSpenSurfaceView.closeControl();
                mSpenPageDoc.selectObject(objContainer);
                mSpenSurfaceView.update();
                break;

            // Ungroup the grouped objects.
            case CONTEXT_MENU_UNGROUP_ID:
                ArrayList<SpenObjectBase> objList = new ArrayList<SpenObjectBase>();
                for (SpenObjectBase selectedObj : objectList) {
                    if (selectedObj.getType() == SpenObjectBase.TYPE_CONTAINER) {
                        objContainer = (SpenObjectContainer) selectedObj;
                        for (SpenObjectBase obj : objContainer.getObjectList()) {
                            objList.add(obj);
                        }
                        mSpenPageDoc.ungroupObject((SpenObjectContainer) selectedObj, false);
                    }
                }
                mSpenSurfaceView.closeControl();
                mSpenPageDoc.selectObject(objList);
                mSpenSurfaceView.update();
            default:
                break;
            }

            return true;
        }

        @Override
        public boolean onCreated(ArrayList<SpenObjectBase> objectList, ArrayList<Rect> relativeRectList,
                ArrayList<SpenContextMenuItemInfo> menu, ArrayList<Integer> styleList, int pressType, PointF point) {

            // Set the Context menu.
            menu.add(new SpenContextMenuItemInfo(CONTEXT_MENU_DELETE_ID, "Delete", true));
            // Display Group menu when more than one object is selected.
            if (objectList.size() > 1) {
                menu.add(new SpenContextMenuItemInfo(CONTEXT_MENU_GROUP_ID, "Group", true));
            }
            // Display Ungroup menu if the selected objects include one or more
            // ObjectContainers.
            for (SpenObjectBase obj : objectList) {
                if (obj.getType() == SpenObjectBase.TYPE_CONTAINER) {
                    menu.add(new SpenContextMenuItemInfo(CONTEXT_MENU_UNGROUP_ID, "Ungroup", true));
                    break;
                }
            }
            if (objectList.size() == 1) {
                return true;
            }
            // Attach an individual control for each object.
            SpenControlList controlList = new SpenControlList(mContext, mSpenPageDoc);
            controlList.setObject(objectList);
            controlList.setGroup(false);
            mSpenSurfaceView.setControl(controlList);
            controlList.setContextMenu(menu);

            return false;
        }

        @Override
        public boolean onClosed(ArrayList<SpenObjectBase> arg0) {
            return false;
        }
    };

    private final SpenSelectionChangeListener mSelectionListener = new SpenSelectionChangeListener() {

        @Override
        public void onChanged(SpenSettingSelectionInfo info) {
            // Close Setting view if selection type is changed.
            mSelectionSettingView.setVisibility(SpenSurfaceView.GONE);
        }
    };

    private void selectButton(View v) {
        // Enable or disable the button according to the current mode.
        mSelectionBtn.setSelected(false);
        mPenBtn.setSelected(false);
        mImgObjBtn.setSelected(false);
        mTextObjBtn.setSelected(false);
        mStrokeObjBtn.setSelected(false);

        v.setSelected(true);

        closeSettingView();
    }

    private void closeSettingView() {
        // Close all the setting views.
        mPenSettingView.setVisibility(SpenSurfaceView.GONE);
        mTextSettingView.setVisibility(SpenSurfaceView.GONE);
        mSelectionSettingView.setVisibility(SpenSurfaceView.GONE);
    }

    @Override
    public void onBackPressed() {
        if (mSpenPageDoc.getObjectCount(true) > 0 && mSpenPageDoc.isChanged()) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
            dlg.setIcon(mContext.getResources().getDrawable(android.R.drawable.ic_dialog_alert));
            dlg.setTitle(mContext.getResources().getString(R.string.app_name))
                    .setMessage("Do you want to exit after save?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveNoteFile(true);
                            dialog.dismiss();
                        }
                    }).setNeutralButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            isDiscard = true;
                            finish();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            dlg = null;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPenSettingView != null) {
            mPenSettingView.close();
        }
        if (mTextSettingView != null) {
            mTextSettingView.close();
        }
        if (mSelectionSettingView != null) {
            mSelectionSettingView.close();
        }

        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.closeControl();
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }

        if (mSpenNoteDoc != null) {
            try {
                if (isDiscard) {
                    mSpenNoteDoc.discard();
                } else {
                    mSpenNoteDoc.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    };
}