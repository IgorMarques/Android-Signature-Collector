package com.samsung.android.sdk.pen.pg.example5_7;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.pg.tool.SDKUtils;
import com.samsung.android.sdk.pen.recognition.SpenCreationFailureException;
import com.samsung.android.sdk.pen.recognition.SpenSignatureVerification;
import com.samsung.android.sdk.pen.recognition.SpenSignatureVerificationInfo;
import com.samsung.android.sdk.pen.recognition.SpenSignatureVerificationManager;
import com.samsung.spensdk3.example.R;

public class PenSample5_7_Signature extends Activity {

    private Context mContext = null;

    public ListView mSignatureList;
    public ArrayList<ListItem> mSignatureListItem;

    private SpenSignatureVerificationManager mSpenSignatureVerificationManager;
    private SpenSignatureVerification mSpenSignatureVerification;
    public ListAdapter mSignatureAdapter;
    
    private final int LIST_CHECK_SIGNATURE = 0;
    private final int LIST_REGISRTATION = 1;
    private final int LIST_VERIFICATION = 2;
    private final int LIST_DELETE_SIGNATURE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_signature_signature);

		// Initialize Spen
        Spen spenPackage = new Spen();
        try {
            spenPackage.initialize(this);
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

		// Set the List
        mSignatureListItem = new ArrayList<ListItem>();
        mSignatureListItem.add(new ListItem("[Check signature]",
            "Check whether the registered signature exists or not"));
        mSignatureListItem.add(new ListItem("[Registration]",
            "Start registration"));
        mSignatureListItem.add(new ListItem("[Verification]",
            "Start verification - Signature must be registered"));
        mSignatureListItem.add(new ListItem("[Delete signature]",
            "Delete the registered signature"));

        mSignatureAdapter = new ListAdapter(this);

        mSignatureList = (ListView) findViewById(R.id.signature_list);
        mSignatureList.setAdapter(mSignatureAdapter);

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
			// Exit the application if the device does not support Verification feature.
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
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
            ad = null;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "SpenSignatureVerificationManager engine not loaded.",
                Toast.LENGTH_SHORT).show();
            return;
        }

        mSignatureList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
                int registeredCount = mSpenSignatureVerification.getRegisteredCount();
                int minimumRequiredCount = mSpenSignatureVerification.getMinimumRequiredCount();
                if (position == LIST_CHECK_SIGNATURE) {
					// Check if there is a signature registered.
                    if (registeredCount == minimumRequiredCount)
                        Toast.makeText(mContext, "Registered signature is existed.",
                            Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(mContext, "Registered Signature is less than minimum required count.",
                            Toast.LENGTH_SHORT).show();
                } else if (position == LIST_REGISRTATION) {
					// Go to Signature Registration.
                    Intent intent = new Intent(PenSample5_7_Signature.this,
                        PenSample5_7_SignatureRegistration.class);
                    startActivity(intent); // create RegistrationActivity
                } else if (position == LIST_VERIFICATION) {
					// If there is a registered signature, go to Signature Verification.
                    if (registeredCount == minimumRequiredCount) {
                        Intent intent = new Intent(PenSample5_7_Signature.this,
                            PenSample5_7_SignatureVerification.class);
                        startActivity(intent);
                    } else
                        Toast.makeText(mContext, "Registered Signature is less than minimum required count.",
                            Toast.LENGTH_SHORT).show();
                } else if (position == LIST_DELETE_SIGNATURE) {
					// Delete the registered signature.
                    if (registeredCount == 0) {
                        Toast.makeText(mContext, "Signature is not registered.",
                            Toast.LENGTH_SHORT).show();                    
                    } else {
                        mSpenSignatureVerification.unregisterAll();
                        if (mSpenSignatureVerification.getRegisteredCount() == 0)
                            Toast.makeText(mContext, "Registered signature is deleted.",
                                Toast.LENGTH_SHORT).show();                    
                    }
                }
                mSignatureAdapter.notifyDataSetChanged();
            }
        });
    }

    static class ListItem {
        ListItem(String iTitle, String isubTitle) {
            Title = iTitle;
            subTitle = isubTitle;
        }

        String Title;
        String subTitle;
    }

    class ListAdapter extends BaseAdapter {
        LayoutInflater Inflater;

        public ListAdapter(Context context) {
            Inflater =
                (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mSignatureListItem.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView,
            ViewGroup parent) {
            if (convertView == null) {
                convertView =
                    Inflater.inflate(R.layout.signature_list_item, parent,
                        false);
            }
            TextView title = (TextView) convertView
                    .findViewById(R.id.signature_list_title);
            title.setText(mSignatureListItem.get(position).Title);
            TextView subtitle = (TextView) convertView
                    .findViewById(R.id.signature_list_subtitle);
            subtitle.setText(mSignatureListItem.get(position).subTitle);
            if ((position == LIST_VERIFICATION || position == LIST_DELETE_SIGNATURE)
                && mSpenSignatureVerification.getRegisteredCount()!= mSpenSignatureVerification
                    .getMinimumRequiredCount()) {
                title.setTextColor(0xFF005D87);
                subtitle.setTextColor(0xFF777777);
            } else {
                title.setTextColor(0xFF00B8FF);
                subtitle.setTextColor(0xFFFFFFFF);
            }
            return convertView;
        }
    }

    @Override
    protected void onResume() {
        if(mSignatureAdapter != null) {
            mSignatureAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSpenSignatureVerification != null) {
            mSpenSignatureVerificationManager
                .destroySignatureVerification(mSpenSignatureVerification);
            mSpenSignatureVerificationManager.close();
        }
    }
}