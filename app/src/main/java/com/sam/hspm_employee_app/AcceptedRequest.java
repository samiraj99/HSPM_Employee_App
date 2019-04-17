package com.sam.hspm_employee_app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    Button BT_Empoloyee_Location;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean mLocationPermissionGranted = false;
    private GoogleMap map;
    private static final float DEFAULT_ZOOM = 15f;
    private String Address;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_request);

        mydatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        final DateandTime dateandTime;
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
        BT_Empoloyee_Location = findViewById(R.id.Button_Employee_Location);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..!");
        progressDialog.show();

        try {
            RequestId = getIntent().getExtras().getString("RequestId").trim();
        } catch (Exception e) {
            Log.d(TAG, "onCreate: Exception " + e.getMessage());
        }

        RetrieveData(RequestId);

        Date cTime = Calendar.getInstance().getTime();
        String date= cTime.getDate()+"/"+(cTime.getMonth()+1)+"/"+(cTime.getYear()-100);
        String time = cTime.getHours()+":"+cTime.getMinutes();
        dateandTime = new DateandTime(date,time);

        BT_Empoloyee_Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCamera(new LatLng(Lat, Lng), DEFAULT_ZOOM, Address);
            }
        });

        BtCompleteRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientDatabase.child("Users").child(UserId).child("Current_Service_Id").setValue(0);
                clientDatabase.child("Users").child(UserId).child("RequestAcceptedBy").setValue(0);
                clientDatabase.child("Services").child(RequestId).child("RequestAcceptedBy").setValue(uid);
                clientDatabase.child("Services").child(RequestId).child("DateTime").setValue(dateandTime);

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

        //getTing Current time


        //maps
        getLocationPermission();
        checkLocationState();


        //update constant location to database
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: " + location.toString());
                updateLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000, 5f, locationListener);
    }

    private void updateLocation(Location location) {
        Co_Ordinates co_ordinates = new Co_Ordinates(location.getLatitude(),location.getLongitude());
        mydatabase.child("Users").child(uid).child("Location").setValue(co_ordinates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: location updated");
            }
        });
    }

    private void getLocationPermission() {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: Clearing Previous location");
        map.clear();
        Log.d(TAG, "moveCamera: Moving Camera to current location");
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            map.addMarker(options);
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                if (mLocationPermissionGranted) {
                    moveCamera(new LatLng(Lat, Lng), 15f, Address);
                    if (ActivityCompat.checkSelfPermission(AcceptedRequest.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                            (AcceptedRequest.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    map.setMyLocationEnabled(true);
                }
                Toast.makeText(AcceptedRequest.this, "Map is Ready", Toast.LENGTH_SHORT).show();
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
                    convertLocation(new LatLng(Lat,Lng));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void convertLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> St_Location = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Address = St_Location.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }

        }
    }
    private void checkLocationState() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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

    @Override
    protected void onDestroy() {
        clientApp.delete();
        super.onDestroy();
    }

    public static class DateandTime {
        String Date;
        String Time;

        public DateandTime(String date, String time) {
            Date = date;
            Time = time;
        }
    }
}
