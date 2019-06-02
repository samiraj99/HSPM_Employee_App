package com.sam.hspm_employee_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompletedServicesDetails extends AppCompatActivity {

    TextView TV_ProblemType, TV_Address, TV_Employee_Name, TV_PcType, TV_AcceptDate, ViewReceipt;
    ImageView back;
    String serviceId;
    DatabaseReference databaseReference , clientDatabase;
    FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    String Address;
    private String RequestAcceptedBy;
    private ProgressDialog progressDialog;
    private ReceiptListAdapter adapter;
    private ArrayList<ReceiptHelper> IssuedListwithAmount = new ArrayList<>();
    View alertLayout;
    private ListView listView;
    private TextView TV_Total;
    private static final String TAG = "CompletedServicesDetails";
    FirebaseApp clientApp;


    @Override
    public void onDestroy() {
        clientApp.delete();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_services_details);

        if (getIntent() != null) {
            serviceId = getIntent().getStringExtra("ServiceId");
        }

        if (clientApp == null ) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId(getString(R.string.ApplicationId))
                    .setApiKey(getString(R.string.ApiKey))
                    .setDatabaseUrl(getString(R.string.DatabaseUrl))
                    .build();
            FirebaseApp.initializeApp(CompletedServicesDetails.this, options, "ClientDatabase");
            clientApp = FirebaseApp.getInstance("ClientDatabase");
            firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
            clientDatabase = firebaseDatabase.getReference().child("CompletedServices").child(serviceId);
        }


        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        TV_AcceptDate = findViewById(R.id.AcceptDate);
        TV_Address = findViewById(R.id.Address);
        TV_ProblemType = findViewById(R.id.ProblemType);
        TV_Employee_Name = findViewById(R.id.Name);
        TV_PcType = findViewById(R.id.PcType);
        ViewReceipt = findViewById(R.id.viewReceipt);
        back = findViewById(R.id.back);


        try {
            progressDialog = new ProgressDialog(CompletedServicesDetails.this);
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
            clientDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ProblemDetails p = dataSnapshot.child("Problem").getValue(ProblemDetails.class);
                    TV_PcType.setText(p.getPcType());
                    TV_ProblemType.setText(p.getProblemType());
                    TV_AcceptDate.setText(dataSnapshot.child("DateTime").child("Date").getValue(String.class) + ", " + dataSnapshot.child("DateTime").child("Time").getValue(String.class));
                    for (DataSnapshot ds : dataSnapshot.child("Address").getChildren()) {
                        CompletedServicesDetails.Co_Ordinates co_ordinates = ds.getValue(CompletedServicesDetails.Co_Ordinates.class);
                        assert co_ordinates != null;
                        Address = convertAddress(new LatLng(co_ordinates.Lat, co_ordinates.Lng));
                    }
                    TV_Address.setText(Address);

                    RequestAcceptedBy = dataSnapshot.child("RequestAcceptedBy").getValue().toString();
                    retrieveEmployeeData(RequestAcceptedBy);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CompletedServicesDetails.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });

        ViewReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                alertLayout = inflater.inflate(R.layout.fragment_receipt, null);
                listView = alertLayout.findViewById(R.id.listview1);
                TV_Total = alertLayout.findViewById(R.id.tvTotal);

                retrieveReceiptData();

                final AlertDialog.Builder builder = new AlertDialog.Builder(CompletedServicesDetails.this);
                builder.setView(alertLayout);
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IssuedListwithAmount = new ArrayList<>();
                        dialog.dismiss();
                    }
                });
                builder.create();
                builder.show();
            }
        });

    }

    private void retrieveEmployeeData(String requestAcceptedBy) {
        try {
            databaseReference.child("Users").child(requestAcceptedBy).child("Profile").child("ProfileDetails").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    TV_Employee_Name.setText(dataSnapshot.child("FullName").getValue(String.class));
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void retrieveReceiptData() {
        Log.d(TAG, "retrieveReceiptData: Called");
        try {
            clientDatabase.child("Receipt").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: In if");
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ReceiptHelper helper = ds.getValue(ReceiptHelper.class);
                            IssuedListwithAmount.add(helper);
                        }
                        adapter = new ReceiptListAdapter(CompletedServicesDetails.this, R.layout.receipt_adapter_layout, IssuedListwithAmount);
                        listView.setAdapter(adapter);
                        calculateTotal();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Co_Ordinates {
        public double Lat, Lng;

        public Co_Ordinates() {

        }

        public Co_Ordinates(double lat, double lng) {
            Lat = lat;
            Lng = lng;
        }

    }

    private String convertAddress(LatLng latLng) {
        String address = null;
        Geocoder geocoder = new Geocoder(CompletedServicesDetails.this, Locale.getDefault());
        try {
            List<Address> Location = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = Location.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private void calculateTotal() {
        int Total = 0;
        for (int i = 0; i < IssuedListwithAmount.size(); i++) {
            ReceiptHelper helper = IssuedListwithAmount.get(i);
            Total = helper.getAmount() + Total;
        }
        TV_Total.setText(String.valueOf(Total));
        progressDialog.dismiss();
    }
}
