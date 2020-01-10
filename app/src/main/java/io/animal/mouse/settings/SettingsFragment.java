package io.animal.mouse.settings;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.animal.mouse.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
//        super.onPreferenceTreeClick(preference);
        String key = preference.getKey();
        switch (key) {
            default:
                Toast.makeText(getContext(), key, Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
