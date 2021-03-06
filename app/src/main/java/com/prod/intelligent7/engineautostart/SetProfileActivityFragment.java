package com.prod.intelligent7.engineautostart;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class SetProfileActivityFragment extends Fragment

{

    public SetProfileActivityFragment() {
    }


    static SetProfileActivity myActivity=null;
    int mCurrentLayout;
    View mRootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mCurrentLayout=0;
        myActivity=(SetProfileActivity)getActivity();
        setRetainInstance(true);
        String iWhichCode=getArguments().getString(MainActivity.OPEN_ACTIVITY, "--");
        if (iWhichCode.charAt(0) == '-') getActivity().finish();
        switch (iWhichCode){
            case MainActivity.SET_SIM:
                mCurrentLayout=R.layout.fragment_set_sim;
                break;
            case MainActivity.SET_PIN:
                mCurrentLayout=R.layout.fragment_set_pin;
                break;
            case MainActivity.SET_PHONES:
                mCurrentLayout=R.layout.fragment_set_phones;
                break;
            default :
                getActivity().finish();
                break;
        }
        mRootView= inflater.inflate(mCurrentLayout, container, false);
        String tmp;
        switch (iWhichCode){
            case MainActivity.SET_SIM:
                EditText line1=(EditText)(mRootView.findViewById(R.id.sim_text));
                String sim;
                tmp=((SetProfileActivity) getActivity()).getSavedValue(MainActivity.SET_SIM);
                if (tmp.charAt(0)=='-') sim=""; else sim=tmp;
                line1.setText(sim);
                break;
            case MainActivity.SET_PIN:
                //mCurrentLayout=R.layout.fragment_set_pin;
                break;
            case MainActivity.SET_PHONES:
                EditText lineP1=(EditText)(mRootView.findViewById(R.id.text_phone1));
                String p1;
                tmp=((SetProfileActivity)getActivity()).getSavedValue(MainActivity.SET_PHONE1);
                if (tmp.charAt(0)=='-') p1=""; else p1=tmp;
                lineP1.setText(p1);
                lineP1=(EditText)(mRootView.findViewById(R.id.text_phone2));
                tmp=((SetProfileActivity)getActivity()).getSavedValue(MainActivity.SET_PHONE2);
                if (tmp.charAt(0)!='-') lineP1.setText(tmp);
                break;
            default :
                //getActivity().finish();
                break;
        }
        return mRootView;
    }
    static ProgressDialog myProgress=null;
    boolean showProgress=false;
    String progressTitle;
    public String confirmOkToCommand(View v, String actionString){
        String retcode="OK";

        EditText line1;
        EditText line2;
        EditText line3;
        String badMsg="";
        String retCommand="";
        switch (actionString){
            case MainActivity.SET_SIM:
                line1=(EditText)mRootView.findViewById(R.id.sim_text);
                String newSim=line1.getText().toString();
                if (newSim.length() < 6 ) {
                    badMsg=getResources().getString(R.string.too_short);
                   new MyToast(getActivity(), badMsg).warn();
                    return null;
                }
                showProgress=false;
                ((SetProfileActivity)getActivity()).setPreferenceValue(MainActivity.SET_SIM, newSim);
                 retcode="OK";
                break;
            case MainActivity.SET_PIN:
                line1=(EditText)mRootView.findViewById(R.id.password_old_pin);
                line2=(EditText)mRootView.findViewById(R.id.password_new_pin);
                line3=(EditText)mRootView.findViewById(R.id.password_confirm_pin);
                String savedPin=((SetProfileActivity)getActivity()).getSavedValue(MainActivity.SET_PIN);
                String oldPin=line1.getText().toString();
                String newPin=line2.getText().toString();
                String cnfPin=line3.getText().toString();
                if (!cnfPin.equalsIgnoreCase(newPin)) badMsg=getResources().getString(R.string.pin_not_consistent);
                else if (savedPin.equalsIgnoreCase(newPin)) badMsg=getResources().getString(R.string.pin_no_difference);
                /*else if (savedPin.charAt(0) != '-' && !oldPin.equalsIgnoreCase(savedPin))
                    badMsg=getResources().getString(R.string.wrong_pin);*/
                if (badMsg.length() > 1){
                    new MyToast(getActivity(), badMsg).warn();
                    return null;
                }
                retCommand="M2-"+oldPin+"-"+newPin+"-"+newPin;
                ((SetProfileActivity)getActivity()).setPreferenceValue(MainActivity.PENDING_NEW_PIN, line2.getText().toString());
                progressTitle=getResources().getString(R.string.sim_setting);
                showProgress=true;
                retcode=retCommand;//"wait";
                break;
            case MainActivity.SET_PHONES:
                line1=(EditText)mRootView.findViewById(R.id.text_phone1);
                line2=(EditText)mRootView.findViewById(R.id.text_phone2);
                EditText line0=(EditText)mRootView.findViewById(R.id.password_set_phones);
                savedPin=((SetProfileActivity)getActivity()).getSavedValue(MainActivity.SET_PIN);
                String savedPhone=((SetProfileActivity)getActivity()).getSavedValue(MainActivity.SET_PHONE1);
                String pin=line0.getText().toString();
                String newPhone1=line1.getText().toString();
                String newPhone2=line2.getText().toString();

                retCommand="M3-"+pin+"-"+newPhone1+"-"+newPhone2;
                ((SetProfileActivity)getActivity()).setPreferenceValue(MainActivity.PENDING_NEW_PHONE1, line1.getText().toString());
                ((SetProfileActivity)getActivity()).setPreferenceValue(MainActivity.PENDING_NEW_PHONE2, line2.getText().toString());
                progressTitle=getResources().getString(R.string.phone_numbers_setting);
                showProgress=true;
                retcode=retCommand;//"wait";
                break;
            default :
                getActivity().finish();
                break;
        }

        return retcode;
    }


}
