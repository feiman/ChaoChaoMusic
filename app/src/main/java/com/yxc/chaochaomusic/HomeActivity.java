package com.yxc.chaochaomusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yxc.chaochaomusic.adapter.Adapter_LocalMusic;
import com.yxc.chaochaomusic.bean.LocalMusic;
import com.yxc.chaochaomusic.fragment.Fragment_Friends;
import com.yxc.chaochaomusic.fragment.Fragment_More;
import com.yxc.chaochaomusic.fragment.Fragment_Music;
import com.yxc.chaochaomusic.service.MusicService;
import com.yxc.chaochaomusic.util.MusicUtil;

import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private ImageView iv_menu, iv_more, iv_music, iv_friends, iv_playOrPause, iv_playNext;
    private TextView tv_musicName, tv_musicAuthor;
    private LinearLayout ly_paly_bottom;
    private ProgressBar progressBar;

    private HomeActivityBroadcastReceiver homeActivityBroadcastReceiver;
    private List<LocalMusic> musicList;
    private LocalMusic music;
    private MusicUtil musicUtil;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;

    private static final int REQUESTCODE_MUSICLIST = 1;
    private static final int REQUESTCODE_MUSICPLAY = 2;

    private int index = 0;
    private int state = 10;//10为播放第一首歌曲 11为暂停 12为继续播放
    private int playPattern = 0;//0：列表循环 1：随机播放 2：单曲循环


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (musicList != null) {
                        if(musicList.size()<1){
                            Toast.makeText(mContext, "没扫描到本地歌曲，去下载两首嘛！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(mContext, "已扫描到本地" + musicList.size() + "首歌曲", Toast.LENGTH_LONG).show();
                        //先默认展示第一首歌曲
                        showPlayInfo();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_home);
        mContext = HomeActivity.this;
        spf = getSharedPreferences("data", MODE_PRIVATE);
        editor = spf.edit();
        iv_menu = findViewById(R.id.iv_menu);
        tv_musicName = findViewById(R.id.tv_musicName);
        tv_musicAuthor = findViewById(R.id.tv_musicAuthor);
        iv_more = findViewById(R.id.iv_more);
        progressBar = findViewById(R.id.progressBar);
        iv_music = findViewById(R.id.iv_music);
        iv_friends = findViewById(R.id.iv_friends);
        iv_playOrPause = findViewById(R.id.iv_playOrPause);
        iv_playNext = findViewById(R.id.iv_playNext);
        ly_paly_bottom = findViewById(R.id.ly_paly_bottom);

        replaceFragment(new Fragment_More());
        iv_more.setSelected(true);

        iv_menu.setOnClickListener(this);
        iv_more.setOnClickListener(this);
        iv_music.setOnClickListener(this);
        iv_friends.setOnClickListener(this);
        iv_playOrPause.setOnClickListener(this);
        iv_playNext.setOnClickListener(this);
        ly_paly_bottom.setOnClickListener(this);

        //启动服务
        startMusicService();
        registerHomeBroadcastReceiver();

        //动态权限申请
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            getLocalMusicData();
        }
    }

    private void registerHomeBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xch.homeActivity");
        homeActivityBroadcastReceiver = new HomeActivityBroadcastReceiver();
        registerReceiver(homeActivityBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(homeActivityBroadcastReceiver);
    }

    class HomeActivityBroadcastReceiver extends BroadcastReceiver {
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

            //歌曲进度条
            int currPosition = intent.getIntExtra("currPosition", -1);
            int duration = intent.getIntExtra("duration", -1);
            if (currPosition != -1) {
                //将当前歌曲时间转化为位置
                int progress = (int) ((currPosition * 1.0) / duration * 100);
                progressBar.setProgress(progress);
            }
        }
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
            case R.id.iv_menu:
                //menu的点击事件
                Toast.makeText(HomeActivity.this, "这是menu", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_more:
                iv_more.setSelected(true);
                iv_music.setSelected(false);
                iv_friends.setSelected(false);
                replaceFragment(new Fragment_More());
                break;
            case R.id.iv_music:
                iv_more.setSelected(false);
                iv_music.setSelected(true);
                iv_friends.setSelected(false);
                replaceFragment(new Fragment_Music());
                break;
            case R.id.iv_friends:
                iv_more.setSelected(false);
                iv_music.setSelected(false);
                iv_friends.setSelected(true);
                replaceFragment(new Fragment_Friends());
                break;
            case R.id.iv_playOrPause://播放或暂停
                if (musicList == null || musicList.size() < 1) {
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
                if (musicList == null || musicList.size() < 1) {
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

    /**
     * 动态添加fragment（碎片）
     *
     * @param fragment
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }

    /**
     * 启动播放音乐的服务
     */
    private void startMusicService() {
        Intent startIntent = new Intent(mContext, MusicService.class);
        startService(startIntent);
    }

    private void stopMusicService() {
        Intent stopIntent = new Intent(mContext, MusicService.class);
        stopService(stopIntent);
    }

    /**
     * 获取本地存储的音乐
     */
    private void getLocalMusicData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                musicUtil = new MusicUtil();
                musicList = musicUtil.getMusicData(mContext);

                //异步消息处理机制
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 获取本地音乐数量（给fragment调用的）
     *
     * @return
     */
    public int getLocaleMusicCount() {
        if (musicList == null) {
            return 0;
        }
        return musicList.size();
    }

    /**
     * 跳转至音乐列表（给fragment调用的）
     */
    public void jumpMusicListActivity() {
        Intent intent = new Intent(mContext, MusicListActivity.class);
        intent.putExtra("musicList", (Serializable) musicList);
        intent.putExtra("index", index);
        startActivityForResult(intent, REQUESTCODE_MUSICLIST);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocalMusicData();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法扫描到本地音乐哟！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_MUSICPLAY:
            case REQUESTCODE_MUSICLIST:
                if (resultCode == RESULT_OK) {
                    playPattern = getPlayPattern();
                    index = data.getIntExtra("index", -1);
                    state = data.getIntExtra("state", -1);
                    if (index != -1) {
                        //刷新播放信息ui
                        showPlayInfo();
                    }
                }
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

    //将当前播放歌曲显示
    private void showPlayInfo() {
        if (musicList != null && musicList.size() > index) {
            tv_musicName.setText(musicList.get(index).getName());
            tv_musicAuthor.setText(musicList.get(index).getAuthor());
        }
    }
}
