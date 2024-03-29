package com.example.yesplayer.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private final MutableLiveData<List<Map<String,String>>> mList;
    private final MutableLiveData<File[]> mFileList;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Nothing found");
        mList = new MutableLiveData<>();
        mList.setValue(new ArrayList<>());
        mFileList = new MutableLiveData<>();
        mFileList.setValue(new File[]{});
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<List<Map<String,String>>> getList() {
        return mList;
    }
    public void setList(List<Map<String,String>> list){
        mList.setValue(list);
    }
    public Map<String,String> getOne(int position){
        return Objects.requireNonNull(mList.getValue()).get(position);
    }
    public LiveData<File[]> getFileList() {
        return mFileList;
    }
    public void setFileList(File[] fileList){
        mFileList.setValue(fileList);
    }
}