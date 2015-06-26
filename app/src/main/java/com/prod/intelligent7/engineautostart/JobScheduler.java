package com.prod.intelligent7.engineautostart;

import android.app.AlarmManager;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by eric on 2015/6/7.
 */
public class JobScheduler extends Thread{
    // need start schedule too; MainActivity.N_BOOT_PARAMS, nBootParam); //HH:MM-on minutes-off minutes-cycle last for minutes

    Context mContext;
    boolean isWakenByReset=false;
    public void refresh(){

    }

    public long getToday0Sec()
    {
        //AlarmManager am = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(mContext.getResources().getString(R.string.my_time_zone_en)));
        int iHr=gToday.get(Calendar.HOUR_OF_DAY);
        int iMin=gToday.get(Calendar.MINUTE);
        int iSec=gToday.get(Calendar.SECOND);
        return gToday.getTimeInMillis()-(iHr*3600+iMin*60+iSec)*1000;
    }
    protected long init_wait;
    protected long start_time;
    protected long on_time;
    protected long off_time;
    protected long last4;
    protected long end_time;
    protected String myToken;
    protected TcpConnectDaemon workDaemon;
    protected PowerManager.WakeLock wlR;
    boolean killScheduledJob;
    public JobScheduler(Context cx, TcpConnectDaemon dm){
        mContext=cx;
        workDaemon = dm;
        killScheduledJob=false;
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wlR = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KEEP_SCHEDULE_ALIVE");
        //PowerManager.WakeLock wl1 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ONE_TIME");
        Log.w("SCHEDULE_JOB", "SHOULD NOT BE CALLED");
    }

    public void killJob(){
        killScheduledJob=true;
        interrupt();
    }
    public void setParameters(long init_wait_t, long on_t, long off_t, long forHowLong){
        init_wait=init_wait_t;
        on_time=on_t;
        off_time=off_t;
        end_time=forHowLong;
    }
    protected void sendStartCommand(long forHowLong){
        if (forHowLong > 0)
            ((ConnectDaemonService)mContext).sendCommand("M5-" + new DecimalFormat("00").format(forHowLong)); //in minute
    }
    protected void sendStopCommand(){
        ((ConnectDaemonService)mContext).sendCommand("M4-00");
    }

    protected void readParameter()
    {
        // yy/mm/dd:hh:mm-last for minutes (one boot case)
        //HH:MM-on minutes-off minutes-cycle last for minutes (ON-OFF case)
    }
    public void setResetStatus(boolean ya)
    {
        isWakenByReset=ya;
    }

    public void run()  //here shows the template
    {
        boolean okStart=false;
        boolean iWasReset=true;
        while (iWasReset) {

            try {

                readParameter();

                sleep(new Date().getTime() - start_time);
                iWasReset=false;
            } catch (InterruptedException e) {
                iWasReset=true;

                e.printStackTrace(); //maybe by reset
            }
            if (iWasReset==false) break;
        }
        while (start_time < end_time)
        {
            /* pending
            //long next_time=start_time+on_time+off_time;
            sendStartCommand();
            sleep(on_time);{ if interrupted reset the start_time and end_time and sleep (new start_time - time now)
            ;}
            sendStopCommand();
            sleep(off_time); { if interrupted reset the start_time and end_time and sleep (new start_time - time now);}
                start_time=(new Date()).getTime();
                */
        }
    }
}
