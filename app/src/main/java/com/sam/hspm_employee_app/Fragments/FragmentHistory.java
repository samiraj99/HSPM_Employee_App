package com.sam.hspm_employee_app.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam.hspm_employee_app.CompletedServices;
import com.sam.hspm_employee_app.PendingServices;
import com.sam.hspm_employee_app.R;
import com.sam.hspm_employee_app.Pager;

public class FragmentHistory extends Fragment {

    private ViewPager viewPager;
    View v1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v1 = inflater.inflate(R.layout.fragment_history, container, false);

        TabLayout tabLayout = v1.findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Completed \nServices"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending \nServices"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = v1.findViewById(R.id.pager);

        Pager adapter = new Pager(getChildFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return v1;
    }
}
