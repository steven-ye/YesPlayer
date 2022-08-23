package com.example.yesplayer.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import jcifs.netbios.NbtAddress;

public class IPUtils {
    public static String getIPAdress(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return  intToIp(ipAddress);
        }else{
            //未开启WIFI
            return getIPAdress();
        }
    }

    private static String intToIp(int ipAddress){
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                ((ipAddress >> 24) & 0xFF);
    }

    public static String getNameByIp(String ip){
        //String firstname = "Unknown";
        String netbiosName = "Unknown";
        try{
            NbtAddress[] nbts = NbtAddress.getAllByAddress(ip);
            netbiosName = nbts[0].getHostName();
            NbtAddress nbtAddress = NbtAddress.getByName(ip);
            //firstname = nbtAddress.firstCalledName();
            //netbiosName = nbtAddress.nextCalledName();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
        return netbiosName;
    }

    /**
     * TODO<获取本机IP前缀>
     * @param devAddress
     *   // 本机IP地址
     * @return String
     */
    public static String getLocAddrIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }

    /**
     * TODO<获取本地ip地址>
     *
     * @return String
     */
    public static String getIPAdress(){
        String ip = null;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        ip = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("IPUtils", "SocketException");
            e.printStackTrace();
        }
        return ip;
    }
}
