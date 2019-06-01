package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class PickUpActivity extends AppCompatActivity {


    String RequestId, UserId, uid , est_Time, est_Cost ;
    private static final String TAG = "PickUpActivity";
    EditText ET_Esti_Time, ET_Esti_Cost;
    DatabaseReference employeeDatabase, clientDatabase;
    FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    FirebaseApp clientApp;
    FirebaseUser firebaseUser;
    ProgressDialog dialog;
    Button BT_PickUp;
    Receipt.DateandTime dateAndTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up);
        try {
            if (getIntent() != null) {
                RequestId = getIntent().getExtras().getString("RequestId");
                UserId = getIntent().getExtras().getString("UserId");
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "onCreate: " + e.getMessage());
        }


        if (clientApp == null) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId(getString(R.string.ApplicationId))
                    .setApiKey(getString(R.string.ApiKey))
                    .setDatabaseUrl(getString(R.string.DatabaseUrl))
                    .build();
            FirebaseApp.initializeApp(PickUpActivity.this, options, "ClientDatabase");
            clientApp = FirebaseApp.getInstance("ClientDatabase");
            firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
            clientDatabase = firebaseDatabase.getReference();
        }


        mAuth = FirebaseAuth.getInstance();
        employeeDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        }

        ET_Esti_Time = findViewById(R.id.EditText_PickUpTime);
        ET_Esti_Cost = findViewById(R.id.EditText_PickUp_EstimateCost);
        BT_PickUp = findViewById(R.id.Button_PickUp);
        dialog = new ProgressDialog(PickUpActivity.this);


        Date cTime = Calendar.getInstance().getTime();
        String date = cTime.getDate() + "/" + (cTime.getMonth() + 1) + "/" + (cTime.getYear() - 100);
        String time = cTime.getHours() + ":" + cTime.getMinutes();
        dateAndTime = new Receipt.DateandTime(date, time);


        BT_PickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setMessage("...");
                dialog.show();

                est_Time = ET_Esti_Time.getText().toString();
                est_Cost = ET_Esti_Cost.getText().toString();

                if (est_Time.isEmpty()){
                    ET_Esti_Time.setError("Field's cant't be empty!");
                    dialog.dismiss();
                    return;

                }if (est_Cost.isEmpty()){
                    ET_Esti_Cost.setError("Field's cant't be empty!");
                    dialog.dismiss();
                    return;
                }

                final HashMap<String, Object> map = new HashMap<>();
                map.put("CurrentService", "0");
                map.put("Current_Service_Id", "0");
                map.put("RequestAcceptedBy", "0");

                final HashMap<String, Object> map1 = new HashMap<>();
                map1.put("EstimateTime", est_Time);
                map1.put("EstimateCost", est_Cost);
                map1.put("Status", null);
                map1.put("DateTime",dateAndTime);
                map1.put("RequestAcceptedBy", uid);

                clientDatabase.child("Services").child(RequestId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        clientDatabase.child("PendingServices").child(RequestId).setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.e(TAG, "onComplete: Copy Failed");
                                } else {
                                    clientDatabase.child("PendingServices").child(RequestId).updateChildren(map1);
                                    clientDatabase.child("Services").child(RequestId).removeValue();
                                    clientDatabase.child("Users").child(UserId).child("History").child("PendingServices").push().setValue(RequestId);
                                    employeeDatabase.child("Users").child(uid).child("History").child("PendingServices").push().setValue(RequestId);
                                    employeeDatabase.child("Users").child(uid).child("AcceptedRequestId").setValue(0);
                                    clientDatabase.child("Users").child(UserId).updateChildren(map);
                                    dialog.dismiss();
                                    Intent i = new Intent(PickUpActivity.this,MainActivity.class);
                                    startActivity(i);
                                    finish();
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

    @Override
    protected void onDestroy() {
        clientApp.delete();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(PickUpActivity.this, MainActivity.class);
        startActivity(i);
        super.onBackPressed();
    }
}
