package com.zqh.videoplayerdemo;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.zqh.videoplayerdemo.views.VideoPlayerView;

public class VideoPlayActivity extends BaseActivity {

    private VideoPlayerView mVideoPlayerView;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videopaly_layout);

        mVideoPlayerView = findViewById(R.id.videoplayer_view);
        mToolbar = findViewById(R.id.videoplayer_toolbar);


        initData();

    }

    private void initData(){
       mToolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.col_slateblue));
        mToolbar.setTitle("video player");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.col_slateblue));
//            GalleryUtils.setFullScreenForAndroid9(this);
        }
        showFullScreen();
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+ R.raw.vid);
        mVideoPlayerView.setVideoUri(uri);
        mVideoPlayerView.startPalyer();
        mVideoPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mToolbar.getVisibility()==View.GONE){
                    hidFullScreen();
                }else{
                    showFullScreen();
                }

            }
        });


    }


    private void showFullScreen(){
        //隐藏toolabr
        mToolbar.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        mVideoPlayerView.setActivityFullScreen(false);
        mVideoPlayerView.setFullScrean(true);
    }

    private void hidFullScreen(){
        //显示toolabr
        mToolbar.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
        }
        mVideoPlayerView.setActivityFullScreen(true);
        mVideoPlayerView.setFullScrean(false);
    }
}
