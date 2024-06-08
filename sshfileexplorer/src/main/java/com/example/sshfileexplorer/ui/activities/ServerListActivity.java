package com.example.sshfileexplorer.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.adapters.ServerListAdapter;
import com.example.sshfileexplorer.ui.dialogs.ServerDialog;
import com.example.sshfileexplorer.ui.dialogs.YesNoDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

import services.SSHService;

public class ServerListActivity extends AppCompatActivity {

    String TAG = "TAG SSH EXPLORER";

    private ServerListAdapter srvListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_server_list);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });


            // title bar
//        try {
//            ActionBar bar = getSupportActionBar();
////            bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.dialog_fg)));
//            bar.setTitle("Select server");
//        }catch (Exception e){
//            // No action bar, nothing modify
//        }

            this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // for background and corner support

            // Create new server

            // onAddDialog
            FloatingActionButton btnAddServer = findViewById(R.id.btnAddServer);
            btnAddServer.setOnClickListener(v -> {
                ServerDialog dialog = new ServerDialog();
                dialog.setButtonConnect("Connect", (view)->{
                    String host = (String)dialog.get(ServerDialog.HOST);
                    String port = (String)dialog.get(ServerDialog.PORT);
                    String login = (String)dialog.get(ServerDialog.LOGIN);
                    String pass = (String)dialog.get(ServerDialog.PASS);
                    dialog.dismiss();

                    if ((Boolean) dialog.get(ServerDialog.PASS_SAVE)) {
                        saveServer(-1, host, port, login, pass);
                    }else{
                        saveServer(-1, host, port, login, "");
                    }
                    connect(host, port, login, pass);
                });
                dialog.setButtonAccept("Save", (view)->{
                    String host = (String)dialog.get(ServerDialog.HOST);
                    String port = (String)dialog.get(ServerDialog.PORT);
                    String login = (String)dialog.get(ServerDialog.LOGIN);
                    String pass = (String)dialog.get(ServerDialog.PASS);
                    dialog.dismiss();

                    if ((Boolean) dialog.get(ServerDialog.PASS_SAVE)) {
                        saveServer(-1, host, port, login, pass);
                    }else{
                        saveServer(-1, host, port, login, "");
                    }
                });
                dialog.show(getSupportFragmentManager(), "");
            });

            // Servers list
            srvListAdapter = new ServerListAdapter(this);
            ListView srvListView = findViewById(R.id.serversList);
            srvListView.setAdapter(srvListAdapter);
            srvListView.setOnItemClickListener((parent, view, position, id) -> {
                try {
                    Map item = srvListAdapter.getItem(position);
                    connect(
                            (String) item.get("host"),
                            (String) item.get("port"),
                            (String) item.get("login"),
                            (String) item.get("pass")
                    );
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            });

            // Edit server

            // onEditDialog
            srvListAdapter.setOnEditListener((parent, view, position) -> {
                ServerDialog dialog = new ServerDialog();
                dialog.setTitle("Edit SSH server");
                try {
                    Map item = srvListAdapter.getItem(position);

                    dialog.set(ServerDialog.HOST, item.get("host"));
                    dialog.set(ServerDialog.PORT, item.get("port"));
                    dialog.set(ServerDialog.LOGIN,item.get("login"));
                    dialog.set(ServerDialog.PASS, item.get("pass"));
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                // onConnect
                dialog.setButtonConnect("Connect", (v)->{
                    String host = (String)dialog.get(ServerDialog.HOST);
                    String port = (String)dialog.get(ServerDialog.PORT);
                    String login = (String)dialog.get(ServerDialog.LOGIN);
                    String pass = (String)dialog.get(ServerDialog.PASS);
                    dialog.dismiss();

                    if ((Boolean) dialog.get(ServerDialog.PASS_SAVE)) {
                        saveServer(position, host, port, login, pass);
                    }else{
                        saveServer(position, host, port, login, "");
                    }
                    connect(host, port, login, pass);
                });

                // onSave
                dialog.setButtonAccept("Save", (v)->{
                    String host = (String)dialog.get(ServerDialog.HOST);
                    String port = (String)dialog.get(ServerDialog.PORT);
                    String login = (String)dialog.get(ServerDialog.LOGIN);
                    String pass = (String)dialog.get(ServerDialog.PASS);
                    dialog.dismiss();

                    if ((Boolean) dialog.get(ServerDialog.PASS_SAVE)) {
                        saveServer(position, host, port, login, pass);
                    }else{
                        saveServer(position, host, port, login, "");
                    }
                });
                dialog.show(getSupportFragmentManager(), "");
            });

            // onRemove
            srvListAdapter.setOnRemoveListener((parent, view, position) -> {
                YesNoDialog dialog = new YesNoDialog();
                String msg = "Do you want to remove \n";
                try {
                    Map item = srvListAdapter.getItem(position);
                    msg += (String) item.get("login");
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                msg += " ?";

                dialog.setTitle("Remove SSH server");
                dialog.setMessage(msg);

                dialog.setButtonYes("Yes", v -> {
                    srvListAdapter.deleteItem(position);
                    srvListAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                });
                dialog.setButtonNo("No", v -> dialog.dismiss());

                dialog.show(getSupportFragmentManager(), "");
            });
    }

    private void saveServer(int pos, String ip_addr, String ip_port, String login, String pass){
        Map item = new HashMap<>();

            item.put("host", ip_addr);
            item.put("port", ip_port);
            item.put("login", login);
            item.put("pass", pass);

            if (pos < 0) {
                srvListAdapter.addItem(item);
            }else{
                srvListAdapter.editItem(pos,item);
            }
            srvListAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, SSHService.class));
        super.onDestroy();
    }

    private void connect(@NonNull String host, @NonNull String port, @NonNull String login, @NonNull String pass){
        Log.d(TAG, String.format("connect to '%s:%s' (%s, %s)", host, port, login, pass));

        if (pass.length() == 0){
            Toast.makeText(this, "Password not entry!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent ssh = new Intent(this, SSHService.class);
        ssh.putExtra("host", host);
        ssh.putExtra("port", Integer.parseInt(port));
        ssh.putExtra("timeout", 1000);
        ssh.putExtra("login", login);
        ssh.putExtra("pass", pass);
        startService(ssh);

        // Run file explorer activity and connect to remote SSH server
        startActivity(new Intent(this, FileExplorerActivity.class));
    }
}