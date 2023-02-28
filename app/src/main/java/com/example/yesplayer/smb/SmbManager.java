package com.example.yesplayer.smb;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.yesplayer.smb.controller.Controller;
import com.example.yesplayer.smb.controller.JCIFSController;
import com.example.yesplayer.smb.controller.SMBJController;
import com.example.yesplayer.smb.info.SmbLinkInfo;
import com.example.yesplayer.smb.info.SmbType;
import com.example.yesplayer.utils.IPUtils;
import com.example.yesplayer.utils.SmbUtils;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.connection.Connection;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jcifs.Address;
import jcifs.NameServiceClient;
import jcifs.context.SingletonContext;

/**
 * Created by xyoye on 2019/12/20.
 */

public class SmbManager {
    final String TAG = "SmbManager";
    private boolean isLinked;
    private Controller controller;
    private final SmbLinkException smbLinkException;

    private boolean smbJEnable = false;
    private boolean jcifsEnable = true;
    private SmbType mSmbType;

    private static class Holder {
        static SmbManager instance = new SmbManager();
    }

    private SmbManager() {
        smbLinkException = new SmbLinkException();
    }

    public static SmbManager getInstance() {
        return Holder.instance;
    }

    /**
     * link to the smb server from smbV2 to smbV1
     *
     * @param smbLinkInfo link data
     */
    public boolean linkStart(SmbLinkInfo smbLinkInfo) {

        smbLinkException.clearException();

        if (!smbLinkInfo.isAnonymous()) {
            if (SmbUtils.containsEmptyText(smbLinkInfo.getAccount(), smbLinkInfo.getAccount())) {
                throw new NullPointerException("Account And Password Must NotNull");
            }
        }

        //SMB V2
        isLinked = true;

        if (smbJEnable && !TextUtils.isEmpty(smbLinkInfo.getRootFolder())) {
            controller = new SMBJController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.SMBJ;
                return true;
            }
        }

        //SMB V1
        if (jcifsEnable) {
            controller = new JCIFSController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                mSmbType = SmbType.JCIFS;
                return true;
            }
        }

        isLinked = false;
        return false;
    }

    /**
     * get smb tools type
     */
    public String getSmbType() {
        return mSmbType == null ? "" : SmbType.getTypeName(mSmbType);
    }

    /**
     * is the link successful
     */
    public boolean isLinked() {
        return isLinked;
    }

    /**
     * get link controller
     */
    public Controller getController() {
        return controller;
    }

    /**
     * link error info
     */
    public SmbLinkException getException() {
        return smbLinkException;
    }

    public void setEnable(boolean smbJEnable, boolean jcifsEnable) {
        this.smbJEnable = smbJEnable;
        this.jcifsEnable = jcifsEnable;
    }

    public String getServerByIp(String ip){
        //String firstname = "Unknown";
        String serverName;
        try{
            SingletonContext tc = SingletonContext.getInstance();
            NameServiceClient nsc = tc.getNameServiceClient();
            Address[] addrs = nsc.getNbtAllByAddress(ip);
            serverName = addrs[0].getHostName();
        }catch (UnknownHostException e){
            //e.printStackTrace();
            serverName = null;
        }
        return serverName;
    }

    public List<Map<String,String>> getServerList(Context context) {
        final List<Map<String,String>> servers = new ArrayList<>();
        String devAddress = IPUtils.getIPAdress(context);
        String localSegment = IPUtils.getLocAddrIndex(devAddress);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for(int i=1;i<255;i++){
            final String ip = localSegment + i;
            Runnable syncTask = () -> {
                String name = getServerByIp(ip);
                if(null != name){
                    Map<String,String> map = new HashMap<>();
                    map.put("name", name);
                    map.put("ip", ip);
                    servers.add(map);
                    Log.d(TAG, "Found Smb Host: " + ip + " " + name);
                }
            };
            executorService.execute(syncTask);
        }

        executorService.shutdown();

        try {
            boolean result = executorService.awaitTermination(120, TimeUnit.SECONDS);
            if(!result) executorService.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }

        return servers;
    }

    public boolean isLinkable(String ip){
        SMBClient client = new SMBClient();
        try (Connection connection = client.connect(ip)) {
            return connection.isConnected();
        }catch (IOException e) {
            //e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return false;
    }
}
