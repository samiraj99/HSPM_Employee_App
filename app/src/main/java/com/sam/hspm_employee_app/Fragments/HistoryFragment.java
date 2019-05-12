/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
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

package com.sam.hspm_employee_app.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sam.hspm_employee_app.HistoryDetails;
import com.sam.hspm_employee_app.R;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {
    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    View v1;
    DatabaseReference databaseReference,clientDatabase;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String uid, serviceId;
    ListView listView;
    ArrayList<String> ServiceID = new ArrayList<>();
    ArrayList<String> ServiceStatus = new ArrayList<>();
    ArrayList<String> DateTime = new ArrayList<>();
    ArrayList<String> Amount = new ArrayList<>();
    private static final String TAG = "FragmentHistory";
    CustomAdapter customAdapter;
    ProgressDialog progressDialog;
    TextView TextView_NoService;
    FirebaseApp clientApp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v1 = inflater.inflate(R.layout.fragment_history, container, false);


        FirebaseApp.initializeApp(getContext());
        //Initialization
        Log.d(TAG, "onCreateView: Initializing variables");

        if (clientApp == null) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId(getString(R.string.ApplicationId))
                    .setApiKey(getString(R.string.ApiKey))
                    .setDatabaseUrl(getString(R.string.DatabaseUrl))
                    .build();
            FirebaseApp.initializeApp(getContext(), options, "ClientDatabase");
            clientApp = FirebaseApp.getInstance("ClientDatabase");
            firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
            clientDatabase = firebaseDatabase.getReference();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        }

        listView = v1.findViewById(R.id.ListView);
        TextView_NoService = v1.findViewById(R.id.TextView_NoService);

        if (Amount != null) {
            customAdapter = new CustomAdapter();
            listView.setAdapter(customAdapter);
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(), HistoryDetails.class);
                i.putExtra("ServiceId", ServiceID.get(position));
                startActivity(i);
                clientApp.delete();
            }
        });
        return v1;
    }

    @Override
    public void onStart() {
        ServiceID.clear();
        ServiceStatus.clear();
        DateTime.clear();
        Amount.clear();
        databaseReference.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("History")) {
                    databaseReference.child("Users").child(uid).child("History").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (dataSnapshot.exists()) {
                                serviceId = dataSnapshot.getValue(String.class);
                                retrieveData(serviceId);
                            }

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: DatabaseError " + databaseError);
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    TextView_NoService.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        super.onStart();
    }


    private void retrieveData(final String sid) {

        Log.d(TAG, "retrieveData: 1 " + sid);

        clientDatabase.child("CompletedServices").child(sid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ServiceID.add(sid);
                ServiceStatus.add("Service Completed");
                DateTime.add(dataSnapshot.child("DateTime").child("Date").getValue().toString() + ", " + dataSnapshot.child("DateTime").child("Time").getValue().toString());
                Amount.add("₹"+dataSnapshot.child("Total").getValue().toString());
                customAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return ServiceStatus.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = getLayoutInflater().inflate(R.layout.history_custom_listview, null);
            TextView serviceStatus, dateAndTime, amount;

            serviceStatus = view.findViewById(R.id.servicestatus);
            dateAndTime = view.findViewById(R.id.dateandtime);
            amount = view.findViewById(R.id.amount);

            serviceStatus.setText(ServiceStatus.get(position));
            dateAndTime.setText(DateTime.get(position));
            amount.setText(Amount.get(position));

            return view;
        }

    }

    @Override
    public void onDestroy() {
        clientApp.delete();
        super.onDestroy();
    }
}
