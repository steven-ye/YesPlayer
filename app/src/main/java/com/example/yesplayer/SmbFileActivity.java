package com.example.yesplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yesplayer.service.SmbService;
import com.example.yesplayer.smb.SmbServer;
import com.example.yesplayer.ui.history.HistoryFragment;
import com.example.yesplayer.utils.CommonUtils;
import com.example.yesplayer.utils.LoadingDialog;
import com.example.yesplayer.utils.SPUtils;
import com.example.yesplayer.utils.SmbFileAdapter;
import com.example.yesplayer.utils.Utils;
import com.xyoye.libsmb.SmbManager;
import com.xyoye.libsmb.info.SmbFileInfo;
import com.xyoye.libsmb.info.SmbLinkInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 局域网文件浏览界面
 */

public class SmbFileActivity extends AppCompatActivity {
    String TAG = "SmbFileActivity";
    private List<SmbFileInfo> smbFileList;
    private SmbFileAdapter smbFileAdapter;

    private SmbLinkInfo smbLinkInfo;
    private SmbManager smbManager;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smbfile);
        String name = getIntent().getStringExtra("name");
        String ip = getIntent().getStringExtra("ip");

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        setTitle(name);
        loadingDialog = new LoadingDialog(this, "查找中");

        initView();

        smbManager = SmbManager.getInstance();
        smbLinkInfo = new SmbLinkInfo();
        smbLinkInfo.setIP(ip);
        smbLinkInfo.setDomain("");
        smbLinkInfo.setRootFolder("");
        smbLinkInfo.setAnonymous(true);

        startService();

        getSelfData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            IApplication.getExecutor().submit(() -> {
                smbManager.getController().release();
                SmbFileActivity.this.finish();
            });
            //getParentData();
        } else if (item.getItemId() == R.id.item_refresh) {
            //getSelfData();
            openDirectory("");
        }
        return super.onOptionsItemSelected(item);
    }

    View viewGoUp;
    private void initView() {
        viewGoUp = findViewById(R.id.go_parent);
        viewGoUp.setOnClickListener(v->{
            getParentData();
        });

        smbFileList = new ArrayList<>();
        smbFileAdapter = new SmbFileAdapter(R.layout.item_smb_file, smbFileList);
        smbFileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            String fileName = smbFileList.get(position).getFileName();
            if (smbFileList.get(position).isDirectory()) {
                openDirectory(fileName);
            } else {
                openFile(fileName);
            }
        });

        EmptyRecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setEmptyView(findViewById(R.id.text_list_empty));
        recyclerView.setAdapter(smbFileAdapter);
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

        AlertDialog dialog = new AlertDialog.Builder(this)
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
                dialog.cancel();
            }
        });

        view.findViewById(R.id.cancel_bt).setOnClickListener(v->{
            onBackPressed();
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
            String currentPath = smbManager.getController().getCurrentPath();
            runOnUiThread(() -> {
                loadingDialog.cancel();
                smbFileList.clear();
                smbFileList.addAll(fileList);
                smbFileAdapter.notifyDataSetChanged();
                //pathTv.setText(currentPath);
                //setTitle(type.replace("Controller", ""));
                System.out.println(currentPath);
                showGoUp();
            });
            return "okay";
        });

        try {
            String error = future.get(120,TimeUnit.SECONDS);
            switch(future.get(120,TimeUnit.SECONDS)){
                case "okay":
                    Log.v("Executor", "done");
                    break;
                case "login":
                    Utils.showToast("登录失败");
                    showLogin();
                    break;
                default:
                    Log.e("Error", error);
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
        IApplication.getExecutor().submit(() -> {
            if (smbManager.getController().isRootDir()) {
                //showToast("无父目录");
                viewGoUp.setVisibility(View.GONE);
                //smbManager.getController().release();
                //SmbFileActivity.this.finish();
                return;
            }
            List<SmbFileInfo> fileList = smbManager.getController().getParentList();
            String currentPath = smbManager.getController().getCurrentPath();
            runOnUiThread(() -> {
                smbFileList.clear();
                smbFileList.addAll(fileList);
                smbFileAdapter.notifyDataSetChanged();
                //pathTv.setText(currentPath);
                showGoUp();
            });
        });
    }

    /**
     * 打开文件夹
     */
    private void openDirectory(String dirName) {
        loadingDialog.show();
        IApplication.getExecutor().submit(() -> {
            List<SmbFileInfo> fileList = smbManager.getController().getChildList(dirName);
            //String currentPath = smbManager.getController().getCurrentPath();
            runOnUiThread(() -> {
                loadingDialog.cancel();
                smbFileList.clear();
                smbFileList.addAll(fileList);
                smbFileAdapter.notifyDataSetChanged();
                //pathTv.setText(currentPath);
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
        if (CommonUtils.isMediaFile(fileName) || CommonUtils.isMusicFile(fileName)) {
            int player = SPUtils.getInstance().getInt("player");
            if(player>0){
                Uri uri = Uri.parse(mediaUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/*");
                startActivity(intent);
            }else{
                Intent intent = new Intent(SmbFileActivity.this, PlayerActivity.class);
                intent.putExtra("fileName", fileName);
                intent.putExtra("videoUrl", mediaUrl);
                startActivity(intent);
            }
            //保存播放记录
            HistoryFragment.saveHistory(fileName,path,mediaUrl,getIntent().getStringExtra("ip"));
        }else{
            Utils.showToast("不是可播放的文件");
        }
    }

    /**
     * 启动后台服务
     */
    private void startService() {
        Intent intent = new Intent(SmbFileActivity.this, SmbService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}
