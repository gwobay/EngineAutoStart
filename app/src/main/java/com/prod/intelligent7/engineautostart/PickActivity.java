package com.prod.intelligent7.engineautostart;

import java.text.DecimalFormat;

import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

public class PickActivity extends FragmentActivity
				implements TimePickerDialog.OnTimeSetListener 
{


	//static ProfilePage mFragment=null;

	boolean n_boot=true;
	public final static int PICK_1=1;
	public final static int PICK_N=99;
	int pick_type;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String fixLine=null;

		if (intent == null || // no extra back from map
					!intent.hasExtra(MainActivity.PICK4WHAT)) done();
		fixLine=intent.getExtras().getString(MainActivity.PICK4WHAT);
		if (fixLine.equalsIgnoreCase(MainActivity.N_BOOT_PARAMS)) {
			n_boot = true;
			pick_type=PICK_N;
			setContentView(R.layout.activity_pick_n);
		}
		else {
			n_boot=false;
			pick_type=PICK_1;
			setContentView(R.layout.activity_pick_1);
		}
	}

	public int getPickType()
	{
		return pick_type;
	}
	static Menu mActionMenu=null;

	static ImageView viewToRefresh=null;


	
	static final int REQ_PICK_IMAGE=1;
	static final int FOR_RINGTON=2;
	static final int FOR_TIMESTART=3;
	static final int FOR_TIMEEND=4;
	static ImageView mProfilePhotoImage=null;


	
	public static final String CURRENT_RINGTON="CURRENT_RINGTON";
	public static final String NO_NOISE_START="NO_NOISE_START";
	public static final String NO_NOISE_END="NO_NOISE_END";
	static String ringUriString=null;
	static String noNoiseStart=null;
	static String noNoiseEnd=null;
	static TextView noNoiseTextField0=null;
	static TextView noNoiseTextField1=null;
		
	void saveRingToneData()
	{
		String fileName=MainActivity.package_name+".profile";
        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor sv=sharedPref.edit();
        sv.putString(CURRENT_RINGTON, ringUriString);
        sv.putString(NO_NOISE_START, noNoiseStart);
        sv.putString(NO_NOISE_END, noNoiseEnd);
        sv.commit();
	}
	public void getRingTone(View v) {
	    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
	    Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	    RingtoneManager.getRingtone(getApplicationContext(), ringUri).play();
	    intent.putExtra(CURRENT_RINGTON, ringUri);
	    if (intent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(intent, FOR_RINGTON);
	    }
	    //noNoiseTextField0=(TextView)((View)v.getParent()).findViewById(R.id.no_noise_period_begin);
	    noNoiseStart=null;
	    //noNoiseTextField1=(TextView)((View)v.getParent()).findViewById(R.id.no_noise_period_end);
	    noNoiseEnd=null;
	}
	
	public void onTimeSet(TimePicker view, int h, int m)
	{
		DecimalFormat dF=new DecimalFormat("00");
		String sTime=dF.format(h)+":"+dF.format(m);	
		if (noNoiseStart==null)
		{
			noNoiseStart=sTime;
			noNoiseTextField0.setText(sTime+" - ");
			//getNoNoisePeriod(getResources().getString(R.string.no_noise_period_end));
		}
		else
		{
			noNoiseEnd=sTime;
			noNoiseTextField1.setText(sTime);
			saveRingToneData();
		}
	}
	
	public void getNoNoisePeriod(String title) {
		TimePickerDialog pT=new TimePickerDialog(this, this, 0, 0, true);
		//FragmentManager manager=getFragmentManager();
		pT.setTitle(title);
		pT.show();//manager,  "datePicker");	
	    //Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
	    //Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	    //RingtoneManager.getRingtone(getApplicationContext(), ringUri).play();
	    //intent.putExtra(CURRENT_RINGTON, ringUri);
	    //if (intent.resolveActivity(getPackageManager()) != null) {
	        //startActivityForResult(intent, FOR_RINGTON);
	    //}
	}

	public void pickDate(View view)
	{
		DatePickerFragment pFrg=new DatePickerFragment();
		View dateView=null;//((View)view.getParent()).findViewById(R.id.birth_date);
		pFrg.setDay0(1994, 6, 1);
		pFrg.pickDate(dateView, this);
	}


	public void saveNBootData(View v)
	{
		View rootV=v.getRootView();
		TimePicker pStart=(TimePicker)rootV.findViewById(R.id.n_boot_time_start);
		int iHr=pStart.getCurrentHour();
		int iMin=pStart.getCurrentMinute();
		String nBootParam=""+iHr+":"+iMin+"-";
		EditText activeP=(EditText)rootV.findViewById(R.id.active_period);
		if (activeP!= null) nBootParam += activeP.getText().toString()+"-";
		EditText idleP=(EditText)rootV.findViewById(R.id.idle_period);
		int iPause=1;
		if (idleP!= null){
			iPause=Integer.parseInt(idleP.getText().toString());
		}
		if (iPause < 1) iPause=1;
		if (iPause > 5) iPause=5;
		nBootParam += (new DecimalFormat("000")).format(iPause);
		pStart=(TimePicker)rootV.findViewById(R.id.n_boot_time_end);
		int eHr=pStart.getCurrentHour();
		int eMin=pStart.getCurrentMinute();
		if (eHr < iHr) eHr += 12;
		int last4=(eHr-iHr)*60+(eMin-iMin);
		nBootParam += ("-"+last4);

		String fileName=MainActivity.package_name+".profile";
		SharedPreferences mem = getSharedPreferences(fileName, Context.MODE_PRIVATE);
		SharedPreferences.Editor adder=mem.edit();
		adder.putString(MainActivity.N_BOOT_PARAMS, nBootParam); //HH:MM-on minutes-off minutes-cycle last for minutes
		adder.commit();
		done();
	}

	public void save1BootData(View v)
	{
		View rootV=v.getRootView();
		DatePicker pStartD=(DatePicker)rootV.findViewById(R.id.one_boot_date);
		int iYY=pStartD.getYear();
		int iMM=pStartD.getMonth()+1;
		int iDD=pStartD.getDayOfMonth();
		TimePicker pStart=(TimePicker)rootV.findViewById(R.id.one_boot_time);
		int iHH=pStart.getCurrentHour();
		int iM60=pStart.getCurrentMinute();
		String bootParam=""+iYY+"/"+iMM+"/"+iDD+"-"+iHH+":"+iM60+"-";
		EditText activeP=(EditText)rootV.findViewById(R.id.last4);
		if (activeP!= null) bootParam += activeP.getText().toString();

		String fileName=MainActivity.package_name+".profile";
		SharedPreferences mem = getSharedPreferences(fileName, Context.MODE_PRIVATE);
		SharedPreferences.Editor adder=mem.edit();
		adder.putString(MainActivity.ONE_BOOT_PARAMS, bootParam); //  yy/mm/dd:hh:mm-last for minutes
		adder.commit();
		done();
	}

	public void doneActivity(View v)
	{
		done();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{			
	//String photo_file_path;
	Uri photo_uri=null;
		if (data != null) photo_uri=data.getData();
		if(requestCode == REQ_PICK_IMAGE && photo_uri != null) 
		{
		/*
		        //User had pick an image.
			Cursor cursor = getContentResolver().query(photo_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
		        
			cursor.moveToFirst();

		        //Link to the image
		        photo_file_path = cursor.getString(0);
		        cursor.close();
		 */       	
		}
		super.onActivityResult(requestCode, resultCode, data);
		/*
		if (requestCode == REQ_PICK_IMAGE && photo_uri != null)
		{			
			//PersonalPage aPP=(PersonalPage)(globalParameters.get("volunteer"));
			if (mFragment==null) return;
			mFragment.setPhotoUri(photo_uri);
			mFragment.replaceProfilePhoto(mProfilePhotoImage);  
		}*/
		if (requestCode==FOR_RINGTON)
		{
			Uri pickedRing=(Uri)data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			//Ringtone aRing=RingtoneManager.getRingtone(getApplicationContext(), pickedRing);
			ringUriString=pickedRing.toString();
			//getNoNoisePeriod(getResources().getString(R.string.no_noise_period_begin));
		}
	}
	
    public void done()
    {
    	//Intent it=new Intent();
    	this.setResult(n_boot?MainActivity.PICK_N:MainActivity.PICK_ONE);
    	finish();
    }
}
