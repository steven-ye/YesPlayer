package com.example.yesplayer;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.yesplayer.ui.smbcifs.SmbFragment;
import com.example.yesplayer.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by steven on 2021/3/8.
 */

public class AddServerDialog extends DialogFragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.form_add_server, container, false);
        EditText name_et = view.findViewById(R.id.name_et);
        EditText ip_et = view.findViewById(R.id.ip_et);

        ip_et.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            Pattern pattern = Pattern.compile("[^\\d.]");
            Matcher matcher = pattern.matcher(source);
            if(matcher.find()){
                Utils.showToast("只能输入数字或.");
                return "";
            }
            return null;
        }});

        view.findViewById(R.id.okay_bt).setOnClickListener(v -> {
            String name = name_et.getText().toString();
            String ip = ip_et.getText().toString();
            if(TextUtils.isEmpty(name)){
                Utils.showToast("请输入主机名称");
                return;
            }
            if(TextUtils.isEmpty(ip)){
                Utils.showToast("请输入IP地址");
                return;
            }
            Map<String,String> map = new HashMap<>();
            map.put("name", name);
            map.put("ip", ip);
            if(clickListener != null) clickListener.onClick(map);
            dismiss();
        });

        view.findViewById(R.id.cancel_bt).setOnClickListener(v->{
            dismiss();
        });
        return view;
    }
    private OnClickListener clickListener;
    public void setOnClickListener(OnClickListener listener){
        clickListener = listener;
    }
    public interface OnClickListener {
        void onClick(Map<String,String> map);
    }
}
