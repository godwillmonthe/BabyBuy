package com.godwill.babybuy;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class Home extends Fragment implements TaskListener{

    private List<TaskItem> tasksArray;
    private RecyclerView recyclerView;
    private TaskAdapter myAdapter;
    private DatabaseReference databaseReference, deleteReference, purchasedReference;
    private TextView emptyTextView;
    ItemTouchHelper itemTouchHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        emptyTextView = view.findViewById(R.id._no_data);
        tasksArray = new ArrayList<>();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

//  database reference is used to get all the entries in a collection called Journal Entries
        purchasedReference = FirebaseDatabase.getInstance().getReference("Completed Tasks").child(mAuth.getCurrentUser().getUid());
        deleteReference = FirebaseDatabase.getInstance().getReference("Deleted Tasks").child(mAuth.getCurrentUser().getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference("Active Tasks").child(mAuth.getCurrentUser().getUid());
        //System.out.println(mAuth.getCurrentUser().getUid());
        SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A95F3"));
        pDialog.setTitleText("Loading Entries...");
        pDialog.setCancelable(true);
        pDialog.show();

        databaseReference.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //a data snapshot is taken and captured and it will be contailing all the data from the collection
//                the data is then stored into the entries array
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    TaskItem taskItem = postSnapshot.getValue(TaskItem.class);
                    System.out.println(taskItem.getTaskName());
                    tasksArray.add(taskItem);
                }

                if (!tasksArray.isEmpty()) {
                    //if data is available, don't show the empty text
                    pDialog.dismiss();
                    myAdapter = new TaskAdapter(getContext(),tasksArray,Home.this);
                    recyclerView.setAdapter(myAdapter);

                } else {
                    pDialog.dismiss();
                    emptyTextView.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }));

    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            switch (direction) {
                case ItemTouchHelper.LEFT:
                    //delete
                    deleteTask(position);
                    break;
                case ItemTouchHelper.RIGHT:
                    //mark as purchased
                    purchase(position);
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.parseColor("#FFDD6B55"))
                    .addSwipeLeftActionIcon(R.drawable.delete)
                    .addSwipeRightBackgroundColor(Color.parseColor("#FF9AEB9D"))
                    .addSwipeRightActionIcon(R.drawable.done)
                    .create()
                    .decorate();
        }
    };

    private void purchase(int position) {
        TaskItem taskItem = tasksArray.get(position);

        String taskName = taskItem.getTaskName();
        String taskDescription = taskItem.getTaskDescription();
        String taskDate = taskItem.getTaskDate();
        String taskID = taskItem.getTaskID();
        String taskImage = taskItem.getTaskImage();

        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Are you sure you want mark this item as purchased?")
                .setConfirmText("Purchase")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("entryId", taskID);
                    map.put("Date", taskDate);
                    map.put("Description", taskDescription);
                    map.put("Image", taskImage);
                    map.put("Name", taskName);

                    purchasedReference.child(taskID).setValue(map).addOnSuccessListener(aVoid -> databaseReference.child(taskID).removeValue().addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(getContext(), "Task Completed", Toast.LENGTH_LONG).show();
                        Intent intent1 = new Intent(getContext(), MainActivity.class);
                        startActivity(intent1);
                    }));

                })
                .setCancelButton("Cancel", sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    myAdapter.notifyDataSetChanged();
                })
                .show();


    }

    private void deleteTask(int position) {

        TaskItem taskItem = tasksArray.get(position);

        String taskName = taskItem.getTaskName();
        String taskDescription = taskItem.getTaskDescription();
        String taskDate = taskItem.getTaskDate();
        String taskID = taskItem.getTaskID();
        String taskImage = taskItem.getTaskImage();

        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Are you sure you want to delete this item")
                .setConfirmText("Delete")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("entryId", taskID);
                    map.put("Date", taskDate);
                    map.put("Description", taskDescription);
                    map.put("Image", taskImage);
                    map.put("Name", taskName);

                    deleteReference.child(taskID).setValue(map).addOnSuccessListener(aVoid -> databaseReference.child(taskID).removeValue().addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(getContext(), "Task Completed", Toast.LENGTH_LONG).show();
                        Intent intent1 = new Intent(getContext(), MainActivity.class);
                        startActivity(intent1);
                    }));

                })
                .setCancelButton("Cancel", sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    myAdapter.notifyDataSetChanged();
                })
                .show();

    }

    @Override
    public void onTaskClick(int position) {
        Intent intent = new Intent(getActivity(), TaskDetails.class);
        intent.putExtra("EntryID", tasksArray.get(position).getTaskID());
        intent.putExtra("Name", tasksArray.get(position).getTaskName());
        intent.putExtra("Image", tasksArray.get(position).getTaskImage());
        intent.putExtra("Description",tasksArray.get(position).getTaskDescription());
        intent.putExtra("Date", tasksArray.get(position).getTaskDate());
        startActivity(intent);
    }
}