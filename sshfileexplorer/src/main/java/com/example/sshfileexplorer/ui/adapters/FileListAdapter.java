package com.example.sshfileexplorer.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sshfileexplorer.R;

import java.util.ArrayList;

import helpers.SSHHelper;

public class FileListAdapter extends BaseAdapter {
    public interface OnListener {void onEvent(AdapterView<?> parent, View view, int position, long id);}
    String TAG = "TAG SSH EXPLORER";
    private ArrayList<SSHHelper.LSFile> list;
    private LayoutInflater inflater;
    private OnListener listener = null;

    public FileListAdapter(Context ctx){
        inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        list = new ArrayList<>();
    }
    @Override
    public int getCount() {return list.size();}
    @Override
    public Object getItem(int position) {return list.get(position);}
    @Override
    public long getItemId(int position) {return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Create View
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_item_file, parent, false);

            // Item select listener
            view.setOnClickListener(v -> {
                AdapterView.OnItemClickListener click = ((AdapterView) parent).getOnItemClickListener();
                if (click != null) {
                    click.onItemClick((AdapterView) parent, v, position, position);
                }
            });
            // Download file button
            view.findViewById(R.id.bFileDownload).setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEvent((AdapterView) parent, v, position, position);
                }
            });
        }

        // Update view

        ImageButton bFileDownload = view.findViewById(R.id.bFileDownload);
//        if (list.get(position).getType() == SSHHelper.LSFile.TYPE_FILE) {
            bFileDownload.setVisibility(View.VISIBLE);
//        }else{
//            bFileDownload.setVisibility(View.INVISIBLE);
//        }
        ((TextView)view.findViewById(R.id.tFileName)).setText(list.get(position).getName());
        ((TextView)view.findViewById(R.id.tFileSize)).setText(String.format("%d", list.get(position).getSize()));
        ((TextView)view.findViewById(R.id.tFileInfo)).setText(list.get(position).getDateTime());

        return view;
    }

    public void addItem(@NonNull SSHHelper.LSFile file){list.add(file);}
    public void deleteItem(long id){list.remove((int)id);}
    public void clear(){list.clear();}
    public void setOnDownloadListener(OnListener onListener){listener = onListener;}
}
