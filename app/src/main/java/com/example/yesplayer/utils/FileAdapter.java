package com.example.yesplayer.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yesplayer.IApplication;
import com.example.yesplayer.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder>{
    List<File> mFiles;
    public FileAdapter(File[] files) {
        mFiles = Arrays.asList(files);
    }
    public FileAdapter(List<File> files) {
        mFiles = files;
    }

    protected void convert(ViewHolder holder, File file) {
        int iconId = R.drawable.ic_baseline_help_outline_24;
        if(file.isDirectory()){
            iconId = R.drawable.ic_baseline_folder_open_24;
        }else if(FileUtils.isMediaFile(file.getName())){
            iconId = R.drawable.ic_baseline_movie_24;
        }else if(FileUtils.isMusicFile(file.getName())){
            iconId = R.drawable.ic_baseline_music_video_24;
        }else if(FileUtils.isImageFile(file.getName())){
            iconId = R.drawable.ic_baseline_image_24;
        }
        holder.setImageResource(R.id.iv, iconId)
                .setText(R.id.tv, file.getName())
                .addOnClickListener(R.id.item_layout);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(IApplication.getContext()).inflate(R.layout.item_file,parent,false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        convert(holder, mFiles.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("这里是点击每一行item的响应事件",""+position+item);
                if(null != itemClickListener){
                    itemClickListener.onClick(holder.mAdapter, v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    private ItemClickListener itemClickListener;
    public void setItemClickListener(ItemClickListener childClickListener){
        itemClickListener = childClickListener;
    }
    public interface ItemClickListener{
        void onClick(FileAdapter adapter, View view, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private final FileAdapter mAdapter;
        public ViewHolder(@NonNull View itemView, FileAdapter adapter) {
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
                if(mAdapter.itemClickListener != null){
                    mAdapter.itemClickListener.onClick(mAdapter, v1, getBindingAdapterPosition());
                }
            });
        }
    }
}