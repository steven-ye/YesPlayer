package com.example.yesplayer.ui.history;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yesplayer.Config;
import com.example.yesplayer.EmptyRecyclerView;
import com.example.yesplayer.IApplication;
import com.example.yesplayer.PlayerActivity;
import com.example.yesplayer.R;
import com.example.yesplayer.service.SmbService;
import com.example.yesplayer.smb.SmbServer;
import com.example.yesplayer.utils.FileAdapter;
import com.example.yesplayer.utils.FileInfo;
import com.example.yesplayer.utils.LoadingDialog;
import com.example.yesplayer.utils.SPUtils;
import com.example.yesplayer.utils.Utils;
import com.xyoye.libsmb.SmbManager;
import com.xyoye.libsmb.info.SmbLinkInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class HistoryFragment extends Fragment {
    HistoryViewModel historyViewModel;
    SmbLinkInfo smbLinkInfo;
    LoadingDialog loadingDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        loadingDialog = new LoadingDialog(getContext(),"????????????????????? ...");

        List<FileInfo> fileList = historyViewModel.getList().getValue();
        assert fileList != null;
        final EmptyRecyclerView recyclerView = root.findViewById(R.id.rv_history);
        recyclerView.setEmptyView(root.findViewById(R.id.text_list_empty));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        FileAdapter fileAdapter = new FileAdapter(R.layout.item_smb_file, fileList);
        recyclerView.setAdapter(fileAdapter);
        fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            FileInfo fileInfo = fileList.get(position);
            play(fileInfo);
        });
        historyViewModel.getList().observe(getViewLifecycleOwner(), objects -> {
            fileList.clear();
            fileList.addAll(objects);
            fileAdapter.notifyDataSetChanged();
        });
        getList();

        smbLinkInfo = new SmbLinkInfo();
        smbLinkInfo.setDomain("");
        smbLinkInfo.setRootFolder("");
        smbLinkInfo.setAnonymous(true);
        startService();

        return root;
    }

    Intent intent;
    /**
     * ??????????????????
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
     * ??????????????????
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
                        Log.v("????????????", "IP is " + smbLinkInfo.getIP());
                        SmbServer.SMB_FILE_NAME = info.getFileName();
                        info.setUrl("http://"+SmbServer.SMB_IP+":"+SmbServer.SMB_PORT+"/smb/"+info.getFileName());
                        playNormal(info);
                    }else{
                        loadingDialog.cancel();
                        Log.e("????????????", "IP is " + smbLinkInfo.getIP());
                        Utils.alert(getContext(), "????????????: " + info.getFileName(), null);
                    }
                });
                return smbManager.isLinked();
            }
        }.execute();
    }

    private void getList(){
        List<Map<String,String>> list = SPUtils.getInstance().getDataList(Config.SP_HISTORY);
        List<FileInfo> fileList = new ArrayList<>();
        for(Map<String,String> map: list){
            FileInfo info = new FileInfo(map.get("name"), map.get("path"),map.get("url"));
            info.setIp(map.get("ip"));
            fileList.add(info);
        }
        historyViewModel.setList(fileList);
    }

    public void onPause(){
        super.onPause();
        loadingDialog.cancel();
    }
    public void onStop() {
        super.onStop();
        stopService();
    }

    // save the history
    public static void saveHistory(String name,String path, String url,String ip){
        boolean settingHistory = PreferenceManager.getDefaultSharedPreferences(IApplication._getContext()).getBoolean("setting_history",true);
        if(!settingHistory) return;
        //????????????????????????????????????
        List<Map<String,String>> list = SPUtils.getInstance().getDataList(Config.SP_HISTORY);

        for(Map<String,String> map:list){
            if(map.get("url").equals(url)) {
                list.remove(map);
                break;
            }
        }
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("path",path);
        map.put("url",url);
        map.put("ip",ip);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy???MM???dd??? HH:mm:ss", Locale.CHINA);// HH:mm:ss
        //??????????????????
        Date date = new Date(System.currentTimeMillis());
        map.put("time", dateFormat.format(date));
        list.add(0, map);
        if(list.size()>30){
            list.remove(list.size()-1);
        }
        SPUtils.getInstance().putDataList(Config.SP_HISTORY,list);
    }
}