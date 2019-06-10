package com.yxc.chaochaomusic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yxc.chaochaomusic.HomeActivity;
import com.yxc.chaochaomusic.MusicListActivity;
import com.yxc.chaochaomusic.R;
import com.yxc.chaochaomusic.bean.LocalMusic;

import java.io.Serializable;
import java.util.List;

public class Fragment_Music extends Fragment implements View.OnClickListener {
    private LinearLayout ly_localMusic;
    private TextView tv_localeMusicCount;
    private HomeActivity homeActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        ly_localMusic = view.findViewById(R.id.ly_localMusic);
        tv_localeMusicCount=view.findViewById(R.id.tv_localeMusicCount);
        homeActivity= (HomeActivity) getActivity();
        int localMusicCount=homeActivity.getLocaleMusicCount();
        tv_localeMusicCount.setText("("+localMusicCount+")");
        ly_localMusic.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ly_localMusic:
                Intent intent=new Intent(getActivity(), MusicListActivity.class);
                List<LocalMusic> musicList=homeActivity.getLocaleMusicList();
                intent.putExtra("musicList", (Serializable) musicList);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
