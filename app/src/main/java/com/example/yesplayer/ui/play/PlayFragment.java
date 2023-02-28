package com.example.yesplayer.ui.play;


import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.yesplayer.CustomMedia.JZMediaExo;
import com.example.yesplayer.CustomMedia.JZMediaIjk;
import com.example.yesplayer.R;
import com.example.yesplayer.utils.FileUtils;
import com.example.yesplayer.utils.Utils;

import cn.jzvd.JZMediaSystem;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class PlayFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_play, container, false);

        Bundle args = getArguments();
        String fileName = args.getString("fileName");
        String mediaUrl = args.getString("mediaUrl");

        ImageView imageView = root.findViewById(R.id.image_music);
        imageView.setVisibility(FileUtils.isMusicFile(fileName)?View.VISIBLE:View.GONE);

        JzvdStd jzvdStd = (JzvdStd) root.findViewById(R.id.jz_video);
        jzvdStd.setUp(mediaUrl, fileName);
        String playerOption = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("setting_player","ijk");
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
        }
        //Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL);
        //jzvdStd.posterImageView.setImageResource(R.drawable.ic_baseline_music_video_24);
        //Glide.with(this).load(R.drawable.ic_baseline_music_video_24).into(jzvdStd.posterImageView);   //推荐使用Glide
        //Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER);
        //准备好立刻播放视频
        jzvdStd.startVideo();

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){if (Jzvd.backPress()) {
                Utils.showToast("Jzvd.backPress");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
}