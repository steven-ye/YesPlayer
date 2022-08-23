package com.example.yesplayer.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yesplayer.object.FileInfo;

import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends ViewModel {

    private final MutableLiveData<List<FileInfo>> mList;

    public HistoryViewModel() {
        mList = new MutableLiveData<>();
        mList.setValue(new ArrayList<>());
    }

    public LiveData<List<FileInfo>> getList() {
        return mList;
    }
    public void setList(List<FileInfo> list){
        mList.postValue(list);
    }
}