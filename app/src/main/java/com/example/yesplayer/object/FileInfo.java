package com.example.yesplayer.object;

import com.example.yesplayer.smb.info.SmbFileInfo;

import java.io.File;

public class FileInfo extends SmbFileInfo {
    private String mUrl;

    public FileInfo(File file){
        super(file.getPath(), file.getName(), file.isDirectory());
        mUrl = "file://" + file.getPath();
    }

    public FileInfo(String name, String path, String url) {
        super(name, path,false);
        mUrl = url;
    }
    public FileInfo(String name, String path, boolean isDir) {
        super(name, path, isDir);
    }

    public String getUrl(){
        return mUrl;
    }
    public void setUrl(String url){
        mUrl = url;
    }
}