package com.coderming.movieapp;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/***
 * to show Toolbr, see http://stackoverflow.com/questions/30114730/how-to-use-appcompatpreferenceactivity/30281205
 * a bit hairy.
 */
public class SettingsActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);    //Add preferences from XML
        bindPreferenceSummaryToValue(R.string.pref_sortby_key);
    }
    private void bindPreferenceSummaryToValue(int pref_key) {
        Preference pref = findPreference(getString(pref_key));
        pref.setOnPreferenceChangeListener(this);

        onPreferenceChange(pref, PreferenceManager
                .getDefaultSharedPreferences(pref.getContext())
                .getString(pref.getKey(), ""));
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
