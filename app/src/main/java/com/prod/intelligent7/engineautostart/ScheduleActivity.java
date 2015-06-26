package com.prod.intelligent7.engineautostart;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;


public class ScheduleActivity extends AppCompatActivity
            implements PickerHostFragment.PickerDataListener
{

    static String application_name="MainActivity";
    static String package_name="com.prod.intelligent7.engineautostart";
    static final String DAEMON="DAEMON";
    static String fileName=MainActivity.package_name+".profile";
    static String mActionString=null;
    static ScheduleActivityFragment mFragment=null;
    boolean for1Boot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_action);

        fileName = MainActivity.package_name + ".profile";
        application_name = getResources().getString(R.string.app_name_en);
        chinessWeekDays=getResources().getStringArray(R.array.week_days);
        Intent myIntent = getIntent();
        if (myIntent == null || (mActionString = myIntent.getStringExtra(MainActivity.OPEN_ACTIVITY)) == null) {
            finish();
            return;
        }
        for1Boot = true;
        mFragment = new ScheduleActivityFragment();
        Bundle aBundle = new Bundle();
        aBundle.putString(MainActivity.OPEN_ACTIVITY, mActionString);
        mFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        //mainUI = fragmentManager.findFragmentById(R.id.set_profile_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        // fragmentManager.findFragmentById(R.id.main_content_frame);
        String[] allData=null;
        if (savedInstanceState == null) {
            fragmentTransaction.add(R.id.set_action_fragment, mFragment).commit();
        } else {
            mActionString=savedInstanceState.getString(MainActivity.OPEN_ACTIVITY);
            pageTitle=savedInstanceState.getString("PAGE_TITLE");
            allData=savedInstanceState.getStringArray(mActionString);
            aBundle.putStringArray("CURRENT_DATA", allData);
            fragmentTransaction.replace(R.id.set_action_fragment, mFragment, "DUMMY_UI").commit();
        }
        //Toast.makeText(this, "PENDING construction of "+mCommand, Toast.LENGTH_LONG).show();

        if (savedData ==null) savedData=new HashMap<String, String>();
        if (allData== null || allData.length < 0) {
            Set<String> aSet = getSavedSet(mActionString);
            if (aSet != null) {
                int iSize = aSet.size();
                allData = new String[iSize];
                aSet.toArray(allData);
            }
        }
        if (allData != null) {
            for (int i = 0; i < allData.length; i++) {
                int igx = allData[i].indexOf(">");
                if (igx < 0) continue;
                savedData.put(allData[i].substring(0, igx), allData[i].substring(igx + 1));
            }
        }
        switch (mActionString){
            case MainActivity.SET_ONE_BOOT:
                pageTitle=getResources().getString(R.string.daily_auto_start_setting);
                break;
            case MainActivity.SET_MULTIPLE_BOOT:
                for1Boot=false;
                pageTitle=getResources().getString(R.string.daily_multiple_start_setting);
                break;
            default :
                finish();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pageTitle==null)  pageTitle=getResources().getString(R.string.app_name);
        setTitle(pageTitle);
    }

    @Override
    public void onBackPressed()
    {

    }

    String[] initData(Bundle savedInstanceState)
    {
       return null;
    }

    HashMap<String, String> getCurrentData(){
        return savedData;
    }


    @Override
    protected void onSaveInstanceState (Bundle outState)
    {
        outState.putString(MainActivity.OPEN_ACTIVITY, mActionString);
        outState.putString("PAGE_TITLE", pageTitle);
        if (savedData.size() > 0) {
            String[] outStrings=new String[savedData.size()];
            Iterator <String> itr=savedData.keySet().iterator();
            int i=0;
            while (itr.hasNext()){
                String key=itr.next();
                outStrings[i++]=key+">"+savedData.get(key);
            }
            outState.putStringArray(mActionString, outStrings);
        }
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedState)
    {
        mActionString=savedState.getString(MainActivity.OPEN_ACTIVITY);
        pageTitle=savedState.getString("PAGE_TITLE");
    }

    static String pageTitle=null;

    public void setTitle(String newTitle)
    {
        ActionBar myBar=getSupportActionBar();
        jobCompleted=false;

        if (myBar == null) return;
        int textSize=myBar.getHeight()/3;
        myBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP);
        myBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        myBar.setDisplayHomeAsUpEnabled(true);
        View myBarView= getLayoutInflater().inflate(R.layout.actionbar_title, null, false);
        TextView textViewTitle = (TextView) myBarView.findViewById(R.id.myActionBarTitle);
        textViewTitle.setText(" "+newTitle);
        if (textSize > 0) {
            if (textSize > 25) textSize = 25;
            if (textSize < 10) textSize = 10;
            textViewTitle.setTextSize(textSize);
        }
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        myBar.setCustomView(myBarView, params);
        myBar.getCustomView().invalidate();
    }

    @Override
    public Intent getSupportParentActivityIntent (){
        completeMyJob();
        return super.getSupportParentActivityIntent();
    }

    public void setDefaultTitle()
    {
        pageTitle=getResources().getString(R.string.app_name);
        setTitle(pageTitle);
    }

    public void saveCurrentData(){
        HashMap<String, String> myData;

        Set<String> toSave=new TreeSet<String>();
        for (int i=0; i<7; i++){
            String data=savedData.get(weekDays[i]);
            if (data==null) continue;
            toSave.add(weekDays[i]+">"+data);
        }

        String fileName=package_name+".profile";//getApplication().getPackageName()+".profile";
        SharedPreferences mSPF = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSPF.edit();//prefs.edit();
        editor.putStringSet(mActionString, toSave);
        editor.commit();
    }

    public void confirmOK(View v){
        //data weekly1Data and weekyNData are here so no need to go down to fragment
       /*
        saveCurrentData();


        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        jIntent.putExtra(ConnectDaemonService.DAEMON_COMMAND, "NEW_SCHEDULE");
        startService(jIntent);

        */
       //new MyToast(this, getResources().getString(R.string.schedule_is_arranged)).info();

        //String result= mFragment.confirmOkToCommand(v, mActionString);
        //if (result != null)
        {
            //NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
            finish();
        }

    }

    boolean jobCompleted;
    public void completeMyJob()
    {
        saveCurrentData();

        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        jIntent.putExtra(ConnectDaemonService.DAEMON_COMMAND, "NEW_SCHEDULE");
        startService(jIntent);

        new MyToast(this, getResources().getString(R.string.schedule_is_arranged)).info();
        jobCompleted=true;
    }

    @Override
    public void finish()
    {
        if (!jobCompleted)
        completeMyJob();
        if (!isFinishing()) {
            super.finish();
        }
    }

    public void onDialogPositiveClick(DialogFragment dialog){

        mFragment.populateSchedule(iCurrentWeekTag, savedData.get(weekTag));
        dialog.dismiss();
        saveCurrentData();
    }
    public void onDialogNegativeClick(DialogFragment dialog){
        dialog.dismiss();
    }

    public static final String[] weekDays={"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    public static String[] chinessWeekDays=null;

    HashMap<String, String> savedData;

    String weekTag=null;
    int iCurrentWeekTag;

    public void onClickToSetData(View v)
    {
        iCurrentWeekTag=-1;
       int id=v.getId();
        switch (id){
            case R.id.monday_t0:
            case R.id.monday_period:
            case R.id.monday_idle:
                iCurrentWeekTag=1;
                weekTag=weekDays[1];
                break;
            case R.id.tuesday_t0:
            case R.id.tuesday_period:
            case R.id.tuesday_idle:
                iCurrentWeekTag=2;weekTag=weekDays[2];
                break;
            case R.id.wenseday_t0:
            case R.id.wenseday_period:
            case R.id.wenseday_idle:
                iCurrentWeekTag=3;weekTag=weekDays[3];
                break;
            case R.id.thurseday_t0:
            case R.id.thurseday_period:
            case R.id.thurseday_idle:
                iCurrentWeekTag=4;weekTag=weekDays[4];
                break;
            case R.id.friday_t0:
            case R.id.friday_period:
            case R.id.friday_idle:
                iCurrentWeekTag=5;weekTag=weekDays[5];
                break;
            case R.id.saturday_t0:
            case R.id.saturday_period:
            case R.id.saturday_idle:
                iCurrentWeekTag=6;weekTag=weekDays[6];
                break;
            case R.id.sunday_t0:
            case R.id.sunday_period:
            case R.id.sunday_idle:
                iCurrentWeekTag=0;weekTag=weekDays[0];
                break;
        }
        PickerHostFragment aDlg=new PickerHostFragment();
        if (for1Boot) {
            aDlg.isOnRecurring(false);
            aDlg.setResources(this, R.layout.dialog_once_daily);
        }
        else {
            aDlg.isOnRecurring(true);
            aDlg.setResources(this, R.layout.dialog_cycle_daily);
        }
        aDlg.setDataBox(savedData);
        aDlg.setSavedData(savedData.get(weekTag));
        aDlg.setJobTag(weekTag);
        aDlg.setTitle(chinessWeekDays[iCurrentWeekTag]);
        DialogFragment dialog = aDlg;

        dialog.show(getSupportFragmentManager(), "PickerHostFragment");

        mCurrentDialog=aDlg;
    }

    PickerHostFragment mCurrentDialog;
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((Switch) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.set_it:
                if (checked)
                {
                    mCurrentDialog.setPickerEnabled(true);
                }
                else
                {
                    savedData.remove(weekTag);
                    mFragment.populateSchedule(iCurrentWeekTag, savedData.get(weekTag));
                    ((DialogFragment)mCurrentDialog).dismiss();
                    //mCurrentDialog.setPickerEnabled(false);
                }
                    break;
            default:
                    break;
        }
    }


    Menu mMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public void openOptionsMenu()
    {
        super.openOptionsMenu();
    }

    public void showMenuItems(View v)
    {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                openOptionsMenu();
            }
        }, 0);
        //openOptionsMenu();// mMenu.findItem(R.id.action_get_recent1).setVisible(true);
    }

    void showLogData(int command){
        ShowLogData logFragment=new ShowLogData();
        logFragment.setCommand(command);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        fragmentTransaction.replace(R.id.container, logFragment, "MAIN_UI").commit();
        mCurrentFragment=logFragment;

        pageTitle=getResources().getString(R.string.check_log);


        setTitle(pageTitle);

    }
   // @Override
    public boolean onOptionsItemSelected1(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert;
        TextView textView;
        Button okB;
        switch (id)
        {
            /*
            case R.id.action_get_sim:
                alert = builder.setMessage(getResources().getString(R.string.sim)+": "+getSavedValue(MainActivity.SET_SIM))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        }).create();
                alert.show();
                textView = (TextView) alert.findViewById(android.R.id.message);
                textView.setTextSize(40);
                okB = (Button) alert.findViewById(android.R.id.button1);
                okB.setTextSize(40);
                break;
            case R.id.action_get_pin:

                alert = builder.setMessage(getResources().getString(R.string.pin)+": "+getSavedValue(MainActivity.SET_PIN))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        }).create();
                alert.show();
                textView = (TextView) alert.findViewById(android.R.id.message);
                textView.setTextSize(40);
                okB = (Button) alert.findViewById(android.R.id.button1);
                okB.setTextSize(40);
                break;
            case R.id.action_get_phones:
                String[] items=new String[2];
                items[0] = getSavedValue(MainActivity.SET_PHONE1);
                items[1]=getSavedValue(MainActivity.SET_PHONE2);
                alert = builder.setTitle(getResources().getString(R.string.phone_numbers)+":")//+getSavedValue(SET_PHONE2))
                        //.setMessage(items[0]+"      "+items[1])
                        .setCancelable(false)
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface d, int i){

                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        }).create();
                alert.show();
                /*
                int alertTitle = getResources().getIdentifier("alertTitle", "id", "android");
                View title = alert.findViewById(alertTitle);
                if (title != null && title instanceof TextView) {
                    ((TextView) title).setTextSize(40);
                }
                //textView.setTextSize(40);
                ListView lv=alert.getListView();
                ListAdapter adp=(ListAdapter) alert.getListView().getAdapter();//findViewById(android.R.id.list);
                textView = (TextView)lv.getChildAt(0);//)adp.getView(0,  null, lv);
                textView.setTextSize(40);
                textView.setTextColor(0xff0000);//android.R.color.holo_red_light);
                textView = (TextView)lv.getChildAt(1);//adp.getView(1, null, lv);
                textView.setTextSize(40);
                okB = (Button) alert.findViewById(android.R.id.button1);
                okB.setTextSize(40);

                break;
            case R.id.action_get_recent1:
                showLogData(ShowLogData.SHOW_NEWEST);
                break;
            case R.id.action_get_recent10:
                showLogData( ShowLogData.SHOW_LAST10);
                break;
            case R.id.action_get_last_failed:
                showLogData(ShowLogData.SHOW_FAILED1);
                break;
                */
            case R.id.action_clean_log:
                final Context ctx=this;
                alert = builder.setMessage(getResources().getString(R.string.confirm_to_clear_log))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ShowLogData popF = new ShowLogData();
                                popF.clearLog(ctx);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //ShowLogData popF = new ShowLogData();
                                //popF.clearLog(ctx);
                            }
                        }).create();
                alert.show();
                textView = (TextView) alert.findViewById(android.R.id.message);
                textView.setTextSize(40);
                okB = (Button) alert.findViewById(android.R.id.button1);
                okB.setTextSize(40);
                okB = (Button) alert.findViewById(android.R.id.button2);
                okB.setTextSize(40);
                break;
            case android.R.id.home:
                //NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                finish();
                return true;
            default:

                break;
        }
        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        // return true;
        //}

        return false; //super.onOptionsItemSelected(item);
    }


    public static Fragment mainUI=null;
    static MySimpleFragment mCurrentFragment=null;


    public String getSavedValue(String key) //default is "--"
    {
        String fileName=package_name+".profile";//getApplication().getPackageName()+".profile";
        SharedPreferences mSPF = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = mSPF.edit();//prefs.edit();
        // String pwd=MainActivity.SET_PIN;//getResources().getString(R.string.pin_setting);
        String tmp=mSPF.getString(key, "--");
        if (tmp.length() < 1) tmp="--";
        return tmp;
    }

    public void setPreferenceValue(String key, String value) //default is "--"
    {
        if (value == null || value.length()<1) return;
        String fileName=package_name+".profile";//getApplication().getPackageName()+".profile";
        SharedPreferences mSPF = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSPF.edit();//prefs.edit();
        //String pwd=MainActivity.SET_PIN;//getResources().getString(R.string.pin_setting);
        editor.putString(key, value);
        editor.commit();
    }

    public Set<String> getSavedSet(String key){
        String fileName=package_name+".profile";//getApplication().getPackageName()+".profile";
        SharedPreferences mSPF = getSharedPreferences(fileName, Context.MODE_PRIVATE);

        return mSPF.getStringSet(key, null);
    }


}
