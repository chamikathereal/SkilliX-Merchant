package com.skillix.merchant;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.adapter.ServiceAdapter;
import com.skillix.merchant.model.Service;
import java.util.ArrayList;


public class MentorServicesFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String userId;
    private RecyclerView addedServiceRecyclerView;
    private ServiceAdapter serviceAdapter;
    private ArrayList<Service> serviceList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mentor_services, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userId = auth.getCurrentUser().getUid();

        addedServiceRecyclerView = view.findViewById(R.id.addedServiceRecyclerView);
        addedServiceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        serviceList = new ArrayList<>();

        firestore.collection("service_price")
                .whereEqualTo("mentor_id", userId)
                .orderBy("price")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot != null) {
                            serviceList.clear(); // Clear the existing list to avoid duplicates

                            // Loop through the documents and add data to the list
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String id = document.getId();
                                String title = document.getString("name");
                                String price = document.getString("price");
                                String description = document.getString("description");


                                Service service = new Service(id, title, price, description);
                                serviceList.add(service);
                            }


                            if (serviceAdapter == null) {
                                serviceAdapter = new ServiceAdapter(getActivity(), serviceList);
                                addedServiceRecyclerView.setAdapter(serviceAdapter);
                            } else {

                                serviceAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });



        Button addServiceButton = view.findViewById(R.id.addServiceButton);
        addServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MentorServicesDialogFragment dialogFragment = new MentorServicesDialogFragment();
                dialogFragment.show(getParentFragmentManager(), "mentorServicesDialog");
            }
        });

        return view;
    }






}