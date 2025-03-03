package com.skillix.merchant.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.skillix.merchant.R;
import com.skillix.merchant.model.Feedback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>{

    ArrayList<Feedback> feedbackArrayList;
    public FeedbackAdapter(ArrayList<Feedback> countryArrayList) {
        this.feedbackArrayList = countryArrayList;
    }


    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedFeedbackItemView = layoutInflater.inflate(R.layout.feedback_item,parent,false);

        FeedbackViewHolder feedbackViewHolder = new FeedbackViewHolder(inflatedFeedbackItemView);

        return feedbackViewHolder;
    }


    public void onBindViewHolder(@NonNull FeedbackAdapter.FeedbackViewHolder holder, int position) {

        Feedback feedback = feedbackArrayList.get(position);

        String fname = feedback.getFname();
        String lneme = feedback.getLname();
        String career = feedback.getCareer();
        String feedbackTxt = feedback.getFeedback();

        holder.feedbackUserProfileImage.setImageResource(R.drawable.profile);
        holder.feedbackUserName.setText(fname +" "+ lneme);
        holder.feedbackUserCareer.setText(career);
        holder.feedbackContext.setText(feedbackTxt);

        Picasso.get()
                .load(feedback.getProfileImgURL())
                .placeholder(R.drawable.profile)  // Optional: Placeholder image
                //.error(R.drawable.error_image)    // Optional: Error image
                .into(holder.feedbackUserProfileImage);

    }


    @Override
    public int getItemCount() {
        return feedbackArrayList.size();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder{

        ShapeableImageView feedbackUserProfileImage;
        TextView feedbackUserName;
        TextView feedbackUserCareer;
        TextView feedbackContext;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            feedbackUserProfileImage = itemView.findViewById(R.id.feedbackUserProfileImage);
            feedbackUserName = itemView.findViewById(R.id.feedbackUserName);
            feedbackUserCareer = itemView.findViewById(R.id.feedbackUserCareer);
            feedbackContext = itemView.findViewById(R.id.enterFeedbackContext);
        }
    }
}
