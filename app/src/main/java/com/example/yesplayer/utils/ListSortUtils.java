package com.example.yesplayer.utils;

import com.example.yesplayer.smb.info.SmbFileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by steven on 2021/12/16.
 */

public class ListSortUtils {
    public static List<File> sort(File[] files) {
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(o1.isDirectory() && o2.isFile())
                    return -1;
                if(o2.isDirectory() && o1.isFile())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        return fileList;
    }

    public static ArrayList<SmbFileInfo> sort(ArrayList<SmbFileInfo> fileList) {
        Collections.sort(fileList, new SmbFileComparator());
        return fileList;
    }

    public static class SmbFileComparator implements Comparator<SmbFileInfo> {
        @Override
        public int compare(SmbFileInfo o1, SmbFileInfo o2) {
            String name1 = o1.getFileName();
            String name2 = o2.getFileName();
            CharacterParser parser = CharacterParser.getInstance();
            name1 = isHanzi(name1) ? parser.getSelling(name1) : "."+name1.toUpperCase();
            name2 = isHanzi(name2) ? parser.getSelling(name2) : "."+name2.toUpperCase();
            if(o1.isDirectory()){
                name1 = "#" + name1;
            }
            if(o2.isDirectory()){
                name2 = "#" + name2;
            }

            return name1.compareTo(name2);
        }

        private boolean isHanzi(String name){
            return name.matches("[\\u4E00-\\u9FA5]+");
        }
    }
}
