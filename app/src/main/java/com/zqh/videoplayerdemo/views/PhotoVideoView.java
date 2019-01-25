package com.zqh.videoplayerdemo.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * 自定义Videview 解决播放时旁边有白边
 */
public class PhotoVideoView extends VideoView {

    private int video_w,video_h;

    public PhotoVideoView(Context context) {
        super(context);
    }

    public PhotoVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        此处设置的默认值可随意,因为getDefaultSize中的size是有值的
            int width = getDefaultSize(video_w,widthMeasureSpec);
            int height = getDefaultSize(video_h,heightMeasureSpec);
            int oren=getContext().getResources().getConfiguration().orientation;
        Log.i("onMeasure","width: "+width+"\n height: "+height+"\n widthMeasureSpec :"+
                widthMeasureSpec+"\n heightMeasureSpec: "+heightMeasureSpec+"\n  video_w: "+video_w+"\n video_h: "+video_h);
            if(oren== Configuration.ORIENTATION_LANDSCAPE){
                if(video_h>video_w){//竖屏视频
                   int screen_width = getResources().getDisplayMetrics().widthPixels-300;
                    int screen_H = getResources().getDisplayMetrics().heightPixels;
//                   int new_width= (int) (((float)video_w/(float)video_h)* screen_width);
                    int new_width= (int) (((float)screen_width/(float)screen_H)* video_w);
                    Log.i("onMeasure","new_width: "+new_width+"\n screen_width: "+screen_width);
                    setMeasuredDimension(new_width, screen_width);
                }else{
                    setMeasuredDimension(width, height);
                }
//                setMeasuredDimension(video_h, video_w);
            }else if(oren==Configuration.ORIENTATION_PORTRAIT){
//                setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
            }

    }

    public void setMDimension(int width,int height){
        setMeasuredDimension(width,height);
    }

    public void setVideo_w(int video_w) {
        this.video_w = video_w;
    }

    public void setVideo_h(int video_h) {
        this.video_h = video_h;
    }

    public int getVideo_w() {
        return video_w;
    }

    public int getVideo_h() {
        return video_h;
    }
}
