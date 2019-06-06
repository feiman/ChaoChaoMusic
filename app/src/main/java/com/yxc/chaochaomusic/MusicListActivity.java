package com.yxc.chaochaomusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yxc.chaochaomusic.adapter.Adapter_LocalMusic;
import com.yxc.chaochaomusic.service.MusicService;
import com.yxc.chaochaomusic.util.LocalMusic;
import com.yxc.chaochaomusic.util.MusicUtil;

import java.util.List;

public class MusicListActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_back, iv_playOrPause;
    private TextView tv_musicName,tv_musicAuthor;
    private LinearLayout ly_paly_bottom;
    private Context mContext;
    private Adapter_LocalMusic adapter;
    private ListView listView;
    private List<LocalMusic> musicList;
    private MusicUtil musicUtil;
    private LocalMusic music;

    private int index = 0;
    private int playPattern = 0;//0：列表循环 1：随机播放 2：单曲循环

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.e("MusicListActivity", musicList.size() + "");
                    adapter = new Adapter_LocalMusic(musicList, mContext);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            index = i;
                            music = musicList.get(i);

                            //发送广播通知服务播放新歌曲
                            Intent intent = new Intent();
                            intent.setAction("com.xch.musicService");
                            intent.putExtra("music", music);
                            intent.putExtra("newmusic", 1);
                            sendBroadcast(intent);
                        }
                    });
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
        setContentView(R.layout.activity_music_list);
        listView = findViewById(R.id.listView);
        tv_musicName = findViewById(R.id.tv_musicName);
        tv_musicAuthor = findViewById(R.id.tv_musicAuthor);
        ly_paly_bottom = findViewById(R.id.ly_paly_bottom);
        iv_back = findViewById(R.id.iv_back);
        iv_playOrPause = findViewById(R.id.iv_playOrPause);
        ly_paly_bottom.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_playOrPause.setOnClickListener(this);
        mContext = MusicListActivity.this;
        //启动服务
        startMusicService();
        //注册广播
        registerActivityBroadcastReceiver();

        //动态权限申请
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MusicListActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            getLocalMusicData();
        }


    }

    /**
     * 注册广播接收器，用于接收serivce发的广播
     */
    private void registerActivityBroadcastReceiver() {
        MusicListActivityBroadcastReceiver musicListActivityBroadcastReceiver = new MusicListActivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xch.musicListActivity");
        registerReceiver(musicListActivityBroadcastReceiver, intentFilter);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setAction("com.xch.musicService");
        switch (view.getId()) {
            case R.id.ly_paly_bottom://跳转至播放页面
                Intent intentActivity = new Intent(mContext, MusicPlayActivity.class);
                intentActivity.putExtra("music", music);
                startActivity(intentActivity);
                break;
            case R.id.iv_back://跳转至播放页面
                finish();
                break;
            case R.id.iv_playOrPause://播放或暂停
                //第一次进入，则播放第一首歌曲
                if (music == null) {
                    music = musicList.get(index);
//                    showPlayInfo();
                    intent.putExtra("music", music);
                }
                intent.putExtra("isPlayOrPause", 1);//判断是否是点击了播放/暂停（这个按钮才需判断播放状态）
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
                //发送广播通知服务播放新歌曲
                Intent intent2 = new Intent();
                intent2.setAction("com.xch.musicService");
                intent2.putExtra("music", music);
                intent2.putExtra("newmusic", 1);
                sendBroadcast(intent2);
            }
        }
    }

    //下一曲按钮歌曲索引
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
    //将当前播放歌曲显示
    public void showPlayInfo() {
        if (musicList != null && musicList.size() > 0) {
            tv_musicName.setText(musicList.get(index).getName());
            tv_musicAuthor.setText(musicList.get(index).getAuthor());
        }
    }
}
