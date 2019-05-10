/*
 * Copyright (c) 2016. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sam.hspm_employee_app.Fragments.ErrorFragment;
import com.sam.hspm_employee_app.Fragments.ItemThreeFragment;
import com.sam.hspm_employee_app.Fragments.MenuFragment;
import com.sam.hspm_employee_app.Fragments.NewServiceFragment;
import com.sam.hspm_employee_app.Fragments.NotVerified;
import com.sam.hspm_employee_app.Fragments.OnGoingServiceFragment;
import com.sam.hspm_employee_app.SignUpForm.FormDetails;

public class MainActivity extends AppCompatActivity {

    DatabaseReference mydatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String uid, AcceptedRequestId;
    ProgressDialog dialog;
    String IsVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mydatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading...");
        dialog.show();
        dialog.setCancelable(false);

        mydatabase.child("Users").child(uid).child("ProfileIsComplete").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().equals("true")) {
                    loadThisActivity();
                } else {
                    Intent i = new Intent(MainActivity.this, FormDetails.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadThisActivity() {

        mydatabase.child("Users").child(uid).child("IsVerified").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                IsVerified = dataSnapshot.getValue().toString();

                if (IsVerified.equals("true")) {

                    mydatabase.child("Users").child(uid).child("AcceptedRequestId").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                AcceptedRequestId = dataSnapshot.getValue().toString();
                                selectFragment();
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    dialog.dismiss();
                    selectFragment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void selectFragment() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_item1:
                                if (IsVerified.equals("true")) {
                                    if (!AcceptedRequestId.equals("0")) {
                                        selectedFragment = ErrorFragment.newInstance();
                                    } else {
                                        selectedFragment = NewServiceFragment.newInstance();
                                    }
                                } else {
                                    selectedFragment = NotVerified.newInstance();
                                }

                                break;
                            case R.id.action_item2:
                                if (IsVerified.equals("true")) {
                                    if (AcceptedRequestId.equals("0")) {
                                        selectedFragment = ErrorFragment.newInstance();
                                    } else {
                                        selectedFragment = OnGoingServiceFragment.newInstance();
                                    }
                                } else {
                                    selectedFragment = NotVerified.newInstance();
                                }
                                break;
                            case R.id.action_item3:
                                selectedFragment = ItemThreeFragment.newInstance();
                                break;
                            case R.id.action_item4:
                                selectedFragment = MenuFragment.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        if (selectedFragment != null) {
                            transaction.replace(R.id.frame_layout, selectedFragment);
                        }
                        transaction.commit();
                        return true;
                    }
                });


        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (IsVerified.equals("true")) {
            if (AcceptedRequestId.equals("0")) {
                transaction.replace(R.id.frame_layout, NewServiceFragment.newInstance());
                transaction.commit();
            } else {
                transaction.replace(R.id.frame_layout, ErrorFragment.newInstance());
                transaction.commit();
            }
        }else {
            transaction.replace(R.id.frame_layout, NotVerified.newInstance());
            transaction.commit();
        }
        //Used to select an item programmatically
        //  bottomNavigationView.getMenu().getItem(0).setChecked(true);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
