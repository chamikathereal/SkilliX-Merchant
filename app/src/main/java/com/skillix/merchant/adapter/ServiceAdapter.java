package com.skillix.merchant.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillix.merchant.MentorServicesDialogFragment;
import com.skillix.merchant.MentorServicesFragment;
import com.skillix.merchant.R;
import com.skillix.merchant.model.Service;

import java.util.ArrayList;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private ArrayList<Service> serviceArrayList;
    private FirebaseFirestore firestore;

    private Context context;

    public ServiceAdapter(Context context, ArrayList<Service> serviceArrayList) {
        this.context = context;  // Assign context
        this.serviceArrayList = serviceArrayList;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.added_services, parent, false);
        return new ServiceViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceArrayList.get(position);
        final String serviceId = service.getId();
        final String serviceName = service.getName();
        final String serviceDescription = service.getDescription();
        final String servicePrice = service.getPrice();

        holder.serviceNameTextView.setText(serviceName);
        holder.serviceDescriptionTextView.setText(serviceDescription);
        holder.servicePriceTextView.setText(servicePrice);

        holder.serviceDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SkilliXLog", "Delete: " + serviceId);
                Log.i("SkilliXLog", "Delete: " + serviceName);

                // Directly execute Firestore delete without background thread
                firestore.collection("service_price").document(serviceId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Update UI on main thread after success
                                holder.itemView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(holder.itemView.getContext(), "Service successfully deleted!", Toast.LENGTH_SHORT).show();
                                        FragmentManager fragmentManager = ((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager();
                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                        // Replace the current fragment with MentorServicesFragment
                                        fragmentTransaction.replace(R.id.fragmentContainerView1, new MentorServicesFragment());
                                        fragmentTransaction.commit();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("SkilliXLog", "Error deleting service: ", e);
                            }
                        });
            }
        });

        holder.serviceEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SkilliXLog", "Edit: " + serviceId);
                Log.i("SkilliXLog", "Edit: " + serviceName);

                MentorServicesDialogFragment dialogFragment = new MentorServicesDialogFragment();

                Bundle args = new Bundle();
                args.putString("serviceId", serviceId);
                dialogFragment.setArguments(args);

                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                dialogFragment.show(fragmentManager, "MentorServicesDialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceArrayList.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {

        TextView serviceNameTextView;
        TextView serviceDescriptionTextView;
        TextView servicePriceTextView;
        Button serviceDeleteButton;
        Button serviceEditButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceNameTextView = itemView.findViewById(R.id.nameTextView);
            serviceDescriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            servicePriceTextView = itemView.findViewById(R.id.priceTextView);
            serviceDeleteButton = itemView.findViewById(R.id.mentorPreviewSendMessageButton);
            serviceEditButton = itemView.findViewById(R.id.hiredMenteeProfileButton);
        }
    }

    private void deleteService(){

    }
}


