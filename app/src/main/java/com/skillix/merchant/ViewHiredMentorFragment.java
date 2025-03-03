package com.skillix.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.adapter.MentorCardAdapter;
import com.skillix.merchant.model.MentorCard;

import java.util.ArrayList;


public class ViewHiredMentorFragment extends Fragment {

    FirebaseFirestore firestore;
    RecyclerView viewHiredMentorRecyclerView;
    ArrayList<MentorCard> mentorCardArrayList;
    MentorCardAdapter mentorCardAdapter;
    private FirebaseAuth auth;

    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_hired_mentor, container, false);

        viewHiredMentorRecyclerView = view.findViewById(R.id.viewHiredMentorRecyclerView);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        loadHiredMentor();

        userId = auth.getCurrentUser().getUid();
        return view;


    }

    private void loadHiredMentor(){


        mentorCardArrayList = new ArrayList<>();
        mentorCardAdapter = new MentorCardAdapter(getActivity(), mentorCardArrayList);

        firestore.collection("mentor_hired").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            for (DocumentSnapshot document : querySnapshot) {
                                String mentorHiredId = document.getId();
                                Log.i("SkilliXLog", mentorHiredId);

                                firestore.collection("mentor_hired").document(mentorHiredId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        String mentorId = document.getString("mentor_Id");
                                                        String menteeId = document.getString("mentee_Id");
                                                        String status = document.getString("status");

                                                        if (menteeId.equals(userId)&&status.equals("onGoing")) {
                                                            Log.i("SkilliXLog", "menteeId.equals(userId)" + mentorId);

                                                            firestore.collection("users").document(mentorId)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                DocumentSnapshot document = task.getResult();

                                                                                if (document.exists()) {
                                                                                    String mentorId = document.getId();
                                                                                    String proIMG = String.valueOf(document.get("profile_image"));
                                                                                    String firstName = String.valueOf(document.get("first_name"));
                                                                                    String lastName = String.valueOf(document.get("last_name"));
                                                                                    String career = String.valueOf(document.get("career"));
                                                                                    String country = String.valueOf(document.get("country"));


                                                                                    mentorCardArrayList.add(
                                                                                            new MentorCard(mentorId, proIMG, firstName, lastName, career, country)
                                                                                    );

                                                                                }
                                                                                mentorCardAdapter.notifyDataSetChanged();
                                                                            }
                                                                        }
                                                                    });

                                                            viewHiredMentorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                                            viewHiredMentorRecyclerView.setAdapter(mentorCardAdapter);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });



    }
}