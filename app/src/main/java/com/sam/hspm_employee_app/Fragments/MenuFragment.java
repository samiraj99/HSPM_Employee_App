package com.sam.hspm_employee_app.Fragments;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam.hspm_employee_app.R;

public class MenuFragment extends Fragment {

    public static MenuFragment newInstance() {
        return new MenuFragment();
    }


    View v1;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v1 = inflater.inflate(R.layout.fragment_menu, container, false);

        return v1;
    }
}
