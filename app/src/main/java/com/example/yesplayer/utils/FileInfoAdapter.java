package com.example.yesplayer.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yesplayer.IApplication;
import com.example.yesplayer.R;
import com.example.yesplayer.object.FileInfo;

import java.io.File;
import java.util.List;

public class FileInfoAdapter extends RecyclerView.Adapter<FileInfoAdapter.ViewHolder>{
    List<FileInfo> fileInfoList;
    int mLayoutResId;
    boolean isShowAction = false;

    public FileInfoAdapter(List<FileInfo> data) {
        mLayoutResId = R.layout.item_file;
        fileInfoList = data;
    }

    public FileInfoAdapter(@LayoutRes int layoutResId, @Nullable List<FileInfo> data) {
        mLayoutResId = layoutResId;
        fileInfoList = data;
    }

    protected void convert(ViewHolder holder, FileInfo item) {
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
        holder.setImageResource(R.id.iv_type_icon, iconId)
                .setText(R.id.tv_file_name, item.getFileName())
                .addOnClickListener(R.id.item_layout);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void showAction(boolean b){
        isShowAction = b;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(IApplication.getContext()).inflate(mLayoutResId,parent,false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        convert(holder, fileInfoList.get(position));
        holder.itemView.setOnClickListener(v -> {
            //Log.e("这里是点击每一行item的响应事件",""+position+item);
            if(null != itemChildClickListener){
                itemChildClickListener.onClick(holder.mAdapter, v, position);
            }
        });

        ImageButton btnDel = holder.itemView.findViewById(R.id.action_delete);
        if(null == btnDel) return;
        btnDel.setOnClickListener(view -> {
            if(null != itemActionClickListener){
                itemActionClickListener.onClick(holder.mAdapter, view, position);
            }
        });
        btnDel.setVisibility(isShowAction ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return fileInfoList.size();
    }

    public void setList(List<FileInfo> list){
        fileInfoList.clear();
        fileInfoList.addAll(list);
        notifyDataSetChanged();
    }

    public void setFileList(List<File> list){
        fileInfoList.clear();
        for(File f:list){
            //Log.w("FileListFragment", "FileName is "+ f.getName());
            FileInfo fileInfo = new FileInfo(f);
            fileInfoList.add(fileInfo);
        }
        notifyDataSetChanged();
    }

    private ItemChildClickListener itemChildClickListener;
    public void setOnItemChildClickListener(ItemChildClickListener childClickListener){
        itemChildClickListener = childClickListener;
    }
    public interface ItemChildClickListener{
        void onClick(FileInfoAdapter adapter, View view, int position);
    }

    private ItemActionClickListener itemActionClickListener;
    public void setOnItemActionClickListener(ItemActionClickListener actionClickListener){
        itemActionClickListener = actionClickListener;
    }
    public interface ItemActionClickListener{
        void onClick(FileInfoAdapter adapter, View view, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private final FileInfoAdapter mAdapter;
        public ViewHolder(@NonNull View itemView, FileInfoAdapter adapter) {
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
                    mAdapter.itemChildClickListener.onClick(mAdapter, v1, getAbsoluteAdapterPosition());
                }
            });
        }
    }
}