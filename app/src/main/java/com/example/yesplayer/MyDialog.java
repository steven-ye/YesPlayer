package com.example.yesplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MyDialog extends DialogFragment  implements DialogInterface.OnClickListener{
    final public static int BUTTON_POSITIVE = AlertDialog.BUTTON_POSITIVE;
    final public static int BUTTON_NEGATIVE = AlertDialog.BUTTON_NEGATIVE;
    FragmentManager fm;

    public static MyDialog alert(String title, String message) {
        MyDialog frag = new MyDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        frag.setCancelable(false);
        return frag;
    }

    public static MyDialog confirm(String title, String message){
        MyDialog frag = new MyDialog();
        Bundle args = new Bundle();
        args.putInt("type", 1);
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    /**
     * DialogFragment需要实现onCreateView或者onCreateDIalog方法。
     * onCreateView():使用定义的xml布局文件展示Dialog。
     * onCreateDialog():利用AlertDialog或者Dialog创建出Dialog。
     */
    @Override
    public  Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title", "提示");
        String message = getArguments().getString("message");
        int type = getArguments().getInt("type", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton("取消", this);

        if(type == 1){
            builder.setPositiveButton("确定", this);
        }
        return builder.create();

        //return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int id) {
        if(null != clickListener) clickListener.onClick(id);
    }

    public void show(FragmentManager manager){
        show(manager, "MyDialog");
        fm = manager;
    }

    OnClickListener clickListener;
    public void setOnClickListener(MyDialog.OnClickListener listener){
        clickListener = listener;
    }
    public interface OnClickListener {
        void onClick(int id);
    }
}
