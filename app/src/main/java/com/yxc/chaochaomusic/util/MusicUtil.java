package com.yxc.chaochaomusic.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;

import com.yxc.chaochaomusic.bean.LocalMusic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yxc on 20190604
 */
public class MusicUtil {

    /**
     * 获取手机里的音乐
     * @return 返回一个集合，集合的对象为MusicResult,包含了歌曲名，演唱者，时长，以及歌曲路径
     */
    public List<LocalMusic> getMusicData(Context context){
        List<LocalMusic> oList=new ArrayList<LocalMusic>();
        ContentResolver resolver=context.getContentResolver();
        Cursor cursor=resolver.query(Media.EXTERNAL_CONTENT_URI,null,null,null, Media.DEFAULT_SORT_ORDER);
        if(cursor!=null){
            while (cursor.moveToNext()){
                LocalMusic music=new LocalMusic();
                //歌曲ID
                long id=cursor.getLong(cursor.getColumnIndex(Media._ID));
                //歌曲名
                String name=cursor.getString(cursor.getColumnIndex(Media.TITLE));
                //演唱者
                String author=cursor.getString(cursor.getColumnIndex(Media.ARTIST));
                //路径
                String path=cursor.getString(cursor.getColumnIndex(Media.DATA));
                //时长
                long duration=cursor.getLong(cursor.getColumnIndex(Media.DURATION));
                //是否为音乐
//                int isMusic=cursor.getInt(cursor.getColumnIndex(Media.IS_MUSIC));
                //专辑
                String album=cursor.getString(cursor.getColumnIndex(Media.ALBUM));

                //大小
                String size=cursor.getString(cursor.getColumnIndex(Media.SIZE));

                if(author.equals("<unkonw>")){
                    author="未知艺术家";
                }

                //小于30秒的不要
                if(duration>3000){
                    music.setName(name);
                    music.setAuthor(author);
                    music.setPath(path);
                    music.setDuration(duration);
                    music.setSize(size);
                    oList.add(music);
                }
            }
        }
        return oList;
    }

}
