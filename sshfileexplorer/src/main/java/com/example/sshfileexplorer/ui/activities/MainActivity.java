package com.example.sshfileexplorer.ui.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.adapters.ServerListAdapter;
import com.example.sshfileexplorer.ui.dialogs.ServerAddDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String TAG = "TAG SSH EXPLORER";

    private ServerListAdapter srvListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // title bar

        try {
            ActionBar bar = getSupportActionBar();
            bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.dialog_fg)));
            bar.setTitle("Select server");
        }catch (Exception e){
            // No action bar, nothing modify
        }

        // "Add server" button

        FloatingActionButton btnAddServer = findViewById(R.id.btnAddServer);
        btnAddServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerAddDialog dialog = new ServerAddDialog(MainActivity.this);
                dialog.show(getSupportFragmentManager(), "");
            }
        });


        // Servers list

        srvListAdapter = new ServerListAdapter(this);

        srvListAdapter.addItem("Opange PI", "192.142.23.55:22");
        srvListAdapter.addItem("Banana PI", "192.142.23.65:23");
        srvListAdapter.addItem("Ubuntu SSH", "192.142.23.25:23");


        ListView srvListView = findViewById(R.id.serversList);
        srvListView.setAdapter(srvListAdapter);
        srvListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] list = (String[]) srvListAdapter.getItem(position);
                Log.w(TAG, String.format("%s (%s)", list[0], list[1]));
            }
        });
    }


    public void addServer(String name, String ip_addr, String ip_port){

        // todo check is exist
        // todo add server to base

        srvListAdapter.addItem(name, ip_addr + ":"+ip_port);
        srvListAdapter.notifyDataSetChanged();
    }

}