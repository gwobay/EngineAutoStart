package com.prod.intelligent7.engineautostart;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class MySimpleFragment extends Fragment {

    public MySimpleFragment()
    {
        super();
    }
    public static LinearLayout.LayoutParams mmParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
    public static LinearLayout.LayoutParams mwParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    public static LinearLayout.LayoutParams wwParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    public static LinearLayout.LayoutParams llParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    public static TableLayout.LayoutParams tbParams=new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    static LinearLayout mRoot=null;
    static Activity mActivity=null;
    static Context mContext=null;
    public void setActivity(Activity mv)
    {
        mActivity=mv;
        mContext=mv.getApplicationContext();
    }
    public void showBlinking(TextView v)
    {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.RESTART);
        anim.setRepeatCount(5);
        //anim.setRepeatCount(Animation.INFINITE);
        anim.setFillAfter(false);
        anim.setFillEnabled(false);
        v.setBackgroundColor(0x6166cfc0);
        v.startAnimation(anim);
        //mTextBackground=v.getBackground();
    }

    //int mTextBackground;
    public void stopBlinking(TextView v)
    {
        if (v.getAnimation()!=null)
            v.getAnimation().cancel();
        v.setAnimation(null);
        v.setBackgroundColor(0xf6c62f90);
        v.invalidate();
        //if (isLandScape)
        //v.setBackgroundResource(R.drawable.shape_rect_blue_large);
        //else
        //v.setBackgroundResource(R.drawable.shape_rect_blue);
        //v.setBackgroundColor(0xf600b984);
        //v.invalidate();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void restoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        //setRetainInstance(true);
        if (savedInstanceState != null)
            restoreInstanceState(savedInstanceState);
    }

    public void saveData() //should be overwritten by child
    {

    }
    public void backToMain()
    {
        MainActivity mAc=(MainActivity)getActivity();
        //mAc.setDefaultTitle();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        fragmentManager.beginTransaction().remove(this).commit();
        //fragmentManager.popBackStackImmediate();
        //mainUI = fragmentManager.findFragmentById(R.id.main_content_fragment);

        //Fragment aFg=fragmentManager.findFragmentById(R.id.main_content_fragment);//R.id.container);

        //mAc.setContentView(null);
        //mAc.setContentView(R.layout.activity_main);

        mAc.resetUiButton();

        if (mAc.mainUI!=null){
            LinearLayout aL= (LinearLayout)mAc.mainUI.getView();
            if (aL != null) {
                aL.removeAllViews();
                LinearLayout newV = ((MainActivityFragment) mAc.mainUI).repaintButtons();
                aL.addView(newV);
            }
        }

        mAc.setDefaultTitle();

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        ((MainActivity)getActivity()).setDefaultTitle();
    }

    protected int szKeySize;
    protected boolean isLargeScreen;
    protected boolean isLandScape;
    protected int mDisplayWidth;
    protected int mDisplayHeight;
    protected int keyPadWidth;
    protected LinearLayout.LayoutParams keyPadParams;

    protected int mLogBarHeight;
    protected void getMyScreenSize() //if land scape use 10-10 layout, otherwise use 4-4-4-4-4 layout
    {
        //Display display = getWindowManager().getDefaultDisplay();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        isLargeScreen=false;
        int iMaxSize=size.x;
        mDisplayWidth = size.x;
        mDisplayHeight = size.y;
        if (size.y > iMaxSize )iMaxSize=size.y;
        if (size.y > 850) isLargeScreen=true;
        keyPadWidth=mDisplayWidth/2;
        keyPadParams=new LinearLayout.LayoutParams(keyPadWidth, mDisplayHeight/4, 0.5f);
        if (mDisplayWidth>mDisplayHeight)
        {
            isLandScape=true;
            szKeySize=mDisplayHeight/10;
            //mTotalColumn=10; mTotalRow=2;
        }
        else
        {
            isLandScape=false;
            szKeySize=mDisplayHeight/15;
            //mTotalColumn=4; mTotalRow=5;
        }
    }
}
