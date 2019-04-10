package com.example.grab;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    public static int option;
    private EditText mEmail, mSdt, mMatkhau1, mMatkhau2;
    private Button mDangky;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    if(option == 1){
                        Intent intent = new Intent(RegisterActivity.this, CustomerMapActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    else if(option == 2){
                        Intent intent = new Intent(RegisterActivity.this, DriverMapActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }
        };
        mEmail = (EditText)findViewById(R.id.email);
        mSdt = (EditText)findViewById(R.id.sdt);
        mMatkhau1 = (EditText)findViewById(R.id.matkhau1);
        mMatkhau2 = (EditText)findViewById(R.id.matkhau2);
        mDangky = (Button)findViewById(R.id.dangky);

        mDangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String sdt = mSdt.getText().toString();
                final String matkhau1 = mMatkhau1.getText().toString();
                final String matkhau2 = mMatkhau2.getText().toString();
                if(matkhau1.equals(matkhau2)){
                    mAuth.createUserWithEmailAndPassword(email, matkhau1).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                            }else{
                                if(option == 1){
                                    String user_id = mAuth.getCurrentUser().getUid();
                                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                                    current_user_db.child("Sdt").setValue(sdt);
                                    current_user_db.child("Name").setValue(email);
                                }else if (option == 2){
                                    String user_id = mAuth.getCurrentUser().getUid();
                                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                                    current_user_db.child("Sdt").setValue(sdt);
                                    current_user_db.child("Name").setValue(email);
                                }
                            }
                        }
                    });
                }else{
                    mMatkhau1.setText("");
                    mMatkhau2.setText("");
                    Toast.makeText(RegisterActivity.this,"Mat khau khong khop, moi nhap lai",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
