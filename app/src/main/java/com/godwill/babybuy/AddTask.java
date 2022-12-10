package com.godwill.babybuy;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddTask extends Fragment {

    private static final int PICK_IMAGE_REQUST = 1;
    EditText taskName, taskDescription;
    Button addTaskButton;
    ImageView taskImage;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    private Uri imageUri;
    private StorageTask uploadTask;
    private String date;
    SweetAlertDialog pDialog;

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

        Calendar calendar = Calendar.getInstance();


        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = dateFormat.format(calendar.getTime());

        pDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A95F3"));
        pDialog.setCancelable(false);

        //Database initialisation
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Active Tasks").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        storageReference = FirebaseStorage.getInstance().getReference("Images").child(mAuth.getCurrentUser().getUid());

        //initialisation of widgets
        taskImage = view.findViewById(R.id.entryImage1);

        taskName = view.findViewById(R.id.name);
        taskDescription = view.findViewById(R.id.description);
//        buttons
        addTaskButton = view.findViewById(R.id.addEntry);
        addTaskButton.setOnClickListener(v -> {
            pDialog.setTitleText("Adding Task ...");
            pDialog.show();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(requireActivity().getApplicationContext(), "Still uploading please wait", Toast.LENGTH_SHORT).show();

            } else {
                postToDatabase();
            }
        });

        //can tap on the image
        taskImage.setOnClickListener(v -> openFileChooser());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUST);
    }

    private void postToDatabase() {
        if (imageUri != null) {
            final StorageReference store = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = store.putFile(imageUri).addOnCompleteListener(task -> store.getDownloadUrl().addOnSuccessListener(uri -> {
                String entryImage = uri.toString().trim();
                String uniquekey = databaseReference.push().getKey();
                String entryDate = date;
                String entryname = taskName.getText().toString().trim();
                String entrydescription = taskDescription.getText().toString().trim();


                HashMap<String, Object> item = new HashMap<>();
                item.put("taskID", uniquekey);
                item.put("taskDate", entryDate);
                item.put("taskDescription", entrydescription);
                item.put("taskImage", entryImage);
                item.put("taskStatus", false);
                item.put("taskName", entryname);


                databaseReference.child(uniquekey).setValue(item).addOnSuccessListener(aVoid -> {
//                    SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
//                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A95F3"));
//                    pDialog.setTitleText("Loading your Items...");
//                    pDialog.setCancelable(true);
//                    pDialog.show();
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    pDialog.dismiss();
                });
            }));

        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }
    }

    private String getFileExtension(Uri imageUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(requireContext().getContentResolver().getType(imageUri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).resize(3000, 2500).into(taskImage);
//            taskImage.setImageURI(imageUri);
        } else {
            Toast.makeText(requireActivity().getApplicationContext(), "Try Again later", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity().getApplicationContext(), MainActivity.class));
        }
    }
}