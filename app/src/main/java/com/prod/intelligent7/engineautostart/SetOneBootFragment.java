package com.prod.intelligent7.engineautostart;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SetOneBootFragment extends MySimpleFragment {
	public static final String BKP_PASSWORD="BKP_PASSWORD";

	public SetOneBootFragment() {
		// TODO Auto-generated constructor stub
	}

	static LinearLayout.LayoutParams mmParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.MATCH_PARENT);
	static LinearLayout.LayoutParams mwParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);
	static LinearLayout.LayoutParams wwParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);
	static LinearLayout.LayoutParams llParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);
	static TableLayout.LayoutParams tbParams=new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);
	static LinearLayout mRoot=null;
	static Activity mActivity=null;
	static Context mContext=null;
	public void setActivity(Activity mv)
	{
		mActivity=mv;
		mContext=mv.getApplicationContext();
	}
	static TextView mAlpha=null;
	static TextView mCurrentText=null;
	static Button mOK=null;
	static int mColumn=-1;
	static int mRow=0; // 0 for id, 1 for password;
	static TextView[] mIdDigits=null;
	static TextView[] mPasswords=null;
	static TableLayout mTable=null;
	static char[] myCodes=new char[4];
	static String mSavedCode="";
	static String mBkpCode="";
	static boolean isDigitPad=false;
	static boolean newUser=false;


	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);


		View rootV=mRootView;
		View dateV=rootV.findViewById(R.id.one_boot_pick_date);
		String sDate=((TextView)dateV).getText().toString();
		View timeV=rootV.findViewById(R.id.one_boot_pick_time);
		String sTime=((TextView)timeV).getText().toString();
		EditText last4V=(EditText)rootV.findViewById(R.id.one_boot_last4);
		String sLast4=last4V.getText().toString();

		outState.putString("SET_DATE", sDate);
		outState.putString("SET_TIME", sTime);
		outState.putString("SET_LAST4", sLast4);

	}

	@Override
	protected void restoreInstanceState(Bundle savedInstanceState) {

	}

	@Override
	public void saveData()
	{
		View rootV=mRootView;
		View dateV=rootV.findViewById(R.id.one_boot_pick_date);
		String sDate=((TextView)dateV).getText().toString();
		View timeV=rootV.findViewById(R.id.one_boot_pick_time);
		String sTime=((TextView)timeV).getText().toString();
		EditText last4V=(EditText)rootV.findViewById(R.id.one_boot_last4);
		String sLast4=last4V.getText().toString();
		if (sDate != null && sDate.length() > 9 &&
				sTime != null && sTime.length() > 4 &&
				sLast4 != null && sLast4.length() > 0) {
			String bootParam = sDate + "-" + sTime + "-" + sLast4;

			((MainActivity) mActivity).setPreferenceValue(MainActivity.ONE_BOOT_PARAMS, bootParam);
			saveConfirmed=true;
			//backToMain();
		}
		else {
			saveConfirmed=false;
			Toast.makeText(mActivity, getResources().getString(R.string.bad_data_entry),
					Toast.LENGTH_LONG).show();
			if(sDate != null && sDate.length() > 10) dateV.requestFocus();
			else if(sTime != null && sTime.length() > 5) timeV.requestFocus();
			else last4V.requestFocus();
		}
	}

	public boolean verifyAndSaveData()
	{
		View rootV=mRootView;
		View dateV=rootV.findViewById(R.id.one_boot_pick_date);
		String sDate=((TextView)dateV).getText().toString();
		View timeV=rootV.findViewById(R.id.one_boot_pick_time);
		String sTime=((TextView)timeV).getText().toString();
		EditText last4V=(EditText)rootV.findViewById(R.id.one_boot_last4);
		String sLast4=last4V.getText().toString();
		saveConfirmed=false;
		if (sDate != null && sDate.length() > 9 &&
				sTime != null && sTime.length() > 4 &&
				sLast4 != null && sLast4.length() > 0) {
			String bootParam = sDate + "-" + sTime + "-" + sLast4;

			((MainActivity) mActivity).setPreferenceValue(MainActivity.ONE_BOOT_PARAMS, bootParam);
			saveConfirmed=true;
			backToMain();
		}
		else {
			saveConfirmed=false;
			Toast.makeText(mActivity, getResources().getString(R.string.bad_data_entry),
					Toast.LENGTH_LONG).show();
			if(sDate != null && sDate.length() > 10) dateV.requestFocus();
			else if(sTime != null && sTime.length() > 5) timeV.requestFocus();
			else last4V.requestFocus();
		}
		return saveConfirmed;
	}

	boolean saveConfirmed;
	public boolean checkIfSaveConfirmed()
	{
		return saveConfirmed;
	}
	String sYear;
	String sMonth;
	static int[] oldMonth={1,3,5,7,8,10,12};
	static int[] evenMonth={4,6,9,11};
	String sDay;
	String sHour;
	String sMinute;
	private AdapterView.OnItemClickListener mYearClickedHandler = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			sYear=((TextView)v).getText().toString();
			setWeekText();
		}
	};
	private AdapterView.OnItemClickListener mMonthClickedHandler = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			sMonth=((TextView)v).getText().toString();
			setWeekText();
			/*
			int endDay=29;
			for  (int month : oldMonth) {
				if (month == Integer.parseInt(sMonth)) {
					fillNumberStringArray(arrayDay, 1, 31);
					return;
				}
			}
			for  (int month : evenMonth) {
				if (month == Integer.parseInt(sMonth)) {
					fillNumberStringArray(arrayDay, 1, 30);
					return;
				}
			}
			fillNumberStringArray(arrayDay, 1, 29);*/
		}
	};
	static String[] chineseNumber;
	void setWeekText()
	{
		Calendar thisD=new GregorianCalendar(Integer.parseInt(sYear), Integer.parseInt(sMonth), Integer.parseInt(sDay));
		int d=thisD.get(Calendar.DAY_OF_WEEK);

		weekText.setText(chineseNumber[d-1]);

	}
	private AdapterView.OnItemClickListener mDayClickedHandler = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			sDay=((TextView)v).getText().toString();
			setWeekText();
		}
	};
	private AdapterView.OnItemClickListener mHourClickedHandler = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			sHour=((TextView)v).getText().toString();
		}
	};
	private AdapterView.OnItemClickListener mMinuteClickedHandler = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position, long id) {
			sMinute=((TextView)v).getText().toString();
		}
	};
	void workOnYearList(ListView v)
	{
		fillNumberStringArray(arrayYear, 15, 26);
		//String[] forYears=getResources().getStringArray(R.array.pick_year);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				R.layout.layout_one_line_text, arrayYear);
				//android.R.layout.simple_list_item_1, arrayYear);
		v.setAdapter(adapter);
		int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
		v.setOnItemClickListener(mYearClickedHandler);
	}

	void workOnMonthList(ListView v)
	{
		fillNumberStringArray(arrayMonth, 1, 12);
		//String[] forMonth=getResources().getStringArray(R.array.pick_month);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				R.layout.layout_one_line_text, arrayMonth);
		v.setAdapter(adapter);
		int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
		v.setOnItemClickListener(mMonthClickedHandler);
		v.setSelection(gToday.get(Calendar.MONTH));
	}

	TextView weekText;
	void workOnDayList(ListView v)
	{
		//if (Integer.parseInt(arrayDay[5])<2)
			fillNumberStringArray(arrayDay, 1, 31);
		//String[] forYears=getResources().getStringArray(R.array.pick_day);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				R.layout.layout_one_line_text, arrayDay);
		v.setAdapter(adapter);
		int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
		v.setOnItemClickListener(mDayClickedHandler);
		v.setSelection(gToday.get(Calendar.DAY_OF_MONTH)-1);
	}

	static String[] arrayYear=new String[12];
	static String[] arrayMonth=new String[12];

	static String[] arrayDay=new String[31];
	static String[] arrayHour=new String[24];
	static String[] arrayMinute=new String[60];

	static void fillNumberStringArray(String[] toFill, int from, int end)
	{
		for (int i=from; i<end+1; i++)
		{
			toFill[i-from]=(new DecimalFormat("00")).format(i);
		}
	}
	void workOnHourList(ListView v)
	{
		fillNumberStringArray(arrayHour, 1, 24);
		//String[] forYears=getResources().getStringArray(R.array.pick_hour);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				R.layout.layout_one_line_text, arrayHour);
		v.setAdapter(adapter);
		int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
		v.setOnItemClickListener(mHourClickedHandler);
		v.setSelection(gToday.get(Calendar.HOUR_OF_DAY));
	}

	void workOnMinuteList(ListView v)
	{
		fillNumberStringArray(arrayMinute, 1, 60);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				R.layout.layout_one_line_text, arrayMinute);
		v.setAdapter(adapter);
		int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
		v.setOnItemClickListener(mMinuteClickedHandler);
		v.setSelection(gToday.get(Calendar.MINUTE));
	}


	void fillAllScrollViews(View v)
	{
		ListView vYear=(ListView)v.findViewById(R.id.ListView_year);
		workOnYearList(vYear);
		ListView vMonth=(ListView)v.findViewById(R.id.ListView_month);
		workOnMonthList(vMonth);
		ListView vDay=(ListView)v.findViewById(R.id.ListView_day);
		workOnDayList(vDay);
		ListView vHour=(ListView)v.findViewById(R.id.ListView_hour);
		workOnHourList(vHour);
		ListView vMinute=(ListView)v.findViewById(R.id.ListView_minute);
		workOnMinuteList(vMinute);
	}

	static View mRootView;
	static GregorianCalendar gToday=new GregorianCalendar();

	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) 
	 {
		 /*
		 chineseNumber=getResources().getStringArray(R.array.week_day);
		 TimeZone.setDefault(TimeZone.getTimeZone("Hongkong"));
		 gToday=new GregorianCalendar(TimeZone.getTimeZone("Hongkong"));
		 sYear=""+gToday.get(Calendar.YEAR);
		 sMonth=""+gToday.get(Calendar.MONTH);
		 sDay=""+gToday.get(Calendar.DAY_OF_MONTH);
		 sHour=""+gToday.get(Calendar.HOUR_OF_DAY);
		 sMinute=""+gToday.get(Calendar.MINUTE);
		 */
		 mActivity=getActivity();
		 mContext=getActivity();

		 mRootView=inflater.inflate(R.layout.layout_set_1_boot,//1boot_date_time,
		 							container, false);
		 /*
		 weekText=(TextView)mRootView.findViewById(R.id.day_of_week);

		 fillAllScrollViews(mRootView);

		 weekText.setText(chineseNumber[gToday.get(Calendar.DAY_OF_WEEK)-1]);
		 */
		 if (savedInstanceState != null)
		 {
			 String sDate=savedInstanceState.getString("SET_DATE");
			 String sTime=savedInstanceState.getString("SET_TIME");
			 String sLast4=savedInstanceState.getString("SET_LAST4");
			 View rootV=mRootView;
			 View dateV=rootV.findViewById(R.id.one_boot_pick_date);
			 if (sDate!=null)
			 ((TextView)dateV).setText(sDate);
			 View timeV=rootV.findViewById(R.id.one_boot_pick_time);
			 if (sTime != null)
			 ((TextView)timeV).setText(sTime);
			 EditText last4V=(EditText)rootV.findViewById(R.id.one_boot_last4);
			 if (sLast4 != null)
			 last4V.setText(sLast4);
		 }
		 return mRootView;
	 }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		if (mRootView==null) return;
		//ListView vYear=(ListView)mRootView.findViewById(R.id.ListView_year);
		//vYear.requestFocus();
		Button vD=(Button)mRootView.findViewById(R.id.one_boot_pick_date);
		vD.requestFocus();

	}
		int szKeySize;
	int mTotalRow;
	int mTotalColumn;
	boolean isLargeScreen;
	boolean isLandScape;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private int keyPadWidth;
	LinearLayout.LayoutParams keyPadParams;

	private int mLogBarHeight;

}
