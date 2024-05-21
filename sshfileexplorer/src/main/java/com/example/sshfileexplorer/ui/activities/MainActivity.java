package com.example.sshfileexplorer.ui.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.sshfileexplorer.ui.dialogs.YesNoDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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


        // todo read from base

        srvListAdapter.addItem("Opange PI", "192.142.23.55:22");
        srvListAdapter.addItem("Banana PI", "192.142.23.65:23");
        srvListAdapter.addItem("Ubuntu SSH", "192.142.23.25:23");

        ListView srvListView = findViewById(R.id.serversList);
        srvListView.setAdapter(srvListAdapter);
        srvListView.setOnItemClickListener((parent, view, position, id) -> {
            String[] list = (String[]) srvListAdapter.getItem(position);

            Log.d(TAG, String.format("%s (%s)", list[0], list[1]));

            // todo open file system activity
            Intent intent = new Intent(this, FileExplorerActivity.class);
            startActivity(intent);

        });
        srvListAdapter.setOnRemoveListener((parent, view, position, id) -> {
            YesNoDialog dialog = new YesNoDialog();
            String[] list = (String[]) srvListAdapter.getItem(position);

            dialog.setTitle("Remove SSH server");
            dialog.setMessage("Do you want to remove \'" + list[0] + "\'?");

            dialog.setButtonYes("Yes", v -> {
                srvListAdapter.deleteItem(id);
                srvListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });
            dialog.setButtonNo("No", v -> dialog.dismiss());

            dialog.show(getSupportFragmentManager(), "");
        });
    }


    public void addServer(String name, String ip_addr, String ip_port){

        // todo check is exist
        // todo add server to base

        srvListAdapter.addItem(name, ip_addr + ":"+ip_port);
        srvListAdapter.notifyDataSetChanged();
    }
}