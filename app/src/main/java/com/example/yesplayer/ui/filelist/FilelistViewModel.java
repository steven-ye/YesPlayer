package com.example.yesplayer.ui.filelist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FilelistViewModel extends ViewModel {

    private final MutableLiveData<Boolean> locked;

    public FilelistViewModel() {
        locked = new MutableLiveData<>();
        locked.setValue(false);
    }

    public LiveData<Boolean> getLocked() {
        return locked;
    }

    public void setLocked(boolean val){
        locked.postValue(val);
    }
}