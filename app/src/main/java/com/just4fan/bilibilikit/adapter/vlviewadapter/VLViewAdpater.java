package com.just4fan.bilibilikit.adapter.vlviewadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.just4fan.bilibilikit.R;

import java.util.List;

/**
 * Created by chuiq on 2017/9/6.
 */

public class VLViewAdpater extends BaseAdapter {
    List<VLViewItem> mList;
    LayoutInflater mInflater;
    public VLViewAdpater(Context context, List<VLViewItem> mList) {
        this.mList=mList;
        mInflater=LayoutInflater.from(context);
    }
    public int getCount() {
        if(mList==null)
            return 0;
        return mList.size();
    }
    public Object getItem(int position) {
        if(mList==null)
            return null;
        return mList.get(position);
    }
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView,ViewGroup viewGroup) {
        if(mList==null)
            return null;
        if(convertView == null) {
            convertView=mInflater.inflate(R.layout.video_listview_item,null);
            VLViewHolder holder=new VLViewHolder();
            holder.cover=convertView.findViewById(R.id.video_listview_item_cover);
            holder.title=convertView.findViewById(R.id.video_listview_item_title);
            holder.danmaku_count =convertView.findViewById(R.id.video_listview_item_danmaku_count);
            holder.size = convertView.findViewById(R.id.video_listview_item_size);
            convertView.setTag(holder);
            if(mList.get(position).type == VLViewItem.TYPE_VIDEO) {
                holder.danmaku_count.setText(mList.get(position).danmaku_count);
                holder.cover.setImageBitmap(mList.get(position).cover_map);
                holder.title.setText(mList.get(position).title);
                holder.size.setText(mList.get(position).size);
            }
            else {
                holder.cover.setImageBitmap(mList.get(position).cover_map);
                holder.title.setText(mList.get(position).title);
            }
        }
        else {
            VLViewHolder holder=(VLViewHolder)convertView.getTag();
            if(mList.get(position).type == VLViewItem.TYPE_VIDEO) {
                holder.danmaku_count.setText(mList.get(position).danmaku_count);
                holder.cover.setImageBitmap(mList.get(position).cover_map);
                holder.title.setText(mList.get(position).title);
                holder.size.setText(mList.get(position).size);
            }
            else {
                holder.cover.setImageBitmap(mList.get(position).cover_map);
                holder.title.setText(mList.get(position).title);
            }
        }
        return convertView;
    }
}
