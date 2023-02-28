package com.example.yesplayer;

import androidx.fragment.app.Fragment;

abstract public class BaseFragment extends Fragment {
    /*
     * fragment中的返回键
     *
     * 默认返回false，交给Activity处理
     * 返回true：执行fragment中需要执行的逻辑
     * 返回false：执行activity中的 onBackPressed
     * */
    public boolean onBackPressed() {
        return false;
    }
}
