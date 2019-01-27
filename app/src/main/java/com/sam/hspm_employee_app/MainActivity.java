package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Geocoder;
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

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    private static String Address;
    FirebaseApp clientApp ;
    ProgressDialog dialog;
    ArrayList<String> AddressLits = new ArrayList<>();
    ArrayList<String> RequestIdLits = new ArrayList<>();
    private static final String TAG = "MainActivity";
    ListView listView;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(getString(R.string.ApplicationId))
                .setApiKey(getString(R.string.ApiKey))
                .setDatabaseUrl(getString(R.string.DatabaseUrl))
                .build();
        FirebaseApp.initializeApp(this,options,"ClientDatabase");
        clientApp = FirebaseApp.getInstance("ClientDatabase");
        firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
        databaseReference = firebaseDatabase.getReference();

        listView = findViewById(R.id.ListView);
        Button BT_SignOut = findViewById(R.id.Button_SignOut);
        firebaseAuth = FirebaseAuth.getInstance();

        final ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,AddressLits);
        listView.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();

        databaseReference.child("Services").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                retriveData(dataSnapshot);
                Log.d(TAG, "onChildAdded: Address "+ AddressLits);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String status = dataSnapshot.child("Status").getValue().toString();
                if (status.equals("true")){
                    try {
                        Co_Ordinates co_ordinates = dataSnapshot.child("Address").child("Co_Ordinates").getValue(Co_Ordinates.class);
                        String Address = convertAddress(new LatLng(co_ordinates.Lat, co_ordinates.Lng));
                        AddressLits.remove(Address);
                        String Id = dataSnapshot.getKey();
                        RequestIdLits.remove(Id);
                        adapter.notifyDataSetChanged();

                    }catch(Exception e){
                        Log.e(TAG, "onChildRemoved: Exception " + e.getMessage());
                    }
                }
               }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                try {
                        Co_Ordinates co_ordinates = dataSnapshot.child("Address").child("Co_Ordinates").getValue(Co_Ordinates.class);
                        String Address = convertAddress(new LatLng(co_ordinates.Lat, co_ordinates.Lng));
                        AddressLits.remove(Address);
                        String Id = dataSnapshot.getKey();
                        RequestIdLits.remove(Id);
                        adapter.notifyDataSetChanged();

                }catch(Exception e){
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this,RequestDetails.class);
                i.putExtra("RequestId",RequestIdLits.get(position));
                startActivity(i);
                clientApp.delete();
            }
        });

        BT_SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });


    }

    private void retriveData(DataSnapshot dataSnapshot) {
       String status = dataSnapshot.child("Status").getValue().toString();
        if (status.equals("false")){
         RequestIdLits.add(dataSnapshot.getKey());
        for (DataSnapshot ds: dataSnapshot.child("Address").getChildren()) {
            try{
                Co_Ordinates co_ordinates = ds.getValue(Co_Ordinates.class);
                assert co_ordinates != null;
             String Address = convertAddress(new LatLng(co_ordinates.Lat,co_ordinates.Lng));
             AddressLits.add(Address);
            }catch (Exception e){
                Log.d(TAG, "retriveData: Exception "+ e.getMessage());
            }
        }
        }
    }
    private String convertAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<android.location.Address> Location = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address = Location.get(0).getAddressLine(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Address;
    }

    @Override
    protected void onStop() {
        AddressLits.clear();
        clientApp.delete();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        AddressLits.clear();
        clientApp.delete();
        super.onDestroy();
    }

    public static class Co_Ordinates {
       public double Lat,Lng;

        public Co_Ordinates() {
        }

        public Co_Ordinates(double lat, double lng) {
            Lat = lat;
            Lng = lng;
        }

    }
}
