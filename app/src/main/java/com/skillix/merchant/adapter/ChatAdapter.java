package com.skillix.merchant.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.skillix.merchant.R;
import com.skillix.merchant.model.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private String currentUserId;

    // Define message types for sent and received
    public static final int MESSAGE_TYPE_SENT = 1;
    public static final int MESSAGE_TYPE_RECEIVED = 2;

    // Constructor for the adapter
    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    // Determine whether the message is sent or received based on the senderId
    @Override
    public int getItemViewType(int position) {

        if (messages.get(position).getSenderId().equals(currentUserId) ) {
            return MESSAGE_TYPE_RECEIVED;  // Received message
        } else {
            return MESSAGE_TYPE_SENT;  // Sent message
        }
    }

    // Create appropriate ViewHolder depending on message type (sent or received)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_message, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_message, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    // Bind data to the view
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        Log.i("SkilliXLog","ChatAdapterCurrentuserId: " + currentUserId);
        if (holder instanceof SentMessageViewHolder) {
            // Bind data to sent message view
            ((SentMessageViewHolder) holder).textMessage.setText(message.getMessage());
        } else if (holder instanceof ReceivedMessageViewHolder) {
            // Bind data to received message view
            ((ReceivedMessageViewHolder) holder).textMessage.setText(message.getMessage());
        }
    }

    // Get the total number of items in the list
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for sent messages
    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public SentMessageViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);  // Sent message text
        }
    }

    // ViewHolder for received messages
    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);  // Received message text
        }
    }
}
