package com.example.yesplayer.service;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.yesplayer.IApplication;
import com.example.yesplayer.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;

public class HttpService extends NanoHTTPD {
    public static final String TAG = HttpService.class.getSimpleName();
    private Context mContext;

    public HttpService(int port) {
        super(port);
        mContext = IApplication.getContext();
    }

    public HttpService(String hostname, int port) {
        super(hostname, port);
        mContext = IApplication.getContext();
    }

    //重写Serve方法，每次请求时会调用该方法
    @Override
    public Response serve(IHTTPSession session) {
        //转码为UTF-8
        ContentType ct = new ContentType(session.getHeaders().get("content-type")).tryUTF8();
        session.getHeaders().put("content-type", ct.getContentTypeHeader());

        //通过session获取请求的方式和类型
        Method method = session.getMethod();
        String uri = session.getUri();
        Log.d(TAG, "Http request: " + uri);
        // 判断post请求并且是上传文件
        if (Method.POST.equals(method) || Method.PUT.equals(method)) {
            try {
                //将上传数据解析到files集合并且存在NanoHTTPD缓存区
                //after the body parsed, by default nanoHTTPD will save the file
                // to cache and put it into params
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                return responseUpload(session, files);
            } catch (IOException ioe) {
                return getResponse("Internal Error IO Exception: " + ioe.getMessage());
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }
        }else if(uri.equals("/files")){
            return responseFiles();
        }else if(uri.equals("/")){
            return responseRootPage(session);
        }else{
            return response404(session, null);
        }
    }

    public Response responseUpload(IHTTPSession session, Map<String, String> files){
        Map<String, List<String>> params = session.getParameters();
        List<File> fileList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            final String paramsKey = entry.getKey();
            //"file"是上传文件参数的key值
            if (paramsKey.contains("file")) {
                final String tmpFilePath = files.get(paramsKey);
                final List<String> fileNames = entry.getValue();
                //可以直接拿上传的文件名保存，也可以解析一下然后自己命名保存
                String fileName = fileNames.get(0);
                File tmpFile = new File(tmpFilePath);
                //targetFile是你要保存的file，这里是保存在SD卡的私有目录（需要获取文件读写权限）
                File targetFile = new File(mContext.getExternalFilesDir(null).getAbsolutePath(), fileName);
                copyFile(tmpFile, targetFile);
                fileList.add(targetFile);
                Log.d(TAG, "文件上传成功：" + targetFile);
            }
        }

        if(null != uploadCallback) {
            uploadCallback.onUpload(fileList);
        }
        return NanoHTTPD.newFixedLengthResponse("success");
    }

    public Response responseRootPage(IHTTPSession session){
        /*
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("Welcome to YesPlayer");
        builder.append("</body></html>\n");
        String content = builder.toString();
        */
        String content = getIndex();
        return NanoHTTPD.newFixedLengthResponse(content);
    }

    public String getIndex(){
        AssetManager asset = mContext.getAssets();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuffer buffer = new StringBuffer();

        try{
            inputStream = asset.open("web/index.html");
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            buffer.append(bufferedReader.readLine());
            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                buffer.append("\n" + line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    //页面不存在，或者文件不存在时
    public Response response404(IHTTPSession session, String url) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("404 Not Found" + url + " !");
        builder.append("</body></html>\n");
        return NanoHTTPD.newFixedLengthResponse(builder.toString());
    }

    //成功请求
    public Response getResponse(String success) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append(success+ " !");
        builder.append("</body></html>\n");
        return NanoHTTPD.newFixedLengthResponse(builder.toString());
    }

    public Response responseFiles(){
        StringBuilder builder = new StringBuilder();
        File path = mContext.getExternalFilesDir(null);
        builder.append("[");
        int i=0;
        for(File file: path.listFiles()){
            if(i>0)builder.append(",");
            builder.append("\"" + file.getName() + "\"");
            i++;
        }
        builder.append("]");
        return NanoHTTPD.newFixedLengthResponse(builder.toString());
    }

    public void copyFile(File file, File targetfile) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        if (!file.exists()) {
            System.err.println("File not exists!");
            return;
        }
        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(targetfile);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                fos.flush();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    UploadCallback uploadCallback;
    public void setUploadCallback(UploadCallback callback){
        uploadCallback = callback;
    }

    public interface UploadCallback{
        void onUpload(List<File> list);
    }
}
