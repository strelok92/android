package com.example.sshfileexplorer.ui.dialogs;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.sshfileexplorer.R;

public class ServerAddDialog extends DialogFragment implements View.OnClickListener {

    String TAG = "TAG DIALOG";

    private EditText ip_addr, ip_port, server_name;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_add_dialog, container, false);

        // buttons

        view.findViewById(R.id.bConnect).setOnClickListener(this);
        view.findViewById(R.id.bAdd).setOnClickListener(this);

        // input params

        ip_addr = (EditText)view.findViewById(R.id.ip_addr);
        ip_addr.addTextChangedListener(new EntryValidator(ip_addr));
        ip_addr.setBackgroundResource(R.drawable.dialog_entry_err);

        ip_port = (EditText)view.findViewById(R.id.ip_port);
        ip_port.addTextChangedListener(new EntryValidator(ip_port));

        server_name = (EditText)view.findViewById(R.id.server_name);

        return view;
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bConnect){
            Log.i(TAG, ip_addr.getText().toString());
            Log.i(TAG, ip_port.getText().toString());
            Log.i(TAG, server_name.getText().toString());
        } else if (v.getId() == R.id.bAdd){

        }
        dismiss();
    }


    private class EntryValidator implements TextWatcher {
        private EditText entry;
        EntryValidator(EditText view){entry = view;}
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0){
                entry.setBackgroundResource(R.drawable.dialog_entry);
            }else{
                entry.setBackgroundResource(R.drawable.dialog_entry_err);
            }
        }
    }
}