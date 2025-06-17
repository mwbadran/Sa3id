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
import androidx.fragment.app.Fragment;

import com.example.sa3id.R;
import com.example.sa3id.userActivities.UserSettingsActivity;

public class ExtraTimeSelectionFragment extends Fragment {
    private static final String PREF_EXTRA_TIME = "extra_time";
    private SharedPreferences prefs;
    private ImageView noExtraTimeCheck;
    private ImageView extraTime25Check;
    private ImageView extraTime33Check;
    private ImageView extraTime50Check;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_extra_time_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // init views
        noExtraTimeCheck = view.findViewById(R.id.noExtraTimeCheck);
        extraTime25Check = view.findViewById(R.id.extraTime25Check);
        extraTime33Check = view.findViewById(R.id.extraTime33Check);
        extraTime50Check = view.findViewById(R.id.extraTime50Check);

        view.findViewById(R.id.noExtraTimeOption).setOnClickListener(v -> selectExtraTime("0"));
        view.findViewById(R.id.extraTime25Option).setOnClickListener(v -> selectExtraTime("25"));
        view.findViewById(R.id.extraTime33Option).setOnClickListener(v -> selectExtraTime("33"));
        view.findViewById(R.id.extraTime50Option).setOnClickListener(v -> selectExtraTime("50"));

        updateExtraTimeSelection(prefs.getString(PREF_EXTRA_TIME, "0"));
    }

    private void selectExtraTime(String extraTime) {
        prefs.edit().putString(PREF_EXTRA_TIME, extraTime).apply();

        updateExtraTimeSelection(extraTime);

        // Notify activity
        if (getActivity() instanceof UserSettingsActivity) {
            ((UserSettingsActivity) getActivity()).onSettingsChanged();
        }
    }

    private void updateExtraTimeSelection(String extraTime) {
        noExtraTimeCheck.setVisibility(extraTime.equals("0") ? View.VISIBLE : View.GONE);
        extraTime25Check.setVisibility(extraTime.equals("25") ? View.VISIBLE : View.GONE);
        extraTime33Check.setVisibility(extraTime.equals("33") ? View.VISIBLE : View.GONE);
        extraTime50Check.setVisibility(extraTime.equals("50") ? View.VISIBLE : View.GONE);
    }
} 