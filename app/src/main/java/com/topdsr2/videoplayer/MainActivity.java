package com.topdsr2.videoplayer;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private String mVideoUrl = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/protraitVideo.mp4";
    private String mVideoUrl2 = "https://s3-ap-northeast-1.amazonaws.com/mid-exam/Video/taeyeon.mp4";
    private Uri mUri;
    private Uri mUri2;

    private SurfaceView mSurfaceView;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder surfaceHolder;

    private Handler mHandler = new Handler();

    private ImageButton mVolumeOff;
    private ImageButton mFastRewind;
    private ImageButton mPlayArrow;
    private ImageButton mFastForward;
    private ImageButton mFullscreen;
    private ImageButton mVolumeMute;
    private ImageButton mPause;
    private ImageButton mFullscreenExit;
    private TextView mTextViewOnTime;
    private TextView mTextViewEndTime;
    private SeekBar mSeekBar;
    private ProgressBar mProgressBufferingBar;
    private View mView;
    private ConstraintLayout mConstraintLayout;

    private ViewGroup.LayoutParams layoutParams;

    private double startTime = 0;
    private double finalTime = 0;
    private int temp = 0;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    public static int oneTimeOnly = 0;

    private int mScreenWidth;
    private int mScreenHeight;

    private boolean isToolVisible = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 5.0
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 確認取消半透明設置。
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // 跟系統表示要渲染 system bar 背景。
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_main);

        mUri = Uri.parse(mVideoUrl);
        mUri2 = Uri.parse(mVideoUrl2);
        mSurfaceView = findViewById(R.id.surfaceView);
        mVolumeOff = findViewById(R.id.volumeOff);
        mFastRewind = findViewById(R.id.fastRewind);
        mPlayArrow = findViewById(R.id.playArrow);
        mFastForward = findViewById(R.id.fastForward);
        mFullscreen = findViewById(R.id.fullscreen);
        mVolumeMute = findViewById(R.id.volumeMute);
        mPause = findViewById(R.id.pause);
        mFullscreenExit = findViewById(R.id.fullscreenExit);
        mTextViewOnTime = findViewById(R.id.textViewOnTime);
        mTextViewEndTime = findViewById(R.id.textViewEndTime);
        mSeekBar = findViewById(R.id.seekBar);
        mProgressBufferingBar = findViewById(R.id.progressBufferingBar);
        mView = findViewById(R.id.view);
        mConstraintLayout = findViewById(R.id.constraintLayout);

        mVolumeOff.setOnClickListener(this);
        mFastRewind.setOnClickListener(this);
        mPlayArrow.setOnClickListener(this);
        mFastForward.setOnClickListener(this);
        mFullscreen.setOnClickListener(this);
        mVolumeMute.setOnClickListener(this);
        mPause.setOnClickListener(this);
        mFullscreenExit.setOnClickListener(this);
        mTextViewOnTime.setOnClickListener(this);
        mTextViewEndTime.setOnClickListener(this);
        mProgressBufferingBar.setOnClickListener(this);
        mSurfaceView.setOnClickListener(this);
        mSeekBar.setClickable(false);

        layoutParams = mSurfaceView.getLayoutParams();
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("error", "ErrorCode : " + what);
                return false;
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    Log.d("error", "onCompletion");
                    mMediaPlayer.seekTo(0);
                } catch (Exception e) {
                    Log.e("error", e.getMessage(), e);
                }
            }
        });


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.heightPixels;
        mScreenHeight = displayMetrics.widthPixels;

        surfaceHolder = mSurfaceView.getHolder();
        //surfaceHolder.setFixedSize(100, 100);

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mMediaPlayer.reset();//重置为初始状态

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDisplay(surfaceHolder);//设置video影片以surfaceviewholder播放
            mMediaPlayer.setDataSource(mVideoUrl2);//设置路径,这里选用的是一个本地文件，Android支持http协议和rtsp协议。也可以填写这两个协议的链接。
            mMediaPlayer.prepare();//缓冲

            int width = mMediaPlayer.getVideoWidth();
            int height = mMediaPlayer.getVideoHeight();
            if (width <= mScreenWidth && height <= mScreenHeight) {
                float mWidth = (float) width / (float) mScreenWidth;
                float mHeight = (float) height / (float) mScreenHeight;
                float max = Math.max(mWidth, mHeight);
                width = (int) Math.ceil((float) width / max);
                height = (int) Math.ceil((float) height / max);
                layoutParams.width = width;
                layoutParams.height = height;
            }
            mSurfaceView.setLayoutParams(layoutParams);
            mView.setMinimumHeight(height);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.volumeOff:
                mMediaPlayer.setVolume(0, 0);
                mVolumeMute.setVisibility(View.VISIBLE);
                mVolumeOff.setVisibility(View.INVISIBLE);
                break;

            case R.id.fastRewind:
                temp = (int) startTime;
                if ((temp - backwardTime) > 0) {
                    startTime = startTime - backwardTime;
                    mMediaPlayer.seekTo((int) startTime);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Cannot jump backward 5 seconds",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.playArrow:
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();

                    mPlayArrow.setVisibility(View.INVISIBLE);
                    mPause.setVisibility(View.VISIBLE);

                    finalTime = mMediaPlayer.getDuration();
                    startTime = mMediaPlayer.getCurrentPosition();
                    if (oneTimeOnly == 0) {
                        mSeekBar.setMax((int) finalTime);
                        oneTimeOnly = 1;
                    }
                    mTextViewOnTime.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime)
                                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
                    );

                    mTextViewEndTime.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) finalTime)
                                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
                    );
                    mSeekBar.setProgress((int) startTime);
                    mHandler.postDelayed(updateSongTime, 100);
                }
                break;

            case R.id.fastForward:
                temp = (int) startTime;
                if ((temp + forwardTime) <= finalTime) {
                    startTime = startTime + forwardTime;
                    mMediaPlayer.seekTo((int) startTime);
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.fullscreen:
                layoutParams.width = ViewGroup.LayoutParams.FILL_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.FILL_PARENT;

                mFullscreenExit.setVisibility(View.VISIBLE);
                mFullscreen.setVisibility(View.INVISIBLE);

                int widthFull = mMediaPlayer.getVideoWidth();
                int heightFull = mMediaPlayer.getVideoHeight();
                if (widthFull <= mScreenWidth && heightFull <= mScreenHeight) {
                    float mWidth = (float) widthFull / (float) mScreenWidth;
                    float mHeight = (float) heightFull / (float) mScreenHeight;
                    float max = Math.max(mWidth, mHeight);
                    widthFull = (int) Math.ceil((float) widthFull / max);
                    heightFull = (int) Math.ceil((float) heightFull / max);
                    layoutParams.width = widthFull;
                    layoutParams.height = ViewGroup.LayoutParams.FILL_PARENT;
                }
                mSurfaceView.setLayoutParams(layoutParams);
                mView.setMinimumHeight(ViewGroup.LayoutParams.FILL_PARENT);

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                break;

            case R.id.volumeMute:
                mMediaPlayer.setVolume(1, 1);
                mVolumeOff.setVisibility(View.VISIBLE);
                mVolumeMute.setVisibility(View.INVISIBLE);
                break;

            case R.id.pause:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPlayArrow.setVisibility(View.VISIBLE);
                    mPause.setVisibility(View.INVISIBLE);
                }
                break;

            case R.id.fullscreenExit:
                int width = mMediaPlayer.getVideoWidth();
                int height = mMediaPlayer.getVideoHeight();
                if (width <= mScreenWidth && height <= mScreenHeight) {
                    float mWidth = (float) width / (float) mScreenWidth;
                    float mHeight = (float) height / (float) mScreenHeight;
                    float max = Math.max(mWidth, mHeight);
                    width = (int) Math.ceil((float) width / max);
                    height = (int) Math.ceil((float) height / max);
                    layoutParams.width = width;
                    layoutParams.height = height;
                }
                mSurfaceView.setLayoutParams(layoutParams);
                mFullscreen.setVisibility(View.VISIBLE);
                mFullscreenExit.setVisibility(View.INVISIBLE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.progressBufferingBar:
                break;
            case R.id.surfaceView:
                if (isToolVisible) {
                    mConstraintLayout.setVisibility(View.INVISIBLE);
                    isToolVisible = false;
                } else {
                    mConstraintLayout.setVisibility(View.VISIBLE);
                    isToolVisible = true;
                }
                break;
            default:
                break;

        }

    }

    private Runnable updateSongTime = new Runnable() {
        public void run() {
            startTime = mMediaPlayer.getCurrentPosition();
            mTextViewOnTime.setText(String.format(
                    "%d:%d", TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
            );
            mSeekBar.setProgress((int) startTime);
            mHandler.postDelayed(this, 100);
        }
    };


}
