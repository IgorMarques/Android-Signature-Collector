package com.samsung.spensdk3.example;

import android.app.Activity;
import android.content.Context;
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

import com.samsung.android.sdk.pen.pg.example1_2.PenSample1_2_PenSetting;
import com.samsung.android.sdk.pen.pg.example1_3.PenSample1_3_EraserSetting;
import com.samsung.android.sdk.pen.pg.example1_4.PenSample1_4_UndoRedo;
import com.samsung.android.sdk.pen.pg.example1_5.PenSample1_5_Background;
import com.samsung.android.sdk.pen.pg.example1_6.PenSample1_6_Replay;
import com.samsung.android.sdk.pen.pg.example1_7.PenSample1_7_Capture;
import com.samsung.android.sdk.pen.pg.example1_8.PenSample1_8_CustomDrawing;
import com.samsung.android.sdk.pen.pg.example1_9.PenSample1_9_NotePad;
import com.samsung.android.sdk.pen.pg.example2_1.PenSample2_1_ImageObject;
import com.samsung.android.sdk.pen.pg.example2_2.PenSample2_2_TextObject;
import com.samsung.android.sdk.pen.pg.example2_3.PenSample2_3_StrokeObject;
import com.samsung.android.sdk.pen.pg.example2_4.PenSample2_4_SaveFile;
import com.samsung.android.sdk.pen.pg.example2_5.PenSample2_5_LoadFile;
import com.samsung.android.sdk.pen.pg.example2_6.PenSample2_6_AttachFile;
import com.samsung.android.sdk.pen.pg.example2_7.PenSample2_7_AddPage;
import com.samsung.android.sdk.pen.pg.example3_1.PenSample3_1_SelectObject;
import com.samsung.android.sdk.pen.pg.example3_2.PenSample3_2_SelectionSetting;
import com.samsung.android.sdk.pen.pg.example3_3.PenSample3_3_Group;
import com.samsung.android.sdk.pen.pg.example3_4.PenSample3_4_MoveObject;
import com.samsung.android.sdk.pen.pg.example4_1.PenSample4_1_SOR;
import com.samsung.android.sdk.pen.pg.example4_2.PenSample4_2_SORList;
import com.samsung.android.sdk.pen.pg.example5_1.PenSample5_1_SmartScroll;
import com.samsung.android.sdk.pen.pg.example5_2.PenSample5_2_SmartZoom;
import com.samsung.android.sdk.pen.pg.example5_3.PenSample5_3_SimpleView;
import com.samsung.android.sdk.pen.pg.example5_4.PenSample5_4_TemporaryStroke;
import com.samsung.android.sdk.pen.pg.example5_5.PenSample5_5_OnlyPen;
import com.samsung.android.sdk.pen.pg.example5_6.PenSample5_6_TextRecognition;
import com.samsung.android.sdk.pen.pg.example5_7.PenSample5_7_Signature;
import com.samsung.android.sdk.pen.pg.example5_8.PenSample5_8_StrokeFrame;
import com.samsung.android.sdk.pen.pg.example5_9.PenSample5_9_ShapeRecognition;
import com.samsung.android.sdk.pen.pg.example5_10.PenSample5_10_EquationRecognition;
import com.samsung.android.sdk.pen.pg.example6_1.PenSample6_1_TiltOrientation;
import com.samsung.android.sdk.pen.pg.example6_2.PenSample6_2_TemplatePage;
import com.samsung.spensdk3.example.SpenCapture.SpenObjectStrokeCapture;

public class Spen_ProgramGuide extends Activity {

    private ListAdapter mListAdapter = null;
    private ListView mListView = null;

    // The item of list
//    private static final int SPEN_HELLOPEN = 0;
    private static final int MY_EXAMPLE = 0;
    private static final int SPEN_PENSETTING = 1;
    private static final int SPEN_ERASERSETTING = 2;
    private static final int SPEN_UNDOREDO = 3;
    private static final int SPEN_BACKGROUND = 4;
    private static final int SPEN_REPLAY = 5;
    private static final int SPEN_CAPTURE = 6;
    private static final int SPEN_CUSTOMDRAWING = 7;
    private static final int SPEN_NOTEPAD = 8;

    private static final int SPEN_IMAGEOBJECT = 9;
    private static final int SPEN_TEXTOBJECT = 10;
    private static final int SPEN_STROKEOBJECT = 11;
    private static final int SPEN_SAVEFILE = 12;
    private static final int SPEN_LOADFILE = 13;
    private static final int SPEN_ATTACHFILE = 14;
    private static final int SPEN_ADDPAGE = 15;

    private static final int SPEN_SELECTOBJECT = 16;
    private static final int SPEN_SELECTIONSETTING = 17;
    private static final int SPEN_GROUP = 18;
    private static final int SPEN_MOVEOBJECT = 19;

    private static final int SPEN_SOR = 20;
    private static final int SPEN_SORLIST = 21;

    private static final int SPEN_SMARTSCROLL = 22;
    private static final int SPEN_SMARTZOOM = 23;
    private static final int SPEN_SIMPLEVIEW = 24;
    private static final int SPEN_TEMPORARYSTROKE = 25;
    private static final int SPEN_ONLYPEN = 26;
    private static final int SPEN_TEXTRECOGNITION = 27;
    private static final int SPEN_SIGNATURERECOGNITION = 28;
    private static final int SPEN_STROKEFRAME = 29;
    private static final int SPEN_SHAPERECOGNITION = 30;
    private static final int SPEN_EQUATIONRECOGNITION = 31;

    private static final int SPEN_PEN_TILTS_ORIENTATIONS = 32;
    private static final int SPEN_TEMPLATEPAGE = 33;

    private static final int TOTAL_LIST_NUM = 34;

    private final String EXAMPLE_NAMES[] = {
            "MyExample",
            "Hello Pen",
            "Pen Setting",
            "Eraser Setting",
            "Undo & Redo",
            "Background",
            "Replay",
            "Capture",
            "Custom Drawing",
            "NotePad",

            "Image Object",
            "Text Object",
            "Stroke Object",
            "Save File",
            "Load File",
            "Attach File",
            "Add Page",

            "Select Object",
            "Selection Setting",
            "Group",
            "Move Object",

            "SOR",
            "SOR List",

            "Smart Scroll",
            "Smart Zoom",
            "Simple View",
            "Temporary Stroke",
            "Only Pen",
            "Text Recognition",
            "Signature Recognition",
            "Stroke Frame",
            "Shape Recognition",
            "Equation Recognition",
            "Pen Tilts and Orientations",
            "Template Page" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spensdk_demo);

        createUI();
    }

    private void createUI() {

        TextView textTitle = (TextView) findViewById(R.id.title);
        textTitle.setText("Spen Program Guide");

        mListAdapter = new ListAdapter(this);
        mListView = (ListView) findViewById(R.id.demo_list);
        mListView.setAdapter(mListAdapter);

        mListView.setItemsCanFocus(false);
        mListView.setTextFilterEnabled(true);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // S Pen SDK Demo programs
                if (position == MY_EXAMPLE) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, SpenObjectStrokeCapture.class);
                    startActivity(intent);
                } else if (position == SPEN_PENSETTING) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample1_2_PenSetting.class);
                    startActivity(intent);
                } else if (position == SPEN_ERASERSETTING) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample1_3_EraserSetting.class);
                    startActivity(intent);
                } else if (position == SPEN_UNDOREDO) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample1_4_UndoRedo.class);
                    startActivity(intent);
                } else if (position == SPEN_BACKGROUND) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample1_5_Background.class);
                    startActivity(intent);
                } else if (position == SPEN_REPLAY) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample1_6_Replay.class);
                    startActivity(intent);
                } else if (position == SPEN_CAPTURE) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample1_7_Capture.class);
                    startActivity(intent);
                } else if (position == SPEN_CUSTOMDRAWING) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample1_8_CustomDrawing.class);
                    startActivity(intent);
                } else if (position == SPEN_NOTEPAD) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample1_9_NotePad.class);
                    startActivity(intent);
                } else if (position == SPEN_IMAGEOBJECT) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample2_1_ImageObject.class);
                    startActivity(intent);
                } else if (position == SPEN_TEXTOBJECT) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample2_2_TextObject.class);
                    startActivity(intent);
                } else if (position == SPEN_STROKEOBJECT) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample2_3_StrokeObject.class);
                    startActivity(intent);
                } else if (position == SPEN_SAVEFILE) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample2_4_SaveFile.class);
                    startActivity(intent);
                } else if (position == SPEN_LOADFILE) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample2_5_LoadFile.class);
                    startActivity(intent);
                } else if (position == SPEN_ATTACHFILE) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample2_6_AttachFile.class);
                    startActivity(intent);
                } else if (position == SPEN_ADDPAGE) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample2_7_AddPage.class);
                    startActivity(intent);
                } else if (position == SPEN_SELECTOBJECT) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample3_1_SelectObject.class);
                    startActivity(intent);
                } else if (position == SPEN_SELECTIONSETTING) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample3_2_SelectionSetting.class);
                    startActivity(intent);
                } else if (position == SPEN_GROUP) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample3_3_Group.class);
                    startActivity(intent);
                } else if (position == SPEN_MOVEOBJECT) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample3_4_MoveObject.class);
                    startActivity(intent);
                } else if (position == SPEN_SOR) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample4_1_SOR.class);
                    startActivity(intent);
                } else if (position == SPEN_SORLIST) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample4_2_SORList.class);
                    startActivity(intent);
                } else if (position == SPEN_SMARTSCROLL) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_1_SmartScroll.class);
                    startActivity(intent);
                } else if (position == SPEN_SMARTZOOM) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_2_SmartZoom.class);
                    startActivity(intent);
                } else if (position == SPEN_SIMPLEVIEW) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_3_SimpleView.class);
                    startActivity(intent);
                } else if (position == SPEN_TEMPORARYSTROKE) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_4_TemporaryStroke.class);
                    startActivity(intent);
                } else if (position == SPEN_ONLYPEN) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_5_OnlyPen.class);
                    startActivity(intent);
                } else if (position == SPEN_TEXTRECOGNITION) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_6_TextRecognition.class);
                    startActivity(intent);
                } else if (position == SPEN_SIGNATURERECOGNITION) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_7_Signature.class);
                    startActivity(intent);
                } else if (position == SPEN_STROKEFRAME) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_8_StrokeFrame.class);
                    startActivity(intent);
                } else if (position == SPEN_SHAPERECOGNITION) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_9_ShapeRecognition.class);
                    startActivity(intent);
                } else if (position == SPEN_EQUATIONRECOGNITION) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample5_10_EquationRecognition.class);
                    startActivity(intent);
                } else if (position == SPEN_PEN_TILTS_ORIENTATIONS) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample6_1_TiltOrientation.class);
                    startActivity(intent);
                } else if (position == SPEN_TEMPLATEPAGE) {
                    Intent intent = new Intent(Spen_ProgramGuide.this, PenSample6_2_TemplatePage.class);
                    startActivity(intent);
                }
            }
        });
    }

    // =========================================
    // List Adapter : S Pen SDK Demo Programs
    // =========================================
    public class ListAdapter extends BaseAdapter {

        public ListAdapter(Context context) {
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.spensdk_demolist_item, parent, false);
            }
            // UI Item
            TextView tvListItemText = (TextView) convertView.findViewById(R.id.listitemText);
            tvListItemText.setTextColor(0xFFFFFFFF);

            // ==================================
            // basic data display
            // ==================================
            if (position < TOTAL_LIST_NUM) {
                tvListItemText.setText(EXAMPLE_NAMES[position]);
            }

            return convertView;
        }

        public void updateDisplay() {
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return TOTAL_LIST_NUM;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
