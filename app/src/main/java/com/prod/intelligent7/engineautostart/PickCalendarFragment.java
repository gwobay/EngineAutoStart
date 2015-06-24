package com.prod.intelligent7.engineautostart;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class PickCalendarFragment extends DialogFragment
       // implements DatePickerDialog.OnDateSetListener
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        //TimeZone.setDefault(TimeZone.getTimeZone("Hongkong"));
        //final Calendar c = Calendar.getInstance();
        //int year = c.get(Calendar.YEAR);
        //int month = c.get(Calendar.MONTH);
        //int day = c.get(Calendar.DAY_OF_MONTH);
        View contentV=getActivity().getLayoutInflater().inflate(R.layout.layout_pick_calendar, null);
        myView= (CalendarView) contentV.findViewById(R.id.calendarView);//new CalendarView(getActivity());
        //myView.setLayoutParams(new FrameLayout.LayoutParams(300, 400));
        myView.setDate(new Date().getTime());
        // Create a new instance of TimePickerDialog and return it
        myDialog= (new AlertDialog.Builder(getActivity()))
                .setMessage(getResources().getString(R.string.label_one_boot_set_date))
                //.setView(myView)
                .setView(contentV)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setDate();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .create();
        return myDialog;
                //new DatePickerDialog(getActivity(), this, year, month, day);
        //DateFormat.is24HourFormat(getActivity()));
    }

    public Dialog myDialog;
    public void updateDialogView()
    {
        if (myDialog==null) return;
        //WindowManager.LayoutParams lp=new WindowManager.LayoutParams();
        //lp.copyFrom(myDialog.getWindow().getAttributes());
        //lp.width=300;
        //lp.height=400;
       // myDialog.getWindow().setAttributes(lp);
    }
    /*
    public PickCalendarFragment()
    {
        super();
        TimeZone.setDefault(TimeZone.getTimeZone("Hongkong"));

    }*/
    static CalendarView myView;
    public void setDate()
    {
        long getTime=myView.getDate();
        //TimeZone.setDefault(TimeZone.getTimeZone("Hongkong"));
        GregorianCalendar gToday=new GregorianCalendar(TimeZone.getTimeZone("Hongkong"));
        gToday.setTimeInMillis(getTime);
        viewToFill.setText(new DecimalFormat("00").format(gToday.get(Calendar.YEAR)) + "/" +
                (new DecimalFormat("00")).format(gToday.get(Calendar.MONTH)+1) + "/" +
                (new DecimalFormat("00")).format(gToday.get(Calendar.DAY_OF_MONTH)));
    }

    TextView viewToFill;
    public void setViewToFill(TextView v)
    {
        viewToFill=v;
    }
    public void onDateSet(DatePicker view, int year, int month, int day) {
        viewToFill.setText((new DecimalFormat("00")).format(year)+"/"+
                (new DecimalFormat("00")).format(month)+"/"+
                        (new DecimalFormat("00")).format(day));
    }
}