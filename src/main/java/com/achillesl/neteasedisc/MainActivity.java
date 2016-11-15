package com.achillesl.neteasedisc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private DiscView mIvDisk;
    private ImageView mIvPlay;
    private boolean isPlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIvDisk = (DiscView) findViewById(R.id.discview);
        mIvPlay = (ImageView) findViewById(R.id.ivPlay);

        mIvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPlay = !isPlay;
                if (isPlay) {
                    mIvPlay.setImageResource(R.drawable.play_fm_btn_pause_prs);
                    mIvDisk.play();
                } else {
                    mIvPlay.setImageResource(R.drawable.play_fm_btn_play_prs);
                    mIvDisk.pause();
                }
            }
        });
    }
}
