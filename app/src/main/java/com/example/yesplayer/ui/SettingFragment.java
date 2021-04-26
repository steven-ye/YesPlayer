package com.example.yesplayer.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.yesplayer.R;

public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    ListPreference listPreference;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        listPreference = findPreference("setting_player");
        assert listPreference != null;
        listPreference.setOnPreferenceChangeListener(this);
        listPreference.callChangeListener(listPreference.getValue());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        listPreference.setValue(newValue.toString()); //必须有，不然会是原来的值
        listPreference.setSummary(listPreference.getEntry().toString());
        return false;
    }
}