package com.example.yesplayer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.yesplayer.CustomMedia.JZMediaExo;
import com.example.yesplayer.CustomMedia.JZMediaIjk;
import com.example.yesplayer.utils.FileUtils;

import cn.jzvd.JZMediaSystem;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        String fileName = getIntent().getStringExtra("fileName");
        String mediaUrl = getIntent().getStringExtra("mediaUrl");

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        setTitle(fileName);

        ImageView imageView = (ImageView)findViewById(R.id.image_music);
        imageView.setVisibility(FileUtils.isMusicFile(fileName)? View.VISIBLE:View.GONE);
        JzvdStd jzvdStd = (JzvdStd) findViewById(R.id.jz_video);
        jzvdStd.setUp(mediaUrl, fileName);
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
        if(FileUtils.isMusicFile(fileName)){
            jzvdStd.setBackgroundResource(R.drawable.ic_baseline_music_video_24);
            //jzvdStd.posterImageView.setImageResource(R.drawable.ic_baseline_music_video_24);
            //Glide.with(this).load(R.drawable.ic_baseline_music_video_24).into(jzvdStd.posterImageView);   //推荐使用Glide
        }
        //Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL);
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
        //Jzvd.releaseAllVideos();
        Jzvd.goOnPlayOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Jzvd.goOnPlayOnResume();
    }
}