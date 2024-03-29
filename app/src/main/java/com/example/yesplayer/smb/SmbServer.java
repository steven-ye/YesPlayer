package com.example.yesplayer.smb;

import android.text.TextUtils;

import com.example.yesplayer.smb.http.HttpContentListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by xyoye on 2019/7/18.
 *
 * 接收请求
 */

public class SmbServer extends Thread implements HttpContentListener {
    //smb绑定的文件名
    public static String SMB_FILE_NAME;
    //smb绑定的本地端口
    public static int SMB_PORT = 2222;
    //smb绑定的本地IP
    public static String SMB_IP = "127.0.0.1";

    //用于接收客户端（播放器）请求的Socket
    private ServerSocket serverSocket = null;

    public void stopSmbServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
                //SMB_IP = "";
                //SMB_PORT = 2222;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        super.run();

        //创建ServerSocket
        int retryCount = 0;
        int port = 2222;
        while (!createServerSocket(port)) {
            retryCount++;
            if (retryCount > 100) {
                return;
            }
            port++;
        }

        //在ServerSocket关闭之前一直监听请求
        while (!serverSocket.isClosed()){
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(getTimeOut());
                //接收到请求后，新建线程处理请求
                new SmbServerThread(socket, this).start();
            }catch (Exception e){
                //e.printStackTrace();
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    private synchronized int getTimeOut(){
        return 15 * 1000;
    }

    //创建ServerSocket
    private boolean createServerSocket(int port) {
        if (serverSocket != null) {
            return true;
        }
        try {
            //InetAddress inetAddress = InetAddress.getByName(hostAddress);
            InetAddress inetAddress = InetAddress.getByName(SMB_IP);
            //SMB_IP = hostAddress;
            SMB_PORT = port;
            serverSocket = new ServerSocket(SMB_PORT, 0, inetAddress);
            return true;
        } catch (IOException e) {
            return false;
        }
        //return false;
    }

    @Override
    //获取视频内容
    public InputStream getContentInputStream() {
        return SmbManager.getInstance().getController().getFileInputStream(SMB_FILE_NAME);
    }

    @Override
    //获取视频格式
    public String getContentType() {
        if (TextUtils.isEmpty(SMB_FILE_NAME))
            return "";
        int lastPoi = SMB_FILE_NAME.lastIndexOf('.');
        int lastSep = SMB_FILE_NAME.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return "." +SMB_FILE_NAME.substring(lastPoi + 1);
    }

    @Override
    //获取视频长度
    public long getContentLength() {
        return SmbManager.getInstance().getController().getFileLength(SMB_FILE_NAME);
    }
}
