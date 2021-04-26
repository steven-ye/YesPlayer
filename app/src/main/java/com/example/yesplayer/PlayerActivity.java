package com.example.yesplayer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.yesplayer.CustomMedia.JZMediaExo;
import com.example.yesplayer.CustomMedia.JZMediaIjk;

import cn.jzvd.JZMediaSystem;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        String videoUrl = getIntent().getStringExtra("videoUrl");
        String fileName = getIntent().getStringExtra("fileName");

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        setTitle(fileName);

        JzvdStd jzvdStd = (JzvdStd) findViewById(R.id.jz_video);
        jzvdStd.setUp(videoUrl, fileName);
        String playerOption = PreferenceManager.getDefaultSharedPreferences(this).getString("setting_player","ijk");
        System.out.println("播放器： "+playerOption);
        switch(playerOption){
            case "system":
                //jzvdStd.setUp(videoUrl, fileName, JzvdStd.SCREEN_NORMAL, JZMediaSystem.class);
                jzvdStd.setMediaInterface(JZMediaSystem.class);
                break;
            case "exo":
                //jzvdStd.setUp(videoUrl, fileName, JzvdStd.SCREEN_NORMAL, JZMediaExo.class);
                jzvdStd.setMediaInterface(JZMediaExo.class);
                break;
            default:
                //jzvdStd.setUp(videoUrl, fileName, JzvdStd.SCREEN_NORMAL, JZMediaIjk.class);
                jzvdStd.setMediaInterface(JZMediaIjk.class);
        }
        //jzvdStd.posterImageView.setImage("http://p.qpic.cn/videoyun/0/2449_43b6f696980311e59ed467f22794e792_1/640");

        //Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER);
        //准备好立刻播放视频
        jzvdStd.startVideo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.file_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
}