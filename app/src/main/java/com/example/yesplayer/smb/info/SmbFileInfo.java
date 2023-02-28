package com.example.yesplayer.smb.info;

/**
 * Created by xyoye on 2019/12/22.
 */

public class SmbFileInfo {
    private String ip;
    private String path;
    private String fileName;
    private boolean isDirectory;

    public SmbFileInfo(String fileName, String path, boolean isDirectory) {
        this.path = path;
        this.fileName = fileName;
        this.isDirectory = isDirectory;
    }

    public SmbFileInfo(String ip, String fileName, String path, boolean isDirectory) {
        this.ip = ip;
        this.path = path;
        this.fileName = fileName;
        this.isDirectory = isDirectory;
    }

    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}
