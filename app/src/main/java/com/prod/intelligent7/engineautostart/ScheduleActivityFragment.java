package com.prod.intelligent7.engineautostart;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;


/**
 * A placeholder fragment containing a simple view.
 */
public class ScheduleActivityFragment extends Fragment {


    boolean for1Boot;
    public ScheduleActivityFragment() {
    }


    int mCurrentLayout;
    String mCurrentJob;
    View mRootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mCurrentLayout=0;
        //setRetainInstance(true);
        for1Boot=true;
        String mCurrentJob=getArguments().getString(MainActivity.OPEN_ACTIVITY, "--");
        if (mCurrentJob.charAt(0) == '-') getActivity().finish();
        switch (mCurrentJob){
            case MainActivity.SET_ONE_BOOT:
                mCurrentLayout=R.layout.fragment_preset_1_boot;
                break;
            case MainActivity.SET_MULTIPLE_BOOT:
                for1Boot=false;
                mCurrentLayout=R.layout.fragment_preset_n_boot;
                break;

            default :
                getActivity().finish();
                break;
        }
        mRootView= inflater.inflate(mCurrentLayout, container, false);

        allData=getArguments().getStringArray("CURRENT_DATA");

        return mRootView;
    }
    String[] allData;

    @Override
    public void onStart()
    {
        super.onStart();
        HashMap<String, String> currentData=((ScheduleActivity)getActivity()).getCurrentData();
        if (currentData==null) return;
        for (int i=0; i<7; i++)
        {
            String aLine=currentData.get(ScheduleActivity.weekDays[i]);
            if (aLine != null){
                populateSchedule(i, aLine);
            }
        }

        String pageTitle;
        if (mCurrentJob== null) return;
        switch (mCurrentJob){
            case MainActivity.SET_ONE_BOOT:
                pageTitle=getResources().getString(R.string.daily_auto_start_setting);
                break;
            case MainActivity.SET_MULTIPLE_BOOT:
                for1Boot=false;
                pageTitle=getResources().getString(R.string.daily_multiple_start_setting);
                break;
            default :
                pageTitle=getResources().getString(R.string.app_name);
                break;
        }
        //((ScheduleActivity)getActivity()).setTitle(pageTitle);

    }

    void setData(String data, int[] rsId){
        String[] terms=data.split("-");
        TextView lineP1=(TextView)(mRootView.findViewById(rsId[0]));
        lineP1.setText(terms[0]);
        TextView lineP2=(TextView)(mRootView.findViewById(rsId[1]));
        lineP2.setText(terms[1]);
        if (for1Boot) return;
        lineP2=(TextView)(mRootView.findViewById(rsId[2]));
        lineP2.setText(terms[2]);
    }

    int[][] cells={
            {
                    R.id.sunday_t0, R.id.sunday_period, R.id.sunday_idle
            },
            {
                    R.id.monday_t0, R.id.monday_period, R.id.monday_idle
            },
            {
                    R.id.tuesday_t0, R.id.tuesday_period, R.id.tuesday_idle
            },
            {
                    R.id.wenseday_t0, R.id.wenseday_period, R.id.wenseday_idle
            },
            {
                    R.id.thurseday_t0, R.id.thurseday_period, R.id.thurseday_idle
            },
            {
                    R.id.friday_t0, R.id.friday_period, R.id.friday_idle
            },
            {
                    R.id.saturday_t0, R.id.saturday_period, R.id.saturday_idle
            }
    };

    public void populateSchedule(int forDay, String data){

            if (data!=null) setData(data, cells[forDay]);
            else
            {
                String closed=getResources().getString(R.string.closed);
               setData(closed+"-"+closed+"-"+closed, cells[forDay]) ;
            }
    }

    void executeCommand(String command){
        Intent jIntent=new Intent(getActivity(), ConnectDaemonService.class);
        //M1-00 (cool) or M1-01 (warm)
        jIntent.putExtra(ConnectDaemonService.DAEMON_COMMAND, command);
        //Toast.makeText(this, "will send start command to server", Toast.LENGTH_LONG).show();
        getActivity().startService(jIntent);
    }

    public String confirmOkToCommand(View v, String actionString){

        switch (actionString){
            case MainActivity.SET_ONE_BOOT:
                String command= "M1-01";
                executeCommand(command);
                new MyToast(getActivity(), ConnectDaemonService.getChinese(command) + "指令已送出").info();
                break;
            case MainActivity.SET_MULTIPLE_BOOT:
                NumberPicker howLong=(NumberPicker)mRootView.findViewById(R.id.for_how_long);
                int t4=howLong.getValue();

                executeCommand("M5-" + (new DecimalFormat("00")).format(t4));
                new MyToast(getActivity(), ConnectDaemonService.getChinese("M5") + "指令已送出").info();
                break;
            case MainActivity.CMD_STOP_NOW:
                executeCommand("M4-00");
                new MyToast(getActivity(), ConnectDaemonService.getChinese("M4-00") + "指令已送出").info();
                break;
            default :
                getActivity().finish();
                break;
        }

        return " ";
    }

}
