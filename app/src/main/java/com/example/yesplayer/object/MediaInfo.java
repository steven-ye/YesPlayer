package com.example.yesplayer.object;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class MediaInfo {
    public String name;
    public String path;
    public String resolution;
    public long size;
    public long duration;
    public long date;
    public Uri uri;

    public MediaInfo(Cursor cursor){
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        long id = cursor.getLong(idColumn);
        path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)); // 视频名称
        resolution = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)); //分辨率
        size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));// 大小
        duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// 时长
        date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));//修改时间
        uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getPath(){
        return path;
    }
    public void setPath(String path){
        this.path = path;
    }

    public String getResolution(){
        return resolution;
    }
    public void setIp(String resolution){
        this.resolution = resolution;
    }

    public long getSize(){
        return size;
    }
    public void setSize(long size){
        this.size = size;
    }

    public long getDuration(){
        return duration;
    }
    public void setDuration(long size){
        this.duration = duration;
    }

    public long getDate(){
        return date;
    }
    public void setDate(long size){
        this.date = date;
    }

    public Uri getUri(){
        return uri;
    }
    public void setUri(Uri uri){
        this.uri = uri;
    }
}