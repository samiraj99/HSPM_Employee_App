package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
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
    private static String RequestId,UserId,Address;
    private static String PcType, ProblemType, SpecifiedProblem;
    private static double Lat, Lng;
    FirebaseApp clientApp ;
    TextView TV_Address,TV_PcType,TV_ProblemType,TV_SpecifiedProblem;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:638610105243:android:9f969d33e4861427")
                .setApiKey("AIzaSyAkJA2LdvbCtbFzTMux4AgV_93hcAO8AbI")
                .setDatabaseUrl("https://hspm-client-app.firebaseio.com/")
                .build();
        FirebaseApp.initializeApp(this, options, "ClientDatabase");
        clientApp = FirebaseApp.getInstance("ClientDatabase");
        firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
        databaseReference = firebaseDatabase.getReference();

        TV_Address = findViewById(R.id.TextView_Address);
        TV_PcType = findViewById(R.id.TextView_PcType);
        TV_ProblemType = findViewById(R.id.TextView_ProblemType);
        TV_SpecifiedProblem = findViewById(R.id.TextView_SpecifiedType);

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
                        TV_SpecifiedProblem.setText("Not Specified");
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
