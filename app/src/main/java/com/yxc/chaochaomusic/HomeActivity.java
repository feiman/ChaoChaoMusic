package com.yxc.chaochaomusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.yxc.chaochaomusic.adapter.Adapter_LocalMusic;
import com.yxc.chaochaomusic.bean.LocalMusic;
import com.yxc.chaochaomusic.fragment.Fragment_Friends;
import com.yxc.chaochaomusic.fragment.Fragment_More;
import com.yxc.chaochaomusic.fragment.Fragment_Music;
import com.yxc.chaochaomusic.service.MusicService;
import com.yxc.chaochaomusic.util.MusicUtil;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private ImageView iv_menu, iv_more, iv_music, iv_friends;

    private List<LocalMusic> musicList;
    private MusicUtil musicUtil;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (musicList != null) {
                        Toast.makeText(mContext, "已扫描到本地" + musicList.size() + "首歌曲", Toast.LENGTH_LONG).show();
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
        iv_menu = findViewById(R.id.iv_menu);
        iv_more = findViewById(R.id.iv_more);
        iv_music = findViewById(R.id.iv_music);
        iv_friends = findViewById(R.id.iv_friends);

        replaceFragment(new Fragment_More());
        iv_more.setSelected(true);

        iv_menu.setOnClickListener(this);
        iv_more.setOnClickListener(this);
        iv_music.setOnClickListener(this);
        iv_friends.setOnClickListener(this);

        //启动服务
        startMusicService();

        //动态权限申请
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            getLocalMusicData();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
            default:
                break;
        }
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
     * 获取本地音乐列表（给fragment调用的）
     *
     * @return
     */
    public List<LocalMusic> getLocaleMusicList() {
        return musicList;
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

}
