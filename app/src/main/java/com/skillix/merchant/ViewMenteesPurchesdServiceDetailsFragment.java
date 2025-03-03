package com.skillix.merchant;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ViewMenteesPurchesdServiceDetailsFragment extends DialogFragment {

    AlertDialog dialog;
    String menteeId, userId, status, mentorHiredId;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    TextView nameOfServiceTextView, pdateOfServiceTextView, priceOfServiceTextView, descriptionOfServiceTextView, statusTextView;
    Button serviceCloseButton, serviceActiveButton;
    ImageView closeImageView;

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_view_mentees_purchesd_service_details, null);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        nameOfServiceTextView = dialogView.findViewById(R.id.nameOfServiceTextView);
        pdateOfServiceTextView = dialogView.findViewById(R.id.pdateOfServiceTextView);
        priceOfServiceTextView = dialogView.findViewById(R.id.priceOfServiceTextView);
        descriptionOfServiceTextView = dialogView.findViewById(R.id.descriptionOfServiceTextView);
        statusTextView = dialogView.findViewById(R.id.statusTextView);
        closeImageView = dialogView.findViewById(R.id.closeImageView);


        serviceCloseButton = dialogView.findViewById(R.id.serviceCloseButton);
        serviceActiveButton = dialogView.findViewById(R.id.serviceActiveButton);


        if (getArguments() != null) {
            menteeId = getArguments().getString("menteeId");
            Log.i("SkilliXTest1Log", menteeId);
        }

        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog when the ImageView is clicked
                dialog.dismiss();
            }
        });

        loadMenteeServiceData();

        serviceCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                completeService();

            }
        });

        serviceActiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activeService();

            }
        });


        builder.setView(dialogView);

        // Custom OnShowListener to handle the positive button click manually
        dialog = builder.create();


        return dialog;
    }

    private void loadMenteeServiceData() {

        firestore.collection("mentor_hired").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            QuerySnapshot querySnapshot = task.getResult();

                            for (DocumentSnapshot document : querySnapshot) {


                                String mentee_Id_db = document.getString("mentee_Id");
                                String mentor_Id_db = document.getString("mentor_Id");

                                //Log.i("SkilliXLog",  "mentor_hired_id" +mentorHiredId);
                                //Log.i("SkilliXLog",  "mentee_Id_db: " +mentee_Id_db);
                                //Log.i("SkilliXLog",  "mentee_Id_db: " +mentor_Id_db);

                                if (userId.equals(mentor_Id_db) && menteeId.equals(mentee_Id_db)) {

                                    mentorHiredId = document.getId();
                                    status = document.getString("status");
                                    String datetime = document.getString("date_time");
                                    String servicePriceId = document.getString("service_price_Id");

                                    try {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date inputDate = dateFormat.parse(datetime);

                                        SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy MMM dd");

                                        Log.i("SkilliXLog", "status: " + status);
                                        Log.i("SkilliXLog", "date_time: " + newDateFormat.format(inputDate));
                                        Log.i("SkilliXLog", "servicePriceId: " + servicePriceId);

                                        pdateOfServiceTextView.setText(newDateFormat.format(inputDate));

                                        if (status.equals("onGoing")) {
                                            statusTextView.setText("OnGoing");

                                            serviceActiveButton.setTextColor(getResources().getColor(R.color.black));
                                            serviceActiveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));
                                            serviceActiveButton.setEnabled(false);
                                        }

                                        if (status.equals("completed")) {
                                            statusTextView.setText("Completed");

                                            statusTextView.setTextColor(getResources().getColor(R.color.color_white)); // Set text color
                                            statusTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_blue))); // Set background tint color

                                            serviceCloseButton.setTextColor(getResources().getColor(R.color.black));
                                            serviceCloseButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));
                                        }

                                        firestore.collection("service_price").document(servicePriceId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                        if (task.isSuccessful()) {

                                                            DocumentSnapshot document = task.getResult();

                                                            if (document.exists()) {

                                                                String name = document.getString("name");
                                                                String price = document.getString("price");
                                                                String description = document.getString("description");

                                                                nameOfServiceTextView.setText(name);
                                                                priceOfServiceTextView.setText(price);
                                                                descriptionOfServiceTextView.setText(description);

                                                                Log.i("SkilliXLog", "name: " + name);
                                                                Log.i("SkilliXLog", "price: " + price);
                                                                Log.i("SkilliXLog", "description: " + description);

                                                            }

                                                        }

                                                    }
                                                });

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.e("SkilliXSubscriptionTestLog", "Error parsing date: " + datetime);
                                    }


                                }


                            }

                        }

                    }
                });

    }

    private void completeService() {

        Log.i("SkilliXLog", "Stateeee: " + mentorHiredId);

        // Ensure status is initialized properly before calling this method
        if (mentorHiredId == null || mentorHiredId.isEmpty()) {
            Toast.makeText(getActivity(), "Invalid mentor hired ID", Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String, Object> completeStatus = new HashMap<>();
        completeStatus.put("status", "completed");

        firestore.collection("mentor_hired").document(mentorHiredId)
                .update(completeStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Show success message
                        ToastUtils.showSuccessToast(getActivity(), "Service Completed!");

                        // Update the UI based on new status
                        statusTextView.setText("Completed");
                        statusTextView.setTextColor(getResources().getColor(R.color.color_white)); // Set text color
                        statusTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_blue))); // Set background tint color

                        serviceActiveButton.setTextColor(getResources().getColor(R.color.color_white));
                        serviceActiveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));

                        serviceCloseButton.setTextColor(getResources().getColor(R.color.black));
                        serviceCloseButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));

                        serviceCloseButton.setEnabled(false); // Disable the button after the service is completed
                        serviceActiveButton.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure, maybe show a failure toast
                        Toast.makeText(getActivity(), "Failed to complete service: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void activeService() {

        Log.i("SkilliXLog", "Stateeee: " + mentorHiredId);

        // Ensure status is initialized properly before calling this method
        if (mentorHiredId == null || mentorHiredId.isEmpty()) {
            Toast.makeText(getActivity(), "Invalid mentor hired ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the status in Firestore
        Map<String, Object> completeStatus = new HashMap<>();
        completeStatus.put("status", "onGoing");

        firestore.collection("mentor_hired").document(mentorHiredId)
                .update(completeStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Show success message
                        ToastUtils.showSuccessToast(getActivity(), "Service Completed!");

                        // Update the UI based on new status
                        statusTextView.setText("OnGoing");
                        statusTextView.setTextColor(getResources().getColor(R.color.color_white));
                        statusTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));

                        serviceActiveButton.setTextColor(getResources().getColor(R.color.black));
                        serviceActiveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));


                        serviceCloseButton.setTextColor(getResources().getColor(R.color.color_white));
                        serviceCloseButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_blue)));


                        serviceActiveButton.setEnabled(false);
                        serviceCloseButton.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure, maybe show a failure toast
                        Toast.makeText(getActivity(), "Failed to complete service: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


}