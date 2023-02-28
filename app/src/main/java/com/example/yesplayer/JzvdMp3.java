package com.example.yesplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import cn.jzvd.JzvdStd;

public class JzvdMp3 extends JzvdStd {

    public JzvdMp3(Context context) {
        super(context);
    }

    public JzvdMp3(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_jzvd_mp3;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == cn.jzvd.R.id.poster &&
                (state == STATE_PLAYING ||
                        state == STATE_PAUSE)) {
            onClickUiToggle();
        } else if (v.getId() == R.id.fullscreen) {

        } else {
            super.onClick(v);
        }
    }

    //changeUiTo 真能能修改ui的方法
    @Override
    public void changeUiToNormal() {
        super.changeUiToNormal();
    }

    @Override
    public void changeUiToPreparing() {
        super.changeUiToPreparing();
    }

    @Override
    public void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        posterImageView.setVisibility(View.VISIBLE);

    }

    @Override
    public void changeUiToPlayingClear() {
        super.changeUiToPlayingClear();
        posterImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        posterImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void changeUiToPauseClear() {
        super.changeUiToPauseClear();
        posterImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void changeUiToComplete() {
        super.changeUiToComplete();
    }

    @Override
    public void changeUiToError() {
        super.changeUiToError();
    }
}