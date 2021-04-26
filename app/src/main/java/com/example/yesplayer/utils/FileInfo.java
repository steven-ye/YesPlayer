package com.example.yesplayer.utils;

import com.xyoye.libsmb.info.SmbFileInfo;

import java.io.Serializable;

public class FileInfo extends SmbFileInfo implements Serializable {
    private String mPath;
    private String mIp;
    private String mUrl;

    public FileInfo(String name, String path, String url) {
        super(name,false);
        mPath = path;
        mUrl = url;
    }
    public FileInfo(String name, String path, boolean isDirectory) {
        super(name,isDirectory);
        mPath = path;
    }

    public String getPath(){
        return mPath;
    }
    public void setPath(String path){
        mPath = path;
    }

    public String getIp(){
        return mIp;
    }
    public void setIp(String ip){
        mIp = ip;
    }

    public String getUrl(){
        return mUrl;
    }
    public void setUrl(String url){
        mUrl = url;
    }
}