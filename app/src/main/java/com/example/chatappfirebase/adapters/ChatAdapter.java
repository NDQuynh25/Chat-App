package com.example.chatappfirebase.adapters;


import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappfirebase.R;
import com.example.chatappfirebase.databinding.ItemContainerReceivedMessageBinding;
import com.example.chatappfirebase.databinding.ItemContainerSentMessageBinding;
import com.example.chatappfirebase.models.ChatMessage;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;
    private final List<ChatMessage> chatMessages;
    private final String senderId;

    private Bitmap receiverProfileImage;

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    public void setReceiverProfileImage(Bitmap receiverProfileImage) {
        this.receiverProfileImage = receiverProfileImage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {

            return new SentMessageViewHolder(

                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_sent_image, parent, false);
            return new SentImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_received_image, parent, false);
            return new ReceivedImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        } else if (holder.getClass() == SentImageViewHolder.class) {
            SentImageViewHolder viewHolder = (SentImageViewHolder) holder;
            viewHolder.textTime.setText(message.dateTime);
            Picasso.get().load(message.message).into(viewHolder.image);
        } else {
            ReceivedImageViewHolder viewHolder = (ReceivedImageViewHolder) holder;
            viewHolder.textTime.setText(message.dateTime);
            viewHolder.imageProfile.setImageBitmap(receiverProfileImage);
            Picasso.get().load(message.message).into(viewHolder.image);

        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    ///
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            if (chatMessages.get(position).message.startsWith("https://firebasestorage.googleapis.com/"))
                return VIEW_TYPE_SENT_IMAGE;
            else
                return VIEW_TYPE_SENT;
        } else {
            if (chatMessages.get(position).message.startsWith("https://firebasestorage.googleapis.com/"))
                return VIEW_TYPE_RECEIVED_IMAGE;
            else
                return VIEW_TYPE_RECEIVED;
        }
    }


    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage message) {
            binding.textMessage.setText(message.message);
            binding.textTime.setText(message.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage message, Bitmap receiverProfileImage) {
            binding.textMessage.setText(message.message);
            binding.textTime.setText(message.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }

    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView image;
        TextView textTime;

        public SentImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView image;
        RoundedImageView imageProfile;
        TextView textTime;

        public ReceivedImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageMessage);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textTime = itemView.findViewById(R.id.textTime);

        }
    }
}
