package com.example.yesplayer.ui.phone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

public class PhoneFragment extends Fragment {

    private PhoneViewModel phoneViewModel;
    private FileAdapter fileAdapter;
    private File rootPath;
    private File folder;
    private View viewGoUp;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        phoneViewModel =
                new ViewModelProvider(this).get(PhoneViewModel.class);
        View root = inflater.inflate(R.layout.fragment_phone, container, false);
        viewGoUp = root.findViewById(R.id.go_parent);
        viewGoUp.setVisibility(View.GONE);
        viewGoUp.setOnClickListener(v->{
            scanFolder(folder.getParentFile());
        });
        final View emptyView = root.findViewById(R.id.text_list_empty);
        final EmptyRecyclerView recyclerView = root.findViewById(R.id.rv_filelist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setEmptyView(emptyView);
        final List<FileInfo> fileList = phoneViewModel.getList().getValue();
        fileAdapter = new FileAdapter(R.layout.item_smb_file, fileList);
        fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            FileInfo fileInfo = fileList.get(position);
            if(fileInfo.isDirectory()){
                scanFolder(fileInfo);
            }else{
                openFile(fileInfo);
            }
        });
        recyclerView.setAdapter(fileAdapter);
        phoneViewModel.getList().observe(getViewLifecycleOwner(), files -> {
            fileList.clear();
            fileList.addAll(files);
            fileAdapter.notifyDataSetChanged();
        });

        rootPath = Environment.getExternalStorageDirectory();
        //rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        //rootPath = Environment.getRootDirectory();
        scanFolder(rootPath);
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
            Utils.showToast("无法打开："+file.getPath());
            Utils.alert(getContext(),"无法打开："+file.getPath());
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
        phoneViewModel.setList(list);
    }


    public void openFile(FileInfo info){
        Utils.log("FileName: "+info.getPath());
        if (!CommonUtils.isMediaFile(info.getFileName()) && !CommonUtils.isMusicFile(info.getFileName())) {
            Utils.showToast("不是可播放的视频文件");
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
}