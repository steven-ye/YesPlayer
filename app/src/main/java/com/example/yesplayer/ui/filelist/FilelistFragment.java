package com.example.yesplayer.ui.filelist;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yesplayer.EmptyRecyclerView;
import com.example.yesplayer.PlayerActivity;
import com.example.yesplayer.R;
import com.example.yesplayer.ui.history.HistoryFragment;
import com.example.yesplayer.utils.CommonUtils;
import com.example.yesplayer.utils.FileAdapter;
import com.example.yesplayer.utils.FileInfo;
import com.example.yesplayer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilelistFragment extends Fragment {
    ContentResolver mContentResolver;
    FilelistViewModel listViewModel;
    List<FileInfo> fileList;
    private File rootPath;
    private File folder;
    private View viewGoUp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentResolver = getContext().getContentResolver();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        listViewModel = new ViewModelProvider(this).get(FilelistViewModel.class);
        View root = inflater.inflate(R.layout.fragment_filelist, container, false);

        viewGoUp = root.findViewById(R.id.go_parent);
        viewGoUp.setVisibility(View.GONE);
        viewGoUp.setOnClickListener(v->{
            scanFolder(folder.getParentFile());
        });

        final EmptyRecyclerView recyclerView = root.findViewById(R.id.rv_filelist);
        View emptyView = root.findViewById(R.id.text_list_empty);
        recyclerView.setEmptyView(emptyView);
        fileList = listViewModel.getList().getValue();
        FileAdapter fileAdapter = new FileAdapter(R.layout.item_file,fileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(fileAdapter);
        fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            FileInfo fileInfo = fileList.get(position);
            if(fileInfo.isDirectory()){
                scanFolder(fileInfo);
            }else{
                openFile(fileInfo);
            }
        });
        listViewModel.getList().observe(getViewLifecycleOwner(), new Observer<List<FileInfo>>() {
            @Override
            public void onChanged(List<FileInfo> fileInfos) {
                fileList.clear();
                fileList.addAll(fileInfos);
                fileAdapter.notifyDataSetChanged();
            }
        });

        Bundle args = getArguments();
        String type = args == null?"":args.getString("type");
        switch (type){
            case "video":
                //rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                listViewModel.setListValue(getVideos());
                break;
            case "audio":
                //rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                listViewModel.setListValue(getMusics());
                break;
            default:
                rootPath = Environment.getExternalStorageDirectory();
                //
                //rootPath = Environment.getRootDirectory();
                scanFolder(rootPath);
        }
        return root;
    }

    public void scanFolder(FileInfo info){
        scanFolder(new File(info.getPath()));
    }
    public void scanFolder(File file){
        System.out.println("Path: "+file.getPath());
        //File file = new File(file.getPath());
        File[] fileList = file.listFiles();
        if(fileList==null){
            Utils.showToast("???????????????"+file.getPath());
            Utils.alert(getContext(),"???????????????"+file.getPath());
            return;
        }
        folder = file;
        if(folder.getPath().equals(rootPath.getPath())){
            viewGoUp.setVisibility(View.GONE);
        }else{
            viewGoUp.setVisibility(View.VISIBLE);
        }
        List<FileInfo> list = new ArrayList<>();
        for(File f:fileList){
            FileInfo fileInfo = new FileInfo(f.getName(),f.getPath(),f.isDirectory());
            list.add(fileInfo);
        }
        listViewModel.setListValue(list);
    }

    public void openFile(FileInfo info){
        Utils.log("FileName: "+info.getPath());
        if (!CommonUtils.isMediaFile(info.getFileName()) && !CommonUtils.isMusicFile(info.getFileName())) {
            Utils.showToast("??????????????????????????????");
            return;
        }
        String videoUrl = "file://"+info.getPath();
        Intent intent = new Intent(getContext(), PlayerActivity.class);
        intent.putExtra("fileName", info.getFileName());
        intent.putExtra("videoUrl", videoUrl);
        startActivity(intent);
        // save the history
        HistoryFragment.saveHistory(info.getFileName(),info.getPath(),videoUrl,null);
    }

    /**
     * ????????????????????????
     * @return
     */
    public List<FileInfo> getMusics() {
        ArrayList<FileInfo> musics = new ArrayList<>();
        Cursor c = null;
        try {
            c = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));// ??????

                if (!new File(path).exists()) {
                    continue;
                }

                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)); // ?????????
                String album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)); // ??????
                String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); // ??????
                long size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));// ??????
                int duration = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));// ??????
                int time = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));// ?????????id
                // int albumId = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                //Music music = new Music(name, path, album, artist, size, duration);
                FileInfo music = new FileInfo(name,path,"file://"+path);
                musics.add(music);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return musics;
    }

    /**
     * ????????????????????????
     * @return
     */
    public List<FileInfo> getVideos() {

        List<FileInfo> videos = new ArrayList<>();

        Cursor c = null;
        try {
            // String[] mediaColumns = { "_id", "_data", "_display_name",
            // "_size", "date_modified", "duration", "resolution" };
            c = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));// ??????
                if (!new File(path).exists()) {
                    continue;
                }

                int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID));// ?????????id
                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)); // ????????????
                String resolution = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)); //?????????
                long size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));// ??????
                long duration = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// ??????
                long date = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));//????????????

                //FileInfo video = new FileInfo(id, path, name, resolution, size, date, duration);
                FileInfo video = new FileInfo(name,path,"file://"+path);
                videos.add(video);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return videos;
    }

    // ?????????????????????
    /*
    public Bitmap getVideoThumbnail(int id) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
        return bitmap;
    }*/
}