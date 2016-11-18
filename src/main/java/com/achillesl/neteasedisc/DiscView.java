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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AchillesL on 2016/11/15.
 */

public class DiscView extends RelativeLayout {

    boolean mIsShowingPauseAnimation = false;
    private ImageView mIvNeedle;
    private ImageView mDiscBlackground;
    private ObjectAnimator mNeedleAnimator;
    private ViewPager mVpDiscContain;
    private ViewPagerAdapter mViewPagerAdapter;
    private List<View> mDiscLayouts = new ArrayList<>();
    private List<MusicData> mMusicDatas = new ArrayList<>();
    private List<ObjectAnimator> mDiscAnimators = new ArrayList<>();
    private boolean mIsNeed2StartAfterAnimator = false;
    private AnimatorStatus animatiorStatus = AnimatorStatus.STOP;
    private MusicStatus musicStatus = MusicStatus.PAUSE;
    private IPlayInfo mIPlayInfo;
    private int mScreenWidth, mScreenHeight;

    public DiscView(Context context) {
        this(context, null);
    }

    public DiscView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiscView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = DisplayUtils.getScreenWidth(context);
        mScreenHeight = DisplayUtils.getScreenHeight(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initDiscBlackground();
        initViewPager();
        initNeedle();
        initObjectAnimator();
    }

    private void initDiscBlackground() {
        mDiscBlackground = (ImageView) findViewById(R.id.ivDiscBlackgound);
        mDiscBlackground.setImageDrawable(getDiscBlackgroundDrawable());

        int marginTop = (int) (DisplayUtils.SCALE_DISC_MARGIN_TOP * mScreenHeight);
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mDiscBlackground
                .getLayoutParams();
        layoutParams.setMargins(0, marginTop, 0, 0);

        mDiscBlackground.setLayoutParams(layoutParams);
    }

    private void initViewPager() {
        mViewPagerAdapter = new ViewPagerAdapter();
        mVpDiscContain = (ViewPager) findViewById(R.id.vpDiscContain);
        mVpDiscContain.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mVpDiscContain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int lastPositionOffsetPixels = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int
                    positionOffsetPixels) {
                Log.d("onPageScrolled", "position: " + position + " positionOffset: " +
                        positionOffset + " positionOffsetPixels: " + positionOffsetPixels);
                //左滑
                if (lastPositionOffsetPixels > positionOffsetPixels) {
                    if (positionOffset < 0.5) {
                        notifyMusicInfoChanged(position);
                    } else {
                        notifyMusicInfoChanged(mVpDiscContain.getCurrentItem());
                    }
                }
                //右滑
                else if (lastPositionOffsetPixels < positionOffsetPixels) {
                    if (positionOffset > 0.5) {
                        notifyMusicInfoChanged(position + 1);
                    } else {
                        notifyMusicInfoChanged(position);
                    }
                }
                lastPositionOffsetPixels = positionOffsetPixels;
            }

            @Override
            public void onPageSelected(int position) {
                resetOtherDiscAnimation(position);
                notifyMusicPicChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("onPageScrolled", "state: " + state);
                doWithAnimatorOnPageScroll(state);
            }
        });
        mVpDiscContain.setAdapter(mViewPagerAdapter);

        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mVpDiscContain.getLayoutParams();
        int marginTop = (int) (DisplayUtils.SCALE_DISC_MARGIN_TOP * mScreenHeight);
        layoutParams.setMargins(0, marginTop, 0, 0);
        mVpDiscContain.setLayoutParams(layoutParams);
    }

    private void resetOtherDiscAnimation(int position) {
        for (int i = 0; i < mDiscLayouts.size(); i++) {
            if (position == i) continue;

            /**
             * 其他页面的动画再次启动时，应该从初始状态开始。
             * 因此需要取消动画，并将图片复位。
             * */
            mDiscAnimators.get(position).cancel();
            ImageView imageView = (ImageView) mDiscLayouts.get(i).findViewById(R.id.ivDisc);
            imageView.setRotation(0);
        }
    }

    private void doWithAnimatorOnPageScroll(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_IDLE:
            case ViewPager.SCROLL_STATE_SETTLING: {
                if (musicStatus == MusicStatus.PLAY) {
                    playAnimator();
                }
                break;
            }
            case ViewPager.SCROLL_STATE_DRAGGING: {
                pauseAnimator();
                break;
            }
        }
    }

    private void initNeedle() {
        mIvNeedle = (ImageView) findViewById(R.id.ivNeedle);

        int needleWidth = (int) (DisplayUtils.SCALE_NEEDLE_WIDTH * mScreenWidth);
        int needleHeight = (int) (DisplayUtils.SCALE_NEEDLE_HEIGHT * mScreenHeight);

        /*设置手柄的外边距为负数，让其隐藏一部分*/
        int marginTop = (int) (DisplayUtils.SCALE_NEEDLE_MARGIN_TOP * mScreenHeight) * -1;
        int marginLeft = (int) (DisplayUtils.SCALE_NEEDLE_MARGIN_LEFT * mScreenWidth);

        Bitmap originBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.play_needle);
        Bitmap bitmap = Bitmap.createScaledBitmap(originBitmap, needleWidth, needleHeight, false);

        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mIvNeedle.getLayoutParams();
        layoutParams.setMargins(marginLeft, marginTop, 0, 0);

        mIvNeedle.setPivotX(50);
        mIvNeedle.setPivotY(50);
        mIvNeedle.setRotation(DisplayUtils.ROTATION_INIT_NEEDLE);
        mIvNeedle.setImageBitmap(bitmap);
        mIvNeedle.setLayoutParams(layoutParams);
    }

    private void initObjectAnimator() {
        mNeedleAnimator = ObjectAnimator.ofFloat(mIvNeedle, View.ROTATION, DisplayUtils.ROTATION_INIT_NEEDLE, 0);
        mNeedleAnimator.setDuration(500);
        mNeedleAnimator.setInterpolator(new LinearInterpolator());
        mNeedleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mIsShowingPauseAnimation) mIsShowingPauseAnimation = false;
                animatiorStatus = (animatiorStatus == AnimatorStatus.PLAY ? AnimatorStatus.STOP :
                        AnimatorStatus.PLAY);
                if (animatiorStatus == AnimatorStatus.PLAY) {
                    int index = mVpDiscContain.getCurrentItem();
                    playDiscAnimator(index);
                }

                /**
                 * 由于ObjectAnimator的bug，onAnimationEnd被调用时，ObjectAnimator.isRunning仍然返回true。
                 * 因此加延时处理。
                 * */
                if (mIsNeed2StartAfterAnimator) {
                    mIsNeed2StartAfterAnimator = false;
                    DiscView.this.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playAnimator();
                        }
                    }, 50);
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

    public void setPlayInfoListener(IPlayInfo listener) {
        this.mIPlayInfo = listener;
    }

    private Drawable getDiscBlackgroundDrawable() {
        int discSize = (int) (mScreenWidth * DisplayUtils.SCALE_DISC_SIZE);
        Bitmap mBitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources
                (), R
                .drawable.play_disc_blackground), discSize, discSize, false);
        RoundedBitmapDrawable mRoundDiscDrawable = RoundedBitmapDrawableFactory.create
                (getResources(), mBitmapDisc);
        return mRoundDiscDrawable;
    }

    private Drawable getDiscDrawable(int musicPicRes) {
        int discSize = (int) (mScreenWidth * DisplayUtils.SCALE_DISC_SIZE);
        int musicPicSize = (int) (mScreenWidth * DisplayUtils.SCALE_MUSIC_PIC_SIZE);

        Bitmap mBitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R
                .drawable.play_disc), discSize, discSize, false);
        Bitmap mBitmapMusicPic = Bitmap.createScaledBitmap(BitmapFactory.decodeResource
                (getResources(), musicPicRes), musicPicSize, musicPicSize, false);
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
        int musicPicMargin = (int) ((DisplayUtils.SCALE_DISC_SIZE - DisplayUtils
                .SCALE_MUSIC_PIC_SIZE) * mScreenWidth / 2);
        layerDrawable.setLayerInset(0, musicPicMargin, musicPicMargin, musicPicMargin,
                musicPicMargin);

        return layerDrawable;
    }

    public void setMusicDataList(List<MusicData> musicDataList) {
        if (musicDataList.isEmpty()) return;

        mDiscLayouts.clear();
        mMusicDatas.clear();
        mDiscAnimators.clear();
        mMusicDatas.addAll(musicDataList);

        int i = 0;
        for (MusicData musicData : mMusicDatas) {
            View discLayout = LayoutInflater.from(getContext()).inflate(R.layout.disc_layout,
                    mVpDiscContain, false);

            ImageView disc = (ImageView) discLayout.findViewById(R.id.ivDisc);
            disc.setImageDrawable(getDiscDrawable(musicData.getMusicPicRes()));

            mDiscAnimators.add(getDiscObjectAnimator(disc, i++));
            mDiscLayouts.add(discLayout);
        }
        mViewPagerAdapter.notifyDataSetChanged();

        MusicData musicData = mMusicDatas.get(0);
        if (mIPlayInfo != null) {
            mIPlayInfo.onMusicInfoChanged(musicData.getMusicName(), musicData.getMusicAuthor());
            mIPlayInfo.onMusicPicChanged(musicData.getMusicPicRes());
        }
    }

    private ObjectAnimator getDiscObjectAnimator(ImageView disc, final int i) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(disc, View.ROTATION, 0, 360);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(20 * 1000);
        objectAnimator.setAutoCancel(false);
        objectAnimator.setInterpolator(new LinearInterpolator());

        return objectAnimator;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /*播放动画*/
    private void playAnimator() {
        /*若暂停动画还未结束，则设置标记，等结束后再播放动画*/
        Log.d("AchillesL", "animatiorStatus: " + animatiorStatus);
        Log.d("AchillesL", "mNeedleAnimator.isRunning(): " + animatiorStatus);
        if (animatiorStatus == AnimatorStatus.PLAY && mIsShowingPauseAnimation) {
            mIsNeed2StartAfterAnimator = true;
        } else if ((animatiorStatus == AnimatorStatus.STOP) && (!mNeedleAnimator.isRunning())) {
            mNeedleAnimator.start();
        }
    }

    /*暂停动画*/
    private void pauseAnimator() {
        if (animatiorStatus == AnimatorStatus.PLAY) {
            int index = mVpDiscContain.getCurrentItem();
            pauseDiscAnimatior(index);
            mIsShowingPauseAnimation = true;
        }
    }

    /*播放唱盘动画*/
    private void playDiscAnimator(int index) {
        ObjectAnimator objectAnimator = mDiscAnimators.get(index);
        if (objectAnimator.isPaused()) {
            objectAnimator.resume();
        } else {
            objectAnimator.start();
        }
    }

    /*暂停唱盘动画*/
    private void pauseDiscAnimatior(int index) {
        ObjectAnimator objectAnimator = mDiscAnimators.get(index);
        objectAnimator.pause();
        mNeedleAnimator.reverse();
    }

    public void notifyMusicInfoChanged(int position) {
        if (mIPlayInfo != null) {
            MusicData musicData = mMusicDatas.get(position);
            mIPlayInfo.onMusicInfoChanged(musicData.getMusicName(), musicData.getMusicAuthor());
        }
    }

    public void notifyMusicPicChanged(int position) {
        if (mIPlayInfo != null) {
            MusicData musicData = mMusicDatas.get(position);
            mIPlayInfo.onMusicPicChanged(musicData.getMusicPicRes());
        }
    }

    public void notifyMusicStatusChanged(MusicStatus musicStatus) {
        if (mIPlayInfo != null) {
            mIPlayInfo.onPlayStatusChanged(musicStatus);
        }
    }

    private void play() {
        musicStatus = MusicStatus.PLAY;
        playAnimator();
        notifyMusicStatusChanged(musicStatus);
    }

    private void pause() {
        musicStatus = MusicStatus.PAUSE;
        pauseAnimator();
        notifyMusicStatusChanged(musicStatus);
    }

    public void playOrPause() {
        if (musicStatus == MusicStatus.PLAY) {
            pause();
        } else {
            play();
        }
        notifyMusicStatusChanged(musicStatus);
    }

    public void next() {
        int currentItem = mVpDiscContain.getCurrentItem();
        if (currentItem == mMusicDatas.size() - 1) {
            Toast.makeText(getContext(), "已经到达最后一首", Toast.LENGTH_SHORT).show();
        } else {
            selectMusicWithButton();
            mVpDiscContain.setCurrentItem(currentItem + 1, true);
        }
    }

    public void last() {
        int currentItem = mVpDiscContain.getCurrentItem();
        if (currentItem == 0) {
            Toast.makeText(getContext(), "已经到达第一首", Toast.LENGTH_SHORT).show();
        } else {
            selectMusicWithButton();
            mVpDiscContain.setCurrentItem(currentItem - 1, true);
        }
    }

    private void selectMusicWithButton() {
        if (musicStatus == MusicStatus.PLAY) {
            mIsNeed2StartAfterAnimator = true;
            pauseAnimator();
        } else if (musicStatus == MusicStatus.PAUSE) {
            play();
        }
    }

    private enum AnimatorStatus {
        PLAY, STOP
    }

    public enum MusicStatus {
        PLAY, PAUSE
    }

    public interface IPlayInfo {
        public void onMusicInfoChanged(String musicName, String musicAuthor);

        public void onMusicPicChanged(int musicPicRes);

        public void onPlayStatusChanged(MusicStatus musicStatus);
    }

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View discLayout = mDiscLayouts.get(position);
            container.addView(discLayout);
            return discLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mDiscLayouts.get(position));
        }

        @Override
        public int getCount() {
            return mDiscLayouts.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
