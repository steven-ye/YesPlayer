package com.example.yesplayer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.yesplayer.IApplication;
import com.example.yesplayer.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by xyoye on 2021/3/30.
 */

public class Utils {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
                System.out.println("here1 ");
            }else {
                System.out.println("here2 ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToast(String text){
        Toast.makeText(IApplication.getContext(),text,Toast.LENGTH_SHORT).show();
    }
    public static void alert(Context context,String text){
        alert(context,text,null);
    }
    public static void alert(Context context,String text, DialogInterface.OnClickListener listener){
        CharSequence[] msg = {text};
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setTitle("温馨提示")
                .setCancelable(false)
                .setItems(msg, null)
                .setPositiveButton("确定", listener)
                .show();
    }

    public static void log(String text){
        System.out.println(text);
    }

    public static void writeFile(String filename, String string){
        //String FILENAME = "hello_file.txt";
        //String string = "hello world!";

        FileOutputStream fos = null;
        try {
            //文件路径  /data/data/com.example.myapplication/files/
            //MODE_PRIVATE（默认）：覆盖、MODE_APPEND：追加
            fos = IApplication.getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String filename){
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try{
            FileInputStream fis = IApplication.getContext().openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(fis));
            String line= "";
            while((line = reader.readLine())!=null){
                builder.append(line);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(reader != null){
                try{
                    reader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
}
