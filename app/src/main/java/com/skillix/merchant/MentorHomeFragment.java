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

import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.adapter.HiredMenteeAdapter;
import com.skillix.merchant.model.HiredMentee;


import java.util.ArrayList;


public class MentorHomeFragment extends Fragment {

    RecyclerView recyclerView1;
    TextView mentorFullName, profileviewCountTextView, mentorRevenueTextView, hiredMenteeCountTextView, subscriptionNameTextView;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    String userId, subId, subscriptionTitle, duration, description, price;
    View viewMentorSubscriptionPackage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mentor_home, container, false);
        recyclerView1 = view.findViewById(R.id.viewHiredMenteeProfileVIewRecyclerView);
        profileviewCountTextView = view.findViewById(R.id.profileviewCountTextView);
        subscriptionNameTextView = view.findViewById(R.id.subscriptionNameTextView);
        hiredMenteeCountTextView = view.findViewById(R.id.hiredMenteeCountTextView);
        mentorRevenueTextView = view.findViewById(R.id.mentorRevenueTextView);
        mentorFullName = view.findViewById(R.id.mentorFullName);
        viewMentorSubscriptionPackage = view.findViewById(R.id.viewMentorSubscriptionPackage);


        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        Log.i("SkilliXLog", "Mentoor_Id" + userId);

        //////////////////////////////////////////////////////////////////////////


        Log.i("SkilliXSubscriptionTestLog", "hi: " + subId);
        viewMentorSubscriptionPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SkilliXSubscriptionTestLog", "hi");
                Log.i("SkilliXSubscriptionTestLog", "hi: " + subId);

                ShowMentorSubscriptionDetailsDialogFragment showsubscriptionDetails = new ShowMentorSubscriptionDetailsDialogFragment();

                Bundle args = new Bundle();
                args.putString("sub_Id", subId);
                args.putString("subscriptionTitle", subscriptionTitle);
                args.putString("duration", duration);
                args.putString("description", description);
                args.putString("price", price);

                showsubscriptionDetails.setArguments(args);

                showsubscriptionDetails.show(getParentFragmentManager(), "showsubscriptionDetailsDialog");
            }
        });


        //////////////////////////////////////////////////////////////////////////


        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {

                                String first_name = document.getString("first_name");
                                String last_name = document.getString("last_name");

                                String fullName = first_name + " " + last_name;
                                mentorFullName.setText(fullName);
                            }

                        }

                    }
                });

        loadProfileViewCount();
        loadRevenue();
        loadMySubscriptionPackage();
        loadHiredMenteeCount();
        loadHiredMenteesList();







        return view;
    }


    private void loadProfileViewCount() {
        firestore.collection("profile_view_count")
                .whereEqualTo("mentor_id", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot documentSnapshots = task.getResult();

                            for (DocumentSnapshot document : documentSnapshots) {


                                String vc = String.valueOf(document.getLong("viewCount"));
                                profileviewCountTextView.setText(vc);

                                Log.i("SkilliXLog", "viewCount: " + vc);
                            }
                        }
                    }
                });
    }

    private void loadRevenue() {

        firestore.collection("mentor_revenue").whereEqualTo("mentor_id", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            QuerySnapshot documentSnapshots = task.getResult();

                            for (DocumentSnapshot document : documentSnapshots) {


                                Double revenue = document.getDouble("revenue");
                                Log.i("SkilliXLog", "revenue: " + String.valueOf(revenue));

                                String revenueString = String.valueOf(revenue);

                                mentorRevenueTextView.setText(revenueString);

                            }

                        }

                    }
                });


    }

    private void loadHiredMenteeCount() {

        firestore.collection("mentor_hired").whereEqualTo("mentor_Id", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            QuerySnapshot documentSnapshots = task.getResult();
                            int hiredMenteeCount = documentSnapshots.size();
                            hiredMenteeCountTextView.setText(String.valueOf(hiredMenteeCount));
                            Log.i("SkilliXLog", "hiredMenteeCount: " + String.valueOf(hiredMenteeCount));

                        }

                    }
                });

    }

    private void loadMySubscriptionPackage() {

        firestore.collection("subscription_payments").whereEqualTo("mentor_id", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            QuerySnapshot documentSnapshots = task.getResult();
                            for (DocumentSnapshot document : documentSnapshots) {


                                Log.i("SkilliXLog", "subscription_id: " + String.valueOf(document.getString("subscription_id")));
                                String subscriptionId = document.getString("subscription_id");

                                if (subscriptionId != null) {
                                    firestore.collection("mentor_subscription").document(subscriptionId)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                    if (task.isSuccessful()) {

                                                        DocumentSnapshot documentSnapshot = task.getResult();

                                                        if (documentSnapshot.exists()) {

                                                            subId = documentSnapshot.getId();
                                                            subscriptionTitle = documentSnapshot.getString("title");
                                                            duration = documentSnapshot.getString("duration");
                                                            description = documentSnapshot.getString("description");
                                                            price = documentSnapshot.getString("price");


                                                            subscriptionNameTextView.setText(subscriptionTitle);
                                                            Log.i("SkilliXLog", "subscriptionTitle: " + subscriptionTitle);
                                                            Log.i("SkilliXLog", "subscriptionTitle: " + subscriptionTitle);

                                                        }

                                                    }

                                                }
                                            });
                                }


                            }
                        }

                    }
                });

    }

    private void loadHiredMenteesList() {
        ArrayList<HiredMentee> hiredMenteeArrayList = new ArrayList<>();
        HiredMenteeAdapter hiredMenteeAdapter = new HiredMenteeAdapter(getActivity(),hiredMenteeArrayList);

        // Set up RecyclerView
        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView1.setAdapter(hiredMenteeAdapter);

        firestore.collection("mentor_hired")
                .whereEqualTo("mentor_Id", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot documentSnapshots = task.getResult();

                            // Track the number of mentees fetched to ensure we notify the adapter once all data is loaded
                            int[] menteeCount = {documentSnapshots.size()};

                            for (DocumentSnapshot document : documentSnapshots) {
                                String menteeId = document.getString("mentee_Id");
                                String service_price_Id = document.getString("service_price_Id");

                                Log.i("SkilliXLog", "menteeId: " + menteeId);
                                Log.i("SkilliXLog", "service_price_Id: " + service_price_Id);

                                firestore.collection("users").document(menteeId)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot documentSnapshot = task.getResult();

                                                    if (documentSnapshot.exists()) {
                                                        String hiredmenteeId = documentSnapshot.getString("user_id");
                                                        String proImgURL = documentSnapshot.getString("profile_image");
                                                        String firstName = documentSnapshot.getString("first_name");
                                                        String lastName = documentSnapshot.getString("last_name");
                                                        String career = documentSnapshot.getString("career");

                                                        String fullName = firstName + " " + lastName;

                                                        Log.i("SkilliXLog", "Hired mentees Data: " +
                                                                "proImgURL: " + proImgURL +
                                                                "fullName: " + fullName +
                                                                "career: " + career);

                                                        firestore.collection("service_price").document(service_price_Id)
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            DocumentSnapshot serviceDocument = task.getResult();

                                                                            if (serviceDocument.exists()) {
                                                                                String servicePackageName = serviceDocument.getString("name");
                                                                                Log.i("SkilliXLog", "servicePackageName: " + servicePackageName);

                                                                                // Add data to the ArrayList
                                                                                hiredMenteeArrayList.add(new HiredMentee(
                                                                                        hiredmenteeId,
                                                                                        proImgURL,
                                                                                        firstName,
                                                                                        lastName,
                                                                                        career,
                                                                                        servicePackageName // Using the service package name
                                                                                ));

                                                                                // Notify adapter once all mentees are processed
                                                                                menteeCount[0]--;
                                                                                if (menteeCount[0] == 0) {
                                                                                    hiredMenteeAdapter.notifyDataSetChanged();
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
                    }
                });
    }

    private void loadHiredMenteesListManual() {


        ArrayList<HiredMentee> hiredMenteeArrayList = new ArrayList<>();

        hiredMenteeArrayList.add(new HiredMentee("5", "https://chamikathereal.github.io/static/media/about.48bf6a450b799af930f9.jpg", "Chamika", "Gayashan", "Undergraduate", "Bronze Package"));
        hiredMenteeArrayList.add(new HiredMentee("5", "https://chamikathereal.github.io/static/media/about.48bf6a450b799af930f9.jpg", "Chamika", "Gayashan", "Undergraduate", "Bronze Package"));
        hiredMenteeArrayList.add(new HiredMentee("5", "https://chamikathereal.github.io/static/media/about.48bf6a450b799af930f9.jpg", "Chamika", "Gayashan", "Undergraduate", "Bronze Package"));


        HiredMenteeAdapter hiredMenteeAdapter = new HiredMenteeAdapter(getActivity(),hiredMenteeArrayList);

        recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView1.setAdapter(hiredMenteeAdapter);
    }


}