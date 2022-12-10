package com.godwill.babybuy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Home extends Fragment implements TaskListener{

    private List<TaskItem> tasksArray;
    private RecyclerView recyclerView;
    private TaskAdapter myAdapter;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private TextView emptyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        emptyTextView = view.findViewById(R.id._no_data);
        tasksArray = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();

//  database reference is used to get all the entries in a collection called Journal Entries
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
                Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }));

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