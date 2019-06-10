package com.yxc.chaochaomusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yxc.chaochaomusic.bean.LocalMusic;

import java.util.List;

public class MusicPlayActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout ly_activity_play;
    private ImageView iv_playPattern, iv_back, iv_playOrPause, iv_prev, iv_next;
    private SeekBar seekBar;
    private TextView musicPlay_title, musicPlay_Author,tv_time;
    private Context mContext;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;

    private List<LocalMusic> musicList;
    private LocalMusic music;
    private int index = 0;
    private int playPattern = 0;//0：列表循环 1：随机播放 2：单曲循环
    private int state = 10;//10为播放第一首歌曲 11为暂停 12为继续播放


    /***
     * 获取MainActivity传递过来的数据
     */
    public void getIntentData() {
        Intent intent = getIntent();
        index = intent.getIntExtra("index", -1);
        state = intent.getIntExtra("state", -1);
        musicList = (List<LocalMusic>) intent.getSerializableExtra("musicList");
        showPlayInfo();
    }

    /**
     * 注册广播接收器，用于接收serivce发的广播
     */
    private void registerActivityBroadcastReceiver() {
        MusicPlayActivityBroadcastReceiver musicPlayActivityBroadcastReceiver = new MusicPlayActivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xch.musicPlayActivity");
        registerReceiver(musicPlayActivityBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_music_play);
        mContext = MusicPlayActivity.this;
        spf = getSharedPreferences("data", MODE_PRIVATE);
        editor = spf.edit();
        seekBar = findViewById(R.id.seekBar);
        tv_time = findViewById(R.id.time);
        iv_playPattern = findViewById(R.id.iv_playPattern);
        ly_activity_play = findViewById(R.id.activity_play);
        iv_playPattern.setOnClickListener(this);
        iv_next = findViewById(R.id.iv_next);
        iv_next.setOnClickListener(this);
        iv_prev = findViewById(R.id.iv_prev);
        iv_prev.setOnClickListener(this);
        iv_playOrPause = findViewById(R.id.iv_playOrPause);
        iv_playOrPause.setOnClickListener(this);
        iv_back = findViewById(R.id.iv_back);
        musicPlay_title = findViewById(R.id.musicPlay_title);
        musicPlay_Author = findViewById(R.id.musicPlay_Author);
        iv_back.setOnClickListener(this);

        getIntentData();
        playPattern = getPlayPattern();//获取本地存储的播放方式
        registerActivityBroadcastReceiver();
        patternChange(false);
        updatePlayOrPauseUI(state);
        musicPlayBg(state);

        seekBarChangeListener();
    }

    class MusicPlayActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //歌曲是否播放完，播放完则播放下一曲
            int playFinish = intent.getIntExtra("playFinish", -1);
            if (playFinish != -1 && playFinish == 1) {
                index = getBottomIndex();
                music = musicList.get(index);
                showPlayInfo();
                //发送广播通知服务播放新歌曲
                Intent intent2 = new Intent();
                intent2.setAction("com.xch.musicService");
                intent2.putExtra("music", music);
                intent2.putExtra("newmusic", 1);
                sendBroadcast(intent2);
            }

            //获取播放状态更改UI（暂停，播放）,state:10为播放第一首歌曲 11为暂停 12为继续播放
            state = intent.getIntExtra("state", -1);
            if (state != -1) {
                updatePlayOrPauseUI(state);
                musicPlayBg(state);
            }

            int currPosition = intent.getIntExtra("currPosition", -1);
            int duration = intent.getIntExtra("duration", -1);
            if (currPosition != -1) {
                //将当前歌曲时间转化为位置
                int progress = (int) ((currPosition * 1.0) / duration * 100);
                seekBar.setProgress(progress);
                tv_time.setText(timeFormat(currPosition, duration));
            }
        }
    }


    /**
     * 上一曲按钮歌曲索引
     *
     * @return
     */
    public int getTopIndex() {
        int mindex = 0;
        if (playPattern == 0) {//顺序播放
            if (index == 0) {
                mindex = musicList.size() - 1;
            } else {
                mindex = --index;
            }
        }
        if (playPattern == 1) {//随机播放
            mindex = (int) (Math.random() * (musicList.size() - 1));
        }
        if (playPattern == 2) {//单曲循环
            mindex = index;
        }
        return mindex;
    }

    /**
     * 下一曲按钮歌曲索引
     *
     * @return
     */
    public int getBottomIndex() {
        int mindex = 0;
        if (playPattern == 0) {//顺序播放
            if (index == musicList.size() - 1) {
                mindex = 0;
            } else {
                mindex = ++index;
            }
        }
        if (playPattern == 1) {//随机播放
            mindex = (int) (Math.random() * (musicList.size() - 1));
        }
        if (playPattern == 2) {//单曲循环
            mindex = index;
        }
        return mindex;
    }

    /**
     * 监听进度条事件
     */
    public void seekBarChangeListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //拖动停止,发送广播更新播放位置
                int progress = seekBar.getProgress();
                Intent intent = new Intent("com.xch.musicService");
                intent.putExtra("progress", progress);
                sendBroadcast(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setAction("com.xch.musicService");
        switch (view.getId()) {
            case R.id.iv_playPattern://循环、列表、随机
                playPattern++;
                patternChange(true);
                break;
            case R.id.iv_back:
                Intent intent2 = new Intent();
                intent2.putExtra("index", index);
                intent2.putExtra("state", state);
                setResult(RESULT_OK, intent2);
                finish();
                break;
            case R.id.iv_prev://上一首
                index = getTopIndex();
                music = musicList.get(index);
                showPlayInfo();
                intent.putExtra("music", music);
                intent.putExtra("newmusic", 1);
                break;
            case R.id.iv_next://下一首
                index = getBottomIndex();
                music = musicList.get(index);
                showPlayInfo();
                intent.putExtra("music", music);
                intent.putExtra("newmusic", 1);
                break;
            case R.id.iv_playOrPause:
                //第一次进入，则播放第一首歌曲
                if (music == null) {
                    music = musicList.get(index);
                    showPlayInfo();
                    intent.putExtra("music", music);
                }
                intent.putExtra("isPlayOrPause", 1);//判断是否是点击了播放/暂停（这个按钮才需判断播放状态）
                break;
            default:
                break;
        }
        sendBroadcast(intent);
    }


    //按下返回键
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("index", index);
        intent.putExtra("state", state);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 根据播放状态改变修改对应布局
     *
     * @param isShowToast 是否弹出toast
     */
    public void patternChange(boolean isShowToast) {
        if (playPattern > 2) {
            playPattern = 0;
        }
        //将播放方式存至本地
        editor.putInt("playPattern", playPattern);
        editor.commit();

        switch (playPattern) {
            case 0:
                iv_playPattern.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.play_icn_loop));
                if (isShowToast) {
                    Toast.makeText(mContext, "列表循环", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                iv_playPattern.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.play_icn_shuffle));
                if (isShowToast) {
                    Toast.makeText(mContext, "随机播放", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                iv_playPattern.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.play_icn_one));
                if (isShowToast) {
                    Toast.makeText(mContext, "单曲循环", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void showPlayInfo() {
        if (musicList != null && musicList.size() > index) {
            musicPlay_title.setText(musicList.get(index).getName());
            musicPlay_Author.setText(musicList.get(index).getAuthor());
        }
    }

    /**
     * 根据状态更改播放暂停按钮UI界面
     *
     * @param state
     */
    private void updatePlayOrPauseUI(int state) {
        switch (state) {
            case 10:
                iv_playOrPause.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.play_rdi_btn_play));
                break;
            case 11:
                iv_playOrPause.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.play_rdi_btn_pause));
                break;
            case 12:
                iv_playOrPause.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.play_rdi_btn_play));
                break;
            default:
                break;
        }
    }

    /**
     * 播放、暂停对应的背景
     * @param state
     */
    public void musicPlayBg(int state) {
        switch (state) {
            case 10:
                ly_activity_play.setBackgroundResource(R.mipmap.playing_bg);
                break;
            case 11:
                ly_activity_play.setBackgroundResource(R.mipmap.playing_bg2);
                break;
            case 12:
                ly_activity_play.setBackgroundResource(R.mipmap.playing_bg);
                break;
            default:
                break;
        }
    }

    private int getPlayPattern() {
        int pattern = spf.getInt("playPattern", -1);
        if (pattern == -1) {
            pattern = 0;
        }
        return pattern;
    }

    private String timeFormat(int currPosition, int duration) {
        int currMinute = currPosition / 1000 / 60;//分钟
        int currSecond = currPosition / 1000 % 60;//秒
        int durationMinute = duration / 1000 / 60;
        int durationSecond = duration / 1000 % 60;
        return timeChange(currMinute) + ":" + timeChange(currSecond) + "/" + timeChange(durationMinute) + ":" + timeChange(durationSecond);
    }

    private String timeChange(int time) {
        if (time < 10) {
            return "0" + time;
        } else {
            return time + "";
        }
    }
}
