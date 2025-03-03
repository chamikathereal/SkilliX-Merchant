package com.skillix.merchant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.adapter.SubscriptionAdapter;
import com.skillix.merchant.model.Subscription;
import com.skillix.merchant.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class MentorHiringDialogFragment extends DialogFragment {

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private ArrayList<Subscription> subscriptionList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userId, mentorId, PAYMENT_NO, first_name, last_name, email, mobile, country;
    private Subscription selectedSubscription;
    private static final int PAYHERE_REQUEST = 1001;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {

                if (response != null) {
                    if (response.isSuccess()) {
                        PAYMENT_NO = String.valueOf(response.getData().getPaymentNo());
                        settledMentorPayment();

                    } else {
                        PAYMENT_NO = "Result: " + response.toString();
                    }
                } else {
                    PAYMENT_NO = "Result: no response";
                }

                Log.d("SkilliXLog", PAYMENT_NO);
                //textView.setText(msg);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null) {
                    Log.d("SkilliXLog", response.toString());
                } else {
                    Log.d("SkilliXLog", "User canceled the request");
                }
            } else {
                // Optional: Handle other result codes if necessary
                Log.d("SkilliXLog", "Unexpected result code: " + resultCode);
            }

        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_mentor_hiring_dialog, null);

        initializeFirebase();
        retrieveArguments();
        setupRecyclerView(view);
        loadSubscriptions();

        loadUser();

        Button hireMentorButton = view.findViewById(R.id.hireButton);
        hireMentorButton.setOnClickListener(v -> handleSubscriptionSelection());

        Button dismissButton = view.findViewById(R.id.dismissMentorServiceButton);
        dismissButton.setOnClickListener(view1 -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId != null) {
            Log.i("SkilliXLog", "Received Mentee ID: " + userId);
        } else {
            Log.e("SkilliXLog", "User is not logged in.");
        }
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            mentorId = getArguments().getString("mentor_Id");
            Log.i("SkilliXLog", "Received Mentor ID: " + mentorId);
        }
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.subscriptionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SubscriptionAdapter(subscriptionList);
        recyclerView.setAdapter(adapter);
    }

    private void loadSubscriptions() {
        if (mentorId == null) {
            Log.e("SkilliXLog", "Mentor ID is null, cannot fetch subscriptions.");
            return;
        }

        firestore.collection("service_price")
                .whereEqualTo("mentor_id", mentorId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        subscriptionList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Subscription subscription = new Subscription(
                                    document.getId(),
                                    document.getString("name"),
                                    document.getString("price"),
                                    document.getString("description")
                            );
                            subscriptionList.add(subscription);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("SkilliXLog", "Error loading subscriptions: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e("SkilliXLog", "Failed to fetch subscriptions", e));
    }

    private void handleSubscriptionSelection() {

        int selectedPosition = adapter.getSelectedPosition();
        if (selectedPosition != -1) {
            selectedSubscription = subscriptionList.get(selectedPosition);

            Log.i("SkilliXLog", "id: " + String.valueOf(selectedSubscription.getId()));
            Log.i("SkilliXLog", "name: " + String.valueOf(selectedSubscription.getName()));
            Log.i("SkilliXLog", "description: " + String.valueOf(selectedSubscription.getDescription()));
            Log.i("SkilliXLog", "price: " + Double.parseDouble(selectedSubscription.getPrice()));

            makePayherePayment();
            //Toast.makeText(getActivity(), "Subscription selected: " + selectedSubscription.getName(), Toast.LENGTH_SHORT).show();

            //Toast.makeText(getActivity(), "Subscription selected: " + selectedSubscription.getId(), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getActivity(), "Please select a subscription", Toast.LENGTH_SHORT).show();
            ToastUtils.showWarningToast(getActivity(), "Please select a subscription!");
        }
    }

    private void settledMentorPayment() {

        Date currentDate = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(currentDate);

        Map<String, Object> mentorHired = new HashMap<>();
        mentorHired.put("service_price_Id", selectedSubscription.getId());
        mentorHired.put("mentee_Id", userId);
        mentorHired.put("mentor_Id", mentorId);
        mentorHired.put("date_time", formattedDate);
        mentorHired.put("status", "onGoing");


        firestore.collection("mentor_hired").add(mentorHired)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //Toast.makeText(getActivity(), "Payment Successful!", Toast.LENGTH_SHORT).show();
                        ToastUtils.showSuccessToast(getActivity(), "Payment Successful!");
                        dismiss();

                        double paymentPrice = Double.parseDouble(selectedSubscription.getPrice());
                        Log.i("SkilliXLog", "paymentPrice: " + paymentPrice);

                        firestore.collection("mentor_revenue").whereEqualTo("mentor_id",mentorId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                        if (task.isSuccessful()){

                                            QuerySnapshot documentSnapshots = task.getResult();

                                            if (documentSnapshots != null && !documentSnapshots.isEmpty()){
                                                for (DocumentSnapshot document : documentSnapshots) {

                                                    String revenueId = document.getId();
                                                    double revenue = document.getDouble("revenue");

                                                    double total = revenue + paymentPrice;

                                                    Map<String,Object> updateRevenue = new HashMap<>();
                                                    updateRevenue.put("revenue",total);
                                                    updateRevenue.put("total_revenue",total);

                                                    firestore.collection("mentor_revenue").document(revenueId).
                                                            update(updateRevenue)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.i("SkilliXLog", "revenue Updated");
                                                                }
                                                            });

                                                    Log.i("SkilliXLog", "revenue: " + String.valueOf(revenue));
                                                    Log.i("SkilliXLog", "paymentPrice: " + paymentPrice);
                                                    Log.i("SkilliXLog", "total: " + total);

                                                }
                                            }else {
                                                Log.i("SkilliXLog", "revenue is empty");
                                                Map<String,Object> addRevenue = new HashMap<>();
                                                addRevenue.put("mentor_id",mentorId);
                                                addRevenue.put("revenue",paymentPrice);
                                                addRevenue.put("total_revenue",paymentPrice);

                                                firestore.collection("mentor_revenue").add(addRevenue)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Log.i("SkilliXLog", "revenue Added");
                                                            }
                                                        });
                                            }



                                        }

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(getActivity(), "Try Again", Toast.LENGTH_SHORT).show();
                        ToastUtils.showErrorToast(getActivity(), "Try Again!");
                        dismiss();
                    }
                });
    }

    private void makePayherePayment() {

        InitRequest req = new InitRequest();
        req.setMerchantId("1229554");       // Merchant ID
        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
        req.setAmount(Double.parseDouble(selectedSubscription.getPrice()));             // Final Amount to be charged
        req.setOrderId(selectedSubscription.getId());        // Unique Reference ID
        req.setItemsDescription(selectedSubscription.getName());  // Item description title
        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName(first_name);
        req.getCustomer().setLastName(last_name);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress("Not Defined!");
        req.getCustomer().getAddress().setCity("Not Defined!");
        req.getCustomer().getAddress().setCountry(country);


        req.setNotifyUrl("xxxx");
        req.getCustomer().getDeliveryAddress().setAddress("Not Defined!");
        req.getCustomer().getDeliveryAddress().setCity("Not Defined!");
        req.getCustomer().getDeliveryAddress().setCountry(country);
        req.getItems().add(new Item(selectedSubscription.getId(), selectedSubscription.getName(), 1, Double.parseDouble(selectedSubscription.getPrice())));

        Intent intent = new Intent(getActivity(), PHMainActivity.class); // Use getActivity() instead of this
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST); // Send unique request ID

    }

    private void loadUser() {
        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {

                                first_name = document.getString("first_name");
                                last_name = document.getString("last_name");
                                email = document.getString("email");
                                mobile = document.getString("mobile");
                                country = document.getString("country");

                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}
