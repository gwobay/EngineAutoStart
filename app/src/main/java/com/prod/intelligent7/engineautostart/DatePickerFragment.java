package com.prod.intelligent7.engineautostart;


import java.text.DecimalFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;



public class DatePickerFragment extends DialogFragment
						implements DatePickerDialog.OnDateSetListener 
{
	static TextView mDateView;
	//static Activity mActivity=null;

	public DatePickerFragment()
	{
		super();
	}
	
	public void forView(TextView v)
	{
		mDateView=v;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(),
				this, my, mm, md);
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) 
	{
		DecimalFormat dF=new DecimalFormat("00");
		String dateData=dF.format(year)+"-"+dF.format(month+1)+"-"+dF.format(day);	
		mDateView.setText(dateData);
	}
	int my;
	int mm;
	int md;
	public void setDay0(int y, int m, int d)
	{
		my=y; mm=m; md=d;
	}
	public void pickDate(View view, Activity aK)
	{	
		mDateView=(TextView)view;
		FragmentManager manager=aK.getFragmentManager();
		show(manager,  "datePicker");		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View retV=null;
		if (((PickActivity)getActivity()).getPickType()==PickActivity.PICK_N)
		retV = inflater.inflate(R.layout.layout_n_boots, container, false);
		else
		retV = inflater.inflate(R.layout.layout_1_boot, container, false);
		return retV;
	}
}
