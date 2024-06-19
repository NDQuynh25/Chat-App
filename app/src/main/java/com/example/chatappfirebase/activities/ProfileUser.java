package com.example.chatappfirebase.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.utilities.Constants;
import com.example.chatappfirebase.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;

public class ProfileUser extends BaseActivity {
    private TextView name, email;
    private RoundedImageView imageView;
    private PreferenceManager preferenceManager;
    private TextView butback, butsignout, butChangePass;
    private AppCompatImageView profile;
    private ImageView qrCodeIm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_user);
        preferenceManager = new PreferenceManager(getApplicationContext());
        name = findViewById(R.id.NameEditText);
        email = findViewById(R.id.EmailProfile);
        imageView = findViewById(R.id.ProfileImage);
        butback = findViewById(R.id.TurnbackBut);
        butsignout = findViewById(R.id.SignOutBut);
        butChangePass = findViewById(R.id.changePasswordView);


        email.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        name.setText(preferenceManager.getString(Constants.KEY_NAME));

        byte[] bytes = android.util.Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), android.util.Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(bitmap);

        back();
        signout();
        changePassword();
        createQR();
    }
    private void createQR () {
        qrCodeIm = findViewById(R.id.idIVQrcode);
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        try {
            Bitmap bitmap = generateQRCode(preferenceManager.getString(Constants.KEY_USER_ID), 900);


            qrCodeIm.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Bitmap generateQRCode(String data, int dimension) throws Exception {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, dimension, dimension);
        Bitmap bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888);

        // Tạo một đối tượng Paint để tô màu cho mã QR
        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // Màu sắc của mã QR
        paint.setStyle(Paint.Style.FILL);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, dimension, dimension, paint); // Tô màu nền

        paint.setColor(Color.BLACK); // Màu sắc của mã QR (viền)
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (bitMatrix.get(i, j)) {
                    canvas.drawRect(i, j, i + 1, j + 1, paint);
                }
            }
        }
        return bitmap;
    }

    private void showToast(String unableToSignOut) {
        Toast.makeText(getApplicationContext(), unableToSignOut, Toast.LENGTH_SHORT).show();
    }

    private void signout() {
        butsignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Signing out...");
                FirebaseAuth.getInstance().signOut();
                preferenceManager.clearPreferences();
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
            }
        });
    }

    private void back() {
        butback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create an intent to switch to second activity upon clicking
                Intent intent = new Intent(ProfileUser.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void changePassword() {
         butChangePass.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(ProfileUser.this, ChangePassword.class);
                 startActivity(intent);
                 finish();
             }
         });
    }
    private void infFriend() {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}