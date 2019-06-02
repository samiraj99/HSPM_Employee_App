package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PendingServicesDetails extends AppCompatActivity {

    TextView TV_ProblemType, TV_Address, TV_Employee_Name, TV_PcType, TV_AcceptDate;
    ImageView back;
    String serviceId;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseApp clientApp;
    private DatabaseReference clientDatabase;
    String Address;
    private String ClientId;
    private ProgressDialog progressDialog;
    private ReceiptListAdapter adapter;
    private ArrayList<ReceiptHelper> IssuedListwithAmount = new ArrayList<>();
    private ListView listView;
    private static final String TAG = "PendingServicesDetails";
    TextView TV_Emp_PhoneNo;
    FirebaseUser firebaseUser;
    String uid;
    Button BT_GenerateReceipt;

    @Override
    public void onDestroy() {
        clientApp.delete();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_services_details);

        if (getIntent() != null) {
            serviceId = getIntent().getStringExtra("ServiceId");
        }

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = mAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        TV_AcceptDate = findViewById(R.id.AcceptDate);
        TV_Address = findViewById(R.id.Address);
        TV_ProblemType = findViewById(R.id.ProblemType);
        TV_Employee_Name = findViewById(R.id.Name);
        TV_PcType = findViewById(R.id.PcType);
        back = findViewById(R.id.back);
        TV_Emp_PhoneNo = findViewById(R.id.PhoneNo);
        BT_GenerateReceipt = findViewById(R.id.Button_Generate_Receipt);

        //Employee Database
        if (clientApp == null) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId(getString(R.string.ApplicationId))
                    .setApiKey(getString(R.string.ApiKey))
                    .setDatabaseUrl(getString(R.string.DatabaseUrl))
                    .build();
            FirebaseApp.initializeApp(PendingServicesDetails.this, options, "clientDatabase");
            clientApp = FirebaseApp.getInstance("clientDatabase");
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
            clientDatabase = firebaseDatabase.getReference();
        }


        try {
            progressDialog = new ProgressDialog(PendingServicesDetails.this);
            progressDialog.setMessage("Loading");
            progressDialog.setCancelable(false);
            progressDialog.show();

            clientDatabase.child("PendingServices").child(serviceId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        ProblemDetails p = dataSnapshot.child("Problem").getValue(ProblemDetails.class);
                        TV_PcType.setText(p.getPcType());
                        TV_ProblemType.setText(p.getProblemType());
                        TV_AcceptDate.setText(dataSnapshot.child("DateTime").child("Accepted").child("Date").getValue(String.class) + ", " + dataSnapshot.child("DateTime").child("Accepted").child("Time").getValue(String.class));
                        for (DataSnapshot ds : dataSnapshot.child("Address").getChildren()) {
                            Co_Ordinates co_ordinates = ds.getValue(Co_Ordinates.class);
                            assert co_ordinates != null;
                            Address = convertAddress(new LatLng(co_ordinates.Lat, co_ordinates.Lng));
                        }
                        TV_Address.setText(Address);

                        ClientId = dataSnapshot.child("Uid").getValue().toString();
                        retrieveEmployeeData(ClientId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                Intent i = new Intent(PendingServicesDetails.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }
        });



        BT_GenerateReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                if (!ClientId.isEmpty()) {
                    final HashMap<String, Object> map = new HashMap<>();
                    map.put("IsPending", "1");
                    map.put("Current_Service_Id", serviceId);

                    clientDatabase.child("Users").child(ClientId).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                Intent i = new Intent(getApplicationContext(),Receipt.class);
                                i.putExtra("RequestId", serviceId);
                                i.putExtra("UserId", ClientId);
                                i.putExtra("IsPending", true);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                clientApp.delete();
                                finish();
                                progressDialog.dismiss();
                            }else {
                                Log.e(TAG, "onComplete: Error");
                            }

                        }
                    });
                }
            }
        });


    }

    private void retrieveEmployeeData(String clientId) {
        try {
            clientDatabase.child("Users").child(clientId).child("Profile").child("ProfileInfo").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    TV_Employee_Name.setText(dataSnapshot.child("Name").getValue(String.class));
                    TV_Emp_PhoneNo.setText(dataSnapshot.child("PhoneNo").getValue(String.class));
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
        Geocoder geocoder = new Geocoder(PendingServicesDetails.this, Locale.getDefault());
        try {
            List<Address> Location = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = Location.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}
