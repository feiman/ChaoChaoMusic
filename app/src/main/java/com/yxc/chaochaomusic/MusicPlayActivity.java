package com.yxc.chaochaomusic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MusicPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_music_play);
    }
}
