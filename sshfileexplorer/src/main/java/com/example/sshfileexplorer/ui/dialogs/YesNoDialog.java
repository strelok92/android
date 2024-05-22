package com.example.sshfileexplorer.ui.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.sshfileexplorer.R;

public class YesNoDialog extends DialogFragment{
    String TAG = "TAG SSH EXPLORER";
    private String title = null;
    private String message = null;
    private String btnYes = null;
    private String btnNo = null;
    private View.OnClickListener btnYesListener = null;
    private View.OnClickListener btnNoListener = null;
    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_yes_no, container, false);

        try {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }catch (Exception e){
            // todo add code
        }

        if (title != null) ((TextView)view.findViewById(R.id.titleYesNo)).setText(title);
        if (message != null) ((TextView)view.findViewById(R.id.msgYesNo)).setText(message);

        Button btn = (Button)view.findViewById(R.id.bYes);

        if (btnYes == null) {
            btn.setVisibility(View.INVISIBLE);
        }else{
            btn.setText(btnYes);
            btn.setOnClickListener(btnYesListener);
        }

        btn = (Button)view.findViewById(R.id.bNo);
        if (btnNo == null) {
            btn.setVisibility(View.INVISIBLE);
        }else{
            btn.setText(btnNo);
            btn.setOnClickListener(btnNoListener);
        }
        return view;
    }

    public void setTitle(CharSequence str){
        title = str.toString();
    }
    public void setMessage(CharSequence str){
        message = str.toString();
    }
    public void setButtonYes(CharSequence str, View.OnClickListener listener){
        btnYes = str.toString();
        btnYesListener = listener;
    }
    public void setButtonNo(CharSequence str, View.OnClickListener listener){
        btnNo = str.toString();
        btnNoListener = listener;
    }
}