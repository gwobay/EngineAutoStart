/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prod.intelligent7.engineautostart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

//import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class ConnectDaemonService extends Service
        implements TcpConnectDaemon.StatusChangeListener, SensorEventListener
{

    //public static final int NOTIFICATION_ID = 1;
    public static int NOTIFICATION_ID;
    public static final String DAEMON_COMMAND="COMMAND";
    public static final ReentrantLock fileLock=new ReentrantLock(); 
    public static Ringtone noise=null;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Logger log;
    public ConnectDaemonService() {
        super();
    }
    public static final String TAG = "EAS JOB";
    PendingIntent scheduleAlarm;
    AlarmManager alarmManager;
    //@Override
   // protected void onHandleIntent(Intent intent) {
        //onBind(intent);
        //Bundle extras = intent.getExtras();
        //GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        //String messageType = null;//gcm.getMessageType(intent);

        //if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                for (int i = 0; i < 5; i++) {
                    Log.i(TAG, "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.

            } */
           // sendNotification("Received: " + extras.toString());
           // Log.i(TAG, "Received: " + extras.toString());
        //}
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        //EASBroadcastReceiver.completeWakefulIntent(intent);
   // }

    public static String ramFileName=MainActivity.package_name+".profile";
    static TcpConnectDaemon mDaemon=null;
    ArrayBlockingQueue<String> outBoundMailBox;
    static ArrayList<String> urgentMailBox;
    final int mailBoxLimit=200;
    Thread hourlyAlarm;
    boolean stopHourAlarm;

    @Override
    public void onCreate() {
        ramFileName=MainActivity.package_name+".profile";
        log=Logger.getAnonymousLogger();
        log.info(getPackageName()+"Got Activated ");
        serverHeartBit=60*1000;
        urgentMailBox=new ArrayList<String>();
        if (mDaemon==null ||
                !mDaemon.isAlive())
        startDaemon();
        scheduleAlarm=null;
        alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //startScheduledJobs();
        //new screen off handling
        /*
        bugPresent = true;
        screenIsOff = false;
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        screenOnOffReceiver = new ScreenReceiver();
        registerReceiver(screenOnOffReceiver, filter);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        WakeLockManager.acquire(this, "screenBugPartial", PowerManager.PARTIAL_WAKE_LOCK);
        registerAccelerometerListener();
        */
        startScheduleAlarms();
    }
    public static final String SERVER_IP="server_ip";
    public static final String SERVER_PORT="server_port";
    int serverHeartBit;
    void startDaemon()
    {
        mDaemon=null;
        String mHost=getSharedPreferences(MainActivity.package_name+".profile", MODE_PRIVATE).getString(SERVER_IP, "220.134.85.189");
        //start a thread to talk to server every minute

        String mPort=getSharedPreferences(MainActivity.package_name+".profile", MODE_PRIVATE).getString(SERVER_PORT, "9696");
        //start a thread to talk to server every minute
        if (mHost.charAt(0)=='-'){
            mHost=getResources().getString(R.string.prod_server);
            mPort=getResources().getString(R.string.port);
        }
        mDaemon=//TcpConnectDaemon.getInstance(mHost, Integer.parseInt(mPort));//
        new TcpConnectDaemon(mHost, Integer.parseInt(mPort));
        if (mDaemon==null) return;
        mDaemon.setModeInterval(TcpConnectDaemon.MODE_REPEAT, serverHeartBit);

        if (outBoundMailBox==null)
            outBoundMailBox=new ArrayBlockingQueue<String>(mailBoxLimit);
        mDaemon.setOutBoundDataQ(outBoundMailBox);
        /* new change mailbox should be hold by me not the postman
        // so that the box is still there even the man quit
        Vector<String> keep=null;
        if (outBoundMailBox!= null && outBoundMailBox.size() > 0)
        {
            keep=new Vector<String>();
            while (outBoundMailBox.size()> 0)
            {
                try {
                    keep.add(outBoundMailBox.take());
                } catch(InterruptedException e){}
            }
        }
        outBoundMailBox=mDaemon.getOutBoundDataQ();
        if (keep!=null && keep.size() > 0)
        {
            for (int i=0; i<keep.size(); i++){
                try {
                    outBoundMailBox.put(keep.get(i));
                } catch(InterruptedException e){}
            }
            keep.clear();
            keep=null;
        }
        */
        mDaemon.attachToService(this);
        //mDaemon.setUrgentMailBox(urgentMailBox);
        mDaemon.addListener(this);
        mDaemon.start();
        // need start schedule too; MainActivity.N_BOOT_PARAMS, nBootParam); //HH:MM-on minutes-off minutes-cycle last for minutes
        // MainActivity.ONE_BOOT_PARAMS, bootParam); //  yy/mm/dd:hh:mm-last for minutes
    }

    public void onDaemonDying(){
        if (mDaemon!=null && mDaemon.isAlive()){
            try {
                mDaemon.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        startDaemon();
    }
    void confirmDaemonAlive()
    {
        if (mDaemon!=null && mDaemon.isAlive())
        {
            try {
                mDaemon.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mDaemon=null;
        while (mDaemon==null) {
            startDaemon();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        if (intent!=null)
            confirmJob(intent, flags, startId);
        return START_STICKY;//mStartMode;
    }

    public class UrgentMailBinder extends Binder {

        ConnectDaemonService getService()
        {
            return ConnectDaemonService.this;
        }
        ArrayList<String> getUrgentMailBox() {
            // Return this instance of LocalService so clients can call public methods
            return urgentMailBox;
        }
    }

    public void sendCommand(String command){
        if (command == null) return;

        if (command.charAt(0)=='M') {
            putInDaemonOutboundQ(command);
            if (command.charAt(1)=='2' ||
                    command.charAt(1)=='3') {
                serverHeartBit=10*1000;
            }
            urgentMailBox.clear();
            return ;//new LocalBinder();
        }

        if (command.indexOf("DISMISSED") >= 0) { //when no response after timeout
            serverHeartBit=60*1000;
            //if (mDaemon.isAlive()) mDaemon.resetHeartBeatInterval(serverHeartBit);
            return;
        }
        if (command.indexOf("SCHEDULE")>=0 ) {
            reStartScheduleAlarms();
            return;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return confirmJob(intent, 0, 0);
    }

    public static final String ALARM_DONE="alarm_done";

    private static long lastStartTime=0;
    private IBinder confirmJob(Intent intent, int flags, int startId)
    {
        // A client is binding to the service with bindService()
        String command=intent.getExtras().getString(DAEMON_COMMAND);
        //Object obj1=intent.getExtras().getClassLoader();
        //confirmDaemonAlive();
        if (command != null)
        {
            if (command.length()<3) return null;
            if (command.substring(0,2).equalsIgnoreCase("M5"))
            {
                long tm=new Date().getTime();
                if (tm - lastStartTime < 3*60*1000) return null;
                lastStartTime=tm;
            }
            if (command.charAt(0)=='M') {
                putInDaemonOutboundQ(command);
                if (command.charAt(1)=='2' ||
                        command.charAt(1)=='3') {
                    //mDaemon.notUrgent=false;
                    serverHeartBit=10*1000;
                    //mDaemon.resetHeartBeatInterval(serverHeartBit);
                    //startServerHearBeatAlarm();
                }
                urgentMailBox.clear();
                urgentMailBox.add(command);

                return new UrgentMailBinder();//new LocalBinder();
            }
            /* as of now checked inside the mail by send daemon
            if (command.indexOf("URGENT") > 0) {
                if (mDaemon.isAlive()) mDaemon.resetHeartBeatInterval(10*1000);
                return null;
            }*/
            if (command.indexOf("DISMISSED") >= 0) { //when no response after timeout
                serverHeartBit=60*1000;
                //if (mDaemon.isAlive()) mDaemon.resetHeartBeatInterval(serverHeartBit);
                return null;
            }
            if (command.indexOf("SCHEDULE")>=0 ) {
                reStartScheduleAlarms();
                return null;
            }

            /*

            if (command.indexOf(ALARM_DONE) >=0)
            {
                Log.i("ALARM_SET", "Alarm Manager done");
                String
                //startScheduleAlarm();
            }
            else if (command.indexOf(ALARM_BIT) >=0){
                startServerHearBeatAlarm();
            }
            else {
                stopScheduledJobs();
                startScheduledJobs();
            }
            */
        }
        command=intent.getExtras().getString(ALARM_DONE);
        if (command == null) return null;
        PowerManager.WakeLock wlR=((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KEEP_THREAD_ALIVE");
        wlR.acquire();
        switch(command){
            case ALARM_BIT:
                //mDaemon.hasCommand=true;
                mDaemon.interrupt();
                confirmDaemonAlive();
                //startServerHearBeatAlarm();
                break;
            case ALARM_1BOOT:
                String param=intent.getExtras().getString(ALARM_1BOOT);
                long on_time=Long.parseLong(param);
                putInDaemonOutboundQ("M5-" + new DecimalFormat("00").format(on_time / 60000));
                //startOneBootAlarm();
                break;
            case ALARM_NBOOT:
                Log.i("ALARM_SET", "got recurring start intent");
                GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(getResources().getString(R.string.my_time_zone_en)));
                if (gToday.get(Calendar.HOUR_OF_DAY) >= 7 && gToday.get(Calendar.HOUR_OF_DAY) < 19)
                    break;
                String params=intent.getExtras().getString(ALARM_NBOOT);
                int idx=params.indexOf("-");
                long last4=Long.parseLong(params.substring(0, idx));
                putInDaemonOutboundQ("M5-" + new DecimalFormat("00").format(last4 / 60000));
                long off_time=Long.parseLong(params.substring(idx + 1));
                setRecurringBootAlarm(last4, off_time);
                break;
            case ALARM_HOUR:
                //if (heartBitIntent==null) startServerHearBeatAlarm();
                if (oneBootIntent==null) startOneBootAlarm();
                if (recurringBootIntent==null) startRecurringBootAlarm();
                //reStartScheduleAlarms();
                startHourlyCheckAlarm();
                break;
            default:
                break;
        }
        wlR.release();

        return null;//mBinder;
    }

    boolean confirmHasMailBox()
    {
        if (outBoundMailBox==null){
            outBoundMailBox=new ArrayBlockingQueue<String>(mailBoxLimit);
            if (mDaemon!=null)
                mDaemon.setOutBoundDataQ(outBoundMailBox);
        }
        //outBoundMailBox=mDaemon.getOutBoundDataQ();
        return true;
    }

    public void putInDaemonOutboundQ(String msg){
        if (!confirmHasMailBox())
            return;
        final String outMsg=msg;
        final TcpConnectDaemon toWakeUp=mDaemon;

        new Thread(new Runnable(){
            public void run()
            {
                if (outBoundMailBox.size() == TcpConnectDaemon.Q_SIZE){
                    log.warning("Warning : too many msg in my Q");
                    return;
                }
                try {
                    outBoundMailBox.put(outMsg);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //toWakeUp.hasCommand=true;
                //toWakeUp.interrupt();
                confirmDaemonAlive();
            }
        }).start();
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()

        return false;//mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    public interface UrgentMailListener {
        public void processMail(String msg);
    }

    public static class UrgentMailRegister {
        private static int instances = 0;
        private static UrgentMailListener myUrgentMailListener = null;

        private UrgentMailRegister() {
        };

        public static boolean register(UrgentMailListener aL) {
            if (instances > 0)
                return false;
            myUrgentMailListener = aL;
            instances = 1;
            return true;
        }

        public static boolean unRegister(UrgentMailListener aL) {
            if (instances < 1)
                return false;
            if (myUrgentMailListener != aL)
                return false;
            myUrgentMailListener = null;
            instances = 0;

            return true;
        }

        public static UrgentMailListener getMyUrgentMailListener() {
            return myUrgentMailListener;
        }
    }
    public void processUrgentMsg(String msg)
    {

            urgentMailBox.add(msg);

        serverHeartBit=60*1000;
    }

    public void processUrgentMsgOld(String msg)
    {
        final UrgentMailListener aL=UrgentMailRegister.getMyUrgentMailListener();
        if (aL==null) return;
        final String data=msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                aL.processMail(data);
            }
        }).start();
        serverHeartBit=60*1000;
    }

    static int alarmRequestId=0;
    static int heartBitRequestId=1000;  //range set between 1000 ~ 1099
    static PendingIntent heartBitIntent;
    public static final String ALARM_BIT="alarm_heart_bit";


    public void startServerHearBeatAlarm(){
        if (heartBitIntent != null ) {
            alarmManager.cancel(heartBitIntent);
            //return;
        }
        heartBitIntent=null;
        //serverHeartBit=60*1000;
        //if (!mDaemon.notUrgent) serverHeartBit=10*1000;
        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.putExtra(ConnectDaemonService.ALARM_DONE, ALARM_BIT);
        heartBitIntent=PendingIntent.getService(this, ++heartBitRequestId % 100 + 1000, jIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+serverHeartBit, heartBitIntent);
        Log.i("ALARM_SET", "restart new alarm set for "+serverHeartBit/1000+ " seconds");
        //mDaemon.hasCommand=true;
        //mDaemon.interrupt();
    }

    static int boot1AlarmRequestId=0; //range set between 150 ~ 159
    static PendingIntent oneBootIntent;
    public static final String ALARM_1BOOT="ALARM_1BOOT";

    void startOneBootAlarm()
    {
        if (oneBootIntent != null ) {
            alarmManager.cancel(oneBootIntent);
            //return;
        }
        oneBootIntent=null;
        String bootParameter=readBootParameter(1);
        Log.i("ALARM_SET", "got 1 boot parameters " + (bootParameter==null?"nothing":bootParameter));
        if (bootParameter==null)return;
        int idx=bootParameter.indexOf("-");
        if (idx < 0) return;
        long init_wait=Long.parseLong(bootParameter.substring(0, idx));
        if (init_wait < 10000){
            long on_time=Long.parseLong(bootParameter.substring(idx+1));
            putInDaemonOutboundQ("M5-" + new DecimalFormat("00").format(on_time / 60000));
            return;
        }
        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.setAction(ALARM_1BOOT);
        jIntent.putExtra(ConnectDaemonService.ALARM_DONE, ALARM_1BOOT);
        jIntent.putExtra(ConnectDaemonService.ALARM_1BOOT, bootParameter.substring(idx+1));
        oneBootIntent=PendingIntent.getService(this, ++boot1AlarmRequestId % 10 + 150, jIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+init_wait, oneBootIntent);
        Log.i("ALARM_SET", "to start one boot in " + init_wait / 60000);
        //startScheduledJobs();
    }

    static int nBootAlarmRequestId=1000; //range set between 200 ~ 299
    static PendingIntent recurringBootIntent;
    public static final String ALARM_NBOOT="ALARM_NBOOT";

    void startRecurringBootAlarm()
    {
        if (recurringBootIntent != null ) {
            alarmManager.cancel(recurringBootIntent);
            //return;
        }
        recurringBootIntent=null;
        GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(getResources().getString(R.string.my_time_zone_en)));
        if (gToday.get(Calendar.HOUR_OF_DAY) >= 7 && gToday.get(Calendar.HOUR_OF_DAY) < 19)
            return;
        String bootParameter=readBootParameter(99);
        Log.i("ALARM_SET", "got n boot parameters " + (bootParameter==null?"nothing":bootParameter));
        if (bootParameter==null)return;
        int idx=bootParameter.indexOf("-");
        if (idx < 0) return;
        long init_wait=Long.parseLong(bootParameter.substring(0, idx));
        if (init_wait < 2000) //init_wait=2000;
        {
            int ixx=bootParameter.indexOf("-", idx+1);
            long on_time=Long.parseLong(bootParameter.substring(idx+1, ixx));
            putInDaemonOutboundQ("M5-" + new DecimalFormat("00").format(on_time / 60000));

            long off_time=Long.parseLong(bootParameter.substring(ixx+1));
            setRecurringBootAlarm(on_time, off_time);
            return;
        }
        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.setAction(ALARM_NBOOT);
        jIntent.putExtra(ConnectDaemonService.ALARM_DONE, ALARM_NBOOT);
        jIntent.putExtra(ConnectDaemonService.ALARM_NBOOT, bootParameter.substring(idx+1));
        recurringBootIntent=PendingIntent.getService(this, ++nBootAlarmRequestId %100 + 200, jIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+init_wait, recurringBootIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + init_wait,
                //on_time + off_time, recurringBootIntent);
        //Log.i("ALARM_SET", "to start recurring boot in " + init_wait/60000);//+" with interval "+(on_time+off_time)/60000);
        Log.i("ALARM_SET", "to start recurring boot in " + init_wait / 60000);
        //startScheduledJobs();
    }

    public static final String NBOOT_PARAM="NBOOT_PARAM";

    void setRecurringBootAlarm(long on_time, long idle_interval)
    {
        if (recurringBootIntent != null ) {
            alarmManager.cancel(recurringBootIntent);
            //return;
        }
        recurringBootIntent=null;
        GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(getResources().getString(R.string.my_time_zone_en)));
        if (gToday.get(Calendar.HOUR_OF_DAY) >= 7 && gToday.get(Calendar.HOUR_OF_DAY) < 21)
            return;
        long total_wait=on_time+idle_interval;
        String bootParameter=""+on_time+"-"+idle_interval;
        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.setAction(ALARM_NBOOT);
        jIntent.putExtra(ConnectDaemonService.ALARM_DONE, ALARM_NBOOT);
        jIntent.putExtra(ConnectDaemonService.ALARM_NBOOT, bootParameter);
        recurringBootIntent=PendingIntent.getService(this, ++nBootAlarmRequestId % 100 +300, jIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + total_wait, recurringBootIntent);
        Log.i("ALARM_SET", "to start recurring boot in " + total_wait / 60000);
        //startScheduledJobs();
    }

    void reStartScheduleAlarms()
    {
        //startServerHearBeatAlarm();
        startOneBootAlarm();
        startRecurringBootAlarm();
    }

    void startScheduleAlarms()
    {
        //startServerHearBeatAlarm();
        startOneBootAlarm();
        startRecurringBootAlarm();
        startHourlyCheckAlarm();
    }


    public static final String ALARM_HOUR="alarm_hour";
    void startHourlyCheckAlarm()
    {
        if (scheduleAlarm != null ) {
            alarmManager.cancel(scheduleAlarm);
            //return;
        }
        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.putExtra(ConnectDaemonService.ALARM_DONE, ALARM_HOUR);
        jIntent.setAction(ALARM_HOUR);
        scheduleAlarm=PendingIntent.getService(this, ++alarmRequestId % 100 + 500, jIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, 1 * 60 * 60 * 1000 + System.currentTimeMillis() , scheduleAlarm);
        Log.i("ALARM_SET", "restart check alarm set for 1 hour");
        //startScheduledJobs();
    }

    void startScheduleAlarm()
    {
        if (scheduleAlarm != null ) {
            alarmManager.cancel(scheduleAlarm);
            //return;
        }
        Intent jIntent=new Intent(this, ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.putExtra(ConnectDaemonService.ALARM_DONE, ALARM_DONE);
        scheduleAlarm=PendingIntent.getService(this, ++alarmRequestId, jIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, 1*60*60*1000+System.currentTimeMillis(), scheduleAlarm);
        Log.i("ALARM_SET", "restart new alarm set for 1 hour");
        startScheduledJobs();
    }

    public String readBootParameter(int mode_1_n)
    {
        String retS=null;
        GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(getResources().getString(R.string.my_time_zone_en)));
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.package_name + ".profile", Context.MODE_PRIVATE);
        String param=sharedPref.getString(MainActivity.ONE_BOOT_PARAMS, "--");
        String paramKey=MainActivity.SET_MULTIPLE_BOOT;
        if (mode_1_n == 1)
            paramKey=MainActivity.SET_ONE_BOOT;

        Set<String> aSet=sharedPref.getStringSet(paramKey, null);
        if (aSet == null) {
            return null;
        }
        String[] allData=new String[aSet.size()];
        allData=aSet.toArray(allData);
        String todayName=ScheduleActivity.weekDays[(gToday.get(Calendar.DAY_OF_WEEK)+6) % 7];//ScheduleActivity.weekDays[gToday.get(Calendar.DAY_OF_WEEK)-1];
        String scheduleDay=todayName;
        param=null;
        for (int i=0; i<aSet.size(); i++){
            if (allData[i].indexOf(scheduleDay)<0) continue;
            param=allData[i].replace('>', '-');  //saved data format change to weekdayName>HH:MM-last4
            //data has form hh:mm-active period
            break;
        }
        int hrNow=gToday.get(Calendar.HOUR_OF_DAY);
        if (param==null){
            if (mode_1_n == 1 || hrNow > 7) return null;
        }
        if (mode_1_n != 1 && hrNow < 7){
            if (param != null){
                int idd=param.indexOf("-");
                int h0=Integer.parseInt(param.substring(idd+1, idd+3));
                if (h0 > 7) param=null;
            }

            scheduleDay=ScheduleActivity.weekDays[(gToday.get(Calendar.DAY_OF_WEEK)+5) % 7]; //check if scheduled yesterday
            if (param==null) {
                for (int i = 0; i < aSet.size(); i++) {
                    if (allData[i].indexOf(scheduleDay) < 0) continue;
                    param = allData[i].replace('>', '-');
                    break;
                }
                if (param == null) {
                    return null;
                }
                int idd=param.indexOf("-");
                int h0=Integer.parseInt(param.substring(idd+1, idd+3));
                if (h0 < 21) {
                    return null;
                }
                hrNow += 24;
                //hrBase = 24;
            }
        }


        String[] terms=param.split("-");
        int icx=terms[1].indexOf(":");
        if (icx < 0) {
            Log.w("SCHEDULE_JOB", "!!! Bad formated start time " + param+" as new schedule");
            return null;
        }
        int hrStart=Integer.parseInt(terms[1].substring(0,icx)); //starting hour
        int minStart=Integer.parseInt(terms[1].substring(icx + 1));
        int minNow=gToday.get(Calendar.MINUTE);

        long init_wait=((hrStart-hrNow)*60+(minStart-minNow))*60*1000;
        //in milli secs
        long on_time=60*1000*Integer.parseInt(terms[2]);
        //if (init_wait>0)
        if (mode_1_n == 1) {
            if (init_wait < -2 * 60 * 1000) return null;
            if (init_wait < 0) init_wait = 0;
            if (on_time < 1) return null;
            //if (init_wait >3605000) return null;
            retS = "" + init_wait + "-" + on_time ;
            return retS;
        }

        long off_time=1000*3600*Integer.parseInt(terms[3]); //in milli secs

        while (init_wait < 0){
            init_wait += (on_time+off_time);
        }

        retS = "" + init_wait + "-" + on_time + "-" +off_time;
        return retS;
    }

    public static final String[] weekDays={"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    boolean killScheduledJob;
    OneTimeScheduler oneBootJob;

    ContinueScheduler recurringJob;

    public void startScheduledJobs()
    {
        killScheduledJob=false;
        //startScheduleAlarm();
        if (oneBootJob== null || !oneBootJob.isAlive()){
        oneBootJob=new OneTimeScheduler(this, mDaemon);
        oneBootJob.start();
        }
        if (recurringJob == null || !recurringJob.isAlive()){
            recurringJob=new ContinueScheduler(this, mDaemon);
        recurringJob.start();
        }
    }

    public void stopScheduledJobs()
    {
        killScheduledJob=true;
        if (oneBootJob!= null && oneBootJob.isAlive())
            oneBootJob.killJob();
        if (recurringJob!= null && recurringJob.isAlive())
            recurringJob.killJob();
        try {
            oneBootJob.join();
            recurringJob.join();
            Log.i("SCHEDULE_JOB", "STOPPed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resetScheduledJobs()
    {
        if (oneBootJob!= null && oneBootJob.isAlive())
            oneBootJob.killJob();
        if (recurringJob!= null && recurringJob.isAlive())
            recurringJob.killJob();

        new Thread(new Runnable() {
            @Override
            public void run() {

                    try {
                        //Thread.sleep(10000L);
                        recurringJob.join();
                        oneBootJob.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                startScheduledJobs();
            }
        }).start();
    }

    int saveData(String tblName, String fixLine)
    {
        String fileName="notification"+tblName;
        int iPending=0;
        Set<String> nullSet=null;
        synchronized(fileLock){
            SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
            Set<String> pendingData=sharedPref.getStringSet("notification", nullSet);
            if (pendingData==null)
                pendingData=new HashSet<String>();
            pendingData.add(fixLine);
            SharedPreferences.Editor adder=sharedPref.edit();
            adder.putStringSet("notification", pendingData);
            adder.commit();
            iPending=pendingData.size();
        }
        return iPending;
    }

    public static class MyTime{
        int year;
        int month;
        int day;
        int hour;
        int minute;
        public MyTime(GregorianCalendar gc)
        {
            year=gc.get(Calendar.YEAR);
            month=gc.get(Calendar.MONTH)+1;
            day=gc.get(Calendar.DAY_OF_MONTH);
            hour=gc.get(Calendar.HOUR_OF_DAY);
            minute=gc.get(Calendar.MINUTE);
        }
        public MyTime()
        {
            GregorianCalendar gc=new GregorianCalendar();
            year=gc.get(Calendar.YEAR);
            month=gc.get(Calendar.MONTH)+1;
            day=gc.get(Calendar.DAY_OF_MONTH);
            hour=gc.get(Calendar.HOUR_OF_DAY);
            minute=gc.get(Calendar.MINUTE);
        }
    }
    boolean car_theft;
    void makeNoise()
    {
        if (car_theft)
        {
            noise = RingtoneManager.getRingtone(this, Settings.System.DEFAULT_RINGTONE_URI);
            noise.play();
            return;
        }


        boolean toRing=false;
        //String fileName=MainActivity.getFileHeader()+ProfilePage.getTableName();
        String fileName=MainActivity.package_name+".profile";
        SharedPreferences mem = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String sRing=mem.getString(PickActivity.CURRENT_RINGTON, "--");//, String);
        if (sRing.charAt(0) == '-') return;
        Uri ringUri=Uri.parse(sRing);//, String);
        String t0=mem.getString(PickActivity.NO_NOISE_START, "--");//, noNoiseStart);
        String t1=mem.getString(PickActivity.NO_NOISE_END, "--");//, noNoiseEnd);
        String[] sT0=t0.split(":");
        int h0=Integer.parseInt(sT0[0]);
        int m0=Integer.parseInt(sT0[1]);
        sT0=t1.split(":");
        int h1=Integer.parseInt(sT0[0]);
        int m1=Integer.parseInt(sT0[1]);
        MyTime tm=new MyTime(new GregorianCalendar(TimeZone.getTimeZone(getResources().getString(R.string.my_time_zone_en))));
        int iNow=tm.hour*60+tm.minute;
        int iU=h1*60+m1;
        int iL=h0*60+m0;
        if (h1 < h0)
        {
            toRing=(iNow > iU && iNow < iL );
        }
        else
            toRing=(iNow > iU || iNow < iL);
        //android.os.Debug.waitForDebugger();
        if (toRing)
        {
            noise=RingtoneManager.getRingtone(getApplicationContext(), ringUri);
            noise.play();
        }   else noise=null;
    }
    // Put the message into a notification and post it.

    private static HashMap<String, String> mcuDictionary;
    static public HashMap<String, String> getMcuCodeDictionary(){

        if (mcuDictionary==null) buildCodeDictionary();
        return mcuDictionary;
    }

    public static String getChinese(String code){
        String retS="";
        if (mcuDictionary==null) buildCodeDictionary();
        return retS+mcuDictionary.get(code);
    }
    private static void buildCodeDictionary() {
        mcuDictionary=new HashMap<String, String>();
        mcuDictionary.put("M1-00","冷气启动");
        mcuDictionary.put("M1-01","暖气启动");
        mcuDictionary.put("M2","车载机密码更换");
        mcuDictionary.put("M3","手机號码设定");
        mcuDictionary.put("M4-00","立即关闭引擎");
        mcuDictionary.put("M4-01","立即关闭冷气");
        mcuDictionary.put("M5","立即启动");
        //mcuDictionary.put("M5","立即启动");

        mcuDictionary.put("S110", "暖气设定成功");

        mcuDictionary.put("S111", "暖气设定失败");

        mcuDictionary.put("S100", "冷氣設定成功");

        mcuDictionary.put("S101", "冷氣設定失敗");

        mcuDictionary.put("S200", "车载机密码设定成功");

        mcuDictionary.put("S201", "车载机密码设定失败");

        mcuDictionary.put("S300", "手机号码设定成功");

        mcuDictionary.put("S301", "手机号码设定失败");

        mcuDictionary.put("S400", "引擎已关闭");

        mcuDictionary.put("S401", "引擎由车主启动,不能关闭");

        mcuDictionary.put("S410", "冷氣已關閉");

        mcuDictionary.put("S411", "冷氣關閉失敗");

        mcuDictionary.put("S500", "引擎已启动");

        mcuDictionary.put("S501", "引擎启动失败");

        mcuDictionary.put("S502", "引擎启动成功");

        mcuDictionary.put("S503", "偷车");

        mcuDictionary.put("S504", "暖车失败");

        mcuDictionary.put("S505", "暖车完毕");

        mcuDictionary.put("S999", "手机号码未授权");
    }


    //static //String header=MainActivity.getFileHeader();

    public void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        car_theft=false;
        if (msg.indexOf("505")>0)
            car_theft=true;
        makeNoise();
        String msgShow=msg;
        //msg :   msg in SXX@time<sender> format
        //SXX need translation from code to simplified chinese

        int i0=msg.indexOf("@");
        int idx=msg.indexOf("<");
        if (i0>0) msgShow=msg.substring(0, i0);
        else if (idx>0) msgShow=msg.substring(0, idx);
        String chinese=getChinese(msgShow);
        if (chinese==null) chinese="DAEMON STATUS";
        if (msg.toLowerCase().indexOf("finish") > 0)
        {
            startDaemon();
        }

        Intent jobIntent=null;//=new Intent(this, MainActivity.class);

        jobIntent=new Intent(this, MainActivity.class);//main will invoke other activities
        String tblName="";
        int iPending=1;



        jobIntent.putExtra("MCU_RESP", "EAS>>"+chinese);


        String header="EAS";//getResources().getString(R.string.candidate_name);
        String MSGs="EAS";//getResources().getString(R.string.msg_for_you);

        //DisplayMetrics metrics = new DisplayMetrics();
        //((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        //.setLargeIcon(Bitmap.createBitmap(metrics,80, 80, Bitmap.Config.ARGB_4444))//
                        .setSmallIcon(R.drawable.engine_auto_start)
                        .setContentTitle(header + "(" + iPending + ")")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msgShow))
                        .setContentText(chinese)
                        .setLights(0xFF0000, 2000, 5000)
                        .setAutoCancel(true);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, jobIntent, PendingIntent.FLAG_UPDATE_CURRENT); //the intent to open when clicked

        mBuilder.setContentIntent(contentIntent); //so MainActivity will be opened when notification is clicked
        mNotificationManager.notify(getPackageName().hashCode(), mBuilder.build());

        if (noise != null)
        {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            noise.stop();
            noise=null;
        }
    }

    public void localNotification(String msg)
    {
        sendNotification(msg);
    }

    private final class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                serviceHandler.postDelayed(setScreenIsOffRunnable, 6666);
                serviceHandler.postDelayed(turnScreenOnFallbackRunnable, 10000);
            }
        }
    }

    public static final String BUG_NOT_PRESENT = "BUG_NOT_PRESENT";

    public static final String BUG_PRESENT = "BUG_PRESENT";

    private boolean bugPresent = true;
    boolean didNotTurnScreenOn = true;
    private PowerManager powerManager;
    private boolean screenIsOff = false;

    private ScreenReceiver screenOnOffReceiver;
    Handler serviceHandler = new Handler();

    Runnable setScreenIsOffRunnable = new Runnable() {
        @Override
        public void run() {
            screenIsOff = true;
        }
    };

    Runnable turnScreenOnFallbackRunnable = new Runnable() {
        @Override
        public void run() {
            if (bugPresent) {
                //CheckForScreenBugActivity.BUG_PRESENT_INTENT = new Intent(BUG_PRESENT);
            }
            turnScreenOn();
        }
    };

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // Not used.
    }

    @Override
    public void onDestroy() {

        unregisterAccelerometerListener();

        unregisterReceiver(screenOnOffReceiver);

        serviceHandler.removeCallbacks(setScreenIsOffRunnable);
        serviceHandler.removeCallbacks(turnScreenOnFallbackRunnable);

        // check here so that certain devices keep their screen on for at least
        // 5 seconds (from turnScreenOn)
        if (didNotTurnScreenOn) {
            WakeLockManager.release("screenBugPartial");
            WakeLockManager.release("screenBugDim");
        }
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (!powerManager.isScreenOn() && screenIsOff && bugPresent) {
            //CheckForScreenBugActivity.BUG_PRESENT_INTENT = new Intent(BUG_NOT_PRESENT);
            turnScreenOn();
        }
    }

    private void registerAccelerometerListener() {
        final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void turnScreenOn() {
        didNotTurnScreenOn = false;
        WakeLockManager.acquire(this, "screenBugDim", PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, 5000);
        //stopSelf();
    }

    private void unregisterAccelerometerListener() {
        final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    Handler myHandler;

    long onTime;
    long idleInterval;
    long initWaitTime;
    static final DecimalFormat dF=new DecimalFormat("00");

    Runnable startOneTime=new Runnable() {
        final long last4=onTime;
        @Override
        public void run() {
            putInDaemonOutboundQ("M5-"+dF.format(last4));
            myHandler.removeCallbacks(this);
        }
    };

    Runnable startNTime=new Runnable() {
        final long last4=onTime;
        final long idleTime=idleInterval;
        @Override
        public void run() {
            putInDaemonOutboundQ("M5-"+dF.format(last4));
            myHandler.postDelayed(this, last4+idleTime);
        }
    };

    void postOneBoot(){
        myHandler.removeCallbacks(startOneTime);
        String oneBootParameters=readBootParameter(1);
        String[] params=oneBootParameters.split("-");
        initWaitTime=Long.parseLong(params[0]);
        if (initWaitTime > 3600*1000) return;
        onTime=Long.parseLong(params[1]);
        myHandler.postDelayed(startOneTime, initWaitTime);
    }

    void postNBoots(){
        myHandler.removeCallbacks(startNTime);
        GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(getResources().getString(R.string.my_time_zone_en)));
        if (gToday.get(Calendar.HOUR_OF_DAY) > 7 && gToday.get(Calendar.HOUR_OF_DAY) < 19)
            return;
        String bootParameters=readBootParameter(99);
        String[] params=bootParameters.split("-");
        initWaitTime=Long.parseLong(params[0]);
        if (initWaitTime > 3610*1000) return;
        onTime=Long.parseLong(params[1]);
        idleInterval=Long.parseLong(params[2]);
        myHandler.postDelayed(startNTime, initWaitTime);
    }

    void rePostSchedule(){
        postOneBoot();
        postNBoots();
    }
}

    class OneTimeScheduler extends JobScheduler {
        // MainActivity.ONE_BOOT_PARAMS, bootParam); //  yyyy/mm/dd-hh:mm-last for minutes
        public OneTimeScheduler(ConnectDaemonService cs, TcpConnectDaemon dm){
            super(cs, dm);
        }
        @Override
        protected void readParameter()
        {
            GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(mContext.getResources().getString(R.string.my_time_zone_en)));
            SharedPreferences sharedPref = mContext.getSharedPreferences(MainActivity.package_name + ".profile", Context.MODE_PRIVATE);
            String param=sharedPref.getString(MainActivity.ONE_BOOT_PARAMS,"--");
            Set<String> aSet=sharedPref.getStringSet(MainActivity.SET_ONE_BOOT, null);
            if (aSet == null) {
                last4=-1; return;
            }
            String[] allData=new String[aSet.size()];
            allData=aSet.toArray(allData);
            String todayName=ScheduleActivity.weekDays[(gToday.get(Calendar.DAY_OF_WEEK)+6) % 7];//ScheduleActivity.weekDays[gToday.get(Calendar.DAY_OF_WEEK)-1];
            param=null;
            for (int i=0; i<aSet.size(); i++){
                if (allData[i].indexOf(todayName)<0) continue;
                param=allData[i].replace('>', '-');  //saved data format change to weekdayName>HH:MM-last4
                //data has form hh:mm-active period
                break;
            }
            if (param==null){
                last4=-1; return;
            }
            String[] terms=param.split("-");

                    int iH=Integer.parseInt(terms[1].substring(0,2));
                    int iM=Integer.parseInt(terms[1].substring(3));
                    int iHr=gToday.get(Calendar.HOUR_OF_DAY);
                    int iMin=gToday.get(Calendar.MINUTE);

                    init_wait=((iH-iHr)*60+(iM-iMin))*60*1000;
                    //in milli secs
                    on_time=60*1000*Integer.parseInt(terms[2]);
                    //if (init_wait>0)
                    last4=on_time;
                    if (init_wait < -1*60*1000) on_time=-1;

                    //in milli secs
                    //last4=init_wait+end_time;
        }

        public String readBootParameter()
        {
            String retS=null;
            readParameter();
            if ((on_time < 1 || init_wait < 0)) return null;
            if (init_wait >3660000) return null;
            retS=""+init_wait+"-"+on_time;
            return retS;
        }
        public void setResetStatus(boolean ya)
        {
            isWakenByReset=ya;
        }

        public void run()
        {
            boolean okStart=false;
            boolean iWasReset=true;
            if (wlR.isHeld()) wlR.release();
            try {
                    readParameter();
                    //iWasReset=false;
                    if (on_time < 1 || init_wait < 0){
                        Log.i("SCHEDULE_JOB", "One Time Start not schedule "+ on_time+" and "+ init_wait+" !!!!");
                        return;
                    }
                    if (init_wait >3605000) {
                        Log.i("SCHEDULE_JOB", "One Time Start one hour away, not scheduled "+ init_wait+" !!!!");
                        return;
                    }
                    if (init_wait>0){
                        Log.i("SCHEDULE_JOB", getId()+" One Time Start sleep " + init_wait / 60000 + " mins. to Sent!");
                        wlR.acquire(init_wait+6000);
                    sleep(init_wait);
                        wlR.release();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //if (!killScheduledJob)
                    //((ConnectDaemonService)mContext).resetScheduledJobs();
                Log.i("SCHEDULE_JOB", "One Time schedule Cancelled !!!!");
                    return;
                }


                sendStartCommand(on_time/60000);
            Log.i("SCHEDULE_JOB", "command to start engine for " + on_time / 60000 + " mins. Sent!");
/*
            try {
                    sleep(on_time);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();

                    //sendStopCommand();
                    //if (!killScheduledJob)
                    //((ConnectDaemonService)mContext).resetScheduledJobs();
                    return;
                }
                //long next_time=start_time+on_time+off_time;
                //sendStopCommand();//in case it is still running
                */
        }
    }

    class ContinueScheduler extends JobScheduler {
    // need start schedule too; MainActivity.N_BOOT_PARAMS, nBootParam); //HH:MM-on minutes-off minutes-cycle last for minutes

        /*boolean isWakenByReset=false;
        public void refresh(){

        }
        long start_time;
        long on_time;
        long off_time;
        long end_time;
        */
        public ContinueScheduler(ConnectDaemonService cs, TcpConnectDaemon dm){
            super(cs, dm);
        }

        GregorianCalendar gToday;


        @Override
        protected void readParameter()
        {
            GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(mContext.getResources().getString(R.string.my_time_zone_en)));
            SharedPreferences sharedPref = mContext.getSharedPreferences(MainActivity.package_name + ".profile", Context.MODE_PRIVATE);
            String param=sharedPref.getString(MainActivity.N_BOOT_PARAMS,"--");
            Set<String> aSet=sharedPref.getStringSet(MainActivity.SET_MULTIPLE_BOOT, null);
            if (aSet == null) {
                last4=-1; return;
            }
            String[] allData=new String[aSet.size()];
            allData=aSet.toArray(allData);
            String todayName=ScheduleActivity.weekDays[(gToday.get(Calendar.DAY_OF_WEEK)+6) % 7];
            String yesterday=ScheduleActivity.weekDays[(gToday.get(Calendar.DAY_OF_WEEK)+5) % 7];
            int hrNow=gToday.get(Calendar.HOUR_OF_DAY);
            param=null;
            for (int i = 0; i < aSet.size(); i++) {
                    if (allData[i].indexOf(todayName) < 0) continue;
                    param = allData[i].replace('>', '-');  //saved data format change to weekdayName>HH:MM-last4
                    //data has form hh:mm-active period
                    break;
            }
            if (hrNow > 7 && param==null){last4=-1; return;}
            int hrBase=0;
            String param0=null;
            if (hrNow < 7){
                if (param != null){
                    int idd=param.indexOf("-");
                    int h0=Integer.parseInt(param.substring(idd+1, idd+3));
                    if (h0 > 7) param=null;
                }
                if (param==null) {
                    for (int i = 0; i < aSet.size(); i++) {
                        if (allData[i].indexOf(yesterday) < 0) continue;
                        param = allData[i].replace('>', '-');
                        break;
                    }
                    if (param == null) {
                        last4 = -1;
                        return;
                    }
                    int idd=param.indexOf("-");
                    int h0=Integer.parseInt(param.substring(idd+1, idd+3));
                    if (h0 < 21) {
                        last4 = -1;
                        return;
                    }
                    hrBase = 24;
                }
            }

            Log.i("SCHEDULE_JOB", "found parameters " + param+" as new schedule");
            String[] terms=param.split("-");
            int icx=terms[1].indexOf(":");
            if (icx < 0) {
                last4=-1;
                return;
            }
            int hrStart=Integer.parseInt(terms[1].substring(0,icx)); //starting hour
            int minStart=Integer.parseInt(terms[1].substring(icx+1));
            hrNow += hrBase;
            //int hrEnd=7+hrBase;

            int minNow=gToday.get(Calendar.MINUTE);

            on_time=1000*60*Integer.parseInt(terms[2]); //in milli secs
            off_time=1000*3600*Integer.parseInt(terms[3]); //in milli secs

            init_wait=((hrStart-hrNow)*60+(minStart-minNow))*60*1000; //in milli secs
            while (init_wait < 0){
                init_wait += (on_time+off_time);
            }
            //change the end time to 07:00AM
            //int endHr=7;
           // if (iH > 7) endHr=7+24;
            //end_time=((endHr-iH)*60+(0-iM))*60*1000;
            //end_time=1000*60*Integer.parseInt(terms[3]); //in milli secs
            //last4=init_wait+end_time;
            last4=1000;
        }

        public String readBootParameter()
        {
            String retS=null;
            readParameter();
            if (last4 < 0) return null;
            if ((on_time < 1 || init_wait < 0)) return null;
            //if (init_wait >3605000) return null;
            retS=""+init_wait+"-"+on_time+"-"+off_time;
            return retS;
        }

        public void setResetStatus(boolean ya)
        {
            isWakenByReset=ya;
        }

        public void run()
        {
            String jobReport="";
            if (wlR.isHeld()) wlR.release();
            gToday=new GregorianCalendar(TimeZone.getTimeZone(mContext.getResources().getString(R.string.my_time_zone_en)));
            int hrNow=gToday.get(Calendar.HOUR_OF_DAY);
            if (hrNow > 7 && hrNow < 19) {
                Log.i("SCHEDULE_JOB", "recurring not schedule !!!!");
                return;
            }
            boolean okStart=false;
            boolean iWasReset=true;
            try {
                    readParameter();
                    //iWasReset=false;
                    if (last4 < 0) {
                        Log.i("SCHEDULE_JOB", "recurring not schedule !!!!");
                        return;
                    }
                    if (init_wait>0) {
                        Log.i("SCHEDULE_JOB", getId()+" sleep " + init_wait / 60000 + " mins. to Sent!");
                        wlR.acquire();
                        sleep(init_wait);
                        wlR.release();
                    iWasReset=false;
                    //last4 -= init_wait;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                //if (!killScheduledJob)
                    //((ConnectDaemonService)mContext).resetScheduledJobs();
                Log.i("SCHEDULE_JOB", "recurring schedule "+jobReport+" !!!!");
                    return;
                }

           gToday=new GregorianCalendar(TimeZone.getTimeZone(mContext.getResources().getString(R.string.my_time_zone_en)));
            int thisHr=gToday.get(Calendar.HOUR_OF_DAY);
            int endHr=7;
            if (thisHr >7) endHr=31;

                last4=(endHr - thisHr)*60+(0-gToday.get(Calendar.MINUTE));
                last4 *= 60000;

            start_time=new Date().getTime();
            end_time = start_time+last4;

            while (start_time < end_time)
            {    /*pending*/
                sendStartCommand(on_time/60000); //change to minute
                Log.i("SCHEDULE_JOB", "recurring command to start engine for "+on_time/60000+" mins. Sent!");
                try {
                    sleep(on_time);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                    jobReport="CANCELLED";
                    //sendStopCommand();
                    //if (!killScheduledJob)
                    //((ConnectDaemonService)mContext).resetScheduledJobs();
                    Log.i("SCHEDULE_JOB", getId()+" recurring schedule "+jobReport+" !!!!");
                    return;
                }
                //long next_time=start_time+on_time+off_time;
                //sendStopCommand();//in case it is still running
                try {
                    Log.i("SCHEDULE_JOB", getId()+" recurring command let engine idle for "+off_time/60000+" mins. Sent!");
                    sleep(off_time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //if (!killScheduledJob)
                    //((ConnectDaemonService)mContext).resetScheduledJobs();
                    jobReport="CANCELLED";
                    Log.i("SCHEDULE_JOB", "recurring schedule "+jobReport+" !!!!");
                    return;
                }
                start_time = new Date().getTime();
                jobReport="FINISHED";
            }
            Log.i("SCHEDULE_JOB", "recurring schedule "+jobReport+" !!!!");
        }
    }

