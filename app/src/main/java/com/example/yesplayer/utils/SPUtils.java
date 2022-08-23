package com.example.yesplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.yesplayer.IApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPUtils {
    private final SharedPreferences sharedPreferences;

    private static class Holder {
        static SPUtils instance = new SPUtils();
    }

    private SPUtils() {
        sharedPreferences = IApplication.getContext()
                .getSharedPreferences("date", Context.MODE_PRIVATE);
    }

    public static SPUtils getInstance() {
        return Holder.instance;
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    /**
     * 保存List
     * @param tag String
     * @param datalist List<T>
     */
    public <T> void putDataList(String tag, List<T> datalist) {
        if (null == datalist || datalist.size() <= 0) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        //editor.clear();
        editor.putString(tag, strJson);
        editor.apply();
    }

    /**
     * 获取List
     * @param tag String
     * @return datalist List<T>
     */
    public <T> List<T> getDataList(String tag) {
        List<T> datalist=new ArrayList<>();
        String strJson = sharedPreferences.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());
        return datalist;
    }

    /**
     * 存储Map集合
     * @param key 键
     * @param map 存储的集合
     * @param <K> 指定Map的键
     * @param <T> 指定Map的值
     */

    public <K,T> void putMap(String key , Map<K,T> map){
        if (map == null || map.isEmpty()){
            return;
        } else {
            map.size();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String strJson  = gson.toJson(map);
        editor.clear();
        editor.putString(key ,strJson);
        editor.apply();
    }

    /**
     * 获取Map集合
     * */
    public <K,T> Map<K,T> getMap(String key){
        Map<K,T> map = new HashMap<>();
        String strJson = sharedPreferences.getString(key,null);
        if (strJson == null){
            return map;
        }
        Gson gson = new Gson();
        map = gson.fromJson(strJson,new TypeToken<Map<K,T> >(){}.getType());
        return map;
    }
}