
package com.sam.hspm_employee_app.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam.hspm_employee_app.R;

public class CompleteServiceErrorFragment extends Fragment {

    public static CompleteServiceErrorFragment newInstance() {
        return new CompleteServiceErrorFragment();
    }


    View v1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v1 = inflater.inflate(R.layout.fragment_complete_service_error, container, false);


        return v1;
    }
}
