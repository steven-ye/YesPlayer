package com.example.yesplayer.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yesplayer.smb.info.SmbFileInfo;
import com.example.yesplayer.IApplication;
import com.example.yesplayer.R;

import java.util.List;

public class SmbFileAdapter extends RecyclerView.Adapter<SmbFileAdapter.ViewHolder>{
    final List<SmbFileInfo> smbFileInfoList;
    int mLayoutResId;
    public SmbFileAdapter(@LayoutRes int layoutResId, @Nullable List<SmbFileInfo> data) {
        mLayoutResId = layoutResId;
        smbFileInfoList = data;
    }

    protected void convert(ViewHolder holder, SmbFileInfo item) {
        int iconId = R.drawable.ic_baseline_help_outline_24;
        if(item.isDirectory()){
            iconId = R.drawable.ic_baseline_folder_open_24;
        }else if(FileUtils.isMediaFile(item.getFileName())){
            iconId = R.drawable.ic_baseline_movie_24;
        }else if(FileUtils.isMusicFile(item.getFileName())){
            iconId = R.drawable.ic_baseline_music_video_24;
        }else if(FileUtils.isImageFile(item.getFileName())){
            iconId = R.drawable.ic_baseline_image_24;
        }
        holder.setImageResource(R.id.iv, iconId)
                .setText(R.id.tv, item.getFileName())
                .addOnClickListener(R.id.item_layout);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(IApplication.getContext()).inflate(mLayoutResId,parent,false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //int position = holder.getAbsoluteAdapterPosition();
        convert(holder, smbFileInfoList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e("这里是点击每一行item的响应事件",""+position+item);
                if(null != itemChildClickListener){
                    itemChildClickListener.onClick(holder.mAdapter, v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return smbFileInfoList.size();
    }

    public void setList(List<SmbFileInfo> list){
        smbFileInfoList.clear();
        smbFileInfoList.addAll(list);
        notifyDataSetChanged();
    }

    private ItemChildClickListener itemChildClickListener;
    public void setOnItemChildClickListener(ItemChildClickListener childClickListener){
        itemChildClickListener = childClickListener;
    }
    public interface ItemChildClickListener{
        void onClick(SmbFileAdapter adapter, View view, int position);
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        private final SmbFileAdapter mAdapter;
        public ViewHolder(@NonNull View itemView, SmbFileAdapter adapter) {
            super(itemView);
            mAdapter = adapter;
        }

        public ViewHolder setImageResource(int viewId, int resId){
            ImageView iv = itemView.findViewById(viewId);
            iv.setImageResource(resId);
            iv.setColorFilter(Color.GRAY);
            return this;
        }

        public ViewHolder setText(int viewId, String txt){
            TextView tv = itemView.findViewById(viewId);
            tv.setText(txt);
            return this;
        }

        public void addOnClickListener(int resId){
            View v = itemView.findViewById(resId);

            v.setOnClickListener(v1 -> {
                if(mAdapter.itemChildClickListener != null){
                    mAdapter.itemChildClickListener.onClick(mAdapter, v1, getAdapterPosition());
                }
            });
        }
    }
}