package com.coderming.movieapp;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bindPreferenceSummaryToValue(R.string.pref_sortby_key);
    }
    private void bindPreferenceSummaryToValue(int pref_key) {
        Preference pref = findPreference(getString(pref_key));
        pref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String str = newValue.toString();
        if (preference instanceof ListPreference) {     // for Radio buttons
            ListPreference listPref = (ListPreference) preference;
            int prefIdx = listPref.findIndexOfValue(str);
            if (prefIdx < 0) {
                preference.setSummary(str);
            } else {
                preference.setSummary(listPref.getEntries()[prefIdx]);
            }
        }
        return true;            // do update
    }
}
