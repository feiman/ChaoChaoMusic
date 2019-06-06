package com.yxc.chaochaomusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.yxc.chaochaomusic.R;
import com.yxc.chaochaomusic.util.LocalMusic;

import java.text.SimpleDateFormat;
import java.util.List;

public class Adapter_LocalMusic extends BaseAdapter {
    private List<LocalMusic> oList;
    private Context ocontext;
    private LayoutInflater oInflater;
    public Adapter_LocalMusic(List<LocalMusic> oList, Context context){
       this.oList=oList;
        this.ocontext=context;
        this.oInflater= LayoutInflater.from(ocontext);
    }
    @Override
    public int getCount() {
        return oList.size();
    }

    @Override
    public Object getItem(int position) {
        return oList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            convertView=oInflater.inflate(R.layout.item_localmusic,null);
            viewHolder=new ViewHolder();
            //得到各个控件的对象
            viewHolder.tv_musicIndex=convertView.findViewById(R.id.tv_musicIndex);
            viewHolder.name=convertView.findViewById(R.id.musicName);
            viewHolder.author= convertView.findViewById(R.id.musicAuthor);
            viewHolder.duration=convertView.findViewById(R.id.musicDuration);
            //绑定viewHolder对象
            convertView.setTag(viewHolder);

        }else {
            //取出viewHolder对象
            viewHolder= (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_musicIndex.setText(position+1+"");
        viewHolder.name.setText(oList.get(position).getName());
        viewHolder.author.setText(oList.get(position).getAuthor());
        viewHolder.duration.setText(getDurations(oList.get(position).getDuration()));
        return convertView;
    }

    /**
     * 存放控件
     */
    class ViewHolder{
        TextView tv_musicIndex;
        TextView name;
        TextView author;
        TextView duration;
    }

    /**
     * long类型转化为String类型
     * @param duration
     * @return
     */
    private String getDurations(long duration){
        SimpleDateFormat formats=new SimpleDateFormat("mm:ss");
        String durationResult=formats.format(duration);
        return durationResult;
    }

}
