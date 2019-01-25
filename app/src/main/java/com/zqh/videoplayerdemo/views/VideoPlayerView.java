package com.zqh.videoplayerdemo.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.zqh.videoplayerdemo.R;
import com.zqh.videoplayerdemo.utils.CommonUtil;

import java.util.Formatter;
import java.util.Locale;

/**
 * 自定义带进度与控制播放与暂停功能，视频播放view
 * <p> 支持格式：MP4、3gp、avi
 * <p> create by zqh 2018/08/06
 */
public class VideoPlayerView extends LinearLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "VideoPlayerView";

    private RelativeLayout mVideoViewLayout;
    private PhotoVideoView mVideoView;

    private RelativeLayout mControlLayout;
    private LinearLayout mButtomView;
    private ImageView mPalyerView;
    private AppCompatSeekBar mSeekBar;
    private ProgressBar mLoaderProgressBar;
    private TextView mAllDution;
    private TextView mCurrentDution;


    private LoadClubVideoUriTask loadClubVideoUriTask;

    //control
//    private MediaController mediaController;
//    private boolean mDragging;

    private boolean isPrepare;

    /**
     * 自定义视频对象
     */
    private Object mCustomVideoData;

    /**
     * 是否第一次失败
     */
    private boolean firstFail = true;

    /**
     * 是否全屏展示，默认 true。 全屏的时，会去计算底部状态栏的高度。
     */
    private boolean isFullScreen =true;

    private Handler mSeekbarProgressHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    int allPro = mVideoView.getDuration();
                    int currentPro = mVideoView.getCurrentPosition();

                    //缓存百分比进度，但是会跟当前播放进度混了。
                    int buff = mVideoView.getBufferPercentage();

                    Log.i(TAG, "mSeekbarProgressHander allPro: " + allPro + "currentPro: " + currentPro + "buff :" + buff);
                    mSeekBar.setMax(allPro);
                    mSeekBar.setProgress(currentPro);
                    mCurrentDution.setText(stringForTime(currentPro));
                    mAllDution.setText(stringForTime(allPro));
                    msg = Message.obtain();
                    msg.what = 1;
                    if (mVideoView.isPlaying()) {
                        mSeekbarProgressHander.sendMessageDelayed(msg, 1000);
                    }
                    break;
            }

        }
    };


    MediaPlayer.OnInfoListener onInfoToPlayStateListener = new MediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.i(TAG, "onInfo what: " + what + "extra: " + extra);
            switch (what) {
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                    mLoaderProgressBar.setVisibility(View.GONE);
                    return true;
                }
                case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                    mLoaderProgressBar.setVisibility(View.VISIBLE);
                    return true;
                }
                case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                    mLoaderProgressBar.setVisibility(View.VISIBLE);
                    return true;
                }
            }
            return false;
        }

    };


    public VideoPlayerView(Context context) {
        this(context, null);
    }

    public VideoPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.video_view_layout, null, false);
        if (attrs != null) {

        }

        initView(view);
        initData();
        addView(view);
    }

    private void initView(View view) {
        mVideoView = view.findViewById(R.id.video_player_view);
        mControlLayout = view.findViewById(R.id.video_player_control_ly);
        mPalyerView = view.findViewById(R.id.video_player_state);
        mSeekBar = view.findViewById(R.id.video_player_seekbar);
        mLoaderProgressBar = view.findViewById(R.id.video_player_loadprogress);
        mCurrentDution = view.findViewById(R.id.video_player_current_dution);
        mAllDution = view.findViewById(R.id.video_player_alldution);
        mVideoViewLayout = view.findViewById(R.id.video_player_ly);
        mButtomView = view.findViewById(R.id.video_player_buttomview);

//        int navigationheight= GalleryUtils.getNavigationBarHeight(getContext());
//        ViewGroup.LayoutParams layoutParams= mButtomView.getLayoutParams();
//        layoutParams.height=navigationheight;
//        mButtomView.setLayoutParams(layoutParams);
        updataButtomControlLayoutUi();
    }

    private void initData() {
        onConfigurationChanged(getResources().getConfiguration());
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        mPalyerView.setImageResource(R.drawable.ic_media_play);
        mSeekBar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        LayerDrawable layerDrawable = (LayerDrawable)
                mSeekBar.getProgressDrawable();
        Drawable dra = layerDrawable.getDrawable(2);
        dra.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mSeekBar.invalidate();
//        mVideoView.setOnInfoListener(onInfoToPlayStateListener);


        mPalyerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPrepare) return;
                if (mVideoView.isPlaying()) {
                    pausePlayer();
                } else {
                    startPalyer();
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                mVideoView.seekTo(progress);
                Log.i(TAG, "onProgressChanged progress : " + progress);
//                if (mCurrentTime != null)
//                    mCurrentTime.setText(stringForTime( (int) newposition));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekbarProgressHander.removeMessages(1);
                Log.i(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendSeekBarProssMesssgae();
                Log.i(TAG, "onStopTrackingTouch");
            }
        });
    }

    public void setActivityFullScreen(boolean full){
        isFullScreen=full;
        updataButtomControlLayoutUi();
    }


    /**
     * 全屏播放
     *<p> 隐藏底部控制面板
     * @param isFullScreen
     */
    public void setFullScrean(boolean isFullScreen) {
        mControlLayout.setVisibility(isFullScreen ? INVISIBLE : VISIBLE);
        mPalyerView.setVisibility(isFullScreen ? INVISIBLE : VISIBLE);
        TranslateAnimation animation = isFullScreen ? CommonUtil.getYAnimation(0, mControlLayout.getHeight()) :
                CommonUtil.getYAnimation(mControlLayout.getHeight(), 0);
        mControlLayout.setAnimation(animation);

    }


    /**
     * 播放地址视频
     *
     * @param path
     */
    public void setVideoPath(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.i(TAG, "video path can't null!");
        } else {
            setVideoUri(Uri.parse(path));
        }
    }

    /**
     * 开始播放
     */
    public void startPalyer() {
        if (mVideoView != null && !mVideoView.isPlaying()) {
            if (mSeekBar.getProgress() == mVideoView.getDuration()) {
                mSeekBar.setProgress(0);
            }
            mVideoView.start();
            sendSeekBarProssMesssgae();
            mPalyerView.setImageResource(R.drawable.ic_media_pause);
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlayer() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        updatePausePlay();
    }

    /**
     * 停止播放
     */
    public void stopPlayer() {
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
            resetVideoUi();
        }
    }

    /**
     * 更新播放按的UI
     * 会出现不准，看下是否直接设置展示UI。
     */
    private void updatePausePlay() {
        if (mPalyerView == null) return;
        if (mVideoView.isPlaying()) {
            mPalyerView.setImageResource(R.drawable.ic_media_pause);
        } else {
            mPalyerView.setImageResource(R.drawable.ic_media_play);
        }
    }

    /**
     * 重置播放UI
     */
    private void resetVideoUi() {
        updatePausePlay();
        mLoaderProgressBar.setVisibility(GONE);
//        mSeekBar.setProgress(0);
    }


    /**
     * 播放uri格式的视频
     *
     * @param videoUri
     */
    public void setVideoUri(Uri videoUri) {
        if (videoUri == null) {
            Log.i(TAG, "videoUri can't null!");
        } else {
            mVideoView.setVideoURI(videoUri);
        }
    }


//    /**
//     * 自定义对象加载
//     *
//     * @param object
//     * @param witchLink
//     */
//    public void setVideoData(Object object, int witchLink) {
//        if (object == null) {
//            Log.i(TAG, "object can't null!");
//            return;
//        } else {
//            if (object instanceof Uri) {
//                setVideoUri((Uri) object);
//                return;
//            }
//            mCustomVideoData = object;
//            if (loadClubVideoUriTask == null || loadClubVideoUriTask.isCancelled()) {
//                loadClubVideoUriTask = new LoadClubVideoUriTask(LoadClubVideoUriTask.DATATYPE_PHOTOBEAN);
//            }
//            loadClubVideoUriTask.setWitchLink(witchLink);
//            loadClubVideoUriTask.execute(object);
//        }
//    }


    private void sendSeekBarProssMesssgae() {
        mSeekbarProgressHander.removeMessages(1);
        Message message = Message.obtain();
        message.what = 1;
        mSeekbarProgressHander.sendMessageDelayed(message, 500);
    }

    public VideoView getmVideoView() {
        return mVideoView;
    }


    /**
     * 视频准备监听
     *
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
//        mVideoView.setMDimension(mp.getVideoWidth(),mp.getVideoHeight());
        int videoW = mp.getVideoWidth();
        int videoH = mp.getVideoHeight();
        mVideoView.setVideo_w(videoW);
        mVideoView.setVideo_h(videoH);
        mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        mLoaderProgressBar.setVisibility(GONE);
        Log.i(TAG, "video view onPrepared");
        isPrepare = true;
        mp.setOnInfoListener(onInfoToPlayStateListener);
        sendSeekBarProssMesssgae();

        updataVideoViewWH(getResources().getConfiguration());
    }

    /**
     * 视频播放完成监听
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "video view onCompletion");
        resetVideoUi();
    }

    /**
     * 视频播放失败监听
     *
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "video view onError what:" + what + "extra : " + extra);
        resetVideoUi();
        return false;
    }


//    @Override
//    public boolean onInfo(MediaPlayer mp, int what, int extra) {
//        com.handcent.common.Log.i(TAG,"onInfo what: "+what+"extra: "+extra);
//        switch (what){
//            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://开始播放
//            case MediaPlayer.MEDIA_INFO_BUFFERING_END://缓存完成，正在恢复播放
//                mLoaderProgressBar.setVisibility(GONE);
//                break;
//            case MediaPlayer.MEDIA_INFO_BUFFERING_START://开始卡顿，在加载更多数据
//                mLoaderProgressBar.setVisibility(VISIBLE);
//                break;
//        }
//
//        return false;
//    }


    StringBuilder mFormatBuilder;
    Formatter mFormatter;

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 获取云端视频链接
     */
    private class LoadClubVideoUriTask extends AsyncTask<Object, String, String> {

        /**
         * 本地文件播放类型
         */
        public static final int DATATYPE_FILE = 0;
        /**
         * 网络视频播放
         */
        public static final int DATATYPE_HTTP = 1;

        /**
         * 当前播放类型，默认本地文件
         */
        private int dataType = DATATYPE_FILE;


        public LoadClubVideoUriTask(int type) {
            dataType = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoaderProgressBar.setVisibility(VISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Object... objects) {
            if (objects == null) return null;
            switch (dataType) {
                case DATATYPE_FILE:
                    break;
                case DATATYPE_HTTP:

                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mLoaderProgressBar.setVisibility(GONE);
            if (!TextUtils.isEmpty(result)) {
                Log.i("LoadClubVideoUriTask", "onPostExecute play video");
                setVideoUri(Uri.parse(result));
                startPalyer();
            }
            cancel(true);
        }
    }

    /**
     * 设置VideoView和最外层相对布局的宽和高
     *
     * @param width  : 像素的单位
     * @param height : 像素的单位
     */
    public void setVideoViewScale(int width, int height) {
        //获取VideoView宽和高
        ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
        //赋值给VideoView的宽和高
        layoutParams.width = width;
        layoutParams.height = height;
        //设置VideoView的宽和高
        mVideoView.setLayoutParams(layoutParams);
        //同上
        ViewGroup.LayoutParams layoutParams1 = mVideoViewLayout.getLayoutParams();
        layoutParams1.width = width;
        layoutParams1.height = height;
        mVideoViewLayout.setLayoutParams(layoutParams1);
    }

    /**
     * 监听屏幕方向改变
     *
     * @param newConfig 配置文件配置了configChanges才会走次此回调
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //配置文件中设置 android:configChanges="orientation|screenSize|keyboardHidden" 不然横竖屏切换的时候重新创建又重新播放
        updataVideoViewWH(newConfig);
        updataButtomControlLayoutUi();
    }

    /**
     * 根据横竖屏更新视频播放视图的宽高
     *
     * @param newConfig
     */
    public void updataVideoViewWH(Configuration newConfig) {
        int video_w = mVideoView.getVideo_w();
        int vieo_h = mVideoView.getVideo_h();
        int screen_width = getResources().getDisplayMetrics().widthPixels;
        int screen_height = getResources().getDisplayMetrics().heightPixels;

        int newVideoW;
        int newVideoH;

        Log.i("onMeasure", "screen_width: " + screen_width + "  screen_height: " + screen_height);
        Log.i("onMeasure", "video_w: " + video_w + "  vieo_h: " + vieo_h);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            newVideoH = screen_height;
            newVideoW = (int) ((float) newVideoH / (float) vieo_h * (float) video_w);
            //当前是横屏
//            if (vieo_h > video_w) {//竖屏的视频
//                newVideoH = vieo_h;
//                if (vieo_h > screen_height) {
//                    newVideoH = screen_height;
//                }
//                newVideoW = (int) ((float) newVideoH / (float) vieo_h * (float) video_w);
//            } else {//横屏的视频
//                newVideoW = video_w > screen_width ? screen_width : video_w;
//                newVideoH = vieo_h > screen_height ? screen_height : vieo_h;
//            }
        } else {//竖屏
            newVideoW = screen_width;
            newVideoH = (int) ((float) newVideoW / (float) video_w * (float) vieo_h);
//            if (vieo_h < video_w) {//横屏的视频
//                newVideoW = video_w;
//
//                if (newVideoW > screen_width) {
//                    newVideoW = screen_width;
//                }
//                newVideoH = (int) ((float) newVideoW / (float) video_w * (float) vieo_h);
//            } else {
//                newVideoW = video_w > screen_width ? screen_width : video_w;
//                newVideoH = vieo_h > screen_height ? screen_height : vieo_h;
//            }
        }
        Log.i("onMeasure", "newVideoW: " + newVideoW + "  newVideoH: " + newVideoH);
        if (newVideoW == 0 || newVideoH == 0) {
            return;
        }
        setVideoViewScale(newVideoW, newVideoH);
    }

    /**
     * 更新底部控制布局的UI，竖屏有虚拟按钮布局，横屏没有。
     */
    private void updataButtomControlLayoutUi() {
        Configuration orientation = getResources().getConfiguration();

        int navigationheight = CommonUtil.getNavigationBarHeight(getContext());
        ViewGroup.LayoutParams layoutParams = mButtomView.getLayoutParams();

        if (orientation.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.height = 0;
        } else {
            if(isFullScreen){
                layoutParams.height = navigationheight;
            }else{
                layoutParams.height = 0;
            }
        }
        mButtomView.setLayoutParams(layoutParams);
    }

    public PhotoVideoView getVideoView() {
        return mVideoView;
    }


    /**
     * 销毁view
     */
    public void onDistroyView() {
        if (loadClubVideoUriTask != null) {
            loadClubVideoUriTask.cancel(true);
            loadClubVideoUriTask = null;
        }
    }
}
