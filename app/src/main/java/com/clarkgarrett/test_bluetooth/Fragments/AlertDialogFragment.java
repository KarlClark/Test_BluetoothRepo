package com.clarkgarrett.test_bluetooth.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.clarkgarrett.test_bluetooth.Listeners.OnAlertDialogListener;
import com.clarkgarrett.test_bluetooth.R;

/**
 * Created by Karl on 5/28/2015.
 */
public class AlertDialogFragment extends DialogFragment{

    private int mTag;
    private OnAlertDialogListener mOnAlertDialogListener;
    private static final String STATEMENT_KEY = "statement_key";
    private static final String QUESTION_KEY ="question_key";
    private static final String POSITIVE_LABEL_KEY = "positive_label_key";
    private static final String NEGATIVE_LABEL_KEY = "negative_label_key";
    private static final String TAG_KEY ="tag_key";

    public static AlertDialogFragment newInstance(int statementResId, int questionResId, int positiveLabelResID, int negativeLabelResId, int tag){
        Bundle args = new Bundle();
        args.putInt(STATEMENT_KEY, statementResId);
        args.putInt(QUESTION_KEY, questionResId);
        args.putInt(POSITIVE_LABEL_KEY, positiveLabelResID);
        args.putInt(NEGATIVE_LABEL_KEY, negativeLabelResId);
        args.putInt(TAG_KEY, tag);
        AlertDialogFragment frag = new AlertDialogFragment();
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mOnAlertDialogListener = (OnAlertDialogListener)getTargetFragment();
        if (mOnAlertDialogListener == null) {
            mOnAlertDialogListener = (OnAlertDialogListener) getActivity();
        }
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_alert_dialog,null);
        Bundle args = getArguments();
        mTag = args.getInt(TAG_KEY);
        if (args.getInt(STATEMENT_KEY) != 0) {
            ((TextView) v.findViewById(R.id.tvStatement)).setText(args.getInt(STATEMENT_KEY));
        }
        if (args.getInt(QUESTION_KEY) != 0) {
            ((TextView) v.findViewById(R.id.tvQuestion)).setText(args.getInt(QUESTION_KEY));
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(v);

        alertDialogBuilder.setPositiveButton(args.getInt(POSITIVE_LABEL_KEY), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mOnAlertDialogListener.onAlertDialogPositiveClick(mTag);
            }
        });

        if (args.getInt(NEGATIVE_LABEL_KEY) != 0) {
            alertDialogBuilder.setNegativeButton(args.getInt(NEGATIVE_LABEL_KEY), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOnAlertDialogListener.onAlertDialogNegativeClick(mTag);
                }
            });
        }

        return alertDialogBuilder.create();
    }
}
