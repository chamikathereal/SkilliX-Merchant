package com.skillix.merchant.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.skillix.merchant.ChatFragment;
import com.skillix.merchant.R;
import com.skillix.merchant.model.MessageModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessageModelAdapter extends RecyclerView.Adapter<MessageModelAdapter.MessageModelViewHolder> {
    ArrayList<MessageModel> messageList;
    private Context context;
    // Constructor
    public MessageModelAdapter(Context context, ArrayList<MessageModel> messageList) {
        this.messageList = messageList;
        this.context = context;
    }

    @Override
    public MessageModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_card, parent, false);
        return new MessageModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageModelViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        String ide = message.getId();
        // Log the data being bound to the view
        Log.d("RecyclerViewAdapter", "Binding data at position " + position + ": "
                + "Full Name: " + message.getFirstName() + " " + message.getFirstName()
                + ", Career: " + message.getCareer()
                + ", Profile Image URL: " + message.getImgUrl());


        //Log.i("SkilliXLooooog",message.getFirstName());

        // Bind data to the views
        holder.fullNameTextView.setText(message.getFirstName() + " " + message.getLastName());
        holder.careerTextView.setText(message.getCareer());

        // Use Glide to load the profile image
//        Picasso.with(holder.itemView.getContext())
//                .load(message.getImgUrl())
//                .into(holder.profileImageView);

        Picasso.get()
                .load(message.getImgUrl())
                .placeholder(R.drawable.profile)  // Optional: Placeholder image
                //.error(R.drawable.error_image)    // Optional: Error image
                .into(holder.profileImageView);

        holder.viewMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView1, new ChatFragment(ide), null);
                fragmentTransaction.addToBackStack(null);  // Optional: Enables back navigation
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageModelViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView, careerTextView;
        ShapeableImageView profileImageView;
        Button viewMessageButton;

        public MessageModelViewHolder(View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.fullNameTextView);
            careerTextView = itemView.findViewById(R.id.careerTextView);
            profileImageView = itemView.findViewById(R.id.messageProfileImage);
            viewMessageButton = itemView.findViewById(R.id.viewMessageButton);
        }
    }
}

