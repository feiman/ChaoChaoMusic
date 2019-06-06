package com.yxc.chaochaomusic.util;


import java.io.Serializable;

/**
 * Created by yxc on 20190604
 */
public class LocalMusic implements Serializable {
    private static final long serialVersionUID = 1L;
    //歌曲名
    private String name;
    //演唱者
    private String author;
    //歌曲路径
    private String path;
    //歌曲时长
    private long duration;
    //歌曲大小
    private String size;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
