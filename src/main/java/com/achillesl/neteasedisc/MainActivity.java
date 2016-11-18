package com.achillesl.neteasedisc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements DiscView.IPlayInfo, View
        .OnClickListener {

    private DiscView mDisc;
    private Toolbar mToolbar;
    private ImageView mIvPlayOrPause, mIvNext, mIvLast;
    private BackgourndAnimationRelativeLayout mRootLayout;

    private List<MusicData> mMusicDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMusicDatas();
        initView();
        makeStatusBarTransparent();
    }

    private void initView() {
        mDisc = (DiscView) findViewById(R.id.discview);
        mIvNext = (ImageView) findViewById(R.id.ivNext);
        mIvLast = (ImageView) findViewById(R.id.ivLast);
        mIvPlayOrPause = (ImageView) findViewById(R.id.ivPlayOrPause);
        mRootLayout = (BackgourndAnimationRelativeLayout) findViewById(R.id.rootLayout);

        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);

        mDisc.setPlayInfoListener(this);
        mIvLast.setOnClickListener(this);
        mIvNext.setOnClickListener(this);
        mIvPlayOrPause.setOnClickListener(this);

        mDisc.setMusicDataList(mMusicDatas);
    }

    /*设置透明状态栏*/
    private void makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void initMusicDatas() {
        MusicData musicData1 = new MusicData(0, R.raw.music_pic1, "Summer", "久石让");
        MusicData musicData2 = new MusicData(0, R.raw.music_pic2, "风居住的街道", "饭碗的彼岸");
        MusicData musicData3 = new MusicData(0, R.raw.music_pic3, "Ce Frumoasa E Iubirea",
                "Giulia");

        mMusicDatas.add(musicData1);
        mMusicDatas.add(musicData2);
        mMusicDatas.add(musicData3);
    }

    private void try2UpdateMusicPicBackground(final int musicPicRes) {
        if (mRootLayout.isNeed2UpdateBackground(musicPicRes)) {
            new Thread(new Runnable() {
                final int DURATION_TIME = 2 * 100;

                @Override
                public void run() {
                    SystemClock.sleep(DURATION_TIME);
                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRootLayout.setForeground(foregroundDrawable);
                            mRootLayout.beginAnimation();
                        }
                    });
                }
            }).start();
        }
    }

    private Drawable getForegroundDrawable(int musicPicRes) {
        /*得到屏幕的宽高比，以便按比例切割图片一部分*/
        final float widthHeightSize = (float) (DisplayUtils.getScreenWidth(MainActivity.this)
                * 1.0 / DisplayUtils.getScreenHeight(this) * 1.0);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), musicPicRes);
        int cropBitmapWidth = (int) (widthHeightSize * bitmap.getHeight());
        int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);

        /*切割部分图片*/
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth,
                bitmap.getHeight());
        /*缩小图片*/
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50, bitmap
                .getHeight() / 50, false);
        /*模糊化*/
        final Bitmap blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 8, true);

        final Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
        /*加入灰色遮罩层，避免图片过亮影响其他控件*/
        foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return foregroundDrawable;
    }

    @Override
    public void onMusicInfoChanged(String musicName, String musicAuthor) {
        mToolbar.setTitle(musicName);
        mToolbar.setSubtitle(musicAuthor);
    }

    @Override
    public void onMusicPicChanged(int musicPicRes) {
        try2UpdateMusicPicBackground(musicPicRes);
    }

    @Override
    public void onPlayStatusChanged(DiscView.MusicStatus musicStatus) {
        if (musicStatus == DiscView.MusicStatus.PAUSE) {
            mIvPlayOrPause.setImageResource(R.drawable.ic_play);
        } else if (musicStatus == DiscView.MusicStatus.PLAY) {
            mIvPlayOrPause.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mIvPlayOrPause) {
            mDisc.playOrPause();
        } else if (v == mIvNext) {
            mDisc.next();
        } else if (v == mIvLast) {
            mDisc.last();
        }
    }
}
