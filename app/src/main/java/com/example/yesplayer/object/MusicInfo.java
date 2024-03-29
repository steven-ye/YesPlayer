package com.example.yesplayer.object;

import android.database.Cursor;
import android.provider.MediaStore;

public class MusicInfo extends MediaInfo{
    public String album;
    public String artist;

    public MusicInfo(Cursor cursor){
        super(cursor);
        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)); // 专辑
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); // 作者
        //int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
    }

    public String getAlbum(){
        return album;
    }
    public void setAlbum(String album){
        this.album = album;
    }

    public String getArtist(){
        return artist;
    }
    public void setArtist(String artist){
        this.artist = artist;
    }
}