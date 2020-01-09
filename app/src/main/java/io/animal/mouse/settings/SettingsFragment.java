package io.animal.mouse.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import io.animal.mouse.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey);
    }

}
