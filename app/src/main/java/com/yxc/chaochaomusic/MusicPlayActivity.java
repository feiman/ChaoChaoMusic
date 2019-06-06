package com.yxc.chaochaomusic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yxc.chaochaomusic.bean.LocalMusic;

import java.util.List;

public class MusicPlayActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_playPattern, iv_back;
    private TextView musicPlay_title,musicPlay_Author;
    private Context mContext;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;

    private List<LocalMusic> musicList;
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
        musicList= (List<LocalMusic>) intent.getSerializableExtra("musicList");
        if(musicList!=null&&musicList.size()>index){
            musicPlay_title.setText(musicList.get(index).getName());
            musicPlay_Author.setText(musicList.get(index).getAuthor());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_music_play);
        mContext = MusicPlayActivity.this;
        spf = getSharedPreferences("data", MODE_PRIVATE);
        editor = spf.edit();
        iv_playPattern = findViewById(R.id.iv_playPattern);
        iv_playPattern.setOnClickListener(this);
        iv_back = findViewById(R.id.iv_back);
        musicPlay_title = findViewById(R.id.musicPlay_title);
        musicPlay_Author = findViewById(R.id.musicPlay_Author);
        iv_back.setOnClickListener(this);

        getIntentData();
        playPattern = getPlayPattern();//获取本地存储的播放方式
        patternChange(false);

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_playPattern://循环、列表、随机
                playPattern++;
                patternChange(true);
                break;
            case R.id.iv_back:
                Intent intent = new Intent();
                intent.putExtra("index", index);
                intent.putExtra("state", state);
                setResult(RESULT_OK, intent);
                finish();
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


    private int getPlayPattern() {
        int pattern = spf.getInt("playPattern", -1);
        if (pattern == -1) {
            pattern = 0;
        }
        return pattern;
    }
}
