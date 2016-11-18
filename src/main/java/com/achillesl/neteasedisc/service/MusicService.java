package com.achillesl.neteasedisc.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import com.achillesl.neteasedisc.model.MusicData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AchillesL on 2016/11/18.
 */

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY_OR_PAUSE_MUSIC = "ACTION_PLAY_OR_PAUSE_MUSIC";
    public static final String ACTION_NEXT_MUSIC = "ACTION_NEXT_MUSIC";
    public static final String ACTION_LAST_MUSIC = "ACTION_LAST_MUSIC";

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private MusicReceiver mMusicReceiver = new MusicReceiver();

    private boolean mIsMusicPause = false;
    private int mCurrentMusicIndex = 0;
    private List<MusicData> mMusicDatas = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initMusicDatas(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        initBoardCastReceiver();
    }

    private void initMediaPlayer() {
        mMediaPlayer.setOnCompletionListener(this);
    }

    private void initMusicDatas(Intent intent) {
        List<MusicData> musicDatas = (List<MusicData>) intent.getSerializableExtra("musicList");
        mMusicDatas.addAll(musicDatas);
    }

    private void initBoardCastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY_OR_PAUSE_MUSIC);
        intentFilter.addAction(ACTION_NEXT_MUSIC);
        intentFilter.addAction(ACTION_LAST_MUSIC);

        registerReceiver(mMusicReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void play(final int index) {
        if (index >= mMusicDatas.size()) return;
        if (mIsMusicPause) {
            mMediaPlayer.start();
        } else {
            mMediaPlayer.reset();
            mMediaPlayer = MediaPlayer.create(getApplicationContext(), mMusicDatas.get(index)
                    .getMusicRes());
            mMediaPlayer.start();
            mIsMusicPause = false;
        }
    }

    private void pause() {
        mMediaPlayer.pause();
        mIsMusicPause = true;
    }

    private void stop() {
        mMediaPlayer.stop();
    }

    private void next() {
        if (mCurrentMusicIndex + 1 < mMusicDatas.size()) {
            play(++mCurrentMusicIndex);
        } else {
            stop();
        }
    }

    private void last() {
        if (mCurrentMusicIndex != 0) {
            play(--mCurrentMusicIndex);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_PLAY_OR_PAUSE_MUSIC)) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                } else {
                    play(mCurrentMusicIndex);
                }
            } else if (action.equals(ACTION_LAST_MUSIC)) {
                last();
            } else if (action.equals(ACTION_NEXT_MUSIC)) {
                next();
            }
        }
    }
}
