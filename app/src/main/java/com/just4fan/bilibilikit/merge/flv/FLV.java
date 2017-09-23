package com.just4fan.bilibilikit.merge.flv;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.just4fan.bilibilikit.json.JSON;
import com.just4fan.bilibilikit.json.SyntaxException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Just4fan on 2017/9/8.
 */

public class FLV {
    final static String msg = "Skip Exception ";
    final static String msg1 = "Read Exception ";
    final static int FLV_HEADER_LEN = 9;
    final static int PRE_TAG_SIZE_LEN = 4;
    final static int TAG_TYPE_LEN = 0x1;
    final static int DATA_SIZE_LEN = 3;
    final static int TIMESTAMP_LEN = 4;
    final static int STREAM_ID_LEN = 3;
    final static int TAG_HEADER_LEN = 11;
    final static int TAG_SCRIPT = 0x12;
    final static int TAG_AUDIO = 0x8;
    final static int TAG_VIDEO = 0x9;
    final static int buf_size = 8192;
    public final static int ERROR_NO_FILES = -0x1;
    public final static int ERROR_GET_SEGMENTS = -0x2;
    public final static int ERROR_WRITE = -0x3;
    public final static int MERGE_CANCEL = -0x4;
    public final static int GET_SEGMENTS_INFO = 0x0;
    public final static int MERGING = 0x1;
    boolean interrupted = false;
    String suffix;
    File dir;
    File des;
    File[] flvs;
    List<Long> segment_list;
    Handler handler;

    public FLV(File dir, File des) {
        segment_list = new ArrayList<>();
        this.dir = dir;
        this.des = des;
    }

    private void deleteDes() {
        if (des.exists())
            des.delete();
    }

    public String getDesPath() {
        return des.getAbsolutePath();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    public boolean init() {
        flvs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File arg0) {
                if (arg0.getName().endsWith("blv"))
                    return true;
                return false;
            }

        });
        if (flvs.length != 0) {
            suffix = ".blv";
        } else {
            flvs = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File arg0) {
                    if (arg0.getName().endsWith("flv"))
                        return true;
                    return false;
                }

            });
            if (flvs.length != 0) {
                suffix = ".flv";
            } else {
                handler.sendEmptyMessage(FLV.ERROR_NO_FILES);
                return false;
            }
        }
        Arrays.sort(flvs, new Comparator<File>() {
            @Override
            public int compare(File arg0, File arg1) {
                String s1 = arg0.getName().replace(suffix, "");
                String s2 = arg1.getName().replace(suffix, "");
                if (Integer.parseInt(s1) > Integer.parseInt(s2))
                    return 1;
                return -1;
            }

        });
        handler.sendEmptyMessage(FLV.GET_SEGMENTS_INFO);
        File index = new File(dir, "index.json");
        String s = "";
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(index);
            InputStreamReader inputStreamReader;
            try {
                inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String temp;
                try {
                    while ((temp = bufferedReader.readLine()) != null)
                        s += temp;
                    bufferedReader.close();
                    inputStreamReader.close();
                    fileInputStream.close();
                    long t1 = new Date().getTime();
                    JSON Json = new JSON(s);
                    try {
                        Json.parse();
                        List array = (List) Json.getRoot().get("segment_list");
                        int len = array.size();
                        for (int i = 0; i < len; i++) {
                            Map<String, Object> segment_map = (Map) array.get(i);
                            long duration;
                            if (i > 0)
                                duration = (Long) segment_map.get("duration") + segment_list.get(i - 1);
                            else
                                duration = (Long) segment_map.get("duration");
                            segment_list.add(duration);
                        }
                        return true;
                    } catch (SyntaxException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(FLV.ERROR_GET_SEGMENTS);
        return false;
    }

    private long parseLong(byte[] bs, int digits) {
        long res = 0;
        for (int i = 0; i < digits; i++) {
            if (bs[i] < 0) {
                res += (bs[i] & 0xff) << 8 * (digits - i - 1);
            } else {
                res += bs[i] << 8 * (digits - i - 1);
            }
        }
        return res;
    }

    private void getTimeStamp(byte[] bs) {
        byte temp = bs[3];
        for (int i = TIMESTAMP_LEN - 2; i >= 0; i--)
            bs[i + 1] = bs[i];
        bs[0] = temp;
    }

    private void getTimeStampBack(byte[] bs) {
        byte temp = bs[0];
        for (int i = 0; i < TIMESTAMP_LEN - 1; i++)
            bs[i] = bs[i + 1];
        bs[3] = temp;
    }

    private void skip(BufferedInputStream bis, long len) throws IOException,SkipException {
        byte[] bs = new byte[8092];
        while (len > 8092)
            len -= bis.read(bs, 0, 8092);
        int read = bis.read(bs, 0, (int) len);
        if(read != len) {
            throw new SkipException(msg);
        }
    }

    private void write(BufferedInputStream bis, BufferedOutputStream bos, long len) throws IOException {
        byte[] bs = new byte[8092];
        while (len > 8092) {
            len -= bis.read(bs, 0, 8092);
            bos.write(bs, 0, 8092);
        }
        bis.read(bs, 0, (int) len);
        bos.write(bs, 0, (int) len);
    }

    private void removeScript(BufferedInputStream bis) throws SkipException, IOException, ReadException {
        long skipLen;
        int tagType = bis.read();
        if (tagType != TAG_SCRIPT)
            return;
        byte[] ds = new byte[DATA_SIZE_LEN];
        bis.read(ds, 0, DATA_SIZE_LEN);
        long dataSize = parseLong(ds, DATA_SIZE_LEN);
        skip(bis, TIMESTAMP_LEN + STREAM_ID_LEN + dataSize);
        byte[] pts = new byte[PRE_TAG_SIZE_LEN];
        bis.read(pts, 0, PRE_TAG_SIZE_LEN);
        long PreTagSize = parseLong(pts, PRE_TAG_SIZE_LEN);
        if (PreTagSize != dataSize + TAG_HEADER_LEN)
            throw new ReadException(msg1);
    }

    private void removeHeader(BufferedInputStream bis) throws IOException, SkipException, ReadException {
        long skipLen;
        long headerLen = 0;
        if (bis.skip(FLV_HEADER_LEN + PRE_TAG_SIZE_LEN) != FLV_HEADER_LEN + PRE_TAG_SIZE_LEN)
            throw new SkipException(msg);
        headerLen += FLV_HEADER_LEN + PRE_TAG_SIZE_LEN;
        for (int i = 0; i < 3; i++) {
            bis.read();
            byte[] ds = new byte[DATA_SIZE_LEN];
            bis.read(ds, 0, DATA_SIZE_LEN);
            long dataSize = parseLong(ds, DATA_SIZE_LEN);
            skip(bis, TIMESTAMP_LEN + STREAM_ID_LEN + dataSize);
            byte[] pts = new byte[PRE_TAG_SIZE_LEN];
            bis.read(pts, 0, PRE_TAG_SIZE_LEN);
            long PreTagSize = parseLong(pts, PRE_TAG_SIZE_LEN);
            if (PreTagSize != dataSize + TAG_HEADER_LEN)
                throw new ReadException(msg1);
            headerLen += dataSize + TAG_HEADER_LEN + PRE_TAG_SIZE_LEN;
        }
    }

    public void Merge() {
        new Thread() {
            @Override
            public void run() {
                if (!init())
                    return;
                Message msg;
                Bundle bundle;
                try {
                    if (interrupted) {
                        handler.sendEmptyMessage(FLV.MERGE_CANCEL);
                        deleteDes();
                        return;
                    }
                    FileInputStream fis = new FileInputStream(flvs[0]);
                    FileOutputStream fos = new FileOutputStream(des);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    msg = new Message();
                    bundle = new Bundle();
                    bundle.putInt("No", 1);
                    bundle.putInt("Progress", 0);
                    bundle.putInt("Count", flvs.length);
                    msg.setData(bundle);
                    msg.what = FLV.MERGING;
                    handler.sendMessage(msg);
                    byte[] head = new byte[100];
                    bis.read(head, 0, FLV_HEADER_LEN + PRE_TAG_SIZE_LEN);
                    bos.write(head, 0, FLV_HEADER_LEN + PRE_TAG_SIZE_LEN);
                    removeScript(bis);
                    byte[] buf = new byte[buf_size];
                    int len;
                    while ((len = bis.read(buf, 0, buf_size)) != -1)
                        bos.write(buf, 0, len);
                    bis.close();
                    for (int i = 1; i < flvs.length; i++) {
                        if (interrupted) {
                            handler.sendEmptyMessage(FLV.MERGE_CANCEL);
                            deleteDes();
                            return;
                        }
                        msg = new Message();
                        bundle = new Bundle();
                        bundle.putInt("No", i + 1);
                        bundle.putInt("Progress", (int) (i * 1.0 / flvs.length * 100));
                        bundle.putInt("Count", flvs.length);
                        msg.setData(bundle);
                        msg.what = FLV.MERGING;
                        handler.sendMessage(msg);
                        fis = new FileInputStream(flvs[i]);
                        bis = new BufferedInputStream(fis);
                        removeHeader(bis);
                        int tagType;
                        while ((tagType = bis.read()) != -1) {
                            bos.write(tagType);
                            byte[] ds = new byte[DATA_SIZE_LEN];
                            bis.read(ds, 0, DATA_SIZE_LEN);
                            bos.write(ds, 0, DATA_SIZE_LEN);
                            long dataSize = parseLong(ds, DATA_SIZE_LEN);
                            byte[] ts = new byte[TIMESTAMP_LEN];
                            bis.read(ts, 0, TIMESTAMP_LEN);
                            getTimeStamp(ts);
                            long timestamp = parseLong(ts, TIMESTAMP_LEN);
                            byte[] new_ts = getNewTS(timestamp + segment_list.get(i - 1));
                            bos.write(new_ts, 0, TIMESTAMP_LEN);
                            write(bis, bos, STREAM_ID_LEN + dataSize);
                            byte[] pts = new byte[PRE_TAG_SIZE_LEN];
                            bis.read(pts, 0, PRE_TAG_SIZE_LEN);
                            long PreTagSize = parseLong(pts, PRE_TAG_SIZE_LEN);
                            if (PreTagSize != dataSize + TAG_HEADER_LEN)
                                throw new ReadException(msg1);
                            bos.write(pts, 0, PRE_TAG_SIZE_LEN);
                        }
                        bis.close();
                        fis.close();
                    }
                    bos.flush();
                    bos.close();
                    msg = new Message();
                    bundle = new Bundle();
                    bundle.putInt("No", flvs.length);
                    bundle.putInt("Progress", (int) (flvs.length * 1.0 / flvs.length * 100));
                    bundle.putInt("Count", flvs.length);
                    msg.setData(bundle);
                    msg.what = FLV.MERGING;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    handler.sendEmptyMessage(ERROR_WRITE);
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private byte[] getNewTS(long ts) {
        byte[] res = new byte[TIMESTAMP_LEN];
        for (int i = 0; i < TIMESTAMP_LEN; i++) {
            long t0 = ts >> 8 * (TIMESTAMP_LEN - i - 1);
            long t1 = t0 << 8 * (TIMESTAMP_LEN - 1);
            res[i] = (byte) (t1 >> 8 * (TIMESTAMP_LEN - 1));
        }
        getTimeStampBack(res);
        return res;
    }
}