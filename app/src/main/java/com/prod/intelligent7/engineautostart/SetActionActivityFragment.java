package com.prod.intelligent7.engineautostart;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.text.DecimalFormat;


/**
 * A placeholder fragment containing a simple view.
 */
public class SetActionActivityFragment extends Fragment {

    public SetActionActivityFragment() {
    }


    int mCurrentLayout;
    View mRootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mCurrentLayout=0;
        setRetainInstance(true);
        String iWhichCode=getArguments().getString(MainActivity.OPEN_ACTIVITY, "--");
        if (iWhichCode.charAt(0) == '-') getActivity().finish();
        switch (iWhichCode){
            case MainActivity.SET_WARMER:
                mCurrentLayout=R.layout.fragment_act_heater;
                break;
            case MainActivity.CMD_START_NOW:
                mCurrentLayout=R.layout.fragment_act_start;
                break;
            case MainActivity.CMD_STOP_NOW:
                mCurrentLayout=R.layout.fragment_act_shut_down;
                break;
            default :
                getActivity().finish();
                break;
        }
        mRootView= inflater.inflate(mCurrentLayout, container, false);

        if (iWhichCode.equalsIgnoreCase(MainActivity.CMD_START_NOW)) {
            NumberPicker howLong = (NumberPicker) mRootView.findViewById(R.id.for_how_long);
            howLong.setMaxValue(30);
            howLong.setMinValue(1);
            howLong.setValue(15);
        }

        /*no stored value to show for action
        switch (iWhichCode){
            case MainActivity.SET_SIM:
                EditText line1=(EditText)(mRootView.findViewById(R.id.sim_text));
                line1.setText(((SetProfileActivity) getActivity()).getSavedValue(MainActivity.SET_SIM));
                break;
            case MainActivity.SET_PIN:
                //mCurrentLayout=R.layout.fragment_set_pin;
                break;
            case MainActivity.SET_PHONES:
                EditText lineP1=(EditText)(mRootView.findViewById(R.id.text_phone1));
                lineP1.setText(((SetProfileActivity)getActivity()).getSavedValue(MainActivity.SET_PHONE1));
                EditText lineP2=(EditText)(mRootView.findViewById(R.id.text_phone2));
                lineP2.setText(((SetProfileActivity)getActivity()).getSavedValue(MainActivity.SET_PHONE2));
                break;
            default :
                //getActivity().finish();
                break;
        }*/
        return mRootView;
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
            case MainActivity.SET_WARMER:
                String command= "M1-01";
                executeCommand(command);
                //new MyToast(getActivity(), ConnectDaemonService.getChinese(command) + "設定已完成指").info();
                new MyToast(getActivity(), //ConnectDaemonService.getChinese(command) +
                                            "暖氣設定已完成").info();
                break;
            case MainActivity.CMD_START_NOW:
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
