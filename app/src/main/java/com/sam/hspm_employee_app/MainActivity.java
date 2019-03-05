package com.sam.hspm_employee_app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseReference, mydatabse;
    FirebaseDatabase firebaseDatabase;
    FirebaseApp clientApp;
    ProgressDialog dialog;
    ArrayList<String> AddressList = new ArrayList<>();
    ArrayList<String> RequestIdList = new ArrayList<>();
    private static final String TAG = "MainActivity";
    ListView listView;
    FirebaseAuth firebaseAuth;
    ArrayAdapter adapter;
    String uid, RequestId;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    boolean IsCureentServiceActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mydatabse = FirebaseDatabase.getInstance().getReference();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(getString(R.string.ApplicationId))
                .setApiKey(getString(R.string.ApiKey))
                .setDatabaseUrl(getString(R.string.DatabaseUrl))
                .build();
        FirebaseApp.initializeApp(this, options, "ClientDatabase");
        clientApp = FirebaseApp.getInstance("ClientDatabase");
        firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
        databaseReference = firebaseDatabase.getReference();

        listView = findViewById(R.id.ListView);
        Button BT_SignOut = findViewById(R.id.Button_SignOut);
        Button Bt_AcceptedRequest = findViewById(R.id.Button_AcceptedRequest);
        firebaseAuth = FirebaseAuth.getInstance();

        uid = firebaseAuth.getCurrentUser().getUid();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, AddressList);
        listView.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();

       if (IsServiceOk()) {
           listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
               @Override
               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   Intent i = new Intent(MainActivity.this, RequestDetails.class);
                   i.putExtra("RequestId", RequestIdList.get(position));
                   startActivity(i);
                   clientApp.delete();
               }
           });

           Bt_AcceptedRequest.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if (IsCureentServiceActive) {
                       Intent i = new Intent(MainActivity.this, AcceptedRequest.class);
                       i.putExtra("RequestId", RequestId);
                       startActivity(i);
                       clientApp.delete();
                   } else {
                       Toast.makeText(MainActivity.this, "You Don't have active service..", Toast.LENGTH_SHORT).show();
                   }
               }
           });
       }

        BT_SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }

    public boolean IsServiceOk(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS){
            Log.d(TAG, "IsServiceOk: Google play service is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "IsServiceOk: An error Occured but we can fix this");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else {
            Toast.makeText(this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    protected void onStart() {

        databaseReference.child("Services").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String status = "true";
                if (dataSnapshot.hasChild("Status")) {
                    status = dataSnapshot.child("Status").getValue().toString();
                }
                if (status.equals("false")) {
                    retrieveData(dataSnapshot);
                }
                Log.d(TAG, "onChildAdded: Address " + AddressList);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String status = "false";
                if (dataSnapshot.hasChild("Status")) {
                    status = dataSnapshot.child("Status").getValue().toString();
                }
                if (status.equals("true")) {
                    try {
                        Co_Ordinates co_ordinates = dataSnapshot.child("Address").child("Co_Ordinates").getValue(Co_Ordinates.class);
                        String Address = convertAddress(new LatLng(co_ordinates.Lat, co_ordinates.Lng));
                        AddressList.remove(Address);
                        String Id = dataSnapshot.getKey();
                        RequestIdList.remove(Id);
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e(TAG, "onChildRemoved: Exception " + e.getMessage());
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                try {
                    Co_Ordinates co_ordinates = dataSnapshot.child("Address").child("Co_Ordinates").getValue(Co_Ordinates.class);
                    String Address = convertAddress(new LatLng(co_ordinates.Lat, co_ordinates.Lng));
                    AddressList.remove(Address);
                    String Id = dataSnapshot.getKey();
                    RequestIdList.remove(Id);
                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Log.e(TAG, "onChildRemoved: Exception " + e.getMessage());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mydatabse.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                IsCureentServiceActive = !Objects.requireNonNull(dataSnapshot.child("AcceptedRequestId").getValue()).toString().equals("0");
                Log.d(TAG, "onDataChange: IsCurrentServiceActive" + IsCureentServiceActive);
                if (IsCureentServiceActive) {
                    RequestId = dataSnapshot.child("AcceptedRequestId").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        super.onStart();
    }

    private void retrieveData(DataSnapshot dataSnapshot) {
        RequestIdList.add(dataSnapshot.getKey());
        for (DataSnapshot ds : dataSnapshot.child("Address").getChildren()) {
            try {
                Co_Ordinates co_ordinates = ds.getValue(Co_Ordinates.class);
                assert co_ordinates != null;
                String Address = convertAddress(new LatLng(co_ordinates.Lat, co_ordinates.Lng));
                AddressList.add(Address);
            } catch (Exception e) {
                Log.d(TAG, "retrieveData: Exception " + e.getMessage());
            }
        }
    }

    private String convertAddress(LatLng latLng) {
        String address = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<android.location.Address> Location = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = Location.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    @Override
    protected void onStop() {
        AddressList.clear();
        adapter.notifyDataSetChanged();
        clientApp.delete();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        AddressList.clear();
        adapter.notifyDataSetChanged();
        clientApp.delete();
        super.onDestroy();
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
}
