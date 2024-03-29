package com.example.yesplayer.utils;

import android.text.TextUtils;
import java.io.File;

/**
 * Created by Steven Ye on 2020/9/18.
 */

public class FileUtils {
    /**
     * 判断媒体格式
     */
    public static boolean isMediaFile(String fileName){
        return isVideoFile(fileName) || isMusicFile(fileName);
    }
    /**
     * 判断视频格式
     */
    public static boolean isVideoFile(String fileName){
        switch (getFileExtension(fileName).toLowerCase()){
            case "3gp":
            case "avi":
            case "flv":
            case "mp4":
            case "m4v":
            case "mkv":
            case "mov":
            case "mpeg":
            case "mpg":
            case "mpe":
            case "rm":
            case "rmvb":
            case "wmv":
            case "asf":
            case "asx":
            case "dat":
            case "vob":
            case "m3u8":
                return true;
            default: return false;
        }
    }
    /**
     * 判断音乐格式
     */
    public static boolean isMusicFile(String fileName){
        switch (getFileExtension(fileName).toLowerCase()){
            case "aac":
            case "ape":
            case "flac":
            case "midi":
            case "ogg":
            case "mp3":
            case "wav":
            case "wma":
                return true;
            default: return false;
        }
    }
    /**
     * 判断视频格式
     */
    public static boolean isImageFile(String fileName){
        switch (getFileExtension(fileName).toLowerCase()){
            case "jpg":
            case "jpeg":
            case "gif":
            case "png":
            case "bmp":
            case "WBMP":
                return true;
            default: return false;
        }
    }

    /**
     * 获取文件格式
     */
    public static String getFileExtension(final String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }
}
