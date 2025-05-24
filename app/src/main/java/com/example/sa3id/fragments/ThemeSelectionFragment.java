package com.example.sa3id.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.sa3id.R;
import com.example.sa3id.userActivities.UserSettings;

public class ThemeSelectionFragment extends Fragment {
    private static final String PREF_THEME = "theme";
    private SharedPreferences prefs;
    private ImageView systemThemeCheck;
    private ImageView lightThemeCheck;
    private ImageView darkThemeCheck;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theme_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Initialize views
        systemThemeCheck = view.findViewById(R.id.systemThemeCheck);
        lightThemeCheck = view.findViewById(R.id.lightThemeCheck);
        darkThemeCheck = view.findViewById(R.id.darkThemeCheck);

        // Set up click listeners
        view.findViewById(R.id.systemThemeOption).setOnClickListener(v -> selectTheme("system"));
        view.findViewById(R.id.lightThemeOption).setOnClickListener(v -> selectTheme("light"));
        view.findViewById(R.id.darkThemeOption).setOnClickListener(v -> selectTheme("dark"));

        // Update UI to show current selection
        updateThemeSelection(prefs.getString(PREF_THEME, "system"));
    }

    private void selectTheme(String theme) {
        // Save preference
        prefs.edit().putString(PREF_THEME, theme).apply();

        // Update UI
        updateThemeSelection(theme);

        // Apply theme
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        // Notify activity
        if (getActivity() instanceof UserSettings) {
            ((UserSettings) getActivity()).onSettingsChanged();
        }

        // Recreate activity to apply theme
        requireActivity().recreate();
    }

    private void updateThemeSelection(String theme) {
        systemThemeCheck.setVisibility(theme.equals("system") ? View.VISIBLE : View.GONE);
        lightThemeCheck.setVisibility(theme.equals("light") ? View.VISIBLE : View.GONE);
        darkThemeCheck.setVisibility(theme.equals("dark") ? View.VISIBLE : View.GONE);
    }
} 