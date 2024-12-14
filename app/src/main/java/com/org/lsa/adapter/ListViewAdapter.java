package com.org.lsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.org.lsa.DashboardActivity;
import com.org.lsa.R;

public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private int[] img;
    private LayoutInflater layoutInflater;
    private String[] name;

    public ListViewAdapter(DashboardActivity mainActivity, String[] names, int[] images) {
        this.context = mainActivity;
        this.img = images;
        this.name = names;
        this.layoutInflater = LayoutInflater.from(mainActivity);
    }

    public int getCount() {
        return this.img.length;
    }

    public Object getItem(int position) {
        return Integer.valueOf(this.img[position]);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.layoutInflater.inflate(R.layout.adapter_custom_user, (ViewGroup) null);
            holder.vhName = (TextView) convertView.findViewById(R.id.itemuser_name);
            holder.vhImage = (ImageView) convertView.findViewById(R.id.itemuser_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.vhName.setText(this.name[position]);
        try {
            holder.vhImage.setImageResource(this.img[position]);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView vhImage;
        TextView vhName;

        ViewHolder() {
        }
    }
}
