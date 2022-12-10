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

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        final EditText email = findViewById(R.id.email_input);
        final EditText password = findViewById(R.id.password_input);
        final Button signInBtn = findViewById(R.id.signInButton);
        final TextView signUpBtn = findViewById(R.id.signUpBtn);
        firebaseAuth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn.this, SignUp.class);
            startActivity(intent);
        });

        signInBtn.setOnClickListener(view -> {
            String txtEmail = email.getText().toString().trim();
            String txtPassword = password.getText().toString();

            if(TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)){
                Toast.makeText(SignIn.this,"Empty Credentials",Toast.LENGTH_LONG).show();
            }else{
                loginUser(txtEmail,txtPassword);
            }
        });
    }

    private void loginUser(String txtEmail, String txtPassword) {
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#9A95F3"));
        pDialog.setTitleText("Logging In");
        pDialog.setCancelable(false);
        pDialog.show();

        firebaseAuth.signInWithEmailAndPassword(txtEmail,txtPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                pDialog.dismissWithAnimation();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(e -> {
            pDialog.dismissWithAnimation();
            Toast.makeText(SignIn.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        });
    }
}