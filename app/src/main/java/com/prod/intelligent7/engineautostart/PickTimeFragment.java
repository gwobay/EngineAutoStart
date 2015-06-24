package com.prod.intelligent7.engineautostart;


import android.app.Dialog;
import android.support.v4.app.Fragment;//android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.util.Calendar;

public class PickTimeFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    TextView viewToFill;
    public void setViewToFill(TextView v)
    {
        viewToFill=v;
    }
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        viewToFill.setText((new DecimalFormat("00")).format(hourOfDay)+":"+
                (new DecimalFormat("00")).format(minute));
    }
}