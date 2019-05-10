package com.sam.hspm_employee_app.SignUpForm;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.anton46.stepsview.StepsView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sam.hspm_employee_app.R;

public class FormDetails extends AppCompatActivity {

    StepsView stepsView;
    String[] FormList = {"Photo", "Profile", "    ID", "Other", " Bank"};
    //Button BtNext, BtPrevious;
    int flag = 0;


    DatabaseReference databaseReference;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String uid;
    int count;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_details);

//        BtNext = findViewById(R.id.BottomNavigation);
//        BtPrevious = findViewById(R.id.BT_Previous);

        progressDialog = new ProgressDialog(FormDetails.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        stepsView = findViewById(R.id.stepsView);
        stepsView.setBarColorIndicator(R.color.colorPrimaryDark)
                .setProgressColorIndicator(R.color.lightColor)
                .setLabelColorIndicator(R.color.colorAccent)
                .setLabels(FormList);

        try {
            databaseReference.child(uid).child("Profile").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    count = (int) dataSnapshot.getChildrenCount();
                    count--;
                    flag = count;
                    ChangeActivity();
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Log.e("FormDetails", "onCreate: " + e);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout1, PhotoUpload.newInstance());
            transaction.commit();
            stepsView.setCompletedPosition(0)
                    .drawView();
        }


    }

    public void ChangeActivity() {
        if (flag <= 4) {
            flag++;
            fragmentTransaction();
        }
    }

    private void fragmentTransaction() {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment selectedFragment = null;
        System.out.println(flag);
        if (FormList[flag].equals(FormList[0])) {
            selectedFragment = PhotoUpload.newInstance();
            stepsView.setCompletedPosition(0)
                    .drawView();
        } else if (FormList[flag].equals(FormList[1])) {
            selectedFragment = PersonalDetails.newInstance();
            stepsView.setCompletedPosition(1)
                    .drawView();
        } else if (FormList[flag].equals(FormList[2])) {
            selectedFragment = IdVerification.newInstance();
            stepsView.setCompletedPosition(2)
                    .drawView();
        } else if (FormList[flag].equals(FormList[3])) {
            selectedFragment = OtherDocuments.newInstance();
            stepsView.setCompletedPosition(3)
                    .drawView();
        } else if (FormList[flag].equals(FormList[4])) {
            selectedFragment = BankDetails.newInstance();
            stepsView.setCompletedPosition(4)
                    .drawView();
        }
        if (selectedFragment != null) {
            transaction.replace(R.id.frame_layout1, selectedFragment);
        }
        transaction.commit();
    }

}
