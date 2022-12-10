package com.godwill.babybuy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Settings extends Fragment {

    private RelativeLayout logout;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private TextView fullNames, userName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fullNames = view.findViewById(R.id.user_fullnames);
        userName = view.findViewById(R.id.user_username);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullnames = snapshot.child("names").getValue().toString();
                String username = snapshot.child("username").getValue().toString();

                fullNames.setText(fullnames);
                userName.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(v -> new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("You will be logged out!")
                .setConfirmText("log out")
                .setConfirmClickListener(sDialog -> {
                    mAuth.signOut();
                    startActivity(new Intent(getActivity(), SignIn.class));
                    getActivity().finish();
                })
                .setCancelText("Cancel")
                .show());
    }

    public void logout(){

        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Logout")
                .setContentText("You are about to log out")
                .setConfirmText("log out")
                .setConfirmClickListener(sDialog -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), SignIn.class);
                    startActivity(intent);
                    getActivity().finish();
                }).setCancelText("Cancel");
    }
}