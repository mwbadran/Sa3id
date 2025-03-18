package com.example.sa3id.UserActivities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.google.android.material.tabs.TabLayout;

public class GradesCalculator extends BaseActivity {
    private TabLayout tabLayout;
    private Fragment totalBagrutFragment;
    private Fragment singleGradeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tabLayout = findViewById(R.id.tabLayout);
        //fragmentContainer = findViewById(R.id.fragmentContainer);

        // Initialize fragments
        totalBagrutFragment = new TotalBagrutFragment();
        singleGradeFragment = new SingleGradeFragment();

        // Add fragments initially but hide singleGradeFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, totalBagrutFragment);
        transaction.add(R.id.fragmentContainer, singleGradeFragment);
        transaction.hide(singleGradeFragment);
        transaction.commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if (tab.getPosition() == 0) {
                    transaction.show(totalBagrutFragment);
                    transaction.hide(singleGradeFragment);
                } else {
                    transaction.show(singleGradeFragment);
                    transaction.hide(totalBagrutFragment);
                }

                transaction.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_grades_calculator;
    }
}
