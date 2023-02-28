package com.example.yesplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Map;

/**
 * Created by steven on 2021/3/8.
 */

public abstract class BaseDialog extends DialogFragment {
    int mLayoutId;
    private OnClickListener onClickListener ;
    public BaseDialog(int layoutId){
        mLayoutId = layoutId;
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(mLayoutId, container, false);
        return initView(view);
    }
    protected abstract View initView(View view);
    protected int getLayoutId() {
        return mLayoutId;
    }
    public void setOnClickListener(OnClickListener clickListener){
        onClickListener = clickListener;
    }
    public interface OnClickListener {
        void onClick(Map<String,String> map);
    }
}
