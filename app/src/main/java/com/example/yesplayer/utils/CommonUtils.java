package com.example.yesplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.yesplayer.IApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by xyoye on 2019/7/23.
 */

public class CommonUtils {
    /**
     * 判断视频格式
     */
    public static boolean isMediaFile(String fileName){
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
     * 判断视频格式
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
