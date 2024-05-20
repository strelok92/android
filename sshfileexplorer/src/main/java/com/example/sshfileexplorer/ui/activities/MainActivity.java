package com.example.sshfileexplorer.ui.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.dialogs.ServerAddDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

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
                ServerAddDialog dialog = new ServerAddDialog();
                dialog.show(getSupportFragmentManager(), "");
            }
        });

        // Create servers list

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> m;
        // item 1
        m = new HashMap<>();
        m.put("tSrvItemTitle", "OrangePI");
        m.put("tSrvItemAddr", "192.168.1.23:22");
        data.add(m);

        // item 2
        m = new HashMap<>();
        m.put("tSrvItemTitle", "BananaPI");
        m.put("tSrvItemAddr", "192.168.10.54:225");
        data.add(m);

        String from[] = {"tSrvItemTitle", "tSrvItemAddr"};
        int to[] = {R.id.tSrvItemTitle, R.id.tSrvItemAddr, };

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.fragment_server_list_item,
                from, to);

        ListView srvList = findViewById(R.id.serversList);
        srvList.setAdapter(adapter);

    }
}