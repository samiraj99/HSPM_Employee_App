package com.sam.hspm_employee_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MenuProfile extends AppCompatActivity {

//  private static final int PICK_IMAGE_REQUEST = 1;
    EditText ET_Address;
    EditText TIET_Name, TIET_PhoneNO;
    FirebaseUser user;
    DatabaseReference databaseReference;
    String St_Name, St_PhoneNo, uid, St_Email, St_Address, St_ProfileUrl;// St_ProfileImageFileName;
    StorageReference storageReference;
    ProgressDialog dialog;
    FirebaseAuth auth;
    TextView TV_Email, TV_Name;
//  Uri imageUri;
    ImageView IV_Edit, IV_Done, IV_Back, IV_Edit_Profile, IV_Profile;

//  String Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_profile);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("UsersProfileImages");
        auth = FirebaseAuth.getInstance();
        TIET_Name = findViewById(R.id.Profile_first_name);
        TIET_PhoneNO = findViewById(R.id.Profile_PhoneNo);
        ET_Address = findViewById(R.id.Profile_Address);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }
        IV_Profile = findViewById(R.id.ImageView_Profile);
       // IV_Edit_Profile = findViewById(R.id.ImageView_Edit_Profile);
      //  IV_Back = findViewById(R.id.ImageView_Arrow_Back);
        //IV_Edit = findViewById(R.id.ImageView_Edit);
        //IV_Done = findViewById(R.id.ImageView_Done);

        TV_Email = findViewById(R.id.Profile_image_email);
        TV_Name = findViewById(R.id.Profile_Image_first_name);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Setting up Profile..!");
        dialog.show();
        databaseReference.child("Users").child(uid).child("Profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    St_Name = dataSnapshot.child("ProfileDetails").child("FullName").getValue(String.class);
                    St_Email = dataSnapshot.child("ProfileDetails").child("Email").getValue(String.class);
                    St_PhoneNo = dataSnapshot.child("ProfileDetails").child("PhoneNo").getValue(String.class);
                    St_Address = dataSnapshot.child("ProfileDetails").child("PermanentAddress").child("Address").getValue(String.class);
                    try {
                        St_ProfileUrl = dataSnapshot.child("ProfileImage").getValue(String.class);
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    TIET_Name.setText(St_Name);
                    TV_Name.setText(St_Name);
                    TIET_PhoneNO.setText(St_PhoneNo);
                    ET_Address.setText(St_Address);
                    TV_Email.setText(St_Email);
                    if (St_ProfileUrl != null) {
                        Picasso.get().load(St_ProfileUrl).into(IV_Profile);
                    }

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (St_Email.length() > 1) {
                                dialog.dismiss();
                            }
                        }
                    }, 2000);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        IV_Edit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                TIET_Name.setEnabled(true);
//                TIET_PhoneNO.setEnabled(true);
//                ET_Address.setEnabled(true);
//                IV_Done.setVisibility(View.VISIBLE);
//                IV_Edit.setVisibility(View.INVISIBLE);
//
//                TIET_Name.setFocusable(true);
//                TIET_Name.setFocusableInTouchMode(true);
//                TIET_Name.requestFocus();
//
//            }
//        });


//        IV_Back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
//        IV_Edit_Profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(intent, PICK_IMAGE_REQUEST);
//            }
//        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            IV_Profile.setImageURI(imageUri);
//            dialog.setMessage("Uploading Image .....");
//            dialog.show();
//            if (St_ProfileImageFileName != null) {
//                DeleteProfile();
//            } else {
//                uploadFile();
//            }
//        }
//
//    }

//    private String getFileExtension(Uri uri) {
//        ContentResolver CR = getContentResolver();
//        MimeTypeMap mime = MimeTypeMap.getSingleton();
//
//        return mime.getExtensionFromMimeType(CR.getType(uri));
//    }

//    private void uploadFile() {
//
//        if (imageUri != null) {
//            final String filename = String.valueOf(System.currentTimeMillis());
//            final StorageReference reference = storageReference.child(filename + "." + getFileExtension(imageUri));
//            reference.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//                    return reference.getDownloadUrl();
//                }
//
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        Uri downloadUri = task.getResult();
//                        UploadImageData upload = new UploadImageData(filename + "." + getFileExtension(imageUri), downloadUri.toString());
//                        databaseReference.child("Users").child(uid).child("Profile").child("ProfileImage")
//                                .setValue(upload).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//                                    if (St_ProfileUrl != null) {
//                                        dialog.dismiss();
//                                    }
//                                }
//                            }
//                        });
//
//                    }
//                }
//            });
//
//        } else {
//            Toast.makeText(this, "No file selected.", Toast.LENGTH_SHORT).show();
//        }
//    }

//    private void DeleteProfile() {
//        StorageReference dbStorageRef = storageReference.child(St_ProfileImageFileName);
//        dbStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                databaseReference.child("Users").child(uid).child("Profile").child("ProfileImage").removeValue();
//                uploadFile();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(MenuProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }

//    private void requestPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
//
//    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        if (Id != null && Id.equals("Login")) {
//            Intent i = new Intent(MenuProfile.this, MainActivity.class);
//            startActivity(i);
//        }
//        finish();
//    }
}