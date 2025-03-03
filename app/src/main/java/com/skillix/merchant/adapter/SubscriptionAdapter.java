package com.skillix.merchant.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skillix.merchant.R;
import com.skillix.merchant.model.Subscription;

import java.util.ArrayList;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    ArrayList<Subscription> subscriptionArrayList;
    private int selectedPosition = -1;

    public SubscriptionAdapter(ArrayList<Subscription> subscriptionArrayList) {
        this.subscriptionArrayList = subscriptionArrayList;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.subscription_item,parent,false);

        SubscriptionViewHolder subscriptionViewHolder = new SubscriptionViewHolder(inflatedView);

        return subscriptionViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {

        Subscription subscription = subscriptionArrayList.get(position);

        holder.subscriptionOptionName.setText(subscription.getName());
        holder.subscriptionDescription.setText(subscription.getDescription());
        holder.subscriptionPrice.setText(String.valueOf(subscription.getPrice()));

        holder.radioButtonSubscription.setChecked(position == selectedPosition);

        holder.radioButtonSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = position;
                //selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return subscriptionArrayList.size();
    }


    static class SubscriptionViewHolder extends RecyclerView.ViewHolder{

        TextView subscriptionOptionName;
        TextView subscriptionDescription;
        TextView subscriptionPrice;
        RadioButton radioButtonSubscription;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            subscriptionOptionName = itemView.findViewById(R.id.nameTextView);
            subscriptionDescription = itemView.findViewById(R.id.descriptionTextView);
            subscriptionPrice = itemView.findViewById(R.id.priceTextView);
            radioButtonSubscription = itemView.findViewById(R.id.subscriptionOptionRadioButton);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
