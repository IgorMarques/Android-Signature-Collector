package com.samsung.android.sdk.pen.pg.example6_2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingEraserInfo;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.SpenSettingTextInfo;
import com.samsung.android.sdk.pen.document.SpenInvalidPasswordException;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenObjectImage;
import com.samsung.android.sdk.pen.document.SpenObjectStroke;
import com.samsung.android.sdk.pen.document.SpenObjectTextBox;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.document.SpenUnsupportedTypeException;
import com.samsung.android.sdk.pen.document.SpenUnsupportedVersionException;
import com.samsung.android.sdk.pen.engine.SpenColorPickerListener;
import com.samsung.android.sdk.pen.engine.SpenControlBase;
import com.samsung.android.sdk.pen.engine.SpenFlickListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTextChangeListener;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;
import com.samsung.android.sdk.pen.pen.SpenPenInfo;
import com.samsung.android.sdk.pen.pen.SpenPenManager;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingEraserLayout.EventListener;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;
import com.samsung.android.sdk.pen.settingui.SpenSettingTextLayout;
import com.samsung.spensdk3.example.R;

public class PenSample6_2_TemplatePage extends Activity {

    private final int REQUEST_CODE_ATTACH_IMAGE = 100;

    private final String ATTACH_IMAGE_KEY = "Attach Image Key";

    private final int MODE_PEN = 0;
    private final int MODE_IMG_OBJ = 1;
    private final int MODE_TEXT_OBJ = 2;
    private final int MODE_STROKE_OBJ = 3;

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    public SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;
    private SpenSettingPenLayout mPenSettingView;
    private SpenSettingTextLayout mTextSettingView;
    private SpenSettingEraserLayout mEraserSettingView;
    private ListView mMenuLayout;

    private ImageView mPenBtn;
    private ImageView mEraserBtn;
    private ImageView mImgObjBtn;
    private ImageView mTextObjBtn;
    private ImageView mStrokeObjBtn;
    private ImageView mSaveFileBtn;
    private ImageView mLoadFileBtn;
    private ImageView mMenuBtn;
    private TextView mTxtView;

    private int mMode = MODE_PEN;
    private Rect mScreenRect;
    private File mFilePath;
    private boolean isDiscard = false;
    private int mToolType = SpenSurfaceView.TOOL_SPEN;
    private int MENU_VISIBLE;

    private LoadFileFromAssetsAsyncTask loadTask;
    private ArrayList<String> fileArrayList;

    private final String[] menuItemList = { "Add page", "Append 3 Pages", "Insert Template Page",
            "Attach Template Page", "Copy & Append Page" };
    private static final int SPEN_ADD_PAGE = 0;
    private static final int SPEN_INSERT_PAGES = 1;
    private static final int SPEN_INSERT_TEMPLATE_PAGE = 2;
    private static final int SPEN_ATTACH_TEMPLATE_PAGE = 3;
    private static final int SPEN_COPY_PASTE_PAGE = 4;

    private String templatePath = "";
    private String templateURI = "";

    @SuppressWarnings("unchecked")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_page);
        mContext = this;

        // Initialize Spen.
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
        mPenSettingView = new SpenSettingPenLayout(getApplicationContext(), new String(), spenViewLayout);
        if (mPenSettingView == null) {
            Toast.makeText(mContext, "Cannot create new PenSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Create TextSettingView
        mTextSettingView = new SpenSettingTextLayout(getApplicationContext(), new String(),
                new HashMap<String, String>(), spenViewLayout);
        if (mTextSettingView == null) {
            Toast.makeText(mContext, "Cannot create new TextSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Create EraserSettingView
        mEraserSettingView = new SpenSettingEraserLayout(getApplicationContext(), new String(), spenViewLayout);
        if (mEraserSettingView == null) {
            Toast.makeText(mContext, "Cannot create new EraserSettingView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewContainer.addView(mPenSettingView);
        spenViewContainer.addView(mTextSettingView);
        spenViewContainer.addView(mEraserSettingView);

        // Create SpenView
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        mSpenSurfaceView.setToolTipEnabled(true);
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewLayout.addView(mSpenSurfaceView);
        mPenSettingView.setCanvasView(mSpenSurfaceView);
        mTextSettingView.setCanvasView(mSpenSurfaceView);
        mEraserSettingView.setCanvasView(mSpenSurfaceView);

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
        // Add a Page to NoteDoc, get an instance, and set it to the member variable.
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
        mEraserSettingView.setEraserListener(mEraserListener);

        // Set a button
        mPenBtn = (ImageView) findViewById(R.id.penBtn);
        mPenBtn.setOnClickListener(mPenBtnClickListener);

        mEraserBtn = (ImageView) findViewById(R.id.eraserBtn);
        mEraserBtn.setOnClickListener(mEraserBtnClickListener);

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

        mMenuBtn = (ImageView) findViewById(R.id.menuBtn);
        mMenuBtn.setOnClickListener(mMenuBtnClickListener);

        mTxtView = (TextView) findViewById(R.id.spen_page);
        mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));

        MENU_VISIBLE = View.GONE;
        mMenuLayout = (ListView) findViewById(R.id.menuLayout);
        ArrayAdapter<String> menuList = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,
                menuItemList);
        mMenuLayout.setAdapter(menuList);
        mMenuLayout.setOnItemClickListener(mOnItemClickListener);
        mMenuLayout.setVisibility(MENU_VISIBLE);
        mMenuLayout.bringToFront();

        selectButton(mPenBtn);

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

        fileArrayList = new ArrayList<String>();
        fileArrayList.add("template_portrait.spd");
        fileArrayList.add("template_landscape.spd");

        loadTask = new LoadFileFromAssetsAsyncTask(this);
        loadTask.execute(fileArrayList);
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

        // Initialize Eraser settings
        SpenSettingEraserInfo eraserInfo = new SpenSettingEraserInfo();
        eraserInfo.size = 30;
        mSpenSurfaceView.setEraserSettingInfo(eraserInfo);
        mEraserSettingView.setInfo(eraserInfo);
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
                        SpenObjectImage imgObj = new SpenObjectImage();
                        Bitmap imageBitmap;
                        // Set a bitmap file to ObjectImage.
                        // If there is a file attached, set it to ObjectImage.
                        if (mSpenNoteDoc.hasAttachedFile(ATTACH_IMAGE_KEY)) {
                            imageBitmap = BitmapFactory.decodeFile(mSpenNoteDoc.getAttachedFile(ATTACH_IMAGE_KEY));
                            // If there is no file attached, set the launcher icon to ObjectImage.
                        } else {
                            imageBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
                        }
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
                        float[] pressures = new float[pointSize];
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

    private final OnClickListener mPenBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mSpenSurfaceView.closeControl();

            // When Spen is in stroke (pen) mode
            if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_STROKE) {
                // If PenSettingView is open, close it.
                if (mPenSettingView.isShown()) {
                    mPenSettingView.setVisibility(View.GONE);
                    // If PenSettingView is not open, close it.
                } else {
                    mPenSettingView.setViewMode(SpenSettingPenLayout.VIEW_MODE_EXTENSION);
                    mPenSettingView.setVisibility(View.VISIBLE);
                }
                // If Spen is not in stroke (pen) mode, change it to stroke mode.
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

    private final OnClickListener mEraserBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // When Spen is in eraser mode
            if (mSpenSurfaceView.getToolTypeAction(mToolType) == SpenSurfaceView.ACTION_ERASER) {
                // If EraserSettingView is open, close it.
                if (mEraserSettingView.isShown()) {
                    mEraserSettingView.setVisibility(View.GONE);
                    // If EraserSettingView is not open, open it.
                } else {
                    mEraserSettingView.setViewMode(SpenSettingEraserLayout.VIEW_MODE_NORMAL);
                    mEraserSettingView.setVisibility(View.VISIBLE);
                }
                // If Spen is not in eraser mode, change it to eraser mode.
            } else {
                mMode = MODE_PEN;
                selectButton(mEraserBtn);
                mSpenSurfaceView.setToolTypeAction(mToolType, SpenSurfaceView.ACTION_ERASER);
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

    private final OnClickListener mMenuBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (MENU_VISIBLE == View.GONE) {
                MENU_VISIBLE = View.VISIBLE;
            } else {
                MENU_VISIBLE = View.GONE;
            }
            mMenuLayout.setVisibility(MENU_VISIBLE);

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

                // Set save directory for the file.
                String saveFilePath = mFilePath.getPath() + '/';
                String fileName = inputPath.getText().toString();
                if (!fileName.equals("")) {
                    saveFilePath += fileName;
                    int checkedRadioButtonId = selectFileExt.getCheckedRadioButtonId();
                    if (checkedRadioButtonId == R.id.radioNote) {
                        saveFilePath += ".spd";
                        saveNoteFile(saveFilePath);
                    } else if (checkedRadioButtonId == R.id.radioImage) {
                        saveFilePath += ".png";
                        captureSpenView(saveFilePath);
                    } else {
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

    private boolean saveNoteFile(String strFileName) {
        try {
            // Save NoteDoc
            // mSpenPageDoc.setTemplateUri(uri);
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

    private void captureSpenView(String strFileName) {

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
            // TODO Auto-generated method stub
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
            boolean checkSetPageDoc = false;
            if (pageCount > 1) {
                // Flick left and turn to the previous page.
                if (direction == DIRECTION_LEFT) {
                    mSpenPageDoc = mSpenNoteDoc.getPage((pageIndex + pageCount - 1) % pageCount);
                    if (mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_LEFT,
                            SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0) == true) {
                        checkSetPageDoc = true;
                    } else {
                        checkSetPageDoc = false;
                        mSpenPageDoc = mSpenNoteDoc.getPage(pageIndex);
                    }

                    // Flick right and turn to the next page.
                } else if (direction == DIRECTION_RIGHT) {
                    mSpenPageDoc = mSpenNoteDoc.getPage((pageIndex + 1) % pageCount);
                    if (mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                            SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0) == true) {
                        checkSetPageDoc = true;
                    } else {
                        checkSetPageDoc = false;
                        mSpenPageDoc = mSpenNoteDoc.getPage(pageIndex);
                    }
                }
                if (checkSetPageDoc == true) {
                    mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));
                }
                return true;
            }
            return false;
        }
    };

    private final EventListener mEraserListener = new EventListener() {
        @Override
        public void onClearAll() {
            // ClearAll button action routines of EraserSettingView
            try {
                mSpenNoteDoc.revertToTemplatePage(mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SpenUnsupportedTypeException e) {
                mSpenPageDoc.removeAllObject();
                e.printStackTrace();
            }
            mSpenSurfaceView.update();
        }
    };

    private void selectButton(View v) {
        // Enable or disable the button according to the current mode.
        mPenBtn.setSelected(false);
        mEraserBtn.setSelected(false);
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
        mEraserSettingView.setVisibility(SpenSurfaceView.GONE);
    }

    private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int which, long milisec) {
            if (mSpenNoteDoc.getWidth() <= mSpenPageDoc.getHeight()) {
                templatePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/SPen/Template/template_portrait.spd";
                templateURI = "template_portrait.spd";
            } else {
                templatePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/SPen/Template/template_landscape.spd";
                templateURI = "template_landscape.spd";
            }

            String indexStr = "";
            switch (which) {
            case SPEN_ADD_PAGE:
                mSpenSurfaceView.closeControl();
                closeSettingView();
                // Create a page next to the current page.
                mSpenPageDoc = mSpenNoteDoc.insertPage(mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()) + 1);
                mSpenPageDoc.setBackgroundColor(0xFFD6E6F5);
                mSpenPageDoc.clearHistory();
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                        SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);

                mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));
                break;

            case SPEN_INSERT_PAGES:
                // Append 3 template pages to note from template page list of note
                if (mSpenNoteDoc.getTemplatePageCount() == 0) {
                    Toast.makeText(mContext, "Have not pageTemplate in note.\nPlease attach template note first.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mSpenSurfaceView.closeControl();
                    closeSettingView();
                    int in = mSpenNoteDoc.getPageCount();
                    mSpenNoteDoc.insertPages(mSpenNoteDoc.getTemplatePageName(0), in, 3);
                    mSpenPageDoc = mSpenNoteDoc.getPage(in + 2);
                    mSpenPageDoc.clearHistory();
                    for (int i = 0; i < 2; i++) {
                        mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                                SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);
                    }
                    mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));
                }
                break;

            case SPEN_INSERT_TEMPLATE_PAGE:
                // Get first page from template.spd file. And set the page as templatePage and insert it at index 0 on
                // current note.
                mSpenSurfaceView.closeControl();
                closeSettingView();
                try {
                    mSpenPageDoc = mSpenNoteDoc.insertTemplatePage(0, templateURI);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SpenUnsupportedTypeException e) {
                    e.printStackTrace();
                }

                mSpenPageDoc.clearHistory();
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                        SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);

                mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));
                break;

            case SPEN_ATTACH_TEMPLATE_PAGE:
                // Get the page from note in templatePath have index 0. Add it into template page list of current note.
                indexStr = mSpenNoteDoc.getTemplatePageCount() + "";
                try {
                    mSpenNoteDoc.attachTemplatePage("TestTemplate" + indexStr, templatePath, 0);
                    Toast.makeText(
                            mContext,
                            "Attach Template Page Done." + "\nTemplate Page Name: "
                                    + mSpenNoteDoc.getTemplatePageName(mSpenNoteDoc.getTemplatePageCount() - 1),
                            Toast.LENGTH_SHORT).show();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (SpenUnsupportedTypeException e1) {
                    e1.printStackTrace();
                }
                if (mSpenNoteDoc.getTemplatePageCount() > 0) {
                    Toast.makeText(mContext, "Template Page Count: " + mSpenNoteDoc.getTemplatePageCount(),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case SPEN_COPY_PASTE_PAGE:
                // Copy current page and append it at last of note.
                mSpenSurfaceView.closeControl();
                closeSettingView();
                try {
                    if (mSpenPageDoc.isChanged()) {
                        mSpenPageDoc.save();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mSpenPageDoc = mSpenNoteDoc.copyPage(mSpenPageDoc, mSpenNoteDoc.getPageCount());
                mSpenPageDoc.clearHistory();
                mSpenSurfaceView.setPageDoc(mSpenPageDoc, SpenSurfaceView.PAGE_TRANSITION_EFFECT_RIGHT,
                        SpenSurfaceView.PAGE_TRANSITION_EFFECT_TYPE_SHADOW, 0);

                mTxtView.setText("Page" + mSpenNoteDoc.getPageIndexById(mSpenPageDoc.getId()));
                break;

            default:
                break;
            }
            mMenuLayout.setVisibility(View.GONE);
            MENU_VISIBLE = View.GONE;
        }
    };

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

        if (mEraserSettingView != null) {
            mEraserSettingView.close();
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

    public class LoadFileFromAssetsAsyncTask extends AsyncTask<ArrayList<String>, Void, Void> implements
            OnDismissListener {

        private static final String TAG = "LoadFileFromAssetsAsyncTask";

        private ProgressDialog loadingDialog;
        private final Context mcontext;

        public LoadFileFromAssetsAsyncTask(Context context) {
            mcontext = context;

            if (context != null) {
                loadingDialog = new ProgressDialog(context);
                loadingDialog.setOnDismissListener(this);
            } else {
                loadingDialog = null;
            }
        }

        @Override
        protected void onPreExecute() {
            if (loadingDialog != null) {
                loadingDialog.setMessage("Loading...");
            }

            loadingDialog.show();
        }

        @Override
        protected Void doInBackground(ArrayList<String>... arg0) {
            Log.e(TAG, "doInBackground");

            for (int i = 0; i < arg0[0].size(); i++) {
                copyFromAsset(mcontext, arg0[0].get(i));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            try {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    this.loadingDialog.dismiss();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            cancel(true);
        }

        public void copyFromAsset(Context context, String fileName) {
            if (context == null || fileName == null || fileName.length() == 0) {
                return;
            }

            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SPen/Template/";
            File spenFolder = new File(filePath);

            if (spenFolder.exists() == false) {
                spenFolder.mkdir();
            }

            InputStream is = null;
            FileOutputStream fos = null;

            try {
                is = context.getAssets().open(fileName);
                fos = new FileOutputStream(new File(filePath + fileName));

                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                Log.e(TAG, "getAsset().open exception: " + e.getMessage());
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "file close exception: " + e.getMessage());
                }
            }
        }
    }

}