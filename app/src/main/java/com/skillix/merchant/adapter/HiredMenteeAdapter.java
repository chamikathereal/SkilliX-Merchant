package com.skillix.merchant.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.skillix.merchant.R;
import com.skillix.merchant.ViewMenteesPurchesdServiceDetailsFragment;
import com.skillix.merchant.model.HiredMentee;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class HiredMenteeAdapter extends RecyclerView.Adapter<HiredMenteeAdapter.HiredMenteeViewHolder> {

    ArrayList<HiredMentee> hiredMenteeArrayList;

    private Context context;
    public HiredMenteeAdapter(Context context, ArrayList<HiredMentee> hiredMenteeArrayList) {
        this.context = context;
        this.hiredMenteeArrayList = hiredMenteeArrayList;
    }

    @NonNull
    @Override
    public HiredMenteeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.hired_mentee_view, parent, false);

        HiredMenteeViewHolder hiredMenteeViewHolder = new HiredMenteeViewHolder(inflatedView);

        return hiredMenteeViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull HiredMenteeViewHolder holder, int position) {

        HiredMentee hiredMentee = hiredMenteeArrayList.get(position);
        final String menteeId = hiredMentee.getMenteeId();
        final Uri mentorImg = Uri.parse(hiredMentee.getProfileImgURL());
        final String firstName = hiredMentee.getFirstName();
        final String lastName = hiredMentee.getLastName();
        final String career = hiredMentee.getCareer();
        final String packageName = hiredMentee.getPackageName();

        final String menteeFullName = firstName + " " + lastName;

        Picasso.get()
                .load(mentorImg)
                .placeholder(R.drawable.profile)  // Optional: Placeholder image
                //.error(R.drawable.error_image)    // Optional: Error image
                .into(holder.menteeProfile);
        holder.menteeName.setText(menteeFullName);
        holder.menteeCareer.setText(career);
        holder.hiredMenteePackageNameTextView.setText(packageName);

        holder.seeMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SkilliXLog", "MenteeID: " + menteeId);

                ViewMenteesPurchesdServiceDetailsFragment dialogFragment = new ViewMenteesPurchesdServiceDetailsFragment();

                Bundle args = new Bundle();
                args.putString("menteeId", menteeId);
                dialogFragment.setArguments(args);

                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                dialogFragment.show(fragmentManager, "ViewMenteesPurchasedService");

            }
        });



    }

    @Override
    public int getItemCount() {
        return hiredMenteeArrayList.size();
    }

    static class HiredMenteeViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView menteeProfile;
        TextView menteeName, menteeCareer,hiredMenteePackageNameTextView;
        Button seeMoreButton;

        public HiredMenteeViewHolder(@NonNull View itemView) {
            super(itemView);

            menteeProfile = itemView.findViewById(R.id.hiredMenteeProImageView);
            menteeName = itemView.findViewById(R.id.hiredMenteeNameTextView);
            menteeCareer = itemView.findViewById(R.id.hiredMenteeCategoryTextView);
            seeMoreButton = itemView.findViewById(R.id.hiredMenteeProfileButton);
            hiredMenteePackageNameTextView = itemView.findViewById(R.id.hiredMenteePackageNameTextView);
        }

    }
}
