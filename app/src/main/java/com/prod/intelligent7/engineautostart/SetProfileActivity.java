package com.prod.intelligent7.engineautostart;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class SetProfileActivity extends AppCompatActivity
        implements  GetTextDialogFragment.GetTextDialogListener,
         ConnectDaemonService.UrgentMailListener
{

    static String application_name="MainActivity";
    static String package_name="com.prod.intelligent7.engineautostart";
    private  static final String FIRST_TIME_USER="first_time";
    static final String DAEMON="DAEMON";
    static String fileName=MainActivity.package_name+".profile";
    static String mActionString=null;
    private static int resultCodeBase=100;
    static SetProfileActivityFragment mFragment=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_codes);

        fileName=MainActivity.package_name+".profile";
        application_name=getResources().getString(R.string.app_name_en);

        Intent myIntent=getIntent();
        if (myIntent == null || (mActionString=myIntent.getStringExtra(MainActivity.OPEN_ACTIVITY))==null) {
                finish();
                return;
            }
            mFragment=new SetProfileActivityFragment();
            Bundle aBundle=new Bundle();
            aBundle.putString(MainActivity.OPEN_ACTIVITY, mActionString);
            mFragment.setArguments(aBundle);

            FragmentManager fragmentManager = getSupportFragmentManager();
            //mainUI = fragmentManager.findFragmentById(R.id.set_profile_fragment);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);//"MAIN_UI");
            // fragmentManager.findFragmentById(R.id.main_content_frame);
        if (savedInstanceState == null){
            fragmentTransaction.add(R.id.set_profile_fragment, mFragment).commit();
        }
         else
            fragmentTransaction.replace(R.id.set_profile_fragment, mFragment, "DUMMY_UI").commit();
            //Toast.makeText(this, "PENDING construction of "+mCommand, Toast.LENGTH_LONG).show();

        switch (mActionString){
            case MainActivity.SET_SIM:
                pageTitle=getResources().getString(R.string.sim_setting);
                break;
            case MainActivity.SET_PIN:
                resultCodeBase=200;
                pageTitle=getResources().getString(R.string.pin_setting);
                break;
            case MainActivity.SET_PHONES:
                resultCodeBase=300;
                pageTitle=getResources().getString(R.string.phone_numbers_setting);
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

    static ProgressDialog myProgress=null;
    View mWaitFormConfirmViewRoot=null;
    public void confirmOK(View v){
        //Toast.makeText(this, "I got called here", Toast.LENGTH_LONG).show();
        mWaitFormConfirmViewRoot=v.getRootView();
        String result= mFragment.confirmOkToCommand(v, mActionString);
        if (result != null)
        {
            if(result.equalsIgnoreCase("OK"))
            {
                finish();
                return;
            }

            myProgress = new ProgressDialog(this);
            myProgress.setTitle(pageTitle);
            myProgress.setMessage(getResources().getString(R.string.wait_for_response));
            myProgress.setCancelable(false);
            myProgress.show();

            //ConnectDaemonService.UrgentMailRegister.register(this);
            got_response = false;

            //msgBox=new ArrayBlockingQueue(2);
            final SetProfileActivity aContext=this;//(SetProfileActivity)getActivity();
            final ProgressDialog myDlg=myProgress;
            final SetProfileActivityFragment aFg=mFragment;
            aBuilder=new AlertDialog.Builder(this);
            WaitForResponseTask aTask=new WaitForResponseTask();
            got_response=false;
            aTask.setMsgBox(msgBox);
            aTask.execute();
            /*
            new Thread(new Runnable() {
                @Override
                public void run() {

                    long end_time = new Date().getTime() + 5 * 60 * 1000;
                    do {
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (checkIfGetResponse()) {

                            break;
                        }

                    } while (end_time > new Date().getTime());

                    myProgress.dismiss();
                    myProgress.cancel();
                    aContext.showResponse();
                }

            }).start();   */

        }

    }

    String myCommand;
    IBinder myBinder;
    ArrayList<String> urgentMailBox;
    ConnectDaemonService myService;
    boolean mBound;
    private ServiceConnection mConnection;

    public void startMyService(Intent jobIntent){
        mBound = false;
        urgentMailBox=null;
        myCommand=jobIntent.getExtras().getString(ConnectDaemonService.DAEMON_COMMAND);

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder binder) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                ConnectDaemonService.UrgentMailBinder bd=(ConnectDaemonService.UrgentMailBinder) binder;
                //mDaemon = (TcpConnectDaemon) bd.getPostMan();
                myBinder=binder;
                urgentMailBox=bd.getUrgentMailBox();
                myService=bd.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
                urgentMailBox=null;
            }
        };

        bindService(jobIntent, mConnection, Context.BIND_AUTO_CREATE);
        //startService(jobIntent);
    }

    void waitForBinder(){
        while (!mBound){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        mBound = false;
        if (myService != null){
            if (urgentMailBox.size() < 1) {
                //urgentMailBox.add(myCommand);
                myService.sendCommand(myCommand);
            } else
            {
                String command=urgentMailBox.get(0);
                if (command.charAt(0)=='M'){
                    urgentMailBox.remove(0);
                }
            }
        }
        /*
        try {
            String msg=urgentMailBox.poll(3, TimeUnit.MINUTES);
            if (msg != null) processMail(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    private class WaitForResponseTask extends AsyncTask<Void, Void, String>
    {
        ArrayBlockingQueue<String> msgBox;
        public void setMsgBox(ArrayBlockingQueue bx){
            msgBox=bx;
        }
        @Override
        protected  String doInBackground(Void ... Void) {
            String resp=null;
            waitForBinder();
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                   waitForResponse();
                }
            }).start();*/

            long endTime=new Date().getTime()+3*60*1000;
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (new Date().getTime() > endTime) return null;
            }
            while  (urgentMailBox.size() <1);

                resp=urgentMailBox.remove(0);
                urgentMailBox.clear();
            return resp;
        }

        @Override
        protected  void onPostExecute(String resp)
        {
            myProgress.dismiss();
            myProgress.cancel();
            myProgress=null;
            if (resp==null)
                cancelUrgentMail();
            else
                processMail(resp);
           showResponse();
        }
    }

    public void cancelUrgentMail(){
        if (got_response) return;
        Intent jIntent=new Intent(SetProfileActivity.this, ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.putExtra(ConnectDaemonService.DAEMON_COMMAND, "URGENCY DISMISSED");
        //Toast.makeText(this, "will send start command to server", Toast.LENGTH_LONG).show();
        startService(jIntent);
    }

    View dlgView;

    class DummyResponder implements DialogInterface.OnClickListener
    {
        int resultCode;
        public void setResultCode(int iResult){
            resultCode=iResult;
        }
        public void setOwner(SetProfileActivity this1)
        {
            fAcv=this1;
        }
        SetProfileActivity fAcv;

        @Override
        public void onClick(DialogInterface dialog, int id) {
            if (fAcv!=null)
            fAcv.finishJob(resultCode);
        }

    }

    public static final int SETPIN_OK=200;
    public static final int SETPIN_FAILED=201;
    public static final int SETPHONE_OK=300;
    public static final int SETPHONE_FAILED=301;
    public static final int SET_NO_RESPONSE=999;

    public void finishJob(int withResult){
        Intent retInt=getIntent();

        setResult(withResult, retInt);
        if (myProgress != null) {
            myProgress.dismiss();
            myProgress.cancel();
            myProgress=null;
        }
        finish();
    }

    @Override
    protected void onDestroy()
    {
        if (myProgress != null) {
            myProgress.dismiss();
            myProgress.cancel();
        }
        super.onDestroy();
    }

    AlertDialog.Builder aBuilder;
    void showResponse() {
        dlgView=getLayoutInflater().inflate(R.layout.dlg_response, (ViewGroup)mWaitFormConfirmViewRoot, false);
        TextView ttl=(TextView)dlgView.findViewById(R.id.title);
        ttl.setText(pageTitle);

        final DummyResponder aDummy=new DummyResponder();
        aDummy.setOwner(null);//SetProfileActivity.this);
        final String ok1=getResources().getString(R.string.understand);

        TextView msg=(TextView)dlgView.findViewById(R.id.message);

        final String resp1=ShowLogData.translate(myResponse);
        aDummy.setOwner(SetProfileActivity.this);

        if (got_response) {
            msg.setText(resp1);
            aDummy.setResultCode(resultCodeBase+(myResponse.charAt(3)-'0'));
            if (myResponse.charAt(3)=='0') {
                msg.setBackgroundResource(R.drawable.ok140x100);//Color.GREEN);
            }
            else msg.setBackgroundResource(R.drawable.my_alert);//Color.YELLOW);
            /*
            aBuilder.setView(dlgView)//setMessage(resp1)
                    .setPositiveButton(ok1, aDummy)
                    .create()
            .show();*/
            //new MyToast(getActivity(), ShowLogData.translate(myResponse)).info();
            //return;
        }
        else {
            final String noResp = getResources().getString(R.string.no_response) +
                    getResources().getString(R.string.please_inform) +
                    getResources().getString(R.string.administration_number);
            //aBuilder=new AlertDialog.Builder(this);
            msg.setText(noResp);
            aDummy.setResultCode(resultCodeBase+9);
            msg.setBackgroundColor(Color.YELLOW);
        }
        aBuilder.setView(dlgView)//setMessage(noResp)
                .setPositiveButton(ok1, aDummy)
                .create()
        .show();
    }

    boolean checkIfGetResponse()
    {
        return got_response;
    }
    ArrayBlockingQueue msgBox;
    boolean got_response;
    String myResponse;
    public void processMail(String data)  {
        myResponse=data.substring(0,4);
        got_response=true;
        //ConnectDaemonService.UrgentMailRegister.unRegister(this);
/*
        try {
            msgBox.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        if (data.substring(0, 4).equalsIgnoreCase("S300")) {
            String okP = getSavedValue(MainActivity.PENDING_NEW_PHONE1);
            String okP2 = getSavedValue(MainActivity.PENDING_NEW_PHONE2);
            if (okP.charAt(0) != '-') {
                setPreferenceValue(MainActivity.SET_PHONE1, okP);
                if (okP2.charAt(0) != '-')
                    setPreferenceValue(MainActivity.SET_PHONE2, okP2);
            }
        } else if (data.substring(0, 4).equalsIgnoreCase("S200")) {
            //String fileName=MainActivity.package_name+".profile";//getApplication().getPackageName()+".profile";
            String okP = getSavedValue(MainActivity.PENDING_NEW_PIN);
            if (okP.charAt(0) != '-') {
                setPreferenceValue(MainActivity.SET_PIN, okP);
            }
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*
        int id = item.getItemId();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert;
        TextView textView;
        Button okB;
        switch (id)
        {
            /*
            case R.id.action_get_sim:
                alert = builder.setMessage(getResources().getString(R.string.sim)+": "+getSavedValue(SET_SIM))
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

                alert = builder.setMessage(getResources().getString(R.string.pin)+": "+getSavedValue(SET_PIN))
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
                items[0] = getSavedValue(SET_PHONE1);
                items[1]=getSavedValue(SET_PHONE2);
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
                ///
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
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;
            default:

                break;
        }
        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        // return true;
        //}
*/
        return super.onOptionsItemSelected(item);
    }



    public String getSim()
    {
        String retSim=null;

        return retSim;
    }

    public String getPin()
    {
        String retPin=null;

        return retPin;
    }

    void saveOneBootScheme(Date when)
    {

    }

    void saveMultipleBootScheme(Date toStart, Date toEnd, long period) //have to create service to monitor when to send command out
    {

    }


    void putInLog(String command)
    {
        //ToDo add time stamp and save it to log table with flag=SUCCESS (1) or FAILED (-1) or other code
        //also add Sim and Phone number, in case this work for multiple SIM and Phones
    }

    public void onDialogPositiveClick(DialogFragment dialog)
    {
        if (dialog.getArguments()==null) return;
        String[] returnData=dialog.getArguments().getStringArray(GetTextDialogFragment.DATA_ENTERED);
        if (returnData==null)return;


    }
    public void onDialogNegativeClick(DialogFragment dialog)
    {

    };

    String mCommand;
    void checkLog()
    {
        Toast.makeText(this, "PENDING construction of " + mCommand, Toast.LENGTH_LONG).show();
    }
    void cleanLog()
    {
        Toast.makeText(this, "PENDING construction of "+mCommand, Toast.LENGTH_LONG).show();
    }

    public static Fragment mainUI=null;
    void setSimNumber()
    {
        //GetTextDialogFragment simFragment=new GetTextDialogFragment();
        ReadSimFragment simFragment=new ReadSimFragment();
        Bundle aBundle=new Bundle();
        //aBundle.putString(GetTextDialogFragment.DATA_ENTRY_LAYOUT, R.layout.get_sim);
        aBundle.putString("PREFERENCE_FILE_NAME", getApplication().getPackageName()+"profile");
        simFragment.setArguments(aBundle);

        //simFragment.show(getSupportFragmentManager(), "getSIM");
        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        // fragmentManager.findFragmentById(R.id.main_content_frame);
        fragmentTransaction.replace(R.id.container, simFragment, "MAIN_UI").commit();
        //Toast.makeText(this, "PENDING construction of "+mCommand, Toast.LENGTH_LONG).show();
        if (getSavedValue(SET_SIM).charAt(0)=='-')
            pageTitle=getResources().getString(R.string.sim_setting);
        else
            pageTitle=getResources().getString(R.string.sim_change);
        setTitle(pageTitle);
    }

    void confirmSimAndPhone()
    {
        if (getSavedValue(SET_SIM).charAt(0)=='-')
        {
            Toast.makeText(this, "Need to set SIM first ", Toast.LENGTH_LONG).show();
            setSimNumber();

        }
        if (getSavedValue(SET_PHONE1).charAt(0)=='-')
        {
            Toast.makeText(this, "Please add phones ", Toast.LENGTH_LONG).show();
            setPhones();

        }
        return ;
    }

    public void sendCommandAndDone(String command)
    {
        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.putExtra(ConnectDaemonService.DAEMON_COMMAND, command);
        //Toast.makeText(this, "will send start command to server", Toast.LENGTH_LONG).show();
        startService(jIntent);
        //mCurrentFragment.backToMain();
    }
    public void updatePinCommand()
    {
        ((ReadPinFragment)mCurrentFragment).saveData();
        String pin=getSavedValue(SET_PIN);
        String oldPin=getSavedValue(OLD_PIN);
        if (pin.charAt(0)=='-' || pin.equalsIgnoreCase(oldPin))
        {
            return;
        }
        String command="M2-"+oldPin+"-"+pin+"-"+pin;
        sendCommandAndDone(command);
        Toast.makeText(this, ConnectDaemonService.getChinese("M2")+"指令已送出", Toast.LENGTH_LONG).show();
    }
    void setPin() //make sure only 4 Digits
    //steps : first send the phone number to booster with 0000 PIN and then update the new PIN
    {
        confirmSimAndPhone();

        ReadPinFragment pinFragment=new ReadPinFragment();
        Bundle aBundle=new Bundle();
        aBundle.putString("PREFERENCE_FILE_NAME", getApplication().getPackageName()+".profile");
        pinFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        fragmentTransaction.replace(R.id.container, pinFragment, "MAIN_UI").commit();
        mCurrentFragment=pinFragment;
        if (getSavedValue(SET_PIN).charAt(0)=='-')
            pageTitle=getResources().getString(R.string.pin_setting);
        else
            pageTitle=getResources().getString(R.string.pin_change);

        setTitle(pageTitle);
    }

    public void savePhoneNumber(View v)
    {
        ((ReadPhoneFragment)mCurrentFragment).saveData();
        String pin=getSavedValue(SET_PIN);
        String p1=getSavedValue(PENDING_NEW_PHONE);//getSavedValue(SET_PHONE1);
        if (p1.charAt(0)=='-')
        {
            //closeFragment(v);
            return;
        }
        String p2=getSavedValue(SET_PHONE2);
        String command="M3-"+pin+"-"+p1+"-";
        if (p2.charAt(0)!='-') command += p2;
        sendCommandAndDone(command);
        Toast.makeText(this, ConnectDaemonService.getChinese("M3")+"指令已送出", Toast.LENGTH_LONG).show();
    }

    static MySimpleFragment mCurrentFragment=null;
    void setPhones()
    {
        if (getSavedValue(SET_SIM).charAt(0)=='-')
        {
            Toast.makeText(this, "Need to set SIM first ", Toast.LENGTH_LONG).show();
            setSimNumber();

        }
        ReadPhoneFragment phoneFragment=new ReadPhoneFragment();
        Bundle aBundle=new Bundle();
        aBundle.putString("PREFERENCE_FILE_NAME", getApplication().getPackageName()+".profile");
        phoneFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        fragmentTransaction.replace(R.id.container, phoneFragment, "MAIN_UI").commit();
        mCurrentFragment=phoneFragment;
        if (getSavedValue(SET_PHONE1).charAt(0)=='-')
            pageTitle=getResources().getString(R.string.phone_numbers_setting);
        else
            pageTitle=getResources().getString(R.string.phone_numbers_change);

        setTitle(pageTitle);
    }


    public void startWarmer(View v)
    {
        String command= "M1-01";
        sendCommandAndDone(command);
        ((SetWarmerFragment)mCurrentFragment).backToMain();
        Toast.makeText(this, ConnectDaemonService.getChinese(command)+"指令已送出", Toast.LENGTH_LONG).show();
    }

    public void startAirCondition(View v)
    {
        ((SetWarmerFragment)mCurrentFragment).backToMain();
    }
    void selectWarmerCooler()
    {
        confirmSimAndPhone();
        SetWarmerFragment airFragment=new SetWarmerFragment();
        Bundle aBundle=new Bundle();
        aBundle.putString("PREFERENCE_FILE_NAME", getApplication().getPackageName()+".profile");
        airFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        fragmentTransaction.replace(R.id.container, airFragment, "MAIN_UI").commit();
        mCurrentFragment=airFragment;

        pageTitle=getResources().getString(R.string.warming_cooling_setting);


        setTitle(pageTitle);
    }

    public void saveOneBootData(View v)
    {
        if (((SetOneBootFragment)mCurrentFragment).verifyAndSaveData())  {
            Toast.makeText(this, "定时启动指令已設定", Toast.LENGTH_LONG).show();
            sendCommandAndDone("NEW SCHEDULE");
        }
        //((SetOneBootFragment)mCurrentFragment).backToMain();
    }

    public void pickTime(View v) {
        DialogFragment newFragment = new PickTimeFragment();
        ((PickTimeFragment)newFragment).setViewToFill((TextView) v);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    View responseView;
    public void pickDate(View v) {
        //DialogFragment newFragment = new PickerHostFragment();
        //((PickerHostFragment)newFragment).setViewToFill((TextView) v);
        DialogFragment newFragment = new PickCalendarFragment();
        ((PickCalendarFragment)newFragment).setViewToFill((TextView) v);
        //UseCalendarFragment newFragment=new UseCalendarFragment();

        //FragmentManager fragmentManager = getSupportFragmentManager();
        //mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        //fragmentTransaction.replace(R.id.container, newFragment, "ONE_BOOT").commit();
        //((UseCalendarFragment)newFragment).setViewToFill((TextView) v);
        // mUseCalendarFragment=newFragment;
        newFragment.show(getSupportFragmentManager(), "calendarPicker");
        responseView=v;
    }
    UseCalendarFragment mUseCalendarFragment;
    public void setCalendarDate(View v){
        View rootV=v.getRootView();
        CalendarView cV= (CalendarView) rootV.findViewById(R.id.calendarView);
        long getTime=cV.getDate();
        //TimeZone.setDefault(TimeZone.getTimeZone("Hongkong"));
        GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone("Hongkong"));
        gToday.setTimeInMillis(getTime);
        ((TextView) responseView).setText(new DecimalFormat("00").format(gToday.get(Calendar.YEAR)) + "/" +
                (new DecimalFormat("00")).format(gToday.get(Calendar.MONTH)) + "/" +
                (new DecimalFormat("00")).format(gToday.get(Calendar.DAY_OF_MONTH)));
        mUseCalendarFragment.backToMain();
    }

    public void noAction(View v){
        mUseCalendarFragment.backToMain();
    }

    void setOneBoot()
    {
        confirmSimAndPhone();

        SetOneBootFragment oneBootFragment=new SetOneBootFragment();
        Bundle aBundle=new Bundle();
        aBundle.putString("PREFERENCE_FILE_NAME", getApplication().getPackageName()+".profile");
        oneBootFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        fragmentTransaction.replace(R.id.container, oneBootFragment, "MAIN_UI").commit();
        mCurrentFragment=oneBootFragment;

        pageTitle=getResources().getString(R.string.daily_auto_start_setting);


        setTitle(pageTitle);

        //PICK4WHAT=ONE_BOOT_PARAMS;
        /*
        Intent pIntent=new Intent(this, PickActivity.class);
        pIntent.putExtra(PICK4WHAT, ONE_BOOT_PARAMS);
        startActivityForResult(pIntent, PICK_ONE);
        //Toast.makeText(this, "PENDING construction of " + mCommand, Toast.LENGTH_LONG).show();
        */
    }

    public static final String N_BOOT_PARAMS="NBOOT_PARAM";
    public static final String ONE_BOOT_PARAMS="1NBOOT_PARAM";

    public static String PICK4WHAT="WHICH_PARAM";
    public void saveNBootData(View v)
    {
        ((SetOnOffBootFragment)mCurrentFragment).saveData();

        ((SetOnOffBootFragment)mCurrentFragment).backToMain();

        Toast.makeText(this, "多次启动指令已設定", Toast.LENGTH_LONG).show();


        sendCommandAndDone("NEW SCHEDULE");
    }
    void setMultipleBoot()
    {
        confirmSimAndPhone();

        SetOnOffBootFragment onOffBootFragment=new SetOnOffBootFragment();
        Bundle aBundle=new Bundle();
        aBundle.putString("PREFERENCE_FILE_NAME", getApplication().getPackageName()+".profile");
        onOffBootFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        fragmentTransaction.replace(R.id.container, onOffBootFragment, "MAIN_UI").commit();
        mCurrentFragment=onOffBootFragment;

        pageTitle=getResources().getString(R.string.daily_multiple_start_setting);


        setTitle(pageTitle);

        /*
        //PICK4WHAT=N_BOOT_PARAMS;
        Intent pIntent=new Intent(this, PickActivity.class);
        pIntent.putExtra(PICK4WHAT, N_BOOT_PARAMS);
        //pIntent.putExtra(PAGE_TITLE, mPageTitles[5]);
        //pIntent.putExtra(mFixKey, fixMsg);
        startActivityForResult(pIntent, PICK_N);
        //Toast.makeText(this, "PENDING construction of "+mCommand, Toast.LENGTH_LONG).show();
        */
    }

    public void startEngine(View v)
    {
        String command="M5-";
        String howLong=((EditText)(v.getRootView().findViewById(R.id.last4_now))).getText().toString();
        int i4=Integer.parseInt(howLong);
        i4=(i4>30)?30:i4;
        command += (new DecimalFormat("00")).format(i4);
        sendCommandAndDone(command);
        ((StartEngineFragment)mCurrentFragment).backToMain();
        Toast.makeText(this, ConnectDaemonService.getChinese("M5")+"指令已送出", Toast.LENGTH_LONG).show();
    }

    public void closeFragment(View v)
    {
        mCurrentFragment.backToMain();
    }
    void startNow()
    {
        confirmSimAndPhone();
        StartEngineFragment startEngineFragment=new StartEngineFragment();
        Bundle aBundle=new Bundle();
        aBundle.putString("PREFERENCE_FILE_NAME", getApplication().getPackageName()+".profile");
        startEngineFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        fragmentTransaction.replace(R.id.container, startEngineFragment, "MAIN_UI").commit();
        mCurrentFragment=startEngineFragment;

        pageTitle=getResources().getString(R.string.start_engine);
        setTitle(pageTitle);
    }
    public void stopEngine(View v)
    {
        String command="M4-00";
        //String howLong=((EditText)(v.getRootView().findViewById(R.id.last4))).getText().toString();
        //int i4=Integer.parseInt(howLong);
        //i4=(i4>30)?30:i4;
        //command += new DecimalFormat("0#").format(i4);
        sendCommandAndDone(command);
        ((StopEngineFragment)mCurrentFragment).backToMain();
        Toast.makeText(this, ConnectDaemonService.getChinese(command)+"指令已送出", Toast.LENGTH_LONG).show();
    }
    void stopNow()
    {
        confirmSimAndPhone();
        StopEngineFragment stopEngineFragment=new StopEngineFragment();
        Bundle aBundle=new Bundle();
        aBundle.putString("PREFERENCE_FILE_NAME", getApplication().getPackageName()+".profile");
        stopEngineFragment.setArguments(aBundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);//"MAIN_UI");
        fragmentTransaction.replace(R.id.container, stopEngineFragment, "MAIN_UI").commit();
        mCurrentFragment=stopEngineFragment;

        pageTitle=getResources().getString(R.string.shut_down_engine);
        setTitle(pageTitle);
    }

    public static final String OPEN_LOG="open_log";
    public static final String CLEAN_LOG="clean_logr";
    public static final String SET_SIM="set_sim_number";
    public static final String SET_PIN="set_pin_code";
    public static final String PENDING_NEW_PIN="pending_pin";
    public static final String OLD_PIN="old_pin";
    public static final String SET_PHONES="set_phone_numbers";
    public static final String SET_PHONE1="set_phone_1";
    public static final String SET_PHONE2="set_phone_2";
    public static final String GET_PHONE_OLD="get_phone_old";
    public static final String PENDING_NEW_PHONE="pending_phone";
    public static final String SET_WARMER="select_warmer_cooler";
    public static final String SET_ONE_BOOT="set_one_boot";
    public static final String SET_MULTIPLE_BOOT="set_multiple_boot";
    public static final String CMD_START_NOW="cmd_start_now";
    public static final String CMD_STOP_NOW="set_stop_now";
    public static final int PICK_DATE=91;
    public static final int PICK_TIME_START=92;
    public static final int PICK_TIME_END=93;
    public static final int PICK_PERIOD=94;
    public static final int PICK_ALL=99;
    //public static final int PICK_TIME_END=93;

    public void executeCommand(String command)
    {
        mCommand=command;
        switch(command)
        {
            case SET_SIM:
                setSimNumber();
                break;
            case SET_PIN:
                setPin();
                break;
            case SET_WARMER:
                selectWarmerCooler();
                break;
            case SET_PHONES:
                setPhones();
                break;
            case SET_ONE_BOOT:
                setOneBoot();
                break;
            case SET_MULTIPLE_BOOT:
                setMultipleBoot();
                break;
            case CMD_START_NOW:
                startNow();
                break;
            case CMD_STOP_NOW:
                stopNow();
                break;

            case OPEN_LOG:
                checkLog();
                break;
            case CLEAN_LOG:
                cleanLog();
            default:
                break;
        }

    }

    public static final int PICK_ONE=61;
    public static final int PICK_N=66;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_ALL)
        {
            // String id=data.getStringExtra(CITIZEN_ID);
            //if (id != null) mCitizenId=id;
            //if (mCitizenId.indexOf("ZZZ") == 0)
            // openAgendaPage(null);
            //else
            {
                //openPersonalPage(null);
            }
        }
        else if (requestCode == PICK_DATE)
        {
            // openCommitmentPage(null);
        }
        else
        {
            //openAgendaPage(null);
        }
    }

    class MyDataBaseJob {

    }

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
