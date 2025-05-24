package com.example.sa3id.userActivities;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.Fragment;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.example.sa3id.fragments.SettingsFragment;

public class UserSettings extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null) {
            // Only add the fragment if this is the first creation
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.settingsContainer, new SettingsFragment());
            transaction.commit();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_user_settings;
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setTitle(R.string.user_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setTitle(R.string.user_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            super.onBackPressed();
        }
    }

    public void onSettingsChanged() {
        // Find the current SettingsFragment and notify it
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.settingsContainer);
        if (currentFragment instanceof SettingsFragment) {
            ((SettingsFragment) currentFragment).saveSettingsToFirebase();
        }
    }
}