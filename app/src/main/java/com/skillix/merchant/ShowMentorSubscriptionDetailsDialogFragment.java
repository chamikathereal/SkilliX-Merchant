package com.skillix.merchant;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ShowMentorSubscriptionDetailsDialogFragment extends DialogFragment {
    AlertDialog dialog;
    TextView nameOfSubscriptionTextView, durationOfSubscriptionTextView, pdateOfSubscriptionTextView, eDateOfSubscriptionTextView, priceOfSubscriptionTextView, descriptionOfSubscriptionTextView;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    String userId, subId, subscriptionTitle, duration, description, price;
    ImageView closeImageView;

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_show_mentor_subscription_details_dialog, null);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        nameOfSubscriptionTextView = dialogView.findViewById(R.id.nameOfSubscriptionTextView);
        durationOfSubscriptionTextView = dialogView.findViewById(R.id.durationOfSubscriptionTextView);
        pdateOfSubscriptionTextView = dialogView.findViewById(R.id.pdateOfSubscriptionTextView);
        eDateOfSubscriptionTextView = dialogView.findViewById(R.id.eDateOfSubscriptionTextView);
        priceOfSubscriptionTextView = dialogView.findViewById(R.id.priceOfSubscriptionTextView);
        descriptionOfSubscriptionTextView = dialogView.findViewById(R.id.descriptionOfSubscriptionTextView);
        closeImageView = dialogView.findViewById(R.id.closeImageView);

        if (getArguments() != null) {
            subId = getArguments().getString("sub_Id");
            subscriptionTitle = getArguments().getString("subscriptionTitle");
            duration = getArguments().getString("duration");
            description = getArguments().getString("description");
            price = getArguments().getString("price");

        }

        if (subId != null) {
            Log.i("SkilliXTest1Log", subId);
            Log.i("SkilliXTest1Log", subscriptionTitle);
            Log.i("SkilliXTest1Log", duration);
            Log.i("SkilliXTest1Log", description);
            Log.i("SkilliXTest1Log", price);
            loadSubData(subId);

            nameOfSubscriptionTextView.setText(subscriptionTitle);
            priceOfSubscriptionTextView.setText(price);
            descriptionOfSubscriptionTextView.setText(description);
            durationOfSubscriptionTextView.setText(duration + " " + "Month");
        }

        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog when the ImageView is clicked
                dialog.dismiss();
            }
        });

        builder.setView(dialogView);

        dialog = builder.create();


        return dialog;
    }

    private void loadSubData(String subId) {


        firestore.collection("subscription_payments")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            QuerySnapshot querySnapshot = task.getResult();
                            for (DocumentSnapshot document : querySnapshot) {

                                String paymentId = document.getId();
                                String mentorId = document.getString("mentor_id");

                                if (mentorId.equals(userId)) {
                                    String dateTime = document.getString("date_time");

                                    String paymentNo = document.getString("payment_no");
                                    String subscriptionId = document.getString("subscription_id");

                                    Log.i("SkilliXSubscriptionTestLog", "dateTime: " + dateTime);
                                    Log.i("SkilliXSubscriptionTestLog", "paymentId: " + paymentId);
                                    Log.i("SkilliXSubscriptionTestLog", "Mentor ID: " + mentorId);
                                    Log.i("SkilliXSubscriptionTestLog", "Payment No: " + paymentNo);
                                    Log.i("SkilliXSubscriptionTestLog", "Subscription ID: " + subscriptionId);

                                    firestore.collection("mentor_subscription").document(subscriptionId)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                    if (task.isSuccessful()){

                                                        DocumentSnapshot document = task.getResult();

                                                        if (document.exists()){

                                                            String duration = document.getString("duration");


                                                            if (dateTime != null && !dateTime.isEmpty()) {
                                                                try {
                                                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                                                    Date inputDate = dateFormat.parse(dateTime);

                                                                    int durationInt = Integer.parseInt(duration);

                                                                    Calendar calendar = Calendar.getInstance();
                                                                    calendar.setTime(inputDate);
                                                                    calendar.add(Calendar.MONTH, durationInt);


                                                                    Date newDate = calendar.getTime();

                                                                    SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy MMM dd");

                                                                    Log.i("SkilliXSubscriptionTestLog", "Date 3 months later: " + newDateFormat.format(newDate));
                                                                    pdateOfSubscriptionTextView.setText(newDateFormat.format(inputDate));
                                                                    eDateOfSubscriptionTextView.setText(newDateFormat.format(newDate));

                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                    Log.e("SkilliXSubscriptionTestLog", "Error parsing date: " + dateTime);
                                                                }
                                                            }

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
}