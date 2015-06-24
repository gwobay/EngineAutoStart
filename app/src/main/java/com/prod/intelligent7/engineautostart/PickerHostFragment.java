package com.prod.intelligent7.engineautostart;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

public class PickerHostFragment extends DialogFragment
    implements CompoundButton.OnCheckedChangeListener
{
    HashMap<String, String> outDataBox;
    int layoutResource;
    View contentView;
    Activity mActivity;
    boolean onRecurring;
    int mHour;
    int mMinute;
    int mActivePeriod;
    int mIdleInterval;
    String myTitle;
    String jobTag;
    String savedData;
    boolean isSwitchOn;

    public PickerHostFragment(){
        super();
        onRecurring=false; //so no limit on time pick
        myTitle="???";
        jobTag="Sunday";
        outDataBox=null;
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        Switch mySwitch=(Switch)buttonView;
        if (isChecked){
            setPickerEnabled(true);
            isSwitchOn=true;
        }
        else
        {
            outDataBox.remove(jobTag);
            isSwitchOn=false;
            ((ScheduleActivity)mActivity).onDialogPositiveClick(this);
        }
    }

    String getResultString()
    {
        DecimalFormat dF=new DecimalFormat("00");
        return dF.format(mHour)+":"+dF.format(mMinute)+"-"+dF.format(mActivePeriod)+"-"+dF.format(mIdleInterval);
    }

    public void finishData(){
        getStartTime();
        getActivePeriod();
        getIdleTimeInHours();
        if (outDataBox!=null){
            outDataBox.put(jobTag, getResultString());
        }
        if (!isSwitchOn){
            outDataBox.remove(jobTag);
        }
    }

    public void setJobTag(String tag){
        jobTag=tag;
    }

    public void setDataBox(HashMap<String, String> aDataBox){
        outDataBox=aDataBox;
    }

    public void setActivity(Activity mA){
        mActivity=mA;
    }
    public View getMyContentView(){
        return contentView;
    }
    public void setResources(Activity mA, int rsc){
        mActivity=mA;
        layoutResource=rsc;
    }
    public void setTitle(String tt){
        myTitle=tt;
    }
    public void isOnRecurring(boolean T_F){
        onRecurring=T_F;
    }
    public void setSavedData(String data)
    {
        savedData=data;
    }
    public void setNewState(String data){
        isSwitchOn=false;
        if (data==null || contentView==null) return;
        String[] terms=data.split("-");
        if (terms.length < 2) return;
        String[] HM=terms[0].split(":");
        if (HM.length < 2) return;
        TimePicker aP=(TimePicker)(contentView.findViewById(R.id.start_time));
        if (aP == null) return;
        aP.setEnabled(true);
        isSwitchOn=true;
        aP.setCurrentHour(Integer.parseInt(HM[0]));
        aP.setCurrentMinute(Integer.parseInt(HM[1]));
        Switch rB=(Switch)(contentView.findViewById(R.id.set_it));
        rB.setChecked(true);
        NumberPicker nP=(NumberPicker)(contentView.findViewById(R.id.active_period));
        if (nP != null) { nP.setEnabled(true);nP.setValue(Integer.parseInt(terms[1]));}
        nP=(NumberPicker)(contentView.findViewById(R.id.idle_interval));
        if (nP != null && terms.length > 2)  { nP.setEnabled(true);nP.setValue(Integer.parseInt(terms[2]));}
    }
    public interface PickerDataListener{

        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone(getResources().getString(R.string.my_time_zone_en)));

        mHour=0;
        mMinute=0;//gToday.get(Calendar.MINUTE);
        mActivePeriod=0;
        mIdleInterval=0;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = mActivity.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        contentView=inflater.inflate(layoutResource, null);

        Switch rB=(Switch)(contentView.findViewById(R.id.set_it));
        rB.setOnCheckedChangeListener(this);
        TimePicker aP=(TimePicker)(contentView.findViewById(R.id.start_time));
        aP.setIs24HourView(true);
        aP.setCurrentHour(gToday.get(Calendar.HOUR_OF_DAY));
        if (onRecurring) {
            aP.setCurrentHour(21);
            aP.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    public void onTimeChanged(TimePicker tp, int hr, int mins) {
                        if (hr > 7 && hr < 21) {
                            tp.setCurrentHour(21);
                        }
                    }
                });
        }
        setLimit();
        setPickerEnabled(false);
        setNewState(savedData);
        TextView wdV=(TextView)(contentView.findViewById(R.id.week_day));
        wdV.setText(myTitle);


        builder//.setTitle(myTitle)
                .setView(contentView)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                                finishData();
                        aListener.onDialogPositiveClick(PickerHostFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        aListener.onDialogNegativeClick(PickerHostFragment.this);
                    }
                });
        return builder.create();
    }

    PickerDataListener aListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            aListener = (PickerDataListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    void setLimit(){
        NumberPicker aP=(NumberPicker)(contentView.findViewById(R.id.active_period));
        if (aP==null) return;
        aP.setMaxValue(30);
        aP.setMinValue(1);
        aP=(NumberPicker)(contentView.findViewById(R.id.idle_interval));
        if (aP==null) return;
        aP.setMaxValue(5);
        aP.setMinValue(1);
    }
   //for special cases
    void getStartTime()
    {
        TimePicker aP=(TimePicker)(contentView.findViewById(R.id.start_time));
        if (aP==null) return;
        mHour=aP.getCurrentHour();
            mMinute=aP.getCurrentMinute();
    }

    void getActivePeriod()
    {
        NumberPicker aP=(NumberPicker)(contentView.findViewById(R.id.active_period));
        if (aP==null) return;
        mActivePeriod=aP.getValue();
    }

    void getIdleTimeInHours()
    {
        NumberPicker aP=(NumberPicker)(contentView.findViewById(R.id.idle_interval));
        if (aP==null) return;
        mIdleInterval=aP.getValue();
    }

    void setPickerEnabled(boolean T_F)
    {
        if (contentView == null) return;
        TimePicker aP=(TimePicker)(contentView.findViewById(R.id.start_time));
        if (aP != null) aP.setEnabled(T_F);
        NumberPicker nP=(NumberPicker)(contentView.findViewById(R.id.active_period));
        if (nP != null) nP.setEnabled(T_F);
        nP=(NumberPicker)(contentView.findViewById(R.id.idle_interval));
        if (nP != null) nP.setEnabled(T_F);
        Switch rB=(Switch)(contentView.findViewById(R.id.set_it));
        if (rB != null) rB.setChecked(T_F);
    }
}