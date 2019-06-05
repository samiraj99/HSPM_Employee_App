package com.sam.hspm_employee_app.SignUpForm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sam.hspm_employee_app.MainActivity;
import com.sam.hspm_employee_app.R;

import java.util.HashMap;

public class BankDetails extends Fragment {

    private EditText holdername, acno, ifsc, paytm, googlepay;
    private Button proceed;
    private TextInputLayout holder, acwrap, ifscwrap, paytmwrap, googlepaywrap;
    private DatabaseReference UserRefs;
    private View v1;
    private FirebaseAuth mAuth;

    ProgressDialog dialog;

    public static BankDetails newInstance() {
        return new BankDetails();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v1 = inflater.inflate(R.layout.fragment_bank, container, false);

        mAuth = FirebaseAuth.getInstance();
        final String currentUser = mAuth.getCurrentUser().getUid();

        UserRefs = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser).child("Profile").child("BankDetails");

        acno = v1.findViewById(R.id.account_no);
        ifsc = v1.findViewById(R.id.ifsc);
        holdername = v1.findViewById(R.id.holder_name);
        paytm = v1.findViewById(R.id.paytm_no);
        googlepay = v1.findViewById(R.id.google_pay);
        acwrap = v1.findViewById(R.id.account_noWrapper);
        ifscwrap = v1.findViewById(R.id.ifscWrapper);
        holder = v1.findViewById(R.id.holderWrapper);
        paytmwrap = v1.findViewById(R.id.paytmWrapper);
        googlepaywrap = v1.findViewById(R.id.googleWrapper);
        proceed = v1.findViewById(R.id.proceed);

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Updating..");


        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ac_no = acno.getText().toString();
                String ifscode = ifsc.getText().toString();
                String acholdername = holdername.getText().toString();
                String payt = paytm.getText().toString();
                String google = googlepay.getText().toString();

                if (TextUtils.isEmpty(ac_no) && (ac_no.length() > 10)) {
                    acwrap.setError("Enter Valid Account Number");
                } else {
                    acwrap.setErrorEnabled(false);
                }

                if (TextUtils.isEmpty(ifscode)) {
                    ifscwrap.setError("Enter Valid IFSC Code");
                } else {
                    ifscwrap.setErrorEnabled(false);
                }

                if (TextUtils.isEmpty(acholdername)) {
                    holder.setError("Enter Account Holders Name");
                } else {
                    holder.setErrorEnabled(false);
                }

                if (TextUtils.isEmpty(payt) && (payt.length() != 10)) {
                    paytmwrap.setError("Enter 10 digit Paytm Number ");
                } else {
                    paytmwrap.setErrorEnabled(false);
                }

                if (TextUtils.isEmpty(google) && (google.length() != 10)) {
                    googlepaywrap.setError("Enter 10 digit Google Pay Number");
                } else {
                    googlepaywrap.setErrorEnabled(false);
                }

                if (google.length() != 0 && payt.length() != 0 && acholdername.length() != 0 && ifscode.length() != 0 && ac_no.length() != 0) {
                    dialog.show();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("AccountNo", ac_no);
                    map.put("IFSCCode", ifscode);
                    map.put("AcHolderName", acholdername);
                    map.put("PaytmNo", payt);
                    map.put("GooglePayNo", google);
                    UserRefs.updateChildren(map);

                    FirebaseDatabase.getInstance().getReference("Users").child(currentUser).child("ProfileIsComplete").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent i = new Intent(getActivity(), MainActivity.class);
                            startActivity(i);
                            getActivity().finish();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        return v1;
    }
}
