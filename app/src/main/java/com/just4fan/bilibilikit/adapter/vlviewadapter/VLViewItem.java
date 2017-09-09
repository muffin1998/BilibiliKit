package com.just4fan.bilibilikit.adapter.vlviewadapter;

import android.graphics.Bitmap;

import com.just4fan.bilibilikit.resource.StaticResouce;
import com.just4fan.bilibilikit.model.VideoList;

import java.io.File;

/**
 * Created by chuiq on 2017/9/6.
 */

public class VLViewItem {
    public static final int TYPE_VIDEO = 0x1;
    public static final int TYPE_VIDEOS = 0x2;
    public static final int TYPE_DRAMA = 0x3;
    public int type;
    public String avid;
    public String title;
    public String cover;
    public String size;
    public String danmaku_count;
    public String type_tag;
    public String is_completed;
    public File parts_dir;
    Bitmap cover_map;
    private VideoList videoList;
    public VLViewItem(int type) {
        cover_map = StaticResouce.cover;
        this.type = type;
    }

    public VLViewItem(long avid, String title, String cover, long size, long danmaku_count, String type_tag, boolean is_completed) {
        this.type = TYPE_VIDEO;
        cover_map = StaticResouce.cover;
        this.avid = avid + "";
        this.title = title;
        this.cover = cover;
        double m_size = 1.0*size/1024/1024;
        if(m_size >= 1024)
            this.size = String .format("%.1f",m_size/1024) + "GB";
        else
            this.size = String .format("%.0f",m_size) + "MB";
        this.danmaku_count = danmaku_count + "";
        this.type_tag = type_tag;
        this.is_completed = is_completed + "";
    }

    public VideoList getVideoList() {
        return videoList;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVideoList(VideoList list) {
        this.videoList = list;
    }

    public int getType() {
        return type;
    }

    public void setCover_map(Bitmap bitmap) {
        this.cover_map = bitmap;
    }
}
