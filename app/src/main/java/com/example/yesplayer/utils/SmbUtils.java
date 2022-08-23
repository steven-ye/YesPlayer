package com.example.yesplayer.utils;

import android.text.TextUtils;

public class SmbUtils {

    public static boolean containsEmptyText(String... strings) {
        for (String string : strings) {
            if (TextUtils.isEmpty(string))
                return true;
        }
        return false;
    }
}
