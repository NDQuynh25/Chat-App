package com.example.chatappfirebase.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.adapters.ChatAdapter;
import com.example.chatappfirebase.databinding.ActivityChatBinding;
import com.example.chatappfirebase.models.ChatMessage;
import com.example.chatappfirebase.models.User;
import com.example.chatappfirebase.network.ApiClient;
import com.example.chatappfirebase.network.ApiService;
import com.example.chatappfirebase.utilities.Constants;
import com.example.chatappfirebase.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private final OnCompleteListener<QuerySnapshot> completeListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getLong(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = getDateObject(documentChange.getDocument().getLong(Constants.KEY_TIMESTAMP));
                    chatMessages.add(chatMessage);
                }
            }
            chatMessages.sort(Comparator.comparing(obj -> obj.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
    };
    private Boolean isReceiverAvailable = false;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageURI;
    private String imageUrl;
    private ImageView image_send;
    private AppCompatImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.background));

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        profile = findViewById(R.id.infoImageView);

        image_send = findViewById(R.id.img_send);
        loadReceiverDetails();
        setListeners();
        init();
        listenMessages();

    }

    private void showToast(String message) {

    }

    private void init() {


        database = FirebaseFirestore.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);

    }

    private void sendImg() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "image_" + timeStamp + ".jpg";

        // Tạo một StorageReference đến vị trí lưu trữ trong Firebase Storage
        StorageReference imageRef = storageReference.child("images/" + imageName);

        // Tải ảnh lên Firebase Storage
        imageRef.putFile(imageURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri.toString();
                                HashMap<String, Object> message = new HashMap<>();
                                message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                                message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                                message.put(Constants.KEY_MESSAGE, imageUrl);
                                message.put(Constants.KEY_TIMESTAMP, System.currentTimeMillis());
                                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).add(message);
                                if (conversionId != null) {
                                    /*NOTE*/
                                    updateConversion("Hình ảnh.");
                                } else {
                                    HashMap<String, Object> conversion = new HashMap<>();
                                    conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                                    conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                                    conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                                    conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                                    conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                                    conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                                    conversion.put(Constants.KEY_LAST_MESSAGE, "Hình ảnh.");
                                    conversion.put(Constants.KEY_TIMESTAMP, System.currentTimeMillis());
                                    addConversion(conversion);
                                }
                                // I don't understand
                                if (!isReceiverAvailable) {
                                    try {
                                        JSONArray tokens = new JSONArray();
                                        tokens.put(receiverUser.token);

                                        JSONObject data = new JSONObject();
                                        data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                                        data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                                        data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                                        data.put(Constants.KEY_MESSAGE, "Hình ảnh.");

                                        JSONObject body = new JSONObject();
                                        body.put(Constants.REMOTE_MSG_DATA, data);
                                        body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                                        sendNotification(body.toString());
                                    } catch (Exception exception) {

                                    }
                                }
                                binding.messageEditText.setText(null);


                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }

    private void sendMessage() {
        if(binding.messageEditText.getText().toString().trim().isEmpty()){
            return;
        }
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.messageEditText.getText().toString().trim());
        message.put(Constants.KEY_TIMESTAMP, System.currentTimeMillis());
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).add(message);
        if (conversionId != null) {
            updateConversion(binding.messageEditText.getText().toString().trim());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.messageEditText.getText().toString().trim());
            conversion.put(Constants.KEY_TIMESTAMP, System.currentTimeMillis());
            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.messageEditText.getText().toString().trim());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {

            }
        }
        binding.messageEditText.setText(null);
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.email = value.getString(Constants.KEY_EMAIL);
                        receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                        if (receiverUser.image == null) {
                            receiverUser.image = value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                            chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                        }
                    }
                    if (isReceiverAvailable) {
                        binding.textAvailability.setText("Online");
                        binding.infoImageView.setColorFilter(Color.parseColor("#FF19AE6D"));
                    } else {
                        binding.textAvailability.setText("Offline");
                        binding.infoImageView.setColorFilter(Color.parseColor("#FFEEA5A5"));
                    }
                });
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        assert receiverUser != null;
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        image_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);

            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileFriend();
            }
        });
        binding.backImageView.setOnClickListener(v -> onBackPressed());
        binding.sendLayout.setOnClickListener(v -> sendMessage());




    }

    private String getReadableDateTime(Long timestamp) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());
            return simpleDateFormat.format(new Date(timestamp));
        } catch (Exception e) {
            return new Date(timestamp).toString();
        }
    }

    private Date getDateObject(Long timestamp) {
        return new Date(timestamp);
    }

    private void checkForConversion() {
        if (chatMessages.size() != 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_CONVERSATION_ID)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_CONVERSATION_ID).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, System.currentTimeMillis()
        );
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_CONVERSATION_ID)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(completeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                imageURI = data.getData();
                image_send.setImageURI(imageURI);
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);

                // Inflate the layout for the dialog
                View dialogView = inflater.inflate(R.layout.image_dialog_layout, null);
                builder.setView(dialogView);

                // Find the ImageView in the layout
                ImageView imageView = dialogView.findViewById(R.id.imageView);
                ImageView cancel = dialogView.findViewById(R.id.buttonCancel);
                ImageView buttonSend = dialogView.findViewById(R.id.buttonSend);

                // Use Glide to load and display the image into the ImageView
                Picasso.get()
                        .load(imageURI.toString())
                        .into(imageView);

                // Create and show the dialog

                AlertDialog dialog = builder.create();
                dialog.show();
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendImg();
                        ImageView imageView = findViewById(R.id.img_send);
                        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/btl-java-04.appspot.com/o/images%2Fsend_image.png?alt=media&token=75b57268-728e-4c15-a616-8ad354ff5fb4").into(imageView);
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        }
    }
    private void profileFriend() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.info_dialog_layout);

        // the layout
        TextView email = dialog.findViewById(R.id.emailProfileFriend);
        RoundedImageView imageProfile = dialog.findViewById(R.id.profileImageFriend);
        TextView name = dialog.findViewById(R.id.nameFriend);



        email.setText(receiverUser.email);
        name.setText(receiverUser.name);
        byte[] bytes = android.util.Base64.decode(receiverUser.image, android.util.Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageProfile.setImageBitmap(bitmap);

        // Create and show the dialog
        dialog.show();
    }
}