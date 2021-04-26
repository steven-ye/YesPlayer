package com.example.yesplayer.ui.phone;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yesplayer.utils.FileInfo;

import java.util.ArrayList;
import java.util.List;

public class PhoneViewModel extends ViewModel {

    private final MutableLiveData<List<FileInfo>> mList;

    public PhoneViewModel() {
        mList = new MutableLiveData<>();
        mList.setValue(new ArrayList<>());
    }

    public LiveData<List<FileInfo>> getList() {
        return mList;
    }

    public void setList(List<FileInfo> list){
        mList.setValue(list);
    }
}