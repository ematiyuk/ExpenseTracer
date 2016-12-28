package com.github.ematiyuk.expensetracer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ematiyuk.expensetracer.R;
import com.github.machinarius.preferencefragment.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    public static final String KEY_PREF_CURRENCY = "pref_currency";

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPrefChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
                    updateSinglePreference(findPreference(key), key);
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        return super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(mSharedPrefChangeListener);
        updatePreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mSharedPrefChangeListener);
    }

    private void updatePreferences() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof PreferenceGroup) {
                PreferenceGroup prefGroup = (PreferenceGroup) pref;
                for (int j = 0; j < prefGroup.getPreferenceCount(); ++j) {
                    Preference singlePref = prefGroup.getPreference(j);
                    updateSinglePreference(singlePref, singlePref.getKey());
                }
            } else {
                updateSinglePreference(pref, pref.getKey());
            }
        }
    }

    private void updateSinglePreference(Preference pref, String key) {
        if (pref == null) return;
        SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
        String defaultValue = getResources().getString(R.string.default_string);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            listPref.setSummary(listPref.getEntry() + " ("
                    + sharedPrefs.getString(key, defaultValue) + ")");
            return;
        }
        pref.setSummary(sharedPrefs.getString(key, defaultValue));
    }
}
