package com.madyapadmaonline.mybanany.mybanany;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class CustomArrayAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<Integer> imgid;
    private final ArrayList<String> itemSize;
    private final ArrayList<String> itemname;
    public ArrayList<Integer> selectedItem = new ArrayList();

    public CustomArrayAdapter(Activity context, ArrayList<String> itemname, ArrayList<String> itemSize, ArrayList<Integer> imageID) {
        super(context, C0322R.layout.list_item, itemname);
        this.context = context;
        this.itemname = itemname;
        this.itemSize = itemSize;
        this.imgid = imageID;
    }

    public void setSelectedItem(ArrayList<Integer> selectedItem) {
        this.selectedItem = selectedItem;
    }

    public View getView(int position, View view, ViewGroup parent) {
        View rowView = this.context.getLayoutInflater().inflate(C0322R.layout.list_item, null, true);
        TextView textView1 = (TextView) rowView.findViewById(C0322R.id.textView_fileSize);
        ImageView imageView = (ImageView) rowView.findViewById(C0322R.id.imageView_fileicon);
        ((TextView) rowView.findViewById(C0322R.id.textView_filename)).setText((CharSequence) this.itemname.get(position));
        textView1.setText((CharSequence) this.itemSize.get(position));
        imageView.setImageResource(((Integer) this.imgid.get(position)).intValue());
        if (this.selectedItem.contains(Integer.valueOf(position))) {
            rowView.setBackgroundColor(Color.parseColor("#c2e184"));
        }
        return rowView;
    }
}
