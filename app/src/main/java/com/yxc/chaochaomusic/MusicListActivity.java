package com.yxc.chaochaomusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yxc.chaochaomusic.adapter.Adapter_LocalMusic;
import com.yxc.chaochaomusic.bean.LocalMusic;

import java.io.Serializable;
import java.util.List;

public class MusicListActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    private ImageView iv_back, iv_playOrPause, iv_playNext;
    private TextView tv_musicName, tv_musicAuthor;
    private LinearLayout ly_paly_bottom;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;
    private static final int REQUESTCODE_MUSICPLAY = 1;
    private Context mContext;
    private Adapter_LocalMusic adapter;
    private ListView listView;
    private List<LocalMusic> musicList;
    private LocalMusic music;
    private MusicListActivityBroadcastReceiver musicListActivityBroadcastReceiver;

    private int index = 0;
    private int state = 10;//10为播放第一首歌曲 11为暂停 12为继续播放
    private int playPattern = 0;//0：列表循环 1：随机播放 2：单曲循环



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_music_list);
        mContext = MusicListActivity.this;
        spf = getSharedPreferences("data", MODE_PRIVATE);
        editor = spf.edit();
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        tv_musicName = findViewById(R.id.tv_musicName);
        tv_musicAuthor = findViewById(R.id.tv_musicAuthor);
        ly_paly_bottom = findViewById(R.id.ly_paly_bottom);
        iv_back = findViewById(R.id.iv_back);
        iv_playOrPause = findViewById(R.id.iv_playOrPause);
        iv_playNext = findViewById(R.id.iv_playNext);
        ly_paly_bottom.setOnClickListener(this);
        iv_playNext.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_playOrPause.setOnClickListener(this);

        playPattern = getPlayPattern();

        //注册广播
        registerActivityBroadcastReceiver();

        getLocalMusicListByIntent();
    }

    private void getLocalMusicListByIntent() {
        Intent intent = getIntent();
        index=intent.getIntExtra("index",0);
        musicList = (List<LocalMusic>) intent.getSerializableExtra("musicList");
        initListView();
    }

    private void initListView() {
        adapter = new Adapter_LocalMusic(musicList, mContext);
        listView.setAdapter(adapter);
        //先默认展示第一首歌曲信息
        showPlayInfo();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                index = i;
                music = musicList.get(i);
                showPlayInfo();

                //发送广播通知服务播放新歌曲
                Intent intent = new Intent();
                intent.setAction("com.xch.musicService");
                intent.putExtra("music", music);
                intent.putExtra("newmusic", 1);
                sendBroadcast(intent);
            }
        });
    }

    /**
     * 注册广播接收器，用于接收serivce发的广播
     */
    private void registerActivityBroadcastReceiver() {
        musicListActivityBroadcastReceiver = new MusicListActivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xch.musicListActivity");
        registerReceiver(musicListActivityBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(musicListActivityBroadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setAction("com.xch.musicService");
        switch (view.getId()) {
            case R.id.ly_paly_bottom://跳转至播放页面
                if(musicList==null||musicList.size()<1){
                    Toast.makeText(mContext, "你没得歌曲，去下载两首嘛！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intentActivity = new Intent(mContext, MusicPlayActivity.class);
                intentActivity.putExtra("musicList", (Serializable) musicList);
                intentActivity.putExtra("index", index);
                intentActivity.putExtra("state", state);
                startActivityForResult(intentActivity, REQUESTCODE_MUSICPLAY);
                break;
            case R.id.iv_back://返回
                Intent intent2 = new Intent();
                intent2.putExtra("index", index);
                intent2.putExtra("state", state);
                setResult(RESULT_OK, intent2);
                finish();
                break;
            case R.id.iv_playOrPause://播放或暂停
                if(musicList==null||musicList.size()<1){
                    Toast.makeText(mContext, "你没得歌曲，去下载两首嘛！", Toast.LENGTH_SHORT).show();
                    return;
                }
                //第一次进入，则播放第一首歌曲
                if (music == null) {
                    music = musicList.get(index);
                    showPlayInfo();
                    intent.putExtra("music", music);
                }
                intent.putExtra("isPlayOrPause", 1);//判断是否是点击了播放/暂停（这个按钮才需判断播放状态）
                break;
            case R.id.iv_playNext://下一首
                if(musicList==null||musicList.size()<1){
                    Toast.makeText(mContext, "你没得歌曲，去下载两首嘛！", Toast.LENGTH_SHORT).show();
                    return;
                }
                index = getBottomIndex();
                music = musicList.get(index);
                showPlayInfo();
                intent.putExtra("music", music);
                intent.putExtra("newmusic", 1);
                break;
            default:
                break;
        }
        sendBroadcast(intent);
    }

    class MusicListActivityBroadcastReceiver extends BroadcastReceiver {
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
            }

            int currPosition = intent.getIntExtra("currPosition", -1);
            int duration = intent.getIntExtra("duration", -1);
            if (currPosition != -1) {
                //将当前歌曲时间转化为位置
                int progress = (int) ((currPosition * 1.0) / duration * 100);
                progressBar.setProgress(progress);
            }
        }
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


    //将当前播放歌曲显示
    private void showPlayInfo() {
        if (musicList != null && musicList.size() > index) {
            tv_musicName.setText(musicList.get(index).getName());
            tv_musicAuthor.setText(musicList.get(index).getAuthor());
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
                iv_playOrPause.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.playbar_btn_play));
                break;
            case 11:
                iv_playOrPause.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.playbar_btn_pause));
                break;
            case 12:
                iv_playOrPause.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.playbar_btn_play));
                break;
            default:
                break;
        }
    }

    /**
     * 从本地存储中读取播放方式
     *
     * @return
     */
    private int getPlayPattern() {
        int pattern = spf.getInt("playPattern", -1);
        if (pattern == -1) {
            pattern = 0;
        }
        return pattern;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_MUSICPLAY:
                if (resultCode == RESULT_OK) {
                    playPattern = getPlayPattern();
                    index = data.getIntExtra("index", -1);
                    state = data.getIntExtra("state", -1);
                    if (index != -1) {
                        //刷新播放信息ui
                        showPlayInfo();
                        listView.setSelection(index);
                    }
                }
                break;
            default:
                break;
        }
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
}
