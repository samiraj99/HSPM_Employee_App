package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AcceptedRequest extends AppCompatActivity {

    String RequestId, UserId;
    private static final String TAG = "AcceptedRequest";
    FirebaseApp clientApp;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference clientDatabase, mydatabase;
    String Name, PhoneNo, uid;
    double Lat, Lng;
    TextView TV_Name, TV_PhoneNo;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    Button BtCompleteRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_request);

        mydatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(getString(R.string.ApplicationId))
                .setApiKey(getString(R.string.ApiKey))
                .setDatabaseUrl(getString(R.string.DatabaseUrl))
                .build();
        FirebaseApp.initializeApp(this, options, "ClientDatabase");
        clientApp = FirebaseApp.getInstance("ClientDatabase");
        firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
        clientDatabase = firebaseDatabase.getReference();

        TV_Name = findViewById(R.id.TextView_Name);
        TV_PhoneNo = findViewById(R.id.TextView_PhoneNo);
        BtCompleteRequest = findViewById(R.id.Button_Complete_Request);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..!");
        progressDialog.show();

        try {
            RequestId = getIntent().getExtras().getString("RequestId").trim();
        } catch (Exception e) {
            Log.d(TAG, "onCreate: Exception " + e.getMessage());
        }

        Log.d(TAG, "onCreate: LNG + LAT " + Lat + Lng);

        RetrieveData(RequestId);


        BtCompleteRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientDatabase.child("Users").child(UserId).child("Current_Service_Id").setValue(0);
                clientDatabase.child("Users").child(UserId).child("RequestAcceptedBy").setValue(0);
                clientDatabase.child("Services").child(RequestId).child("RequestAcceptedBy").setValue(uid);


                clientDatabase.child("Services").child(RequestId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        clientDatabase.child("Users").child(UserId).child("History").child(RequestId).setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.e(TAG, "onComplete: Copy Failed");
                                } else {
                                    clientDatabase.child("Users").child(UserId).child("History").
                                            child(RequestId).child("Uid").removeValue();
                                    clientDatabase.child("Users").child(UserId).child("History")
                                            .child(RequestId).child("Status").removeValue();

                                    clientDatabase.child("Services").child(RequestId).removeValue();
                                    //mydatabse is Employees database.
                                    mydatabase.child("Users").child(uid).child("AcceptedRequestId").setValue(0);
                                    Intent i = new Intent(AcceptedRequest.this, MainActivity.class);
                                    startActivity(i);
                                    clientApp.delete();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

        });

    }

    private void displayData() {
        clientDatabase.child("Users").child(UserId).child("Profile").child("ProfileInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Name = dataSnapshot.child("Name").getValue().toString();
                    PhoneNo = dataSnapshot.child("PhoneNo").getValue().toString();
                    TV_Name.setText(Name);
                    TV_PhoneNo.setText(PhoneNo);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void RetrieveData(String RequestId) {
        clientDatabase.child("Services").child(RequestId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserId = dataSnapshot.child("Uid").getValue().toString();
                    Lat = Double.parseDouble(dataSnapshot.child("Address").child("Co_Ordinates").child("Lat").getValue().toString());
                    Lng = Double.parseDouble(dataSnapshot.child("Address").child("Co_Ordinates").child("Lng").getValue().toString());
                    Log.d(TAG, "onDataChange: Lat,Lng" + Lat + Lng);
                    displayData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        clientApp.delete();
        super.onDestroy();
    }
}
