package com.godwill.babybuy;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddTask extends Fragment {

    private static final int PICK_IMAGE_REQUST = 1;
    EditText entryName, entryDescription;
    Button postButton;
    ImageView productImage;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private Uri imageUri;
    private StorageTask uploadTask;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendar = Calendar.getInstance();


        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = dateFormat.format(calendar.getTime());


        //Database initialisation
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Active Tasks").child(mAuth.getCurrentUser().getUid());
        storageReference = FirebaseStorage.getInstance().getReference("Images").child(mAuth.getCurrentUser().getUid());

        //initialisation of widgets
        productImage = view.findViewById(R.id.entryImage1);

        entryName = view.findViewById(R.id.name);
        entryDescription = view.findViewById(R.id.description);
//        buttons
        postButton = view.findViewById(R.id.addEntry);
        postButton.setOnClickListener(v -> {
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getActivity().getApplicationContext(), "Still uploading please wait", Toast.LENGTH_SHORT).show();

            } else {
                postToDatabase();
            }
        });

        //can tap on the image
        productImage.setOnClickListener(v -> openFileChooser());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUST);
    }

    private void postToDatabase() {
        if (imageUri != null) {
            final StorageReference store = storageReference.child(System.currentTimeMillis() + "." + getFileExtenstion(imageUri));

            uploadTask = store.putFile(imageUri).addOnCompleteListener(task -> store.getDownloadUrl().addOnSuccessListener(uri -> {
                String entryImage = uri.toString().trim();
                String uniquekey = databaseReference.push().getKey();
                String entryDate = date;
                String entryname = entryName.getText().toString().trim();
                String entrydescription = entryDescription.getText().toString().trim();


                HashMap<String, Object> item = new HashMap<>();
                item.put("taskID", uniquekey);
                item.put("taskDate", entryDate);
                item.put("taskDescription", entrydescription);
                item.put("taskImage", entryImage);
                item.put("taskStatus", false);
                item.put("taskName", entryname);


                databaseReference.child(uniquekey).setValue(item).addOnSuccessListener(aVoid -> {
                    SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A95F3"));
                    pDialog.setTitleText("Loading your Items...");
                    pDialog.setCancelable(true);
                    pDialog.show();
                    Toast.makeText(getContext(), "Posted", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                });
            }));

        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtenstion(Uri imageUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContext().getContentResolver().getType(imageUri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            productImage.setImageURI(imageUri);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Try Again later", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        }
    }
}