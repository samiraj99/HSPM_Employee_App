
package com.sam.hspm_employee_app.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.sam.hspm_employee_app.R;
import com.sam.hspm_employee_app.RequestDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class NewServiceFragment extends Fragment {

    public static NewServiceFragment newInstance() {
        return new NewServiceFragment();
    }

    View v1;
    SwipeRefreshLayout pulltorefresh;
    DatabaseReference databaseReference, mydatabse;
    FirebaseDatabase firebaseDatabase;
    FirebaseApp clientApp;
    ProgressDialog dialog;
    ArrayList<String> AddressList = new ArrayList<>();
    ArrayList<String> RequestIdList = new ArrayList<>();
    private static final String TAG = "newServiceFragment";
    ListView listView;
    FirebaseAuth firebaseAuth;
    ArrayAdapter adapter;
    String uid, RequestId;
    List<Address> Location;
    public FusedLocationProviderClient fusedLocationProviderClient;
    Geocoder geocoder;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    boolean IsCureentServiceActive;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v1 = inflater.inflate(R.layout.fragment_new_service, container, false);

        mydatabse = FirebaseDatabase.getInstance().getReference();


        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(getString(R.string.ApplicationId))
                .setApiKey(getString(R.string.ApiKey))
                .setDatabaseUrl(getString(R.string.DatabaseUrl))
                .build();
        FirebaseApp.initializeApp(getContext(), options, "ClientDatabase");
        clientApp = FirebaseApp.getInstance("ClientDatabase");
        firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
        databaseReference = firebaseDatabase.getReference();

        pulltorefresh = v1.findViewById(R.id.pulltorefresh);
        listView = v1.findViewById(R.id.ListView);
        firebaseAuth = FirebaseAuth.getInstance();

        geocoder = new Geocoder(getContext(), Locale.getDefault());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        requestPermission();
        updateLocation();

        uid = firebaseAuth.getCurrentUser().getUid();

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, AddressList);
        listView.setAdapter(adapter);

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();

        if (IsServiceOk()) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getActivity(), RequestDetails.class);
                    i.putExtra("RequestId", RequestIdList.get(position));
                    startActivity(i);
                    clientApp.delete();
                }
            });

        }
        return v1;
    }

    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<android.location.Location>() {
            @Override
            public void onSuccess(android.location.Location location) {
                if (location != null) {
                    Co_Ordinates co_ordinates = new Co_Ordinates(location.getLatitude(), location.getLongitude());
                    mydatabse.child("Users").child(uid).child("Location").setValue(co_ordinates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: Location Updated");
                        }
                    });
                }

            }
        });
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    public boolean IsServiceOk() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "IsServiceOk: Google play service is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "IsServiceOk: An error Occured but we can fix this");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getContext(), "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public void onStart() {

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
        if (!RequestIdList.contains(dataSnapshot.getKey())) {
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
    }

    private String convertAddress(LatLng latLng) {
        String address = null;
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> Location = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = Location.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }


    @Override
    public void onDestroy() {
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
