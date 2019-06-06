package com.yxc.chaochaomusic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.yxc.chaochaomusic.util.LocalMusic;

public class MusicService extends Service {
    private int newmusic;
    private LocalMusic music;
    private int mstate = 10;//10为播放第一首歌曲 11为暂停 12为继续播放
    private int currPosition, duration;//当前播放时间 总时长
    private MediaPlayer player = new MediaPlayer();//播放歌曲类

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //注册广播
        registerServiceBroadcastReceiver();
        //监听音乐是否播放完
        monitorMusicIsCompletion();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 注册广播接收器，用于接收activity发的广播
     */
    private void registerServiceBroadcastReceiver() {
        MusicServiceBroadcastReceiver musicServiceBroadcastReceiver = new MusicServiceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.xch.musicService");
        registerReceiver(musicServiceBroadcastReceiver, intentFilter);
    }
    /**
     * 监听歌曲播放是否完成
     */
    public void monitorMusicIsCompletion() {
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            //当歌曲播放完成后调用该方法
            public void onCompletion(MediaPlayer mp) {
                //发送广播给Activity播放下一曲
                Intent intent = new Intent("com.xch.musicListActivity");
                intent.putExtra("playFinish", 1);
                sendBroadcast(intent);
                currPosition = 0;
                duration = 0;
            }
        });
    }

    class MusicServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //接收是否播放新歌曲
            newmusic = intent.getIntExtra("newmusic", -1);
            if (newmusic != -1 && newmusic == 1) {
                music = (LocalMusic) intent.getSerializableExtra("music");
                if (music != null) {
                    //播放歌曲
                    playMusic(music);
                    mstate = 11;
                }
            }

            //如果是暂停播放按钮，根据mstate控制播放状态
            int isPlayOrPause = intent.getIntExtra("isPlayOrPause", -1);
            if (isPlayOrPause != -1&&isPlayOrPause==1) {
                switch (mstate) {
                    //第一次播放歌曲
                    case 10:
                        music = (LocalMusic) intent.getSerializableExtra("music");
                        playMusic(music);
                        mstate = 11;
                        break;
                    //暂停
                    case 11:
                        player.pause();
                        mstate = 12;
                        break;
                    //继续播放
                    case 12:
                        player.start();
                        mstate = 11;
                        break;
                }
            }
        }
    }

    /**
     * 播放歌曲
     *
     * @param localMusic:歌曲对象
     */
    public void playMusic(LocalMusic localMusic) {
        //player不为空，说明正在播放歌曲
        if (player != null) {
            //停止播放
            player.stop();
            //等待
            player.reset();
            try {
                //获取歌曲播放路径
                player.setDataSource(localMusic.getPath());
                //准备歌曲
                player.prepare();
                //播放歌曲
                player.start();

                duration = player.getDuration();//获取当前播放歌曲总时长

//                new playMusicThread().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
