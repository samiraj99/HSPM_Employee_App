package com.sam.hspm_employee_app.SignUpForm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sam.hspm_employee_app.R;

import java.io.IOException;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class IdVerification extends Fragment implements AdapterView.OnItemClickListener {

    public static IdVerification newInstance() {
        return new IdVerification();
    }

    AlertDialog dialog;
    Spinner spinner;
    ImageButton select;
    Button choose;
    String Storage_Path;

    // Root Database Name for Firebase Database.
    String Database_Path = "Users";

    Uri FilePathUri;


    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;


    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String uid;
    // Image request code for onActivityResult() .
    int Image_Request_Code = 7;

    int count;

    String TempImageName;

    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v1 = inflater.inflate(R.layout.activity_id_verification, container, false);

        spinner = v1.findViewById(R.id.spinner);
        select = v1.findViewById(R.id.ShowImage);
        choose = v1.findViewById(R.id.btnchoose);
        final Button upload = v1.findViewById(R.id.btnUpload);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        Storage_Path = "Employees /" + uid + "/";

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.ID_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        storageReference = FirebaseStorage.getInstance().getReference();

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);

        progressDialog = new ProgressDialog(getActivity());

        // Adding click listener to Choose image button.
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Creating intent.
                Intent intent = new Intent();

                // Setting intent type as image to select image from phone storage.
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), Image_Request_Code);

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Calling method to upload selected image on Firebase storage.
                UploadImageFileToFirebaseStorage();

            }
        });


        return v1;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            FilePathUri = data.getData();

            try {

                // Getting selected image into Bitmap.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), FilePathUri);

                // Setting up bitmap selected image into ImageView.
                select.setImageBitmap(bitmap);
                choose.setText("Image Selected");


                // After selecting image change choose button above text.

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getActivity().getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadImageFileToFirebaseStorage() {

        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("Image is Uploading...");
            progressDialog.setCancelable(false);

            // Showing progressDialog.
            progressDialog.show();

            TempImageName = spinner.getSelectedItem().toString();
            // Creating second StorageReference.
            final StorageReference storageReference2nd = storageReference.child(Storage_Path + TempImageName + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.

            storageReference2nd.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Hiding the progressDialog after done uploading.


                            TempImageName = spinner.getSelectedItem().toString();
                            // Showing toast message after done uploading.
                            Toast.makeText(getContext(), TempImageName + " Uploaded Successfully ", Toast.LENGTH_LONG).show();

                            storageReference2nd.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    databaseReference.child(uid).child("Profile").child("IDs").child(TempImageName).setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                            databaseReference.child(uid).child("Profile").child("IDs").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    count = (int) dataSnapshot.getChildrenCount();

                                                    if (count > 2) {
                                                        ((FormDetails) Objects.requireNonNull(getActivity())).ChangeActivity();
                                                    }
                                                    progressDialog.dismiss();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    });


                                }
                            });

                            // Getting image upload ID.
                            // Adding image upload id s child element into databaseReference.

                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(getActivity(), "Upload file with size less than 250kb", Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");

                            if (progress == 100) {

                            }

                        }
                    });


        } else {

            Toast.makeText(getActivity(), "Please Select Image", Toast.LENGTH_LONG).show();

        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}