package io.animal.mouse.settings;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import de.psdev.licensesdialog.LicensesDialogFragment;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import io.animal.mouse.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final static String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean temp = super.onPreferenceTreeClick(preference);

        String key = preference.getKey();
        switch (key) {
            case "open_sources":
                showLicenseDialog();
            break;

            default:
                Toast.makeText(getContext(), key, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onPreferenceTreeClick(). Key: " + key);
        }
        return temp;
    }

    private void showLicenseDialog() {
        final Notices notices = new Notices();
        notices.addNotice(new Notice("EventBus", "https://github.com/greenrobot/EventBus", "Example Person", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Android SeekCircle", "https://github.com/Necat0r/SeekCircle", "Apache Version 2.0", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("material-pause-play-animation", "https://github.com/alexjlockwood/adp-path-morph-play-to-pause", "The MIT License (MIT)", new MITLicense()));

        final LicensesDialogFragment fragment = new LicensesDialogFragment.Builder(getContext())
                .setNotices(notices)
                .setShowFullLicenseText(false)
                .setIncludeOwnLicense(true)
                .build();

        fragment.show(getChildFragmentManager(), null);
    }
}
