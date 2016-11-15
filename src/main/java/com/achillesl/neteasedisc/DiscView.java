package com.achillesl.neteasedisc;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by AchillesL on 2016/11/15.
 */

public class DiscView extends RelativeLayout {

    /*手柄起始角度*/
    private final float NEEDLE_INIT_ROTATION = -30;
    /*唱盘比例*/
    private final float DISC_SIZE_SCALE = (float) (720.0 / 1080.0);
    /*专辑图片*/
    private final float MUSIC_PIC_SIZE_SCALE = (float) (472.0 / 1080.0);
    /*唱片转盘*/
    private ImageView mIvDisk;
    /*唱片手柄*/
    private ImageView mIvNeedle;
    private ObjectAnimator mDiscAnimator, mNeedleAnimator;
    private boolean isPlayMode = false;

    public DiscView(Context context) {
        this(context, null);
    }

    public DiscView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiscView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIvDisk = (ImageView) findViewById(R.id.ivDisc);
        mIvNeedle = (ImageView) findViewById(R.id.ivNeedle);

        setDiscBlackground();
        initNeedleStatus();
        initObjectAnimator();
    }

    private void initObjectAnimator() {
        mDiscAnimator = ObjectAnimator.ofFloat(mIvDisk, View.ROTATION, 0, 360);
        mDiscAnimator.setDuration(20 * 1000);
        mDiscAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mDiscAnimator.setInterpolator(new LinearInterpolator());

        mNeedleAnimator = ObjectAnimator.ofFloat(mIvNeedle, View.ROTATION, NEEDLE_INIT_ROTATION, 0);
        mNeedleAnimator.setDuration(500);
        mNeedleAnimator.setInterpolator(new LinearInterpolator());
        mNeedleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (isPlayMode) {
                    if (mDiscAnimator.isPaused()) {
                        mDiscAnimator.resume();
                    } else {
                        mDiscAnimator.start();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void initNeedleStatus() {
        /*设置手柄锚点*/
        mIvNeedle.setPivotX(50);
        mIvNeedle.setPivotY(50);
        mIvNeedle.setRotation(NEEDLE_INIT_ROTATION);
    }

    private void setDiscBlackground() {
        int discSize = (int) (getScreenWidth() * DISC_SIZE_SCALE);
        int musicPicSize = (int) (getScreenWidth() * MUSIC_PIC_SIZE_SCALE);

        Bitmap mBitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources
                (), R
                .drawable.play_disc), discSize, discSize, false);
        Bitmap mBitmapMusicPic = Bitmap.createScaledBitmap(BitmapFactory.decodeResource
                (getResources(), R.drawable.pic_music), musicPicSize, musicPicSize, false);
        RoundedBitmapDrawable mRoundDiscDrawable = RoundedBitmapDrawableFactory.create
                (getResources(), mBitmapDisc);
        RoundedBitmapDrawable mRoundMusicDrawable = RoundedBitmapDrawableFactory.create
                (getResources(), mBitmapMusicPic);

        mRoundDiscDrawable.setAntiAlias(true);
        mRoundMusicDrawable.setAntiAlias(true);

        Drawable[] drawables = new Drawable[2];
        drawables[0] = mRoundMusicDrawable;
        drawables[1] = mRoundDiscDrawable;

        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        int musicPicMargin = (int) ((DISC_SIZE_SCALE - MUSIC_PIC_SIZE_SCALE) * getScreenWidth() /
                2);
        layerDrawable.setLayerInset(0, musicPicMargin, musicPicMargin, musicPicMargin,
                musicPicMargin);

        mIvDisk.setImageDrawable(layerDrawable);
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDiscAnimator.cancel();
        mIvDisk.clearAnimation();
    }

    public void play() {
        isPlayMode = true;
        mNeedleAnimator.start();
    }

    public void pause() {
        isPlayMode = false;
        mNeedleAnimator.reverse();
        mDiscAnimator.pause();
    }

}
