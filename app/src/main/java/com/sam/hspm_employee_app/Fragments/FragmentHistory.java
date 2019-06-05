package com.sam.hspm_employee_app.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam.hspm_employee_app.R;
import com.sam.hspm_employee_app.SlidingTabLayout;
import com.sam.hspm_employee_app.ViewPagerAdapter;

public class FragmentHistory extends Fragment {

    ViewPager view_pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Completed Services","Pending \nServices"};
    int Numboftabs =2;
    View v1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v1 = inflater.inflate(R.layout.fragment_history, container, false);

        adapter =  new ViewPagerAdapter(getChildFragmentManager(),Titles,Numboftabs);

        // Assigning ViewPager View and setting the adapter
        view_pager = v1.findViewById(R.id.pager);
        view_pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = v1.findViewById(R.id.tabLayout);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(view_pager);

        return v1;
    }
}
