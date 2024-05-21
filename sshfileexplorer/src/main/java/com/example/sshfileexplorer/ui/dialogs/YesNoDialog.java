package com.example.sshfileexplorer.ui.dialogs;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sshfileexplorer.R;

public class YesNoDialog extends DialogFragment {

    private Activity parentActivity;
    private Runnable btnYes, btnNo;

    public YesNoDialog(Activity activity, Runnable yes, Runnable no ) {
        parentActivity = activity;
        btnYes = yes;
        btnNo = no;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_yes_no_dialog, container, false);
    }
}