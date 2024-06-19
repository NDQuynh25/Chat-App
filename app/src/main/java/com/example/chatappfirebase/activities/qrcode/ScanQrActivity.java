package com.example.chatappfirebase.activities.qrcode;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.activities.ChatActivity;
import com.example.chatappfirebase.activities.UsersActivity;
import com.example.chatappfirebase.adapters.UsersAdapter;
import com.example.chatappfirebase.models.User;
import com.example.chatappfirebase.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.example.chatappfirebase.databinding.ActivityUsersBinding;

import com.example.chatappfirebase.utilities.PreferenceManager;


public class ScanQrActivity extends AppCompatActivity {


    private ActivityUsersBinding binding;

    private PreferenceManager preferenceManager;

    private ArrayList<User> friendsArrayList = new ArrayList<>();
    private String ans;
    private ImageView scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.background));
        setContentView(R.layout.activity_scan_qr);
        scan = findViewById(R.id.scanner);
        friendsArrayList.clear();

        getFriends();
        int sz = friendsArrayList.size();
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();



            }
        });




    }
    private void Result() {

    }
    private void getFriends() {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<QuerySnapshot> task) {
                           if(task.isSuccessful() && task.getResult() != null) {



                               for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                   User user = new User();
                                   user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                   user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                   user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                   user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                   user.id = queryDocumentSnapshot.getId();
                                   friendsArrayList.add(user);
                               }

                           }

                       }


                });
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);


    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result ->
    {

        if (result != null && result.getContents() != null) {
            ans = result.getContents().toString();
            for(User x : friendsArrayList) {
                if (x.id.toString().trim().equals(ans.toString().trim())) {


                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra(Constants.KEY_USER, x);
                    startActivity(intent);

                    break;
                }
            }

        }
    });



}