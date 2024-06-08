package com.example.sshfileexplorer.ui.dialogs;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.activities.ServerListActivity;

public class ServerDialog extends DialogFragment implements View.OnClickListener {
    String TAG = "TAG SSH EXPLORER";
    private View.OnClickListener btnAcceptListener = null;
    private View.OnClickListener btnConnectListener = null;
    private EditText ip_addr, ip_port, login, pass;
    private CheckBox save_pass;
    private String btnConnect = "Connect";
    private String btnAccept = "Add";
    private String title = "Add SSH server";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_server_add, container, false);

        try {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }catch (Exception e){Log.e(TAG, e.toString());}

        // buttons

        Button btn = view.findViewById(R.id.bConnect);
        btn.setOnClickListener(this);
        btn.setText(btnConnect);

        btn = view.findViewById(R.id.bAdd);
        if (btnAccept == null){
            btn.setVisibility(View.INVISIBLE);
        }else {
            btn.setOnClickListener(this);
            btn.setText(btnAccept);
        }

        ((TextView)view.findViewById(R.id.server_dialog_title)).setText(title);

        // input params

        ip_addr = (EditText)view.findViewById(R.id.ip_addr);
        ip_addr.addTextChangedListener(new EntryValidator(ip_addr));
        if (host_input != null) ip_addr.setText(host_input);

        ip_port = (EditText)view.findViewById(R.id.ip_port);
        ip_port.addTextChangedListener(new EntryValidator(ip_port));
        if (port_input != null) ip_port.setText(port_input);

        login = (EditText)view.findViewById(R.id.server_login);
        login.addTextChangedListener(new EntryValidator(login));
        if (login_input != null) login.setText(login_input);

        pass = (EditText)view.findViewById(R.id.server_pass);
        pass.addTextChangedListener(new EntryValidator(pass));
        if (pass_input != null) pass.setText(pass_input);

        save_pass = view.findViewById(R.id.save_pass);

        return view;
    }
    public void setTitle(CharSequence str){
        title = str.toString();
    }
    public void setButtonAccept(CharSequence str, View.OnClickListener listener){
        if (str != null) {
            btnAccept = str.toString();
        }else{
            btnAccept = null;
        }
        btnAcceptListener = listener;
    }
    public void setButtonConnect(CharSequence str, View.OnClickListener listener){
        btnConnect = str.toString();
        btnConnectListener = listener;
    }
    @Override
    public void onClick(View v) {
        if (ip_addr.length() == 0){
            Toast.makeText(getContext(), "IP address is incorrect!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ip_port.length() == 0){
            Toast.makeText(getContext(), "IP port is incorrect!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (login.length() == 0){
            Toast.makeText(getContext(), "Login is incorrect!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((pass.length() == 0)&&(save_pass.isChecked())){
            Toast.makeText(getContext(), "Password is incorrect!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((v.getId() == R.id.bConnect)){
            if (btnConnectListener != null) btnConnectListener.onClick(v);
        }else{
            if (btnAcceptListener != null) btnAcceptListener.onClick(v);
        }
        dismiss();
    }
    public static final int HOST = 0;
    public static final int PORT = 1;
    public static final int LOGIN = 2;
    public static final int PASS = 3;
    public static final int PASS_SAVE = 4;
    public Object get(int id){
        switch (id){
            case HOST:
                return ip_addr.getText().toString();
            case PORT:
                return ip_port.getText().toString();
            case LOGIN:
                return login.getText().toString();
            case PASS:
                return pass.getText().toString();
            case PASS_SAVE:
                return save_pass.isChecked();
            default:
                return null;
        }
    }
    private String host_input = null;
    private String port_input = null;
    private String login_input = null;
    private String pass_input = null;

    public void set(int id, Object o) {
        switch (id){
            case HOST:
                host_input=(String)o;
                break;
            case PORT:
                port_input=(String)o;
                break;
            case LOGIN:
                login_input=(String)o;
                break;
            case PASS:
                pass_input=(String)o;
                break;
            default:
                break;
        }
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