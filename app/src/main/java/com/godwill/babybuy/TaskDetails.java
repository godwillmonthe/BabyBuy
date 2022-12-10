package com.godwill.babybuy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.HashMap;

public class TaskDetails extends AppCompatActivity {

    TextView taskName, taskDesc, taskDate;
    ImageView taskImage, back;
    ImageButton share,edit;
    Button purchase, delete;
    DatabaseReference databaseReference, purchasedReference, deleteReference;
    FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        purchasedReference = FirebaseDatabase.getInstance().getReference("Completed Tasks").child(mAuth.getCurrentUser().getUid());
        deleteReference = FirebaseDatabase.getInstance().getReference("Deleted Tasks").child(mAuth.getCurrentUser().getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference("Active Tasks").child(mAuth.getCurrentUser().getUid());
        storageReference = FirebaseStorage.getInstance().getReference("Images").child(mAuth.getCurrentUser().getUid());

        taskName = findViewById(R.id.taskName);
        taskDesc = findViewById(R.id.taskDescription);
        taskDate = findViewById(R.id.taskDate);
        taskImage = findViewById(R.id.taskImage);
        share = findViewById(R.id.share_Button);
        edit = findViewById(R.id.edit_button);
        purchase = findViewById(R.id.purchase_button);
        delete = findViewById(R.id.delete_button);
        back = findViewById(R.id.taskBackButton);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting Entries ");

        Intent intent = getIntent();
        String entryId = intent.getStringExtra("EntryID");
        String name = intent.getStringExtra("Name");
        String image = intent.getStringExtra("Image");
        String description = intent.getStringExtra("Description");
        String date = intent.getStringExtra("Date");
        boolean isPurchased = intent.getBooleanExtra("isPurchased", false);

        taskName.setText(name);
        taskDesc.setText(description);
        taskDate.setText(date);
        if (isPurchased) {
            purchase.setText("Purchased");
            purchase.setEnabled(false);
        }
        else {
            purchase.setText("Not Purchased");
            purchase.setEnabled(true);
        }
        Picasso.get().load(image).into(taskImage);

        back.setOnClickListener(v -> finish());

        delete.setOnClickListener(v -> {
            // 2. Confirmation message
            new SweetAlertDialog(TaskDetails.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("Are you sure you want to delete this item")
                    .setConfirmText("Delete!")
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("entryId", entryId);
                        map.put("Date", date);
                        map.put("Description", description);
                        map.put("Image", image);
                        map.put("Name", name);

                        deleteReference.child(entryId).setValue(map).addOnSuccessListener(aVoid -> databaseReference.child(entryId).removeValue().addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(getApplicationContext(), "Successfully Deleted", Toast.LENGTH_LONG).show();
                            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent1);
                        }));

                    })
                    .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                    .show();

        });

        purchase.setOnClickListener(v -> {
            // 2. Confirmation message
            new SweetAlertDialog(TaskDetails.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure?")
                    .setContentText("Are you sure you want mark this item as purchased?")
                    .setConfirmText("Purchase!")
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("taskID", entryId);
                        map.put("taskDate", date);
                        map.put("taskDescription", description);
                        map.put("taskImage", image);
                        map.put("taskName", name);

                        purchasedReference.child(entryId).setValue(map).addOnSuccessListener(aVoid -> databaseReference.child(entryId).removeValue().addOnSuccessListener(aVoid12 -> {
                            Toast.makeText(getApplicationContext(), "Successfully Completed", Toast.LENGTH_LONG).show();
                            Intent intent12 = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent12);
                        }));

                    })
                    .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                    .show();

        });

        edit.setOnClickListener(v -> {
            Intent editTaskItem = new Intent(TaskDetails.this, Update.class);
            editTaskItem.putExtra("EntryID", entryId);
            editTaskItem.putExtra("Name", name);
            editTaskItem.putExtra("Image", image);
            editTaskItem.putExtra("Description", description);
            editTaskItem.putExtra("Date", date);
            startActivity(editTaskItem);
        });
        share.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Please Check out my Baby Buy Items");
            sendIntent.putExtra(Intent.EXTRA_TEXT, name);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "   "+ description);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
    }
}