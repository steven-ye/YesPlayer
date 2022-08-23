package com.example.yesplayer.ui.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.example.yesplayer.R;

public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    public ListPreference listPreference;
    public SwitchPreferenceCompat historyPreference;
    public SwitchPreferenceCompat hiddenPreference;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        initViews();
    }

    public void initViews(){
        listPreference = findPreference("setting_player");
        historyPreference = findPreference("setting_history");
        hiddenPreference = findPreference("setting_hidden");
        listPreference.setOnPreferenceChangeListener(this);
        listPreference.callChangeListener(listPreference.getValue());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        listPreference.setValue(newValue.toString()); //必须有，不然会是原来的值
        listPreference.setSummary(listPreference.getEntry().toString());
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }
}