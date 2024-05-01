package com.example.compass;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class MainActivityDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private MainActivity mainActivity;
    private DialogInterface.OnClickListener onClickListener=null;

    public static final int TYPE_CLEAR = 0;
    public static final int TYPE_CALIB = 1;

    private int type;

    MainActivityDialog(int dType){
        type = dType;
    }
    @NonNull
    public Dialog onCreateDialog(Bundle instance){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (type == TYPE_CLEAR) {
            return builder.setTitle("Message")
                    .setMessage(R.string.want_clear)
                    .setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.no, this)
                    .create();
        }else if (type == TYPE_CALIB){
            return builder.setTitle("Message")
                    .setMessage(R.string.want_calib)
                    .setPositiveButton(R.string.yes, this)
                    .setNegativeButton(R.string.no, this)
                    .create();
        }else{
            return builder.setTitle("Message")
                    .setMessage("UNKNOWN")
                    .create();
        }
    }

    public void setOnDialogListener(DialogInterface.OnClickListener listener){
        onClickListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (onClickListener != null) {
            onClickListener.onClick(dialog, which);
        }
    }
}
