package com.yxc.chaochaomusic;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.yxc.chaochaomusic.fragment.Fragment_Friends;
import com.yxc.chaochaomusic.fragment.Fragment_More;
import com.yxc.chaochaomusic.fragment.Fragment_Music;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView iv_menu, iv_more, iv_music, iv_friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_home);
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
}
