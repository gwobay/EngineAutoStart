package com.prod.intelligent7.engineautostart;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class SetActionActivity extends AppCompatActivity {

    static String application_name="MainActivity";
    static String package_name="com.prod.intelligent7.engineautostart";
    static final String DAEMON="DAEMON";
    static String fileName=MainActivity.package_name+".profile";
    static String mActionString=null;
    static SetActionActivityFragment mFragment=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_action);

        fileName=MainActivity.package_name+".profile";
        application_name=getResources().getString(R.string.app_name_en);

        Intent myIntent=getIntent();
        if (myIntent == null || (mActionString=myIntent.getStringExtra(MainActivity.OPEN_ACTIVITY))==null) {
            finish();
            return;
        }
        mFragment=new SetActionActivityFragment();
        Bundle aBundle=new Bundle();
        aBundle.putString(MainActivity.OPEN_ACTIVITY, mActionString);
        mFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        //mainUI = fragmentManager.findFragmentById(R.id.set_profile_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        // fragmentManager.findFragmentById(R.id.main_content_frame);
        if (savedInstanceState == null){
            fragmentTransaction.add(R.id.set_action_fragment, mFragment).commit();
        }
        else
            fragmentTransaction.replace(R.id.set_action_fragment, mFragment, "DUMMY_UI").commit();
        //Toast.makeText(this, "PENDING construction of "+mCommand, Toast.LENGTH_LONG).show();

        switch (mActionString){
            case MainActivity.SET_WARMER:
                pageTitle=getResources().getString(R.string.warming_cooling_setting);
                break;
            case MainActivity.CMD_START_NOW:
                pageTitle=getResources().getString(R.string.start_engine);
                break;
            case MainActivity.CMD_STOP_NOW:
                pageTitle=getResources().getString(R.string.shut_down_engine);
                break;
            default :
                finish();
                break;
        }

    }

    @Override
    protected void onSaveInstanceState (Bundle outState)
    {
        outState.putString(MainActivity.OPEN_ACTIVITY, mActionString);
        outState.putString("PAGE_TITLE", pageTitle);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedState)
    {
        mActionString=savedState.getString(MainActivity.OPEN_ACTIVITY);
        pageTitle=savedState.getString("PAGE_TITLE");
    }
    static String pageTitle=null;
    @Override
    protected void onStart() {
        super.onStart();
        if (pageTitle==null)  pageTitle=getResources().getString(R.string.app_name);
        setTitle(pageTitle);
    }

    public void setTitle(String newTitle)
    {
        ActionBar myBar=getSupportActionBar();

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

    public void setDefaultTitle()
    {
        pageTitle=getResources().getString(R.string.app_name);
        setTitle(pageTitle);
    }

    public void confirmOK(View v){
        //Toast.makeText(this, "I got called here", Toast.LENGTH_LONG).show();

        String result= mFragment.confirmOkToCommand(v, mActionString);
        if (result != null)
        {
            //NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
            finish();
        }

    }


    Menu mMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);

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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }


    public static Fragment mainUI=null;
    static MySimpleFragment mCurrentFragment=null;


    public String getSavedValue(String key) //default is "--"
    {
        String fileName=package_name+".profile";//getApplication().getPackageName()+".profile";
        SharedPreferences mSPF = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = mSPF.edit();//prefs.edit();
        // String pwd=MainActivity.SET_PIN;//getResources().getString(R.string.pin_setting);
        return mSPF.getString(key, "--");
    }

    public void setPreferenceValue(String key, String value) //default is "--"
    {
        String fileName=package_name+".profile";//getApplication().getPackageName()+".profile";
        SharedPreferences mSPF = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSPF.edit();//prefs.edit();
        //String pwd=MainActivity.SET_PIN;//getResources().getString(R.string.pin_setting);
        editor.putString(key, value);
        editor.commit();
    }

}
