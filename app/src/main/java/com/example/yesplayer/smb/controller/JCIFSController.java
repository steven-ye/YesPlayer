package com.example.yesplayer.smb.controller;

import android.text.TextUtils;
import android.util.Log;

import com.example.yesplayer.smb.SmbLinkException;
import com.example.yesplayer.smb.info.SmbFileInfo;
import com.example.yesplayer.smb.info.SmbLinkInfo;
import com.example.yesplayer.smb.info.SmbType;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.context.SingletonContext;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by xyoye on 2019/12/20.
 */

public class JCIFSController implements Controller {
    private final String TAG = "JCIFSController";
    private static final String RootFlag = "/";

    private String mPath;
    private String mAuthUrl;
    private List<SmbFileInfo> rootFileList;
    private final SingletonContext tContext;

    {
        tContext = SingletonContext.getInstance();
    }

    @Override
    public boolean linkStart(SmbLinkInfo smbLinkInfo, SmbLinkException exception) {
        //build smb url
        mAuthUrl = "smb://" + (smbLinkInfo.isAnonymous()
                ? smbLinkInfo.getIP() + "/"
                : smbLinkInfo.getAccount() + ":" + smbLinkInfo.getPassword() + "@" + smbLinkInfo.getIP() + "/"
        );

        try {
            SmbFile smbFile = new SmbFile(mAuthUrl, tContext);
            rootFileList = getFileInfoList(smbFile.listFiles());

            mPath = RootFlag;
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e(TAG, e.getMessage());
            exception.addException(SmbType.JCIFS, e.getMessage());
        }
        return false;
    }

    @Override
    public List<SmbFileInfo> getParentList() {
        if (isRootDir())
            return new ArrayList<>();

        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        try {
            //is first directory like smbJ share
            String parentPath = mPath.substring(0, mPath.length() - 1);
            int index = parentPath.indexOf("/", 1);

            //get parent path index
            int endIndex = parentPath.lastIndexOf("/");
            mPath = mPath.substring(0, endIndex) + "/";

            if (index == -1)
                return rootFileList;

            SmbFile smbFile = new SmbFile(mAuthUrl + mPath, tContext);
            if (smbFile.isDirectory() && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()));
            }
        } catch (MalformedURLException | SmbException e) {
            //e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        return fileInfoList;
    }

    @Override
    public List<SmbFileInfo> getSelfList() {
        if (isRootDir())
            return rootFileList;

        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        try {
            SmbFile smbFile = new SmbFile(mAuthUrl + mPath, tContext);
            if (smbFile.isDirectory() && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()));
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }

        return fileInfoList;
    }

    @Override
    public List<SmbFileInfo> getChildList(String dirName) {
        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        try {
            if(!TextUtils.isEmpty(dirName)) mPath += dirName + "/";
            SmbFile smbFile = new SmbFile(mAuthUrl + mPath, tContext);
            if (smbFile.isDirectory() && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()));
            }
        } catch (MalformedURLException | SmbException e) {
            //e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }

        return fileInfoList;
    }

    @Override
    public InputStream getFileInputStream(String fileName) {
        try {
            String path = mPath + fileName + "/";
            SmbFile smbFile = new SmbFile(mAuthUrl + path, tContext);
            if (smbFile.isFile() && smbFile.canRead()) {
                return smbFile.getInputStream();
            }
        } catch (IOException e) {
            //e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }

        return null;
    }

    @Override
    public long getFileLength(String fileName) {
        try {
            String filePath = mPath + fileName + "/";
            SmbFile smbFile = new SmbFile(mAuthUrl + filePath, tContext);
            return smbFile.getContentLengthLong();
        } catch (MalformedURLException e) {
            //e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
        return 0;
    }

    @Override
    public String getCurrentPath() {
        return mPath.length() == 1 ? mPath : mPath.substring(0, mPath.length() - 1);
    }

    @Override
    public boolean isRootDir() {
        return RootFlag.equals(mPath);
    }

    @Override
    public void release() {

    }

    private List<SmbFileInfo> getFileInfoList(SmbFile[] smbFiles) {
        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        for (SmbFile smbFile : smbFiles) {
            try {
                boolean isDirectory = smbFile.isDirectory();
                //remove / at the end of the path
                String smbFileName = smbFile.getName();
                smbFileName = smbFileName.endsWith("/")
                        ? smbFileName.substring(0, smbFileName.length() - 1)
                        : smbFileName;
                //Log.d(TAG, mPath + smbFileName);
                fileInfoList.add(new SmbFileInfo(smbFileName, mPath, isDirectory));
            } catch (SmbException e) {
                //e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        }

        return fileInfoList;
    }
}
