
package com.sam.hspm_employee_app.Fragments;

        import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
        import com.sam.hspm_employee_app.ContactUs;
        import com.sam.hspm_employee_app.LoginActivity;
        import com.sam.hspm_employee_app.MenuProfile;
        import com.sam.hspm_employee_app.R;

public class MenuFragment extends Fragment {


    public static MenuFragment newInstance() {
        return new MenuFragment();
    }


    CardView profile,contact,logout;
    View v1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v1 = inflater.inflate(R.layout.fragment_menu, container, false);

        profile =v1.findViewById(R.id.tvProfile);
        contact =v1.findViewById(R.id.tvContact);
        logout =v1.findViewById(R.id.tvLogout);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), MenuProfile.class);
                startActivity(i);
            }
        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), ContactUs.class);
                startActivity(i);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i=new Intent(getActivity(), LoginActivity.class);
                FirebaseAuth.getInstance().signOut();
                startActivity(i);
                getActivity().finish();
            }
        });

        return v1;
    }
}