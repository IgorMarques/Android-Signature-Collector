package com.samsung.android.sdk.pen.pg.example5_7;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.samsung.android.sdk.pen.recognition.SpenCreationFailureException;
import com.samsung.android.sdk.pen.recognition.SpenSignatureVerification;
import com.samsung.android.sdk.pen.recognition.SpenSignatureVerification.ResultListener;
import com.samsung.android.sdk.pen.recognition.SpenSignatureVerificationInfo;
import com.samsung.android.sdk.pen.recognition.SpenSignatureVerificationManager;
import com.samsung.spensdk3.example.R;

public class PenSample5_7_SignatureVerification extends Activity {

    public ArrayList<ListItem> mSignatureListItem;

    private Context mContext = null;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    public SpenSurfaceView mSpenSurfaceView;

    public ListAdapter mSignatureAdapter;
    public ListView mSignatureList;

    int mVerificationLevel =
        SpenSignatureVerification.VERIFICATION_LEVEL_MEDIUM;

    private SpenSignatureVerificationManager mSpenSignatureVerificationManager;
    private SpenSignatureVerification mSpenSignatureVerification;

    private final int LIST_VERIFICATION = 0;
    private final int LIST_VERIFICATION_LEVEL = 1;
    private final int LIST_RETRY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature_verification);
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

		// Create SpenView
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
		// Set PageDoc to View
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);

        Toast.makeText(mContext, "Draw your signature to verify.",
            Toast.LENGTH_SHORT).show();

		// Set the List
        mSignatureListItem = new ArrayList<ListItem>();
        mSignatureListItem.add(new ListItem("[Verification]",
            "Verify the signature"));
        mSignatureListItem.add(new ListItem("[Verification Level]",
            "Select verification level"));
        mSignatureListItem.add(new ListItem("[Retry]",
            "Clear the screen to redraw signature"));

        mSignatureAdapter = new ListAdapter(this);

        mSignatureList = (ListView) findViewById(R.id.signature_list);
        mSignatureList.setAdapter(mSignatureAdapter);

        if(isSpenFeatureEnabled == false) {
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext,
                "Device does not support Spen. \n You can draw stroke by finger",
                Toast.LENGTH_SHORT).show();
        }

		// Settings for Verification
        mSpenSignatureVerificationManager =
            new SpenSignatureVerificationManager(mContext);

        List<SpenSignatureVerificationInfo> signatureVerificationList =
            mSpenSignatureVerificationManager.getInfoList();
        try {
            if (signatureVerificationList.size() > 0) {
                for (SpenSignatureVerificationInfo info : signatureVerificationList) {
                    if (info.name.equalsIgnoreCase("SpenSignature")) {
                        mSpenSignatureVerification = mSpenSignatureVerificationManager
                            .createSignatureVerification(info);
                        break;
                    }
                }
            } else {
                finish();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "SpenSignatureVerificationManager class not found.",
                Toast.LENGTH_SHORT).show();
            return;
        } catch (InstantiationException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Failed to access the SpenSignatureVerificationManager constructor.",
                Toast.LENGTH_SHORT).show();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Failed to access the SpenSignatureVerificationManager field or method.",
                Toast.LENGTH_SHORT).show();
            return;
        } catch (SpenCreationFailureException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "This device does not support Recognition.",
                Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "mSpenSignatureVerificationManager engine not loaded.",
                Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mSpenSignatureVerification
                .setResultListener(new ResultListener() {
                    @Override
                    public void onResult(List<SpenObjectStroke> input,
                        boolean result) {
						// Check if the signature verification was successful.
                        if (result) {
                            Toast.makeText(mContext, "Success!",
                                Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "Failure!",
                                Toast.LENGTH_SHORT).show();
                        }
                        mSpenPageDoc.removeAllObject();
                    mSpenSurfaceView.update();
                    }
                });
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "SpenSignatureVerification is not loaded.",
                Toast.LENGTH_SHORT).show();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "SpenSignatureVerification is not loaded.",
                Toast.LENGTH_SHORT).show();
            return;
        }

        mSignatureList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
                if (position == LIST_VERIFICATION) {
                    ArrayList<SpenObjectBase> strokeList =
                        mSpenPageDoc.getObjectList(SpenObjectBase.TYPE_STROKE);
                    if (strokeList.size() > 0) {
						// List the objects on the viewport.
                        ArrayList<SpenObjectStroke> list =
                            new ArrayList<SpenObjectStroke>();
                        for (int i = 0; i < strokeList.size(); i++) {
                            list.add((SpenObjectStroke) strokeList.get(i));
                        }

						// Send a request to verify the signature against the registered ones.
                        try {
                            mSpenSignatureVerification.request(list);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "SpenSignatureVerification is not loaded.",
                                Toast.LENGTH_SHORT).show();
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "SpenSignatureVerification is not loaded.",
                                Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                } else if (position == LIST_VERIFICATION_LEVEL) {

					// Set the signature verification level.
                    AlertDialog.Builder ab = new AlertDialog.Builder(
                        PenSample5_7_SignatureVerification.this);

                    mVerificationLevel =
                        mSpenSignatureVerification.getVerificationLevel();

                    String[] strLevel = { "Low", "Medium", "High" };

                    ab.setTitle("Select verification level")
                        .setSingleChoiceItems(strLevel, mVerificationLevel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                    DialogInterface dialog, int which) {
                                    mVerificationLevel = which;
                                }
                            })
                        .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void
                                    onClick(DialogInterface dialog,
                                        int whichButton) {
                                    mSpenSignatureVerification
                                        .setVerificationLevel(mVerificationLevel);
                                    mSignatureAdapter
                                        .notifyDataSetChanged();
                                }
                            }).setNegativeButton("Cancel", null).show();
                } else if (position == LIST_RETRY) {
					// Purge the objects to start over.
                    mSpenPageDoc.removeAllObject();
                    mSpenSurfaceView.update();
                    Toast.makeText(mContext, "Draw your signature to verify.",
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // Items for ListView
    static class ListItem {
        ListItem(String iTitle, String isubTitle) {
            Title = iTitle;
            subTitle = isubTitle;
        }

        String Title;
        String subTitle;
    }

    // Adapter class for list Item
    class ListAdapter extends BaseAdapter {
        LayoutInflater Inflater;

        public ListAdapter(Context context) {
            Inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mSignatureListItem.size();
        }

        @Override
        public Object getItem(int position) {
            return mSignatureListItem.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView,
            ViewGroup parent) {
            if (convertView == null) {
                convertView = Inflater.inflate(
                    R.layout.signature_list_item, parent, false);
            }

            if (position == LIST_VERIFICATION) {
                TextView title = (TextView) convertView
                    .findViewById(R.id.signature_list_title);

                if (mVerificationLevel
                    == SpenSignatureVerification.VERIFICATION_LEVEL_LOW) {
                    title.setText(mSignatureListItem.get(position).Title
                        + "  ( Level = Low )");
                } else if (mVerificationLevel
                    == SpenSignatureVerification.VERIFICATION_LEVEL_MEDIUM) {
                    title.setText(mSignatureListItem.get(position).Title
                        + "  ( Level = Medium )");
                } else if (mVerificationLevel
                    == SpenSignatureVerification.VERIFICATION_LEVEL_HIGH) {
                    title.setText(mSignatureListItem.get(position).Title
                        + "  ( Level = High )");
                }

                TextView subtitle = (TextView) convertView
                        .findViewById(R.id.signature_list_subtitle);
                subtitle
                    .setText(mSignatureListItem.get(position).subTitle);
            } else {
                TextView title = (TextView) convertView
                        .findViewById(R.id.signature_list_title);
                title.setText(mSignatureListItem.get(position).Title);
                TextView subtitle = (TextView) convertView
                        .findViewById(R.id.signature_list_subtitle);
                subtitle
                    .setText(mSignatureListItem.get(position).subTitle);
            }
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpenSignatureVerification != null) {
            mSpenSignatureVerificationManager
                .destroySignatureVerification(mSpenSignatureVerification);
            mSpenSignatureVerificationManager.close();
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