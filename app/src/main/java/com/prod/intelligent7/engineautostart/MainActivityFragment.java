package com.prod.intelligent7.engineautostart;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    static LinearLayout.LayoutParams llParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
    static LinearLayout.LayoutParams llWrapParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    static LinearLayout.LayoutParams llWWParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    static TableLayout.LayoutParams tbParams=new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);

    MainActivity myActivity;

    public MainActivityFragment() {
        isLandScape=false;
    }

    final static int greyOutTextColor=0x5fffffff;
    final static int normalTextColor=Color.BLACK;
    int control_width, control_height;

    Button buildOpenLogButton()
    {
        Button retB=new Button(getActivity());
        LinearLayout.LayoutParams aParams=new LinearLayout.LayoutParams(mDisplayWidth/2, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        aParams.setMargins(2, 2, 2, 2);
        retB.setLayoutParams(aParams);
        retB.setGravity(Gravity.LEFT);
        retB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.open_log, 0, 0, 0);
        retB.setText(getResources().getString(R.string.open_log));
        int textSize=mLogBarHeight/3;
        if (textSize < 16) textSize=16;
        retB.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        retB.setTextColor(Color.BLACK);
        retB.setBackgroundColor(Color.YELLOW);//Color.GRAY);
        retB.setOnClickListener(new ButtonClickListener(OPEN_LOG));
        return retB;
    }
    Button buildClearLogButton()
    {
        Button retB=new Button(getActivity());
        LinearLayout.LayoutParams aParams=new LinearLayout.LayoutParams(mDisplayWidth/2, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        aParams.setMargins(2, 2, 2, 2);
        retB.setLayoutParams(aParams);
        retB.setGravity(Gravity.RIGHT);
        retB.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clean_log, 0);
        retB.setText(getResources().getString(R.string.clear_log));
        int textSize=mLogBarHeight/3;
        if (textSize < 16) textSize=16;
        retB.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        retB.setTextColor(Color.BLACK);
        retB.setBackgroundColor(Color.YELLOW);
        retB.setOnClickListener(new ButtonClickListener(CLEAN_LOG));
        return retB;
    }

    LinearLayout buildLogBar()
    {
        LinearLayout onePair=new LinearLayout(getActivity());
        onePair.setLayoutParams(llWrapParams);
        onePair.setOrientation(LinearLayout.HORIZONTAL);//0HORIZONTAL, 1Vertical);
        //id.setGravity(1);
        onePair.addView(buildOpenLogButton());
        onePair.addView(buildClearLogButton());
        return onePair;
    }

    void constructButton(Button retB, String text)
    {
        LinearLayout.LayoutParams aParams=new LinearLayout.LayoutParams(control_width, control_height, 0.5f);
        aParams.setMargins(2, 3, 2, 3);
        retB.setLayoutParams(aParams);
        retB.setGravity(Gravity.CENTER);
        retB.setText(text);
        int textSize=control_height / 8;
        if (textSize < 28) textSize=28;
        retB.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        //retB.setTextColor(Color.RED);
        //retB.setBackgroundColor(0x847fffb4);
    }
    Button buildSIMButton()
    {
      Button retB=new Button(getActivity());
        String savedSim=((MainActivity)myActivity).getSavedValue(MainActivity.SET_SIM);
        //if (savedSim.charAt(0)=='-')
            constructButton(retB, getResources().getString(R.string.sim_setting));
        //else
            //constructButton(retB, getResources().getString(R.string.sim_change));
        ButtonClickListener ll= new ButtonClickListener(SET_SIM);
        retB.setOnClickListener(ll);
        uiButtons.put(SET_SIM, retB);
        uiButtonListeners.put(SET_SIM, ll);
        return retB;
    }

    Button buildPINButton()
    {
        Button retB=new Button(getActivity());
        String savedPin=((MainActivity)myActivity).getSavedValue(MainActivity.SET_PIN);
        //if (savedPin.charAt(0)=='-')
            constructButton(retB, getResources().getString(R.string.pin_setting));
        //else
            //constructButton(retB, getResources().getString(R.string.pin_change));
        ButtonClickListener ll= new ButtonClickListener(SET_PIN);
        //retB.setOnClickListener(ll);
        uiButtons.put(SET_PIN, retB);
        uiButtonListeners.put(SET_PIN, ll);
        if (imNewUser) retB.setAlpha(0.2f);
        else
            retB.setOnClickListener(ll);

        return retB;
    }

    Button buildPhonesButton()
    {
        Button retB=new Button(getActivity());
        String savedPin=((MainActivity)myActivity).getSavedValue(MainActivity.SET_PHONE1);
        //if (savedPin.charAt(0)=='-')
            constructButton(retB, getResources().getString(R.string.phone_numbers_setting));
       // else
           // constructButton(retB, getResources().getString(R.string.phone_numbers_change));

        ButtonClickListener ll= new ButtonClickListener(SET_PHONES);
        retB.setOnClickListener(ll);
        uiButtons.put(SET_PHONES, retB);
        uiButtonListeners.put(SET_PHONES, ll);

        return retB;
    }
    Button buildWarmerCoolerButton()
    {
        Button retB=new Button(getActivity());

            constructButton(retB, getResources().getString(R.string.warming_cooling_setting));
        ButtonClickListener ll= new ButtonClickListener(SET_WARMER);
        //retB.setOnClickListener(ll);
        uiButtons.put(SET_WARMER, retB);
        uiButtonListeners.put(SET_WARMER, ll);

        if (imNewUser) retB.setAlpha(0.2f);
        else
        retB.setOnClickListener(ll);
        return retB;
    }

    Button buildDailyOneStartButton()
    {
        Button retB=new Button(getActivity());
        constructButton(retB, getResources().getString(R.string.daily_auto_start_setting));
        ButtonClickListener ll= new ButtonClickListener(SET_ONE_BOOT);
        //retB.setOnClickListener(ll);
        uiButtons.put(SET_ONE_BOOT, retB);
        uiButtonListeners.put(SET_ONE_BOOT, ll);
        if (imNewUser) retB.setAlpha(0.2f);
        else
            retB.setOnClickListener(ll);

        return retB;
    }

    Button buildDailyMultipleButton()
    {
        Button retB=new Button(getActivity());
        constructButton(retB, getResources().getString(R.string.daily_multiple_start_setting));
        ButtonClickListener ll= new ButtonClickListener(SET_MULTIPLE_BOOT);
        //retB.setOnClickListener(ll);
        uiButtons.put(SET_MULTIPLE_BOOT, retB);
        uiButtonListeners.put(SET_MULTIPLE_BOOT, ll);
        if (imNewUser) retB.setAlpha(0.2f);
        else
           retB.setOnClickListener(ll);

        return retB;
    }
    Button buildStartNowButton()
    {
        Button retB=new Button(getActivity());
        constructButton(retB, getResources().getString(R.string.start_engine));
        ButtonClickListener ll= new ButtonClickListener(CMD_START_NOW);
        //retB.setOnClickListener(ll);
        uiButtons.put(CMD_START_NOW, retB);
        uiButtonListeners.put(CMD_START_NOW, ll);
        if (imNewUser) retB.setAlpha(0.2f);
        else
            retB.setOnClickListener(ll);

        return retB;
    }
    Button buildStopNowButton()
    {
        Button retB=new Button(getActivity());
        constructButton(retB, getResources().getString(R.string.shut_down_engine));
        ButtonClickListener ll= new ButtonClickListener(CMD_STOP_NOW);
        //retB.setOnClickListener(ll);
        uiButtons.put(CMD_STOP_NOW, retB);
        uiButtonListeners.put(CMD_STOP_NOW, ll);
        if (imNewUser) retB.setAlpha(0.2f);
        else
           retB.setOnClickListener(ll);

        return retB;
    }

    LinearLayout buildControlButtons1Pair(int whichPair, boolean vertically)
    {
        LinearLayout onePair=new LinearLayout(getActivity());
        LinearLayout.LayoutParams aParams=null;
        if (vertically) {
            aParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 0.5f);
            onePair.setOrientation(LinearLayout.VERTICAL);//0HORIZONTAL, 1Vertical);
        } else
        {
            aParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
          onePair.setOrientation(LinearLayout.HORIZONTAL);//0HORIZONTAL, 1Vertical);
        }
        //aParams.setMargins(2, 0, 2, -1);
        onePair.setLayoutParams(aParams);
        onePair.setGravity(Gravity.CENTER);

        //id.setGravity(1);
        switch (whichPair)
        {
            case 0:
                onePair.addView(buildSIMButton());
                onePair.addView(buildPhonesButton());
                break;
            case 1:
                onePair.addView(buildPINButton());
                onePair.addView(buildWarmerCoolerButton());
                break;
            case 2:
                onePair.addView(buildDailyOneStartButton());
                onePair.addView(buildDailyMultipleButton());
                break;
            case 3:
                onePair.addView(buildStopNowButton());
                onePair.addView(buildStartNowButton());
                break;
        }
        return onePair;
    }

    LinearLayout buildControlButtons()
    {
        LinearLayout onePair=new LinearLayout(getActivity());
        onePair.setLayoutParams(llWrapParams);
        boolean vertically = !isLandScape;
        if (vertically) {
            onePair.setOrientation(LinearLayout.VERTICAL);//0HORIZONTAL, 1Vertical);
            LinearLayout aBlank=new LinearLayout(getActivity());
            aBlank.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 5));
            onePair.addView(aBlank);
        } else
            onePair.setOrientation(LinearLayout.HORIZONTAL);//0HORIZONTAL, 1Vertical);

        for (int i=0; i<4; i++)
            onePair.addView(buildControlButtons1Pair(i, !vertically));

        onePair.setTag("BUTTONS");
        return onePair;

    }

    static HashMap<String,  Button> uiButtons=new HashMap<String,  Button>();
    static HashMap<String,  ButtonClickListener> uiButtonListeners=new HashMap<String,  ButtonClickListener>();

    @Override
    public void onStop(){
        super.onStop();
        uiButtonOff();
    }

    @Override
    public void onResume(){
        super.onResume();
       uiButtonOn();
    }

    public void uiButtonOn()
    {
        Iterator<String> itr=uiButtons.keySet().iterator();
        while (itr.hasNext()) {
            String tag=itr.next();
            if (!imNewUser || tag.equalsIgnoreCase(SET_SIM) || tag.equalsIgnoreCase(SET_PHONES) )
            uiButtons.get(tag).setOnClickListener(uiButtonListeners.get(tag));
        }
    }

    public void uiButtonOff()
    {
        Iterator<String> itr=uiButtons.keySet().iterator();
        while (itr.hasNext()) {
            uiButtons.get(itr.next()).setOnClickListener(null);
        }
    }
    boolean isLandScape;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mLogBarHeight;
    void getMyScreenSize()
    {
        //Display display = getWindowManager().getDefaultDisplay();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDisplayWidth = size.x;
        mDisplayHeight = size.y;
        isLandScape=(mDisplayWidth>mDisplayHeight);
        //If you're not in an Activity you can get the default Display via WINDOW_SERVICE:
        TypedValue tv = new TypedValue();
        int mActionBarHeight=0;
        if (getActivity().getTheme().resolveAttribute(
                android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, getActivity().getResources().getDisplayMetrics());
        }
        mDisplayHeight -= mActionBarHeight ;
        mLogBarHeight = mActionBarHeight*2/3;
        if (mLogBarHeight < 10) mLogBarHeight=10;
        mLogBarHeight=0; //now use menu item
        mDisplayHeight -= mLogBarHeight;
        int nC=2, nR=4; //# of col. row
        if (isLandScape) {nC=4; nR=2;}
        mDisplayHeight -= (8*nR);
        mDisplayWidth -= 4*nC;
        control_height=mDisplayHeight/nR;
        control_width=(mDisplayWidth-5)/nC;
    }

    public LinearLayout repaintButtons()
    {
        getMyScreenSize();
        myActivity=(MainActivity)getActivity();
        imNewUser=(myActivity.getSavedValue(MainActivity.SET_PHONE1).charAt(0)=='-' ||
                myActivity.getSavedValue(MainActivity.SET_SIM).charAt(0)=='-'  );
        return buildControlButtons();
    }

    public View paintView(){

        // ScrollView sv=new ScrollView(getActivity());
        LinearLayout retV=new LinearLayout(getActivity());
        retV.setOrientation(LinearLayout.VERTICAL);//0HORIZONTAL, 1Vertical);
        //id.setGravity(1);//center_horizontal
        retV.setLayoutParams(llWrapParams);
        //retV.addView(buildLogBar()); //now in menu item
        retV.addView(buildControlButtons());

        return retV;
    }
boolean imNewUser;

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getMyScreenSize();
        myActivity=(MainActivity)getActivity();
        imNewUser=false;
        if (myActivity.getSavedValue(MainActivity.SET_PHONE1).charAt(0)=='-' ||
                myActivity.getSavedValue(MainActivity.SET_SIM).charAt(0)=='-'  )
            imNewUser=true;
    }

    View mRootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        mRootView = paintView();
        return mRootView;
       // return inflater.inflate(R.layout.fragment_main, container, false);
    }

    void greyOutButtons()
    {

    }
    @Override
    public void onStart()
    {
        super.onStart();

        if (!imNewUser) return;
        if (myActivity.getSavedValue(MainActivity.SET_SIM).charAt(0)=='-')
        {
            new MyToast((Context)myActivity, getResources().getString(R.string.welcome)).showOK();
            imNewUser = true;
        }
        else
        {
            String pending2 = myActivity.getSavedValue(myActivity.PENDING_NEW_PHONE2);
            String pending1 = myActivity.getSavedValue(myActivity.PENDING_NEW_PHONE1);
            if (pending1.charAt(0) == '-') {
                imNewUser = true;
                new MyToast((Context)myActivity, getResources().getString(R.string.welcome)).showOK();
                //setPhones();
            } else {
                myActivity.setPreferenceValue(MainActivity.SET_PHONE1, pending1);
                myActivity.setPreferenceValue(MainActivity.SET_PHONE2, pending2);

                String savedPin=myActivity.getSavedValue(MainActivity.SET_PIN);

                String newPhone1=myActivity.getSavedValue(MainActivity.SET_PHONE1);
                String newPhone2=myActivity.getSavedValue(MainActivity.SET_PHONE2);
                if (savedPin.charAt(0) == '-') savedPin="0000";
                imNewUser = false;
                //paintView();

                String cmd="M3-"+savedPin+"-"+newPhone1+"-"+newPhone2;
                Intent jIntent2=new Intent(getActivity(), ConnectDaemonService.class);
                //M1-00 (cool) or M1-01 (warm)
                jIntent2.putExtra(ConnectDaemonService.DAEMON_COMMAND, cmd);
                //Toast.makeText(this, "will send start command to server", Toast.LENGTH_LONG).show();
                getActivity().startService(jIntent2);

                ((LinearLayout)mRootView).removeAllViews();
                getMyScreenSize();
                ((LinearLayout) mRootView).addView(buildControlButtons());
            }
        }
    }
    String mCommand;
    public static final String OPEN_LOG="open_log";
    public static final String CLEAN_LOG="clean_logr";
    public static final String SET_SIM="set_sim_number";
    public static final String SET_PIN="set_pin_code";
    public static final String SET_PHONES="set_phone_numbers";
    public static final String SET_WARMER="select_warmer_cooler";
    public static final String SET_ONE_BOOT="set_one_boot";
    public static final String SET_MULTIPLE_BOOT="set_multiple_boot";
    public static final String CMD_START_NOW="cmd_start_now";
    public static final String CMD_STOP_NOW="set_stop_now";

    class ButtonClickListener implements Button.OnClickListener
    {
        String toDo;
        public ButtonClickListener(String forCommand)
        {
            toDo=forCommand;
        }
        public void onClick(View v)
        {
            ((MainActivity )getActivity()).executeCommand(toDo);
        }
    }
}
