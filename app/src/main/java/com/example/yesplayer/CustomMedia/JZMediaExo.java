package com.example.yesplayer.CustomMedia;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.Jzvd;

import com.example.yesplayer.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by MinhDV on 5/3/18.
 */
public class JZMediaExo extends JZMediaInterface implements Player.Listener {
  private final String TAG = "JZMediaExo";
  private SimpleExoPlayer simpleExoPlayer;
  private Runnable callback;
  private long previousSeek = 0;

  public JZMediaExo(Jzvd jzvd) {
    super(jzvd);
  }

  @Override
  public void start() {
    simpleExoPlayer.setPlayWhenReady(true);
  }

  @Override
  public void prepare() {
    Log.e(TAG, "prepare");
    Context context = jzvd.getContext();

    release();
    mMediaHandlerThread = new HandlerThread("JZVD");
    mMediaHandlerThread.start();
    mMediaHandler = new Handler(context.getMainLooper());//主线程还是非主线程，就在这里
    handler = new Handler();
    mMediaHandler.post(() -> {
      // 2. Create the player after 2.12
      simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();

      String currUrl = jzvd.jzDataSource.getCurrentUrl().toString();
      String currTag = jzvd.jzDataSource.getCurrentKey().toString();
      //MediaItem mediaItem = MediaItem.fromUri(currUrl);
      MediaItem mediaItem = new MediaItem.Builder().setUri(currUrl).setTag(currTag).build();

      Log.e(TAG, "URL Link = " + currUrl);
      Log.e(TAG, "URL TAG = " + currTag);

      simpleExoPlayer.addListener(this);
      boolean isLoop = jzvd.jzDataSource.looping;
      if (isLoop) {
        simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
      } else {
        simpleExoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
      }

      simpleExoPlayer.setMediaItem(mediaItem);
      simpleExoPlayer.prepare();
      simpleExoPlayer.play();

      callback = new onBufferingUpdate();

      if (jzvd.textureView != null) {
        SurfaceTexture surfaceTexture = jzvd.textureView.getSurfaceTexture();
        if (surfaceTexture != null) {
          simpleExoPlayer.setVideoSurface(new Surface(surfaceTexture));
        }
      }
    });

  }

  @Override
  public void onRenderedFirstFrame() {
    Log.e(TAG, "onRenderedFirstFrame");
  }

  @Override
  public void pause() {
    simpleExoPlayer.setPlayWhenReady(false);
  }

  @Override
  public boolean isPlaying() {
    return simpleExoPlayer.getPlayWhenReady();
  }

  @Override
  public void seekTo(long time) {
    if (simpleExoPlayer == null) {
      return;
    }
    if (time != previousSeek) {
      if (time >= simpleExoPlayer.getBufferedPosition()) {
        jzvd.onStatePreparingPlaying();
      }
      simpleExoPlayer.seekTo(time);
      previousSeek = time;
      jzvd.seekToInAdvance = time;

    }
  }

  @Override
  public void release() {
    if (mMediaHandler != null && mMediaHandlerThread != null && simpleExoPlayer != null) {//不知道有没有妖孽
      HandlerThread tmpHandlerThread = mMediaHandlerThread;
      SimpleExoPlayer tmpMediaPlayer = simpleExoPlayer;
      JZMediaInterface.SAVED_SURFACE = null;

      mMediaHandler.post(() -> {
        tmpMediaPlayer.release();//release就不能放到主线程里，界面会卡顿
        tmpHandlerThread.quit();
      });
      simpleExoPlayer = null;
    }
  }

  @Override
  public long getCurrentPosition() {
    if (simpleExoPlayer != null)
      return simpleExoPlayer.getCurrentPosition();
    else return 0;
  }

  @Override
  public long getDuration() {
    if (simpleExoPlayer != null)
      return simpleExoPlayer.getDuration();
    else return 0;
  }

  @Override
  public void setVolume(float leftVolume, float rightVolume) {
    simpleExoPlayer.setVolume(leftVolume);
    simpleExoPlayer.setVolume(rightVolume);
  }

  @Override
  public void setSpeed(float speed) {
    PlaybackParameters playbackParameters = new PlaybackParameters(speed, 1.0F);
    simpleExoPlayer.setPlaybackParameters(playbackParameters);
  }

  @Override
  public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

  }

  @Override
  public void onLoadingChanged(boolean isLoading) {
    Log.e(TAG, "onLoadingChanged");
  }

  @Override
  public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
    Log.e(TAG, "onPlayerStateChanged" + playbackState + "/ready=" + String.valueOf(playWhenReady));
    handler.post(() -> {
      switch (playbackState) {
        case Player.STATE_IDLE: {
        }
        break;
        case Player.STATE_BUFFERING: {
          jzvd.onStatePreparingPlaying();
          handler.post(callback);
        }
        break;
        case Player.STATE_READY: {
          if (playWhenReady) {
            jzvd.onStatePlaying();
          }
        }
        break;
        case Player.STATE_ENDED: {
          jzvd.onCompletion();
        }
        break;
      }
    });
  }

  @Override
  public void onRepeatModeChanged(int repeatMode) {

  }

  @Override
  public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

  }

  @Override
  public void onPlaybackParametersChanged(@NotNull PlaybackParameters playbackParameters) {

  }

  @Override
  public void setSurface(Surface surface) {
    if (simpleExoPlayer != null) {
      simpleExoPlayer.setVideoSurface(surface);
    } else {
      Log.e("AGVideo", "simpleExoPlayer为空");
    }
  }

  @Override
  public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    if (SAVED_SURFACE == null) {
      SAVED_SURFACE = surface;
      prepare();
    } else {
      jzvd.textureView.setSurfaceTexture(SAVED_SURFACE);
    }
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

  }

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    return false;
  }

  @Override
  public void onSurfaceTextureUpdated(SurfaceTexture surface) {

  }

  private class onBufferingUpdate implements Runnable {
    @Override
    public void run() {
      if (simpleExoPlayer != null) {
        final int percent = simpleExoPlayer.getBufferedPercentage();
        handler.post(() -> jzvd.setBufferProgress(percent));
        if (percent < 100) {
          handler.postDelayed(callback, 300);
        } else {
          handler.removeCallbacks(callback);
        }
      }
    }
  }
}