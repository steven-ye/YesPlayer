package com.example.yesplayer.ui.filelist;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yesplayer.EmptyRecyclerView;
import com.example.yesplayer.MyDialog;
import com.example.yesplayer.PlayerActivity;
import com.example.yesplayer.R;
import com.example.yesplayer.helper.HistoryHelper;
import com.example.yesplayer.object.MediaInfo;
import com.example.yesplayer.object.MusicInfo;
import com.example.yesplayer.service.HttpService;
import com.example.yesplayer.utils.FileUtils;
import com.example.yesplayer.object.FileInfo;
import com.example.yesplayer.utils.FileInfoAdapter;
import com.example.yesplayer.utils.ListSortUtils;
import com.example.yesplayer.utils.NetWorkUtils;
import com.example.yesplayer.utils.Utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilelistFragment extends Fragment {
    final private String TAG = getClass().getSimpleName();
    Context mContext;
    ContentResolver mContentResolver;
    FilelistViewModel viewModel;
    FileInfoAdapter fileAdapter;
    private File rootPath;
    private File folder;
    private View viewGoUp;
    String type;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        assert mContext != null;
        mContentResolver = mContext.getContentResolver();
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(FilelistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_filelist, container, false);

        viewGoUp = root.findViewById(R.id.go_parent);
        viewGoUp.setVisibility(View.GONE);
        viewGoUp.setOnClickListener(v->open(folder.getParentFile()));

        final EmptyRecyclerView recyclerView = root.findViewById(R.id.rv_filelist);
        View emptyView = root.findViewById(R.id.text_list_empty);
        recyclerView.setEmptyView(emptyView);
        List<FileInfo> fileList = HistoryHelper.getList();
        fileAdapter = new FileInfoAdapter(fileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(fileAdapter);
        fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            FileInfo fileInfo = fileList.get(position);
            if(fileInfo.isDirectory()){
                open(fileInfo);
            }else{
                play(fileInfo);
            }
        });
        fileAdapter.setOnItemActionClickListener((adapter, view, position) -> {
            FileInfo fileInfo = fileList.get(position);
            Log.d(TAG, "Delete " + fileInfo.getFileName());
            MyDialog dialog  = MyDialog.confirm("提示", "确定要删除此文件?");
            dialog.setOnClickListener(id -> {
                if(id == MyDialog.BUTTON_POSITIVE){
                    File file = new File(fileInfo.getPath());
                    if(file.delete()) {
                        fileList.remove(position);
                        fileAdapter.notifyItemRemoved(position);
                    }
                }
            });
            dialog.show(getParentFragmentManager());
        });

        Bundle args = getArguments();
        type = args == null?"":args.getString("type");
        switch (type){
            case "video":
                getVideos();
                break;
            case "audio":
                getMusics();
                break;
            default:
                fileAdapter.showAction(true);
                //rootPath = Environment.getExternalStoragePublicDirectory();
                //rootPath = Environment.getExternalStorageDirectory();
                //rootPath = Environment.getRootDirectory();
                rootPath = mContext.getExternalFilesDir(null);
                open(rootPath);
        }
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if(type.equals("") || type.equals("local")){
            inflater.inflate(R.menu.menu_file_local, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.item_refresh){
            Utils.showToast(getString(R.string.refresh));
            if(type.equals("")) open(rootPath);
            return true;
        }else if(item.getItemId() == R.id.item_server){
            startHttpServer();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void open(FileInfo info){
        open(new File(info.getPath()));
    }
    public void open(File file){
        if(null == file) return;
        System.out.println("Path: "+file.getPath());
        FileFilter fileFilter = file1 -> {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean showHidden = sp.getBoolean("setting_hidden", true);
            return showHidden || !file1.isHidden();
        };
        //File file = new File(file.getPath());
        File[] files = file.listFiles(fileFilter);
        if(files==null){
            Utils.showToast("无法打开："+file.getPath());
            Utils.alert(mContext,"无法打开："+file.getPath());
            return;
        }
        folder = file;
        if(folder.getPath().equals(rootPath.getPath())){
            viewGoUp.setVisibility(View.GONE);
        }else{
            viewGoUp.setVisibility(View.VISIBLE);
        }
        List<File> list = ListSortUtils.sort(files);
        fileAdapter.setFileList(list);
    }

    /**
     * 获取本机音乐列表
     */
    public void getMusics() {
        ArrayList<FileInfo> list = new ArrayList<>();
        try (Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )) {
            while (cursor.moveToNext()) {
                MusicInfo info = new MusicInfo(cursor);
                FileInfo music = new FileInfo(info.getName(), info.getPath(),"file://"+info.getPath());
                list.add(music);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileAdapter.setList(list);
    }


    /**
     * 获取本机视频列表
     */
    public void getVideos() {
        ArrayList<FileInfo> list = new ArrayList<>();
        //String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
        try (Cursor cursor = mContentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null, null,
                MediaStore.Video.Media.DEFAULT_SORT_ORDER
        )) {
            while (cursor.moveToNext()) {
                MediaInfo info= new MediaInfo(cursor);
                FileInfo fileInfo = new FileInfo(info.getName(),
                        info.getPath(),
                        "file://" + info.getPath());
                list.add(fileInfo);
                Log.d("Video path",
                        info.getUri().getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fileAdapter.setList(list);
    }

    // 获取视频缩略图
    /*
    public Bitmap getVideoThumbnail(int id) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
        return bitmap;
    }
    */

    public void play(FileInfo info){
        Utils.log("FileName: "+info.getPath());
        if (!FileUtils.isMediaFile(info.getFileName()) && !FileUtils.isMusicFile(info.getFileName())) {
            Utils.showToast("不是可播放的视频文件");
            return;
        }
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra("fileName", info.getFileName());
        intent.putExtra("videoUrl", info.getUrl());
        startActivity(intent);
        // save the history
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean settingHistory = sp.getBoolean("setting_history",true);
        if(settingHistory) {
            HistoryHelper.save(info);
        }
    }

    //端口号可以自定义
    HttpService http = new HttpService(9988);
    public void startHttpServer(){
        try {
            if(http.isAlive()) return;
            http.setUploadCallback((List<File> fileList)->{
                getActivity().runOnUiThread(() -> {
                    if(fileList.size()>0){
                        for(File f: fileList){
                            Utils.showToast("上传成功: " + f.getName());
                        }
                        open(rootPath);
                    }else{
                        Utils.showToast("没有上传文件");
                    }
                });
            });
            http.start();
            Utils.showToast("服务启动完成");
            //  启动完成后根据IP地址就可以访问到数据了
            String localIpAddress = NetWorkUtils.getLocalIpAddress(getContext());
            Log.d("localIpAddress:", "HttpServer服务启动完成 " + localIpAddress+":"+http.getListeningPort());
            MyDialog dialog = MyDialog.alert("文件上传服务已经开启", "请访问http://"+localIpAddress+":"+http.getListeningPort() + "。 上传文件时请不要关闭此弹窗！！！");
            dialog.setOnClickListener(view->{
                stopHttpServer();
            });
            dialog.show(getParentFragmentManager());
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("服务启动错误");
            Utils.showToast("服务启动错误");
        }
    }

    public void stopHttpServer(){
        if(http != null && http.isAlive()) {
            http.stop();
            Log.d(TAG, "HttpServer服务已经关闭");
            Utils.showToast("服务已经关闭");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopHttpServer();
    }
}