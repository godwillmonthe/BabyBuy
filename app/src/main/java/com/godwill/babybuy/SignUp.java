package com.godwill.babybuy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SignUp extends AppCompatActivity {

    DatabaseReference mDBRef;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        mDBRef = FirebaseDatabase.getInstance().getReference();

        final EditText fullNames = findViewById(R.id.fullNames);
        final EditText username = findViewById(R.id.username_input_signup);
        final EditText email = findViewById(R.id.email_input_signup);
        final EditText password = findViewById(R.id.password_input_signup);

        final Button signUpBtn = findViewById(R.id.signup_button);
        final TextView signInBtn = findViewById(R.id.signInBtn);

        signInBtn.setOnClickListener(view -> finish());

        signUpBtn.setOnClickListener(view -> {
            String txtUsername = username.getText().toString();
            String txtFullName = fullNames.getText().toString();
            String txtEmail = email.getText().toString().trim();
            String txtPassword = password.getText().toString();

            if(TextUtils.isEmpty(txtUsername) || (TextUtils.isEmpty(txtFullName))
                    || (TextUtils.isEmpty(txtEmail)) || (TextUtils.isEmpty(txtPassword))
            ){
                Toast.makeText(getApplicationContext(),"Please Enter All Details",Toast.LENGTH_LONG).show();
            }else if(txtPassword.length()<6){
                Toast.makeText(getApplicationContext(),"Password Too Short !",Toast.LENGTH_LONG).show();
            }else {
                registerUser(txtUsername,txtFullName, txtEmail,txtPassword);
            }
        });
    }

    private void registerUser(String txtUsername, String txtFullName, String txtEmail, String txtPassword) {

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A95F3"));
        pDialog.setTitleText("Creating Account...");
        pDialog.setCancelable(false);
        pDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(txtEmail,txtPassword).addOnSuccessListener(authResult -> {
            HashMap<String,Object> map = new HashMap<>();
            map.put("names",txtFullName);
            map.put("emailAddress",txtEmail);
            map.put("username",txtUsername);
            map.put("id", Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());


            mDBRef.child("Users").child(firebaseAuth.getCurrentUser().getUid()).setValue(map)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"User Created Successfully",Toast.LENGTH_LONG).show();
                            Intent intent= new Intent(SignUp.this, SignIn.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            pDialog.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    });
        }).addOnFailureListener(e -> {
            pDialog.dismiss();
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        });
    }
}