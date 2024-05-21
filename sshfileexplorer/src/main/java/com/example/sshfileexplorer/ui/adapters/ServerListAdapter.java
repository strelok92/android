package com.example.sshfileexplorer.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sshfileexplorer.R;

import java.util.ArrayList;

public class ServerListAdapter extends BaseAdapter {
    String TAG = "TAG SSH EXPLORER";

    private LayoutInflater lInflater;
    private ArrayList<String[]> srvList;
    private OnRemoveListener removeListener = null;

    public ServerListAdapter(Context ctx){
        lInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        srvList = new ArrayList<>();
    }
    @Override
    public int getCount() {return srvList.size();}
    @Override
    public Object getItem(int position) {return srvList.get(position);}
    @Override
    public long getItemId(int position) {return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Create View

        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.fragment_server_list_item, parent, false);

            // Item select listener
            view.setOnClickListener(v -> {
                AdapterView.OnItemClickListener listener = ((AdapterView) parent).getOnItemClickListener();
                if (listener != null) {
                    listener.onItemClick((AdapterView) parent, v, position, position);
                }
            });

            // Remove item listener
            view.findViewById(R.id.bSrvItemDel).setOnClickListener(v -> {
                if (removeListener != null){
                    removeListener.onItemRemove((AdapterView) parent, v, position, position);
                }
            });
        }

        // Update item data on the View
        String[] item = srvList.get(position);

        ((TextView)view.findViewById(R.id.tSrvItemTitle)).setText(item[0]);
        ((TextView)view.findViewById(R.id.tSrvItemAddr)).setText(item[1]);

        return view;
    }
    public void addItem(@NonNull String title,@NonNull String data){srvList.add(new String[]{title, data});}
    public void deleteItem(long id){srvList.remove((int)id);}
    public void setOnRemoveListener(OnRemoveListener listener){
        removeListener = listener;
    }
}


