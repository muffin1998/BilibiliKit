package com.just4fan.bilibilikit.adapter.vlviewadapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.just4fan.bilibilikit.resource.StaticResouce;
import com.just4fan.bilibilikit.model.VideoList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
    private Handler coverSaveHandler;
    public VLViewItem(int type, Handler coverSaveHandler) {
        cover_map = StaticResouce.cover;
        this.coverSaveHandler = coverSaveHandler;
        this.type = type;
    }

    public VLViewItem(long avid, String title, String cover, long size, long danmaku_count, String type_tag, boolean is_completed, Handler coverSaveHandler) {
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
        this.coverSaveHandler = coverSaveHandler;
    }

    public void coverSave() {
        new Thread() {
            @Override
            public void run() {
                File cache = new File(StaticResouce.cachePath, cover.hashCode() + "");
                if(cache.exists()) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(cache);
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(StaticResouce.coverPath, cover.hashCode() + ""));
                        byte[] buf = new byte[512];
                        int len;
                        try {
                            while ((len = fileInputStream.read(buf)) != -1)
                                fileOutputStream.write(buf, 0, len);
                            fileInputStream.close();
                            fileOutputStream.close();
                            coverSaveHandler.sendEmptyMessage(1);
                            return;
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    coverSaveHandler.sendEmptyMessage(0);
                }
                else {
                    try {
                        String s = cover.replaceAll("\\\\", "");
                        URL url = new URL(s);
                        try {
                            InputStream inputStream = url.openStream();
                            File file = new File(StaticResouce.coverPath, s.hashCode() + ".jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            int len;
                            byte[] bytes = new byte[512];
                            while((len = inputStream.read(bytes, 0, 512)) != -1) {
                                fileOutputStream.write(bytes, 0, len);
                            }
                            coverSaveHandler.sendEmptyMessage(1);
                            return;
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    coverSaveHandler.sendEmptyMessage(0);
                }
            }
        }.start();
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
