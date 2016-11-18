package com.achillesl.neteasedisc;

/**
 * Created by AchillesL on 2016/11/15.
 */

public class MusicData {
    private int mMusicRes;
    private int mMusicPicRes;
    private String mMusicName;
    private String mMusicAuthor;

    public MusicData(int mMusicRes, int mMusicPicRes, String mMusicName, String mMusicAuthor) {
        this.mMusicRes = mMusicRes;
        this.mMusicPicRes = mMusicPicRes;
        this.mMusicName = mMusicName;
        this.mMusicAuthor = mMusicAuthor;
    }

    public int getMusicRes() {
        return mMusicRes;
    }

    public int getMusicPicRes() {
        return mMusicPicRes;
    }

    public String getMusicName() {
        return mMusicName;
    }

    public String getMusicAuthor() {
        return mMusicAuthor;
    }
}
