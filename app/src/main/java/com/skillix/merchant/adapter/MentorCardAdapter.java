package com.skillix.merchant.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillix.merchant.ChatFragment;
import com.skillix.merchant.R;
import com.skillix.merchant.ViewMentorProfileFragment;
import com.skillix.merchant.model.MentorCard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MentorCardAdapter extends RecyclerView.Adapter<MentorCardAdapter.MentorCardViewHolder> {

    ArrayList<MentorCard> mentorCardArrayList;

    private Context context;

    public MentorCardAdapter(Context context, ArrayList<MentorCard> mentorCardArrayList) {
        this.context = context;
        this.mentorCardArrayList = mentorCardArrayList;
    }


    @NonNull
    @Override
    public MentorCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.mentor_profile_card_view,parent,false);

        MentorCardViewHolder mentorCardViewHolder = new MentorCardViewHolder(inflatedView);

        return mentorCardViewHolder;

    }


    @Override
    public void onBindViewHolder(@NonNull MentorCardViewHolder holder, int position) {
        MentorCard mentorCard = mentorCardArrayList.get(position);
        final String mentorId = mentorCard.getMentorId();
        final Uri mentorImg = Uri.parse(mentorCard.getProfileImgURL());
        final String mentorFirstName = mentorCard.getFirstName();
        final String mentorLastName = mentorCard.getLastName();

        final String mentorFullName = mentorFirstName + " " + mentorLastName;

        final String mentorCategory = mentorCard.getCareer();
        final String mentorCountry = mentorCard.getCountry();

        Picasso.get()
                .load(mentorCard.getProfileImgURL())
                .placeholder(R.drawable.profile)  // Optional: Placeholder image
                //.error(R.drawable.error_image)    // Optional: Error image
                .into(holder.mentorPreviewProImageView);

        holder.mentorPreviewNameTextView.setText(mentorFullName);
        holder.mentorPreviewCategoryTextView.setText(mentorCategory);
        holder.mentorPreviewCountryTextView.setText(mentorCountry);

        holder.mentorPreviewSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SkilliXLog", "mentor Preview Send Message Button");
                Log.i("SkilliXLog", "mentorId: " + mentorId);
                Log.i("SkilliXLog", "mentorImg: " + mentorImg);
                Log.i("SkilliXLog", "mentorFullName: " + mentorFullName);
                Log.i("SkilliXLog", "mentorCategory: " + mentorCategory);
                Log.i("SkilliXLog", "mentorCountry: " + mentorCountry);
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView1, new ChatFragment(mentorId), null);
                fragmentTransaction.addToBackStack(null);  // Optional: Enables back navigation
                fragmentTransaction.commit();
            }
        });

        holder.mentorPreviewViewProfileButton.setOnClickListener(new View.OnClickListener() {
            private int clickCount = 0; // Initialize count
            @Override
            public void onClick(View view) {

                clickCount++;

                Log.i("SkilliXLog","Clicked: " + clickCount);

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//
                // Reference to the mentor's profile view count
                DocumentReference docRef = firestore.collection("profile_view_count").document(mentorId);

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // If document exists, get the current view count and increment it
                                Long currentCount = document.getLong("viewCount");
                                if (currentCount == null) currentCount = 0L;
                                long updatedCount = currentCount + 1;

                                // Update the Firestore document with the new count
                                ((DocumentReference) docRef).update("viewCount", updatedCount)
                                        .addOnSuccessListener(aVoid -> Log.i("SkilliXLog", "View count updated: " + updatedCount))
                                        .addOnFailureListener(e -> Log.e("SkilliXLog", "Error updating view count", e));
                            } else {
                                // If document doesn't exist, create a new one
                                HashMap<String, Object> viewCountData = new HashMap<>();
                                viewCountData.put("mentor_id", mentorId);
                                viewCountData.put("viewCount", 1);

                                docRef.set(viewCountData)
                                        .addOnSuccessListener(aVoid -> Log.i("SkilliXLog", "View count initialized: 1"))
                                        .addOnFailureListener(e -> Log.e("SkilliXLog", "Error initializing view count", e));

                            }
                        } else {
                            Log.e("SkilliXLog", "Failed to fetch view count", task.getException());
                        }
                    }
                });

                Log.i("SkilliXLog", "mentor Preview View Profile Button");
                Log.i("SkilliXLog", "mentorId: " + mentorId);
                Log.i("SkilliXLog", "mentorImg: " + mentorImg);
                Log.i("SkilliXLog", "mentorFullName: " + mentorFullName);
                Log.i("SkilliXLog", "mentorCategory: " + mentorCategory);
                Log.i("SkilliXLog", "mentorCountry: " + mentorCountry);

                // Get FragmentManager from the context (Activity)
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();

                // Prepare the fragment and send the data
                Bundle bundle = new Bundle();
                bundle.putString("mentorId", mentorId);
                bundle.putString("mentorFullName", mentorFullName);
                bundle.putString("mentorCategory", mentorCategory);
                bundle.putString("mentorCountry", mentorCountry);
                bundle.putString("mentorImg", mentorImg.toString());  // Convert Uri to String

                ViewMentorProfileFragment fragment = new ViewMentorProfileFragment();
                fragment.setArguments(bundle);

                // Replace fragment
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView1, fragment, null);
                fragmentTransaction.addToBackStack(null); // Optional: Enables back navigation
                fragmentTransaction.commit();



            }
        });
    }


    @Override
    public int getItemCount() {
        return mentorCardArrayList.size();
    }

    static class MentorCardViewHolder extends RecyclerView.ViewHolder{

        ImageView mentorPreviewProImageView;
        TextView mentorPreviewNameTextView, mentorPreviewCategoryTextView, mentorPreviewCountryTextView;
        Button mentorPreviewSendMessageButton, mentorPreviewViewProfileButton;


        public MentorCardViewHolder(@NonNull View itemView) {
            super(itemView);

            mentorPreviewProImageView = itemView.findViewById(R.id.hiredMenteeProImageView);
            mentorPreviewNameTextView = itemView.findViewById(R.id.hiredMenteeNameTextView);
            mentorPreviewCategoryTextView = itemView.findViewById(R.id.hiredMenteeCategoryTextView);
            mentorPreviewCountryTextView = itemView.findViewById(R.id.mentorPreviewCountryTextView);
            mentorPreviewSendMessageButton = itemView.findViewById(R.id.mentorPreviewSendMessageButton);
            mentorPreviewViewProfileButton = itemView.findViewById(R.id.hiredMenteeProfileButton);
        }
    }

}
