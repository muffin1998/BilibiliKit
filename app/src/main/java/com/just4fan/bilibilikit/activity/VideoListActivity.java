package com.just4fan.bilibilikit.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.just4fan.bilibilikit.R;
import com.just4fan.bilibilikit.resource.StaticResouce;
import com.just4fan.bilibilikit.adapter.vlviewadapter.VLViewAdpater;
import com.just4fan.bilibilikit.adapter.vlviewadapter.VLViewItem;
import com.just4fan.bilibilikit.merge.flv.FLV;
import com.just4fan.bilibilikit.model.VideoList;

import java.io.File;
import java.util.List;

public class VideoListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        PopupMenu.OnMenuItemClickListener,
        DialogInterface.OnShowListener{

    //Tag
    String DEBUG_TAG = "VideoListActivity";

    //Data
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    int requestCode = 0x1;
    long clickedTime = 0;
    String videoPath = "Android//data//tv.danmaku.bili//download";
    String textMergeNoFile;
    String textMergeGetSegmentsError;
    String textMergeWriteError;
    String textMenuMerge;
    String textMenuCoverDownload;
    String textgetSegment;
    String textMerge;
    String textMergeCancelled;
    String textof;
    String textMergeFiles;
    String textDesPath;
    String textCoverSaveSuccess;
    String textCoverSaveFail;


    //Object
    VideoList videoList;
    VideoList parent_vl;
    List<VLViewItem> vlviewItemList;
    List<VLViewItem> parent_vi;
    VLViewItem curClicked;
    VLViewAdpater adpater;
    ListView video_listview;
    FLV flv;

    //View
    SwipeRefreshLayout layout;
    ProgressBar progressBar;
    TextView progressMessage;
    PopupMenu menu;
    AlertDialog mergeProgressDialog;

    //Handler
    Handler coverUpdateHandler;
    Handler mergeProgessHandler;
    Handler coverSaveHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int state = ContextCompat.checkSelfPermission(this, permissions[0]);
            if (state != PackageManager.PERMISSION_GRANTED)
                this.requestPermissions(permissions, requestCode);
        }
        setStaticResouce();
        Init();
        setContentView(R.layout.video_listview);
        getViews();
        Set();
    }

    private void setStaticResouce() {
        StaticResouce.cover = BitmapFactory.decodeResource(getResources(), R.drawable.default_cover);
        StaticResouce.cachePath = getExternalCacheDir();
        StaticResouce.videoPath = getExternalFilesDir("video");
        StaticResouce.coverPath = getExternalFilesDir("cover");
        StaticResouce.doubleClickSpan = 300;
    }

    private void getViews() {
        video_listview = (ListView)findViewById(R.id.video_listview);
        layout = (SwipeRefreshLayout)findViewById(R.id.video_listview_layout);
    }

    private void Set() {
        video_listview.setAdapter(adpater);
        video_listview.setOnItemClickListener(this);
        videoList.setHandler(coverUpdateHandler, coverSaveHandler);
        layout.setOnRefreshListener(this);
        mergeProgressDialog.setOnShowListener(this);
    }

    private void Init() {
        textMergeNoFile = getString(R.string.merge_video_no_files);
        textMergeGetSegmentsError = getString(R.string.merge_video_get_segments_fail);
        textMergeWriteError = getString(R.string.merge_video_write_error);
        textMenuMerge = getString(R.string.video_list_item_menu_merge);
        textMergeCancelled = getString(R.string.merge_video_dialog_cancel_success);
        textMenuCoverDownload = getString(R.string.video_list_item_menu_cover_save);
        textgetSegment = getString(R.string.merge_video_get_segments_info);
        textMerge = getString(R.string.merge_video);
        textof = getString(R.string.merge_video_of);
        textMergeFiles = getString(R.string.merge_video_files);
        textDesPath = getString(R.string.merge_des_path);
        textCoverSaveSuccess = getString(R.string.cover_save_success);
        textCoverSaveFail = getString(R.string.cover_save_fail);
        coverUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0)
                    return;
                else if(msg.what == 1)
                    ((BaseAdapter)video_listview.getAdapter()).notifyDataSetChanged();
            }
        };
        mergeProgessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == FLV.GET_SEGMENTS_INFO) {
                    progressBar.setProgress(0);
                    progressMessage.setText(R.string.merge_video_get_segments_info);
                }
                else if(msg.what == FLV.ERROR_GET_SEGMENTS) {
                    Toast.makeText(VideoListActivity.this, textMergeGetSegmentsError, Toast.LENGTH_SHORT).show();
                    mergeProgressDialog.dismiss();
                }
                else if(msg.what == FLV.ERROR_WRITE) {
                    Toast.makeText(VideoListActivity.this, textMergeWriteError, Toast.LENGTH_SHORT).show();
                    mergeProgressDialog.dismiss();
                }
                else if(msg.what == FLV.ERROR_NO_FILES) {
                    Toast.makeText(VideoListActivity.this, textMergeNoFile, Toast.LENGTH_SHORT).show();
                    mergeProgressDialog.dismiss();
                }
                else if(msg.what == FLV.MERGE_CANCEL) {
                    Toast.makeText(VideoListActivity.this, textMergeCancelled, Toast.LENGTH_SHORT).show();
                    mergeProgressDialog.dismiss();
                }
                else {
                    Bundle bundle = msg.getData();
                    int progress = bundle.getInt("Progress");
                    progressBar.setProgress(progress);
                    int num = bundle.getInt("No");
                    int count = bundle.getInt("Count");
                    if (progress == 100) {
                        progressMessage.setText(R.string.merge_completed);
                        mergeProgressDialog.dismiss();
                        new AlertDialog.Builder(VideoListActivity.this).setTitle(R.string.merge_completed).
                                setMessage(textDesPath + flv.getDesPath()).
                                setPositiveButton(R.string.merge_completed_dialog_button_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                }).
                                create().
                                show();
                    }
                    else
                        progressMessage.setText(textMerge + " " + num + " " + textof + " " + count + " " + textMergeFiles);
                }
            }
        };
        coverSaveHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0)
                    Toast.makeText(VideoListActivity.this, textCoverSaveFail, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(VideoListActivity.this, textCoverSaveSuccess, Toast.LENGTH_SHORT).show();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mergeProgressDialog = builder.setView(R.layout.video_merge_progress_dialog).
                setTitle(R.string.merging).
                setCancelable(false).
                setNegativeButton(R.string.merge_video_dialog_cancel, null).
                create();
        videoList = VideoList.getVideoList(new File(Environment.getExternalStorageDirectory(), videoPath),
                coverUpdateHandler,
                coverSaveHandler);
        vlviewItemList = videoList.getVlViewItemList();
        adpater = new VLViewAdpater(this, vlviewItemList);
    }

    @Override
    public void onRefresh() {
        videoList.Refresh();
        video_listview.invalidateViews();
        layout.setRefreshing(false);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        Button button = mergeProgressDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flv.setInterrupted(true);
                progressMessage.setText(R.string.merge_video_dialog_cancelling);
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        VLViewItem item = vlviewItemList.get((int)l);
        if(item.type == VLViewItem.TYPE_VIDEOS || item.type == VLViewItem.TYPE_DRAMA) {
            layout.setRefreshing(true);
            parent_vi = vlviewItemList;
            parent_vl = videoList;
            videoList = item.getVideoList();
            vlviewItemList = videoList.getVlViewItemList();
            //videoList.setHandler();
            video_listview.setAdapter(new VLViewAdpater(this, videoList.getVlViewItemList()));
            layout.setRefreshing(false);
        }
        else {
            curClicked = vlviewItemList.get(i);
            menu = new PopupMenu(this, view);
            menu.getMenu().add(0, 0, 0, textMenuMerge);
            menu.getMenu().add(0, 1, 1, textMenuCoverDownload);
            menu.setOnMenuItemClickListener(this);
            menu.show();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getOrder() == 0) {
            if("flv".equals(curClicked.v_type) || "blv".equals(curClicked.v_type)) {
                mergeProgressDialog.show();
                progressBar = (ProgressBar) mergeProgressDialog.findViewById(R.id.video_merge_progressBar);
                progressMessage = (TextView) mergeProgressDialog.findViewById(R.id.video_merge_message);
                flv = new FLV(curClicked.parts_dir, new File(StaticResouce.videoPath, curClicked.title + ".flv"));
                flv.setHandler(mergeProgessHandler);
                flv.Merge();
            }
        }
        else if(item.getOrder() == 1) {
            curClicked.coverSave();
        }
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        video_listview.setSelection(i);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {//点击的是返回键
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if(parent_vl != null && parent_vi != null) {
                    vlviewItemList = parent_vi;
                    videoList = parent_vl;
                    parent_vl = null;
                    parent_vi = null;
                    video_listview.setAdapter(adpater);
                    return true;
                }
                else {
                    long curTime = System.currentTimeMillis();
                    if(clickedTime != 0) {
                        if(curTime - clickedTime <= StaticResouce.doubleClickSpan) {
                            finish();
                        }
                    }
                    clickedTime = curTime;
                    Toast.makeText(this, R.string.double_back_exit, Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == this.requestCode) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.write_external_storage_denied, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
