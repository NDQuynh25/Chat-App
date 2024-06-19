package com.example.chatappfirebase.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.chatappfirebase.databinding.ActivitySignUpBinding;
import com.example.chatappfirebase.utilities.Constants;
import com.example.chatappfirebase.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void  setListeners(){
        binding.SignInTextView.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        });
        binding.SignUpButton.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.ProfileImageFrameLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImageLauncher.launch(intent);
        });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String name = binding.NameEditText.getEditText().getText().toString().trim();
        String email = binding.EmailEditText.getEditText().getText().toString();
        String password = binding.PassWordEditText.getEditText().getText().toString();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.NameEditText.getEditText().getText().toString().trim());
        user.put(Constants.KEY_EMAIL, binding.EmailEditText.getEditText().getText().toString().trim());
        user.put(Constants.KEY_PASSWORD, binding.PassWordEditText.getEditText().getText().toString().trim());
        user.put(Constants.KEY_IMAGE, encodedImage);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(SignUpActivity.this, "Please enter complete information!", Toast.LENGTH_SHORT).show();
            loading(false);
        }
        if (!email.matches(emailPattern)) {

            // Thông báo vào EditText email nếu nhập email không đúng định dạng của emailPattern
            binding.NameEditText.setError("Invalid Email!");
            loading(false);
        }
        else if (password.length() < 8) {
            binding.PassWordEditText.setError("Password must be 8 characters or more!");
            loading(false);
        }
        else if (password.contains(" ")) {
            binding.PassWordEditText.setError("Password cannot contain spaces!");
            loading(false);
        }
        else {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                                /*Câu lệnh `String id = task.getResult().getUser().getUid();
                                được sử dụng trong **Firebase Authentication** để lấy **UID** của người dùng hiện tại **UID** là viết tắt của "User ID".
                                UID là một chuỗi định danh duy nhất được cấp phát cho mỗi người dùng khi họ đăng ký tài khoản trên Firebase
                                Ví dụ, nếu bạn muốn lưu thông tin người dùng vào cơ sở dữ liệu thời gian thực (Realtime Database) của Firebase, bạn có thể sử dụng **UID** để tạo một nút con mới trong cơ sở dữ liệu với tên là **UID** của người dùng
                                */

                        String id = task.getResult().getUser().getUid();
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(id)
                                .set(user)
                                .addOnSuccessListener(documentReference -> {
                                    loading(false);
//                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
//                                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
//                                preferenceManager.putString(Constants.KEY_NAME, binding.NameEditText.getEditText().getText().toString().trim());
//                                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    loading(false);
                                    showToast(e.getMessage());
                                });
                    }
                }
            });

        }
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 480;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.ProfileImage.setImageBitmap(bitmap);
                            binding.AddImageTextView.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails(){
        if(encodedImage == null){
            showToast("Select Profile Image");
            return false;
        }
        else if(binding.NameEditText.getEditText().getText().toString().trim().isEmpty()){
            showToast("Enter Name");
            return false;
        }
        else if(binding.EmailEditText.getEditText().getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.EmailEditText.getEditText().getText().toString().trim()).matches()){
            showToast("Enter Valid Email");
            return false;
        }
        else if(binding.PassWordEditText.getEditText().getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }
        else if(binding.ConfirmPassWordEditText.getEditText().getText().toString().trim().isEmpty()){
            showToast("Enter Confirm Password");
            return false;
        }
        else if(!binding.PassWordEditText.getEditText().getText().toString().trim().equals(binding.ConfirmPassWordEditText.getEditText().getText().toString().trim())){
            showToast("Password and Confirm Password must be same");
            return false;
        }
        return true;
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.SignUpButton.setVisibility(View.INVISIBLE);
            binding.SignUpProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.SignUpProgressBar.setVisibility(View.INVISIBLE);
            binding.SignUpButton.setVisibility(View.VISIBLE);
        }
    }
}