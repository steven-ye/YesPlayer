package com.example.yesplayer.helper;

import com.example.yesplayer.Config;
import com.example.yesplayer.object.FileInfo;
import com.example.yesplayer.utils.SPUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryHelper {
    public static List<Map<String,String>> getDataList(){
        return SPUtils.getInstance().getDataList(Config.SP_HISTORY);
    }
    public static void putDataList(List<Map<String,String>> list){
        SPUtils.getInstance().putDataList(Config.SP_HISTORY, list);
    }
    public static List<FileInfo> getList(){
        List<Map<String,String>> list = getDataList();
        List<FileInfo> fileList = new ArrayList<>();
        for(Map<String,String> map: list){
            FileInfo info = new FileInfo(map.get("name"), map.get("path"), map.get("url"));
            info.setIp(map.get("ip"));
            fileList.add(info);
        }
        return fileList;
    }

    public static void save(String name,String path,String url,String ip){
        //如果设置允许记录播放历史
        List<Map<String,String>> list = SPUtils.getInstance().getDataList(Config.SP_HISTORY);

        for(Map<String,String> map:list){
            if(map.get("url").equals(url)) {
                list.remove(map);
                break;
            }
        }
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("path",path);
        map.put("url",url);
        map.put("ip",ip);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        map.put("time", dateFormat.format(date));
        list.add(0, map);
        if(list.size()>20){
            list.remove(list.size()-1);
        }
        SPUtils.getInstance().putDataList(Config.SP_HISTORY,list);
    }

    public static void save(FileInfo fileInfo){
        save(fileInfo.getFileName(), fileInfo.getPath(), fileInfo.getUrl(), fileInfo.getIp());
    }

    public static boolean remove(int position){
        List<Map<String,String>> fileList = getDataList();
        if(position > fileList.size()) return false;
        fileList.remove(position);
        putDataList(fileList);
        return true;
    }
}
