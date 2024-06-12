package com.example.sshfileexplorer.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sshfileexplorer.R;

import java.util.ArrayList;

public class FileListAdapter extends BaseAdapter {
    public interface FileItem{String getName(); int getSize();char getType(); String getDate();};
    public interface OnListener {void onEvent(AdapterView<?> parent, View view, int position, long id);}
    String TAG = "TAG SSH EXPLORER";
    private ArrayList<FileItem> list;
    private LayoutInflater inflater;
    private OnListener downloadListener = null;
    private OnListener selectListener = null;

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

            // Item long select listener
            view.setOnLongClickListener(v -> {
                AdapterView.OnItemLongClickListener click = ((AdapterView) parent).getOnItemLongClickListener();
                if (click != null) {
                    click.onItemLongClick((AdapterView) parent, v, position, position);
                }
                return true;
            });
            // Item select listener
            view.setOnClickListener(v -> {
                AdapterView.OnItemClickListener click = ((AdapterView) parent).getOnItemClickListener();
                if (click != null) {
                    click.onItemClick((AdapterView) parent, v, position, position);
                }
            });
            // Download file button
            view.findViewById(R.id.bFileDownload).setOnClickListener(v -> {
                if (downloadListener != null) {
                    downloadListener.onEvent((AdapterView) parent, v, position, position);
                }
            });
        }

        // Update view

        ImageButton bFileDownload = view.findViewById(R.id.bFileDownload);
        if (list.get(position).getType() == 'd') {
            bFileDownload.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.iFile).setVisibility(View.GONE);
            view.findViewById(R.id.iFolder).setVisibility(View.VISIBLE);
        }else{
            bFileDownload.setVisibility(View.VISIBLE);
            view.findViewById(R.id.iFile).setVisibility(View.VISIBLE);
            view.findViewById(R.id.iFolder).setVisibility(View.GONE);
        }
        ((TextView)view.findViewById(R.id.tFileName)).setText(list.get(position).getName());
        int size = list.get(position).getSize();

        String fileInfo = "";
        if (size < 1024){
            fileInfo += String.format("%d b  ", size);
        }else if (size < 1024*1024){
            fileInfo += String.format("%d Kb  ", size/1024);
        }else if (size < 1024*1024*1024){
            fileInfo += String.format("%d Mb  ", size/(1024*1024));
        }else{
            fileInfo += String.format("%d Gb  ", size/(1024*1024*1024));
        }
        fileInfo += list.get(position).getDate();
        ((TextView)view.findViewById(R.id.tFileSize)).setText(fileInfo);
        return view;
    }

    public void addItem(@NonNull FileItem file){list.add(file);}
    public void deleteItem(long id){list.remove((int)id);}
    public void clear(){list.clear();}
    public void setOnDownloadListener(OnListener onListener){
        downloadListener = onListener;}
}
