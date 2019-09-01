package tomhirsh2.gmail.com.easytaskapp;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.text.TextUtils;


public class SettingsFragment extends PreferenceFragment {
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private static String DISTANCE_SUMMARY;
    private static String DISTANCE_SUMMARY_METERS;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        DISTANCE_SUMMARY = getString(R.string.DistanceSummary);
        DISTANCE_SUMMARY_METERS = getString(R.string.DistanceSummaryMeters);
        bindSummaryValue(findPreference("key_ringtone"));
        bindSummaryValue(findPreference("key_distance"));

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            }
        };
    }

    private static void bindSummaryValue(Preference preference) {
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if(preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                // set the summary to reflect the new value
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }

            else if(preference instanceof EditTextPreference) {
                preference.setSummary(DISTANCE_SUMMARY + " " + stringValue + " " + DISTANCE_SUMMARY_METERS);
            }

            else if(preference instanceof RingtonePreference) {
                if(TextUtils.isEmpty(stringValue)) {
                    // no ringtone
                    preference.setSummary(R.string.RingtoneSummary);
                }
                else {
                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                    if(ringtone == null) {
                        // clear the summary
                        preference.setSummary(R.string.RingtoneSummary);
                    }
                    else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            }
            return true;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}