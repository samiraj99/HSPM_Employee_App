package com.sam.hspm_employee_app.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam.hspm_employee_app.R;

public class NotVerified extends Fragment {


    public static NotVerified newInstance(){
        return new NotVerified();
    }

    View v1;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v1 = inflater.inflate(R.layout.fragment_not_verified, container, false);

        return  v1;
    }
}
