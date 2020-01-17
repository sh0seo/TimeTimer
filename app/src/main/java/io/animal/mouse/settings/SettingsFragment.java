package io.animal.mouse.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import io.animal.mouse.R;

public class SettingsFragment extends PreferenceFragmentCompat {

//    private final static String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey);
    }

//    @Override
//    public boolean onPreferenceTreeClick(Preference preference) {
//        boolean temp = super.onPreferenceTreeClick(preference);
//
//        String key = preference.getKey();
//        switch (key) {
//            case "lock_screen":
//                EventBus.getDefault().post(new KeepScreenEvent(true));
//            break;
//
//            default:
//                Toast.makeText(getContext(), key, Toast.LENGTH_SHORT).show();
//                Log.e(TAG, "onPreferenceTreeClick(). Key: " + key);
//        }
//        return temp;
//    }
}
