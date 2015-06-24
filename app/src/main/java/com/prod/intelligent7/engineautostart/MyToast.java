package com.prod.intelligent7.engineautostart;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by eric on 2015/6/11.
 */
public class MyToast  {

    Toast mToast;
    Context mContext;
    String myMsg;
    private MyToast()
    {

    }
    public MyToast(Context mCx, String msg){
        mContext=mCx;
        myMsg=" "+msg+" ";
        scale=STANDARD;
        mToast = new Toast(mCx);
    }

    static final int WARNING=-1;
    static final int STANDARD=0;
    static final int OK=1;
    int scale;
    void setInstance()
    {
        View layout;
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        if (scale==OK)
        layout = inflater.inflate(R.layout.toast_ok, null, false);
        else if (scale==WARNING)
            layout = inflater.inflate(R.layout.toast_warning, null, false);
        else layout = inflater.inflate(R.layout.toast_standard, null, false);
        // View  (ViewGroup) ((Activity) mContext).findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text_toast);
        if (scale==WARNING)
            text.setGravity(Gravity.BOTTOM);
        //text.setBackground(mContext.getResources().getDrawable());
        text.setText(myMsg);


        mToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(layout);
        //mToast.show();
    }
    public void info() {
        scale=STANDARD;
        setInstance();
        mToast.show();
    }
    public void warn() {
        scale=WARNING;
        setInstance();
        mToast.show();
    }
    public void showOK() {
        scale=OK;
        setInstance();
        mToast.show();
    }
    public void show() {
        scale=STANDARD;
        setInstance();
        mToast.show();
    }
}
