package com.luzharif.akurextended;

/**
 * Created by LuZharif on 09/08/2015.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends PreferenceActivity {

    SharedPreferences dataPref;
    SharedPreferences.Editor dataPrefEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        dataPref = getSharedPreferences("DataPreference", 0);
        dataPrefEdit = dataPref.edit();
        int statusSettings = dataPref.getInt("statusSettingKamera", 1);
        // Sets resolution entries
        ListPreference resolutionsPreference = (ListPreference) findPreference("ukurancitra");

        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();

        Bundle specCamera = getIntent().getExtras();
        entries = specCamera.getStringArrayList("entries");
        entryValues = specCamera.getStringArrayList("entryValues");
        resolutionsPreference.setEntries(entries.toArray(new CharSequence[entries.size()]));
        resolutionsPreference.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
    }
}
