package com.example.yesplayer.ui.filelist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yesplayer.utils.FileInfo;

import java.util.ArrayList;
import java.util.List;

public class FilelistViewModel extends ViewModel {

    private final MutableLiveData<List<FileInfo>> mList;

    public FilelistViewModel() {
        mList = new MutableLiveData<>();
        mList.setValue(new ArrayList<>());
    }

    public LiveData<List<FileInfo>> getList() {
        return mList;
    }

    public void setListValue(List<FileInfo> list){
        mList.postValue(list);
    }
}