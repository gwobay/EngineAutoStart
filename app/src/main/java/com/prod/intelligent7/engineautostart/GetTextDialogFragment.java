package com.prod.intelligent7.engineautostart;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

/**
 * Created by eric on 5/25/2015.
 */
public class GetTextDialogFragment extends DialogFragment {

    public GetTextDialogFragment(){}
// use contructor to set the attrs

    View mViewFromXml; //has textView for data entry

    //public GetTextDialogFragment(View xmlV)
   // {
        //mViewFromXml=xmlV;
   // }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */

    public interface GetTextDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    GetTextDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the GetTextDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the GetTextDialogListener so we can send events to the host
            mListener = (GetTextDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement GetTextDialogListener");
        }
    }

    public static final String DATA_ENTRY_LAYOUT="ENTRY_LAYOUT_XML";
    public static final String DATA_ENTRY_FIELDS="DATA_ENTRY_FIELDS";
    public static final String DATA_ENTERED="DATA_ENTERED";
    public static final String DATA_ENTRY_LINE1="DATA_ENTRY_LINE1";
    public static final String DATA_ENTRY_LINE2="DATA_ENTRY_LINE2";
    public static final String DATA_ENTRY_LINE3="DATA_ENTRY_LINE3";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers

        mViewFromXml=null;
        int xmlId=getArguments().getInt(DATA_ENTRY_LAYOUT, 0);
        //caller should provide the view id following the command String e.g., SET_SIM<z>xml view ID
        // //the label name string and eidtText field id in pair; e.g., sim_card<z>edit text field ID
        if (xmlId > 0){
            mViewFromXml=getActivity().getLayoutInflater().inflate(xmlId, null);
        }
        final int[] fieldIds=getArguments().getIntArray(DATA_ENTRY_FIELDS);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //add set view here to
        if (mViewFromXml != null)
        builder.setView(mViewFromXml);
                // take care by the xml TextView label
        else builder.setMessage(R.string.confirm);

        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                String[] dataRead=new String[fieldIds.length];
                for (int i=0; i<fieldIds.length; i++) {
                    TextView getText1 =(TextView) mViewFromXml.findViewById(fieldIds[i]);
                    if (getText1 != null)
                    {
                        dataRead[i]=(String)getText1.getText();
                    }
                }
                GetTextDialogFragment.this.getArguments().putStringArray(DATA_ENTERED, dataRead);
                mListener.onDialogPositiveClick(GetTextDialogFragment.this);
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(GetTextDialogFragment.this);
                    }
                });

        return builder.create();
    }
}