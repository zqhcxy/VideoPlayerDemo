# VideoPlayerDemo
## 说明：
 使用VideoView 进行视频的播放，适配横竖屏切换画面拉伸问题。
#主要代码 横竖屏切换：
```java
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
        } else {//竖屏
            newVideoW = screen_width;
            newVideoH = (int) ((float) newVideoW / (float) video_w * (float) vieo_h);
        }
        Log.i("onMeasure", "newVideoW: " + newVideoW + "  newVideoH: " + newVideoH);
        if (newVideoW == 0 || newVideoH == 0) {
            return;
        }
        setVideoViewScale(newVideoW, newVideoH);
    }
```
