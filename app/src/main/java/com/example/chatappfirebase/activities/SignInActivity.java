package com.example.chatappfirebase.activities;



import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.databinding.ActivitySignInBinding;
import com.example.chatappfirebase.utilities.Constants;
import com.example.chatappfirebase.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }


    private void  setListeners(){
        binding.SignUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });
        binding.SignInButton.setOnClickListener(v -> {
            if(isValidSignInDetails()){
                signIn();
            }
        });
        TextView foget = findViewById(R.id.btn_forget);
        foget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });
        //binding.btnForget.setOnClickListener(v->forgetPassword());
    }
    private void forgetPassword() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.forget_password_dialog_layout);

        // the layout
        TextInputLayout email = dialog.findViewById(R.id.fgEmail);
        ImageView cancel = dialog.findViewById(R.id.fgCancel);
        ImageView buttonSend = dialog.findViewById(R.id.fgSend);

        // Create and show the dialog
        dialog.show();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = email.getEditText().getText().toString().trim();

                auth.sendPasswordResetEmail(emailAddress).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    showToast("Gmail sent");
                                    dialog.dismiss();
                                }
                            }
                        });
            }

        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });



    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String email = binding.EmailEditText.getEditText().getText().toString();
        String password = binding.PassWordEditText.getEditText().getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String  userid = firebaseAuth.getCurrentUser().getUid();
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(userid)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful() && task1.getResult() != null ){
                                            DocumentSnapshot documentSnapshot = task1.getResult();
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }else{
                                            loading(false);
                                            showToast("Unable to sign in");
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            loading(false);
                            showToast("Unable to sign in");
                        }
                    }
                });

//        database.collection(Constants.KEY_COLLECTION_USERS)
//                .whereEqualTo(Constants.KEY_EMAIL, binding.EmailEditText.getEditText().getText().toString())
//                .whereEqualTo(Constants.KEY_PASSWORD, binding.PassWordEditText.getEditText().getText().toString())
//                .get()
//                .addOnCompleteListener(task -> {
//                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
//                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
//                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
//                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
//                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
//                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
//                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                    }else{
//                        loading(false);
//                        showToast("Unable to sign in");
//                    }
//                });
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.SignInButton.setVisibility(View.INVISIBLE);
            binding.SignInProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.SignInProgressBar.setVisibility(View.INVISIBLE);
            binding.SignInButton.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails(){


        if(binding.EmailEditText.getEditText().getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.EmailEditText.getEditText().getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }else if(binding.PassWordEditText.getEditText().getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        return true;
    }

}