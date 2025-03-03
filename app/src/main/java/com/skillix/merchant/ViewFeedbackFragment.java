package com.skillix.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.adapter.FeedbackAdapter;
import com.skillix.merchant.model.Feedback;

import java.util.ArrayList;


public class ViewFeedbackFragment extends Fragment {

    FirebaseFirestore firestore;
    FirebaseAuth auth;
    String userId;
    RecyclerView feedbackViewRescyclerView;
    FeedbackAdapter feedbackAdapter;
    ArrayList<Feedback> feedbackArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_view_feedback, container, false);

        feedbackViewRescyclerView = view.findViewById(R.id.feedbackViewRescyclerView);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userId = auth.getCurrentUser().getUid();

        loadMentorFeedbacks(view);

        return view;
    }

    private void loadMentorFeedbacks(View view){


        feedbackArrayList = new ArrayList<>();

        firestore.collection("feedback").whereEqualTo("mentor_id", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Feedback", "Error getting feedback: ", error);
                            return;
                        }

                        if (value != null) {
                            feedbackArrayList.clear(); // Clear the previous list

                            for (DocumentSnapshot document : value) {
                                String feedbackText = String.valueOf(document.get("feedback_text"));
                                String menteeId = String.valueOf(document.get("mentee_id"));
                                String mentorId = String.valueOf(document.get("mentor_id"));

                                // Query the users collection for mentee details
                                firestore.collection("users").document(menteeId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot userDocument = task.getResult();

                                                    if (userDocument.exists()) {

                                                        String fname = String.valueOf(userDocument.get("first_name"));
                                                        String lname = String.valueOf(userDocument.get("last_name"));
                                                        String career = String.valueOf(userDocument.get("career"));
                                                        String profileImgURL = String.valueOf(userDocument.get("profile_image"));


                                                        feedbackArrayList.add(new Feedback(
                                                                profileImgURL, fname, lname, career, feedbackText
                                                        ));


                                                        feedbackAdapter.notifyDataSetChanged();
                                                    }
                                                } else {
                                                    Log.e("Feedback", "Error getting mentee details: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e("Feedback", "Error: Data is null");
                        }
                    }
                });

        feedbackAdapter = new FeedbackAdapter(feedbackArrayList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        feedbackViewRescyclerView.setLayoutManager(linearLayoutManager);
        feedbackViewRescyclerView.setAdapter(feedbackAdapter);
    }
}