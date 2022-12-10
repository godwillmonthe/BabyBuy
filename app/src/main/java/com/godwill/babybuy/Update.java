package com.godwill.babybuy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Update extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUST = 1;
    EditText taskName, taskDescription, taskDate;
    Button postButton;
    ImageView taskImage, back;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private Uri imageUri;
    private String taskID;

    private StorageTask uploadTask;
    SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        Intent intentE = getIntent();
        taskID = intentE.getStringExtra("EntryID");
        String name = intentE.getStringExtra("Name");
        String image = intentE.getStringExtra("Image");
        String description = intentE.getStringExtra("Description");
        String date = intentE.getStringExtra("Date");

        //Database initialisation
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Active Tasks").child(mAuth.getCurrentUser().getUid()).child(taskID);
        storageReference = FirebaseStorage.getInstance().getReference("Images").child(mAuth.getCurrentUser().getUid()).child(taskID);

        //initialisation of widgets
        taskImage = findViewById(R.id.updateImageBtn);
        taskName = findViewById(R.id.updateName);
        taskDescription = findViewById(R.id.updateDescription);
        taskDate = findViewById(R.id.update_Date);
        back = findViewById(R.id.updateBackButton);

        pDialog = new SweetAlertDialog(Update.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A95F3"));
        pDialog.setCancelable(false);


        postButton = findViewById(R.id.updateBtn);

        back.setOnClickListener(view -> finish());

        //setting the values of the widgets to the values of the task
        postButton.setOnClickListener(v -> {

            pDialog.setTitleText("Updating Task");
            pDialog.show();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getApplicationContext(), "Upload in progress...", Toast.LENGTH_SHORT).show();

            } else {
                pushUpdateToDatabase();
            }
        });

        //can tap on the image
        taskImage.setOnClickListener(v -> openFileChooser());


        taskName.setText(name);
        taskDate.setText(date);
        taskDescription.setText(description);
        Picasso.get().load(image).into(taskImage);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUST);
    }

    private String getFileExtension(Uri imageUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(imageUri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            taskImage.setImageURI(imageUri);
        } else {
            Toast.makeText(getApplicationContext(), "Try Again later", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    private void pushUpdateToDatabase() {
        if (imageUri != null) {
            final StorageReference store = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = store.putFile(imageUri).addOnCompleteListener(task -> store.getDownloadUrl().addOnSuccessListener(uri -> {
                String newTaskImage = uri.toString().trim();
                Map<String, Object> map = new HashMap<>();
                map.put("taskImage",newTaskImage);
                map.put("taskName", taskName.getText().toString());
                map.put("taskDescription", taskDescription.getText().toString());
                map.put("taskDate", taskDate.getText().toString());
                databaseReference.updateChildren(map).addOnSuccessListener(aVoid -> {
//                    SweetAlertDialog pDialog = new SweetAlertDialog(Update.this, SweetAlertDialog.PROGRESS_TYPE);
//                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A95F3"));
//                    pDialog.setTitleText("Updating Item Details");
//                    pDialog.setCancelable(true);
//                    pDialog.show();
//                    Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    pDialog.dismissWithAnimation();
                    finish();
                })
                        .addOnFailureListener(e -> {
                            pDialog.dismissWithAnimation();
                            Toast.makeText(getApplicationContext(), "Update Unsuccessful!", Toast.LENGTH_LONG).show();
                        });
            }));
        }
        else {
            Map<String, Object> map = new HashMap<>();
            map.put("taskName", taskName.getText().toString());
            map.put("taskDescription", taskDescription.getText().toString());
            map.put("taskDate", taskDate.getText().toString());
            databaseReference.updateChildren(map).addOnSuccessListener(aVoid -> {
//                Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                pDialog.dismissWithAnimation();
                finish();
            })
                    .addOnFailureListener(e -> {
                        pDialog.dismissWithAnimation();
                        Toast.makeText(getApplicationContext(), "Update Unsuccessful!!", Toast.LENGTH_LONG).show();
                    });
        }
    }
}