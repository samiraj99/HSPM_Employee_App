package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.List;
import java.util.Locale;

public class RequestDetails extends AppCompatActivity {

    private static final String TAG = "RequestDetails";
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    private static String RequestId,UserId,Address,uid;
    private static String PcType, ProblemType, SpecifiedProblem;
    private static double Lat, Lng;
    FirebaseApp clientApp ;
    TextView TV_Address,TV_PcType,TV_ProblemType,TV_SpecifiedProblem;
    ProgressDialog dialog;
    FirebaseAuth firebaseAuth;
    FirebaseUser  firebaseUser;
    Button BT_Accept;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(getString(R.string.ApplicationId))
                .setApiKey(getString(R.string.ApiKey))
                .setDatabaseUrl(getString(R.string.DatabaseUrl))
                .build();
        FirebaseApp.initializeApp(this, options, "ClientDatabase");
        clientApp = FirebaseApp.getInstance("ClientDatabase");
        firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
        databaseReference = firebaseDatabase.getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseAuth.getUid();

        TV_Address = findViewById(R.id.TextView_Address);
        TV_PcType = findViewById(R.id.TextView_PcType);
        TV_ProblemType = findViewById(R.id.TextView_ProblemType);
        TV_SpecifiedProblem = findViewById(R.id.TextView_SpecifiedType);
        BT_Accept = findViewById(R.id.Button_Accept);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading");
        dialog.show();


        try {
            RequestId = getIntent().getExtras().getString("RequestId").trim();
        } catch (Exception e) {
            Log.d(TAG, "onCreate: Exception GetExtras" + e.getMessage());
        }


        databaseReference.child("Services").child(RequestId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Log.d(TAG, "onDataChange: Database Access");
                    UserId = dataSnapshot.child("Uid").getValue().toString();
                    PcType = dataSnapshot.child("Problem").child("PcType").getValue().toString();
                    ProblemType = dataSnapshot.child("Problem").child("ProblemType").getValue().toString();
                    SpecifiedProblem = dataSnapshot.child("Problem").child("SpecifiedProblem").getValue().toString().trim();
                    Lat = ((double) dataSnapshot.child("Address").child("Co_Ordinates").child("Lat").getValue());
                    Lng = ((double) dataSnapshot.child("Address").child("Co_Ordinates").child("Lng").getValue());
                    convertAddress(new LatLng(Lat,Lng));
                    dialog.dismiss();
                    TV_Address.setText(Address);
                    if (SpecifiedProblem.isEmpty()){
                        TV_SpecifiedProblem.setText(getString(R.string.notspecified));
                    }else {
                        TV_SpecifiedProblem.setText(SpecifiedProblem);
                    }
                    TV_ProblemType.setText(ProblemType);
                    TV_PcType.setText(PcType);

                } catch (Exception e) {
                    Log.d(TAG, "onDataChange: FirebaseDatabase " + e.getMessage());
                }
                Log.d(TAG, "onDataChange: Data "+ UserId + PcType + ProblemType + SpecifiedProblem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: DatabaseError "+ databaseError.getMessage());
            }
        });

        BT_Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Services").child(RequestId).child("Status").setValue("true");
                databaseReference.child("Services").child(RequestId).child("RequestAcceptedBy").setValue(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: REQUEST ACCEPTED");
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        clientApp.delete();
        super.onDestroy();
    }

    private void convertAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> Location = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
             Address = Location.get(0).getAddressLine(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
