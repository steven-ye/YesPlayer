package com.example.yesplayer.ui.filelist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yesplayer.EmptyRecyclerView;
import com.example.yesplayer.IApplication;
import com.example.yesplayer.MainActivity;
import com.example.yesplayer.PlayerActivity;
import com.example.yesplayer.R;
import com.example.yesplayer.helper.HistoryHelper;
import com.example.yesplayer.service.SmbService;
import com.example.yesplayer.smb.SmbManager;
import com.example.yesplayer.smb.SmbServer;
import com.example.yesplayer.smb.info.SmbFileInfo;
import com.example.yesplayer.smb.info.SmbLinkInfo;
import com.example.yesplayer.utils.FileUtils;
import com.example.yesplayer.utils.ListSortUtils;
import com.example.yesplayer.utils.LoadingDialog;
import com.example.yesplayer.utils.SPUtils;
import com.example.yesplayer.utils.SmbFileAdapter;
import com.example.yesplayer.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SmbFileFragment extends Fragment {
    final private String TAG = getClass().getSimpleName();
    private Context mContext;
    private MainActivity mActivity;
    private SmbFileAdapter smbFileAdapter;
    private View viewGoUp;

    private SmbLinkInfo smbLinkInfo;
    private SmbManager smbManager;

    private LoadingDialog loadingDialog;

    String ip;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mActivity = (MainActivity)getActivity();
        loadingDialog = new LoadingDialog(mContext, "查找中");
        Bundle args = getArguments();
        assert args != null;
        ip = args.getString("ip");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_smbfilelist, container, false);

        viewGoUp = root.findViewById(R.id.go_parent);
        viewGoUp.setOnClickListener(v-> getParentData());

        List<SmbFileInfo> smbFileList = new ArrayList<>();
        smbFileAdapter = new SmbFileAdapter(R.layout.item_smb_file, smbFileList);
        smbFileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            String fileName = smbFileList.get(position).getFileName();
            if (smbFileList.get(position).isDirectory()) {
                openDirectory(fileName);
            } else {
                openFile(fileName);
            }
        });

        EmptyRecyclerView recyclerView = root.findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerView.setEmptyView(root.findViewById(R.id.text_list_empty));
        recyclerView.setAdapter(smbFileAdapter);

        smbManager = SmbManager.getInstance();
        smbLinkInfo = new SmbLinkInfo();
        smbLinkInfo.setIP(ip);
        smbLinkInfo.setDomain("");
        smbLinkInfo.setRootFolder("");
        smbLinkInfo.setAnonymous(true);

        getSelfData();

        startService();

        return root;
    }

    private void showGoUp(){
        int visible = smbManager.getController().isRootDir()?View.GONE:View.VISIBLE;
        viewGoUp.setVisibility(visible);
    }

    private void showLogin(){
        View view = getLayoutInflater().inflate(R.layout.form_server_login, null);
        EditText account_et = view.findViewById(R.id.account_et);
        EditText password_et = view.findViewById(R.id.password_et);
        EditText domain_et = view.findViewById(R.id.domain_et);
        CheckBox anonymous_cb = view.findViewById(R.id.anonymous_cb);
        account_et.setText(smbLinkInfo.getAccount());
        password_et.setText(smbLinkInfo.getPassword());
        domain_et.setText(smbLinkInfo.getDomain());
        anonymous_cb.setChecked(smbLinkInfo.isAnonymous());

        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                //.setTitle("登录名和密码")
                .setCancelable(false)
                .setView(view)
                .show();

        view.findViewById(R.id.login_bt).setOnClickListener(v -> {
            String account = account_et.getText().toString();
            String password = password_et.getText().toString();
            String domain = domain_et.getText().toString();
            smbLinkInfo.setAccount(account);
            smbLinkInfo.setPassword(password);
            smbLinkInfo.setDomain(domain);
            smbLinkInfo.setAnonymous(anonymous_cb.isChecked());
            if(!anonymous_cb.isChecked() && TextUtils.isEmpty(account)){
                Utils.showToast("请输入账号");
            }else{
                getSelfData();
                alertDialog.cancel();
            }
        });

        view.findViewById(R.id.cancel_bt).setOnClickListener(v->{
            alertDialog.cancel();
            mActivity.onBackPressed();
        });
    }

    /**
     * 获取当前文件文件列表
     */
    private void getSelfData() {
        loadingDialog.show();
        ExecutorService executorService = IApplication.getExecutor();
        Future<String> future = executorService.submit(() -> {
            if(!smbManager.linkStart(smbLinkInfo)){
                if(smbManager.isLinkable(smbLinkInfo.getIP())){
                    smbLinkInfo.setAnonymous(false);
                    return "login";
                }
                return "fail";
            }
            String type = smbManager.getController().getClass().getSimpleName();
            Log.i("Type", type);
            List<SmbFileInfo> fileList = smbManager.getController().getSelfList();
            List<SmbFileInfo> files = filterList(fileList);
            //String currentPath = smbManager.getController().getCurrentPath();
            mActivity.runOnUiThread(() -> {
                loadingDialog.cancel();
                smbFileAdapter.setList(files);
                showGoUp();
            });
            return "okay";
        });

        try {
            String error = future.get(10, TimeUnit.SECONDS);
            switch(error){
                case "okay":
                    Log.v("Executor", "done");
                    break;
                case "login":
                    Utils.showToast("登录失败");
                    showLogin();
                    break;
                default:
                    Log.e("Executor Error", error);
            }
        } catch (InterruptedException e) {
            Log.e("InterruptedException",e.getMessage());
            //e.printStackTrace();
        } catch (ExecutionException e) {
            Log.e("ExecutionException",e.getMessage());
            //e.printStackTrace();
        } catch (TimeoutException e) {
            //e.printStackTrace();
            future.cancel(true); //超时后取消任务
        } finally {
            loadingDialog.cancel();
        }
    }

    /**
     * 获取父目录文件列表
     */
    private void getParentData() {
        ExecutorService executorService = IApplication.getExecutor();
        Future<?> future = executorService.submit(() -> {
            if (smbManager.getController().isRootDir()) {
                //showToast("无父目录");
                viewGoUp.setVisibility(View.GONE);
                //smbManager.getController().release();
            }else{
                List<SmbFileInfo> fileList = smbManager.getController().getParentList();
                //String currentPath = smbManager.getController().getCurrentPath();
                mActivity.runOnUiThread(() -> {
                    List<SmbFileInfo> files = filterList(fileList);
                    smbFileAdapter.setList(files);
                    showGoUp();
                });
            }
        });

        try{
            future.get(10, TimeUnit.SECONDS);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 打开文件夹
     */
    private void openDirectory(String dirName) {
        loadingDialog.show();
        IApplication.getExecutor().submit(() -> {
            List<SmbFileInfo> fileList = smbManager.getController().getChildList(dirName);
            List<SmbFileInfo> files = filterList(fileList);
            //String currentPath = smbManager.getController().getCurrentPath();
            mActivity.runOnUiThread(() -> {
                loadingDialog.cancel();
                /*
                smbFileList.clear();
                smbFileList.addAll(files);
                smbFileAdapter.notifyDataSetChanged();
                */
                smbFileAdapter.setList(files);
                showGoUp();
            });
        });
    }

    /**
     * 打开文件
     */
    private void openFile(String fileName) {
        //文件Url由开启监听的IP和端口及视频地址组成
        String httpUrl = "http://" + SmbServer.SMB_IP + ":" + SmbServer.SMB_PORT;
        String mediaUrl = httpUrl + "/smb/" + fileName;
        SmbServer.SMB_FILE_NAME = fileName;
        Log.i("fileName", fileName);
        String path = smbManager.getController().getCurrentPath();
        if (FileUtils.isMediaFile(fileName) || FileUtils.isMusicFile(fileName)) {
            int player = SPUtils.getInstance().getInt("player");
            if(player>0){
                Uri uri = Uri.parse(mediaUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/*");
                startActivity(intent);
            }else{
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("videoUrl", mediaUrl);
                startActivity(intent);
            }
            //保存播放记录
            HistoryHelper.save(fileName,path,mediaUrl,ip);
        }else{
            Utils.showToast("不是可播放的文件");
        }
    }

    private List<SmbFileInfo> filterList(List<SmbFileInfo> fileList){
        ArrayList<SmbFileInfo> list = new ArrayList<>();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(IApplication.getContext());
        boolean showHidden = sp.getBoolean("setting_hidden",true);

        for(SmbFileInfo info: fileList){
            if(info.getFileName().startsWith("$")||info.getFileName().endsWith("$")) continue;
            //是否显示隐藏文件/目录
            if(!showHidden && info.getFileName().startsWith(".")) continue;
            //只展示媒体文件
            if(info.isDirectory() || FileUtils.isMediaFile(info.getFileName())){
                list.add(info);
            }
        }
        return ListSortUtils.sort(list);
    }

    /**
     * 启动后台服务
     */
    private void startService() {
        Intent intent = new Intent(mActivity, SmbService.class);
        mContext.startForegroundService(intent);
    }
}
