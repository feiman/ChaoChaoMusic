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

import com.yxc.chaochaomusic.MusicListActivity;
import com.yxc.chaochaomusic.R;

public class Fragment_Music extends Fragment implements View.OnClickListener {
    private LinearLayout ly_localMusic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        ly_localMusic = view.findViewById(R.id.ly_localMusic);
        ly_localMusic.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ly_localMusic:
                Intent intent=new Intent(getActivity(), MusicListActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
