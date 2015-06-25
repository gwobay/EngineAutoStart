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
import java.util.concurrent.locks.ReentrantLock;


public class SetProfileActivity extends AppCompatActivity
        implements  ConnectDaemonService.UrgentMailListener
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
            Intent jIntent=new Intent(this, ConnectDaemonService.class);
            myCommand=result;
            jIntent.putExtra(ConnectDaemonService.DAEMON_COMMAND, ConnectDaemonService.GET_BINDER);
            jIntent.putExtra(ConnectDaemonService.SERVICE_TYPE, ConnectDaemonService.URGENT);
            //Toast.makeText(this, "will send start command to server", Toast.LENGTH_LONG).show();
            startMyService(jIntent);

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
            //aTask.setMsgBox(msgBox);
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
        //myCommand=jobIntent.getExtras().getString(ConnectDaemonService.DAEMON_COMMAND);


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
            //if (urgentMailBox.size() < 1) {
                //urgentMailBox.add(myCommand);
                myService.sendCommand(myCommand);
            /*} else
            {
                String command=null;

                    command = urgentMailBox.get(0);

                if (command != null && command.charAt(0)=='M'){
                    urgentMailBox.remove(0);
                }
            }
            */
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
        //ArrayBlockingQueue<String> msgBox;
        //public void setMsgBox(ArrayBlockingQueue bx){
            //msgBox=bx;
        //}
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

            boolean got_resp=false;
            long endTime=new Date().getTime()+3*60*1000;
            while (!got_resp) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                while (urgentMailBox.size() > 0) {
                    resp = urgentMailBox.remove(0);
                    if (resp.charAt(0) == 'S') {
                        urgentMailBox.clear();
                        return resp;
                    }
                }

                if (new Date().getTime() > endTime) return null;
            }

            return null;
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
