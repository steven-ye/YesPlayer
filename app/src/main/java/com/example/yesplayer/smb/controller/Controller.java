package com.example.yesplayer.smb.controller;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.yesplayer.smb.SmbLinkException;
import com.example.yesplayer.smb.info.SmbFileInfo;
import com.example.yesplayer.smb.info.SmbLinkInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by xyoye on 2019/12/20.
 */

public interface Controller {

    /**
     * link to smb server
     * @param smbLinkInfo link data
     * @param exception if link fails, put the error message in the exception
     * @return is the link successful
     */
    boolean linkStart(SmbLinkInfo smbLinkInfo, SmbLinkException exception);

    /**
     * get parent file list info
     */
    List<SmbFileInfo> getParentList();

    /**
     * get self file list info
     */
    List<SmbFileInfo> getSelfList();

    /**
     * get child file list info
     * @param dirName child directory file name
     */
    List<SmbFileInfo> getChildList(String dirName);

    /**
     * get file input stream
     * @param fileName file name
     */
    @Nullable
    InputStream getFileInputStream(String fileName);

    /**
     * get file length
     * @param fileName file name
     */
    long getFileLength(String fileName);

    /**
     * get current path
     */
    String getCurrentPath();

    /**
     * self directory is root directory
     */
    boolean isRootDir();

    void release();
}
