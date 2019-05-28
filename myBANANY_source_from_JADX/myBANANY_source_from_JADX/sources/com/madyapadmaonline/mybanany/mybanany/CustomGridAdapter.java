package com.madyapadmaonline.mybanany.mybanany;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class CustomGridAdapter extends BaseAdapter {
    static LayoutInflater inflater = null;
    Context context;
    ArrayList<String> itemNames = new ArrayList();

    public CustomGridAdapter(MainActivity mainActivity, ArrayList<String> itemNames) {
        this.context = mainActivity;
        this.itemNames = itemNames;
        inflater = (LayoutInflater) this.context.getSystemService("layout_inflater");
    }

    public int getCount() {
        return this.itemNames.size();
    }

    public Object getItem(int position) {
        return Integer.valueOf(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(C0322R.layout.grid_item, null);
        TextView textView = (TextView) view.findViewById(C0322R.id.ftpServerName);
        ((ImageView) view.findViewById(C0322R.id.ftpFolderImage)).setImageResource(C0322R.drawable.ftp_server);
        textView.setText((CharSequence) this.itemNames.get(position));
        return view;
    }
}
