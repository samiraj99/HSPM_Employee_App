package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    EditText ET_Email, ET_Pass;
    private static String ST_email, ST_pass;
    Button BT_SignIn;
    private static FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "onCreate: Declaring Variable");

        ET_Email = findViewById(R.id.EditText_Email);
        ET_Pass = findViewById(R.id.EditText_Password);
        BT_SignIn = findViewById(R.id.Button_signIn);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);


        Log.d(TAG, "onCreate: Checking Current User");

        if (mAuth.getCurrentUser() != null) {
            Intent MainActivityPage = new Intent(this, MainActivity.class);
            startActivity(MainActivityPage);
            finish();
        }

        BT_SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setMessage("Log in...");
                progressDialog.show();

                ST_email = ET_Email.getText().toString();
                ST_pass = ET_Pass.getText().toString();

                Log.d(TAG, "onClick: Validating data");
                if (TextUtils.isEmpty(ST_email)) {
                    ET_Email.setError("Fields can't be empty");
                } else if (TextUtils.isEmpty(ST_pass)) {
                    ET_Pass.setError("Fields can't be empty");
                }else {
                    mAuth.signInWithEmailAndPassword(ST_email,ST_pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Log.d(TAG, "onComplete: Successfully Login");
                                Toast.makeText(LoginActivity.this, "Successfully Login", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                Intent i = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(i);
                                finish();
                            }else {
                                Log.d(TAG, "onComplete: AuthError " + task.getException().getMessage());
                                Toast.makeText(LoginActivity.this, "Invalid email and password", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }

            }
        });



    }
}
