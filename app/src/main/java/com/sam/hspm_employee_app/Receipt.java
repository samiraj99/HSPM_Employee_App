package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Receipt extends AppCompatActivity {

    AlertDialog dialog;
    Button Bt_Add, BT_sendReceipt, BT_completeRequest;
    ImageView imageView_Add;
    EditText Et_solvedIssue, Et_Amount;
    ArrayList<String> SolvedProblem;
    ArrayList<Integer> Amount;
    ListAdapter adapter;
    ListView listView;
    int Total = 0;
    ArrayList<Helper> IssuedListwithAmount = new ArrayList<Helper>();
    TextView textView_Total;
    DatabaseReference clientDatabase, mydatabase;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String uid;
    FirebaseApp clientApp;
    String RequestId, UserId;
    ProgressDialog progressDialog;
    public DateandTime dateAndTime;
    boolean IsPending;
    private static final String TAG = "Receipt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        imageView_Add = findViewById(R.id.ImageView_Add);
        SolvedProblem = new ArrayList<>();
        Amount = new ArrayList<>();
        progressDialog = new ProgressDialog(this);

        listView = findViewById(R.id.listview1);
        textView_Total = findViewById(R.id.tvTotal);
        BT_sendReceipt = findViewById(R.id.Button_SendReceipt);
        BT_completeRequest = findViewById(R.id.Button_Complete_Request);

        mydatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setApplicationId(getString(R.string.ApplicationId))
                .setApiKey(getString(R.string.ApiKey))
                .setDatabaseUrl(getString(R.string.DatabaseUrl))
                .build();
        FirebaseApp.initializeApp(this, firebaseOptions, "ClientDatabase");
        clientApp = FirebaseApp.getInstance("ClientDatabase");
        firebaseDatabase = FirebaseDatabase.getInstance(clientApp);
        clientDatabase = firebaseDatabase.getReference();

        try {
            if (getIntent() != null) {
                RequestId = getIntent().getExtras().getString("RequestId");
                UserId = getIntent().getExtras().getString("UserId");
                IsPending = getIntent().getExtras().getBoolean("IsPending");
            }
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e.getMessage());
        }


        //get current time


        imageView_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Receipt.this);
                View view = getLayoutInflater().inflate(R.layout.alert_dialog, null);
                Et_solvedIssue = view.findViewById(R.id.et_solve_issue);
                Et_Amount = view.findViewById(R.id.etAmount);
                Bt_Add = view.findViewById(R.id.btnAdd);
                builder.setView(view);
                dialog = builder.create();
                dialog.show();
                Bt_Add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ST_SolvedIssue = Et_solvedIssue.getText().toString();
                        if (ST_SolvedIssue.isEmpty()) {
                            Et_solvedIssue.setError("Can't Empty");
                            return;
                        } else {
                            SolvedProblem.add(ST_SolvedIssue);
                        }

                        int St_Amount = 0;

                        if (Et_Amount.getText().toString().isEmpty()) {
                            Et_Amount.setError("Can't Empty");
                            return;
                        } else {
                            St_Amount = Integer.parseInt(Et_Amount.getText().toString());
                            Amount.add(St_Amount);
                        }

                        Total = St_Amount + Total;


                        textView_Total.setText(String.valueOf(Total));

                        Helper helper = new Helper(ST_SolvedIssue, St_Amount);
                        IssuedListwithAmount.add(helper);

                        adapter = new ListAdapter(Receipt.this, R.layout.adapter_view_layout, IssuedListwithAmount);
                        listView.setAdapter(adapter);

                        Toast.makeText(Receipt.this, "Item added Successfully", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    }
                });


            }
        });

        BT_sendReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsPending) {
                    sendReceipt("PendingServices");
                } else {
                    sendReceipt("Services");
                }
            }
        });


        BT_completeRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsPending) {
                    completeRequest("PendingServices");
                } else {
                    completeRequest("Services");
                }
            }
        });


    }

    void completeRequest(final String serviceType) {
        clientDatabase.child(serviceType).child(RequestId).child("RequestAcceptedBy").setValue(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                clientDatabase.child(serviceType).child(RequestId).child("DateTime").child("Completed").setValue(getTime());
                mydatabase.child("Users").child(uid).child("AcceptedRequestId").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        clientDatabase.child("Users").child(UserId).child("Payment").setValue(1);
                        clientDatabase.child("Users").child(UserId).child("CurrentService").setValue(0);
                        Intent i = new Intent(Receipt.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        clientApp.delete();
                    }
                });

            }
        });
    }


    void sendReceipt(final String serviceType) {
        progressDialog.setMessage("Sending Receipt");
        progressDialog.setCancelable(true);
        progressDialog.show();

        for (int i = 0; i < SolvedProblem.size(); i++) {
            Helper helper = new Helper(SolvedProblem.get(i), Amount.get(i));
            clientDatabase.child(serviceType).child(RequestId).child("Receipt").child(String.valueOf(i)).setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        clientDatabase.child(serviceType).child(RequestId).child("Total").setValue(Total);
                        Toast.makeText(Receipt.this, "Receipt Send", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        BT_sendReceipt.setVisibility(View.GONE);
                        BT_completeRequest.setVisibility(View.VISIBLE);
                        imageView_Add.setVisibility(View.INVISIBLE);
                        clientDatabase.child("Users").child(UserId).child("Receipt").setValue("1");
                        if (IsPending){clientDatabase.child("Users").child(UserId).child("CurrentService").setValue("1");}
                    }
                }
            });
        }
    }

    Receipt.DateandTime getTime() {

        Date cTime = Calendar.getInstance().getTime();
        String date = cTime.getDate() + "/" + (cTime.getMonth() + 1) + "/" + (cTime.getYear() - 100);
        String time = cTime.getHours() + ":" + cTime.getMinutes();
        dateAndTime = new Receipt.DateandTime(date, time);

        return dateAndTime;
    }

    @Override
    protected void onDestroy() {
        clientApp.delete();
        super.onDestroy();
    }

    public static class DateandTime {
        public String Date;
        public String Time;

        public DateandTime(String date, String time) {
            Date = date;
            Time = time;
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Receipt.this, MainActivity.class);
        startActivity(i);
        super.onBackPressed();
    }
}
