package com.example.chatappfirebase.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private AppCompatButton changePassword;
    private EditText currentPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_change_password);

        changePassword = findViewById(R.id.ChangeButton);
        currentPassword = findViewById(R.id.CurrentPassWordET);
        newPassword = findViewById(R.id.NewPassWordET);
        confirmPassword = findViewById(R.id.ConfirmPassWordET);


        changepass();


    }

    private void changepass() {

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldpass = currentPassword.getText().toString().trim();
                String newpass = newPassword.getText().toString().trim();
                String confirm = confirmPassword.getText().toString().trim();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail().toString(), oldpass);

                if (oldpass.equals(newpass)){
                    Toast.makeText(ChangePassword.this, "The new password is the old password", Toast.LENGTH_SHORT).show();
                }
                else if (!newpass.equals(confirm)) {
                    Toast.makeText(ChangePassword.this, "The confirm is not match to the new password", Toast.LENGTH_SHORT).show();
                }
                else if (newpass.length() < 8) {
                    Toast.makeText(ChangePassword.this, "New password must have more than 8 characters", Toast.LENGTH_SHORT).show();
                }
                else {
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "User password updated.", Toast.LENGTH_SHORT).show();

                                                    FirebaseAuth.getInstance().signOut();
                                                    preferenceManager.clearPreferences();
                                                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Unable change password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ChangePassword.this, "Wrong old password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

            }
        });
    }

    private boolean checkpass(String oldpass, String newpass, String confirm) {
        if (oldpass.equals(newpass) || !newpass.equals(confirm) || newpass.length() < 8){
            return false;
        }
        return true;
    }

}