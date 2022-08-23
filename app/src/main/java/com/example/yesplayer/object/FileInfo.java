package com.example.yesplayer.object;

import com.example.yesplayer.smb.info.SmbFileInfo;

import java.io.File;

public class FileInfo extends SmbFileInfo {
    private String mPath;
    private String mIp;
    private String mUrl;

    public FileInfo(File file){
        super(file.getName(), file.isDirectory());
        mPath = file.getPath();
        mUrl = "file://" + file.getPath();
    }

    public FileInfo(String name, String path, String url) {
        super(name,false);
        mPath = path;
        mUrl = url;
    }
    public FileInfo(String name, String path, boolean isDir) {
        super(name,isDir);
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