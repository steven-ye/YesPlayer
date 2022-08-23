package com.example.yesplayer.ui.history;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yesplayer.EmptyRecyclerView;
import com.example.yesplayer.PlayerActivity;
import com.example.yesplayer.R;
import com.example.yesplayer.helper.HistoryHelper;
import com.example.yesplayer.service.SmbService;
import com.example.yesplayer.smb.SmbManager;
import com.example.yesplayer.smb.SmbServer;
import com.example.yesplayer.smb.info.SmbLinkInfo;
import com.example.yesplayer.object.FileInfo;
import com.example.yesplayer.utils.FileInfoAdapter;
import com.example.yesplayer.utils.LoadingDialog;
import com.example.yesplayer.utils.Utils;

import java.util.List;

public class HistoryFragment extends Fragment {
    HistoryViewModel historyViewModel;
    SmbLinkInfo smbLinkInfo;
    LoadingDialog loadingDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        loadingDialog = new LoadingDialog(getContext(),"加载中，请稍等 ...");

        List<FileInfo> fileList = HistoryHelper.getList();
        assert fileList != null;
        final EmptyRecyclerView recyclerView = root.findViewById(R.id.rv_history);
        recyclerView.setEmptyView(root.findViewById(R.id.text_list_empty));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        FileInfoAdapter fileAdapter = new FileInfoAdapter(R.layout.item_file, fileList);
        fileAdapter.showAction(true);
        recyclerView.setAdapter(fileAdapter);
        fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            FileInfo fileInfo = fileList.get(position);
            play(fileInfo);
        });
        fileAdapter.setOnItemActionClickListener((adapter, view, position) -> {
            FileInfo fileInfo = fileList.get(position);
            Log.d("Delete history", fileInfo.getFileName());
            if(HistoryHelper.remove(position)){
                fileList.remove(position);
                fileAdapter.notifyDataSetChanged();
                Utils.showToast("删除成功");
            }
        });

        smbLinkInfo = new SmbLinkInfo();
        smbLinkInfo.setDomain("");
        smbLinkInfo.setRootFolder("");
        smbLinkInfo.setAnonymous(true);
        startService();

        return root;
    }

    Intent intent;
    /**
     * 启动后台服务
     */
    private void startService() {
        intent = new Intent(getContext(), SmbService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
        } else {
            getActivity().startService(intent);
        }
    }
    /**
     * 停止后台服务
     */
    private void stopService() {
        if(intent==null)return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
        } else {
            getActivity().stopService(intent);
        }
    }

    private void play(FileInfo info){
        loadingDialog.show();
        if(!TextUtils.isEmpty(info.getIp())){
            smbLinkInfo.setIP(info.getIp());
            smbLinkInfo.setRootFolder(info.getPath()+"/");
            //info.setUrl("http://"+info.getIp()+info.getPath()+"/"+info.getFileName());
            playSmb(info);
        }else{
            playNormal(info);
        }
    }

    private void playNormal(FileInfo info){
        Intent intent = new Intent(getContext(), PlayerActivity.class);
        intent.putExtra("fileName", info.getFileName());
        intent.putExtra("path", info.getPath());
        intent.putExtra("videoUrl", info.getUrl());
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private void playSmb(FileInfo info){
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                SmbManager smbManager = SmbManager.getInstance();
                if(smbManager.linkStart(smbLinkInfo)){
                    smbManager.getController().getChildList(info.getPath());
                }
                getActivity().runOnUiThread(()->{
                    if(smbManager.isLinked()){
                        Log.v("连接成功", "IP is " + smbLinkInfo.getIP());
                        SmbServer.SMB_FILE_NAME = info.getFileName();
                        info.setUrl("http://"+SmbServer.SMB_IP+":"+SmbServer.SMB_PORT+"/smb/"+info.getFileName());
                        playNormal(info);
                    }else{
                        loadingDialog.cancel();
                        Log.e("连接失败", "IP is " + smbLinkInfo.getIP());
                        Utils.alert(getContext(), "连接失败: " + info.getFileName(), null);
                    }
                });
                return smbManager.isLinked();
            }
        }.execute();
    }

    public void onPause(){
        super.onPause();
        loadingDialog.cancel();
    }
    public void onStop() {
        super.onStop();
        stopService();
    }
}