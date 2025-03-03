package com.skillix.merchant;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.skillix.merchant.adapter.SubscriptionAdapter;
import com.skillix.merchant.model.Subscription;
import com.skillix.merchant.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;


public class PurchaseSubscriptionFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String userId;

    private RecyclerView recyclerView;
    private SubscriptionAdapter subscriptionAdapter;
    private ArrayList<Subscription> subscriptionArrayList;
    private String subscriptionDocumentId;
    private String title;
    private String description;
    private String price;
    private String PAYMENT_NO;
    private String selectedSubscriptionDocumentId;
    private String selectedTitle;
    private String selectedDescription;
    private String selectedPrice;
    private String first_name;
    private String last_name;
    private String email;
    private String mobile;
    private String address;
    private String country;

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
                        subscribedPayment();


                    } else {
                        PAYMENT_NO = "Result: " + response.toString();
                    }
                } else {
                    PAYMENT_NO = "Result: no response";
                }

                Log.d("SkilliXLog", PAYMENT_NO);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null) {
                    Log.d("SkilliXLog", response.toString());
                } else {
                    Log.d("SkilliXLog", "User canceled the request");
                }
            } else {

                Log.d("SkilliXLog", "Unexpected result code: " + resultCode);
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_purchase_subscription, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();

        recyclerView = rootView.findViewById(R.id.mentorSubscriptionPurchasingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        subscriptionArrayList = new ArrayList<>();
        subscriptionAdapter = new SubscriptionAdapter(subscriptionArrayList);
        recyclerView.setAdapter(subscriptionAdapter);


        loadSubscriptions();

        loadUser();

        Button purchaseMentorSubscription = rootView.findViewById(R.id.purchaseMentorSubscription);
        purchaseMentorSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedPosition = subscriptionAdapter.getSelectedPosition();
                if (selectedPosition != -1) {

                    Subscription selectedSubscription = subscriptionArrayList.get(selectedPosition);


                    selectedSubscriptionDocumentId = selectedSubscription.getId();
                    selectedTitle = selectedSubscription.getName();
                    selectedDescription = selectedSubscription.getDescription();
                    selectedPrice = selectedSubscription.getPrice();

                    Log.i("SkilliXLog",
                            "Subscription selected: \n" +
                                    "ID: " + selectedSubscriptionDocumentId + "\n" +
                                    "Name: " + selectedTitle + "\n" +
                                    "Description: " + selectedDescription + "\n" +
                                    "Price: " + selectedPrice);

                    makePayherePayment();

                } else {
                    //Toast.makeText(getActivity(), "Please select a subscription", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(getActivity(), "Please select a subscription!");
                }
            }
        });

        return rootView;
    }

    private void loadSubscriptions() {
        firestore.collection("mentor_subscription")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot documentSnapshots = task.getResult();
                            if (documentSnapshots != null) {
                                subscriptionArrayList.clear();

                                for (DocumentSnapshot document : documentSnapshots) {

                                    subscriptionDocumentId = document.getId();
                                    description = document.getString("description");
                                    title = document.getString("title");
                                    price = document.getString("price");


                                    subscriptionArrayList.add(new Subscription(subscriptionDocumentId, title, price, description));
                                }

                                subscriptionAdapter.notifyDataSetChanged();
                            } else {
                                //Toast.makeText(getContext(), "Subscriptions not found", Toast.LENGTH_SHORT).show();
                                ToastUtils.showWarningToast(getActivity(), "Subscriptions not found!");
                            }
                        }else {
                            //Toast.makeText(getContext(), "Failed to load subscriptions", Toast.LENGTH_SHORT).show();
                            ToastUtils.showErrorToast(getActivity(), "Failed to load subscriptions!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
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
                                address = document.getString("address");
                                country = document.getString("country");

                                Log.i("SkilliXLog", "first_name: " + document.getString("first_name"));
                                Log.i("SkilliXLog", "last_name: " + document.getString("last_name"));
                                Log.i("SkilliXLog", "email: " + document.getString("email"));
                                Log.i("SkilliXLog", "mobile: " + document.getString("mobile"));
                                Log.i("SkilliXLog", "address: " + document.getString("address"));
                                Log.i("SkilliXLog", "country: " + document.getString("country"));


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

    private void showNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "C1")
                .setSmallIcon(R.drawable.main_logo)
                .setContentTitle("SkilliX!")
                .setContentText(getString(R.string.notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "C1",
                    "Channel1",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(notificationChannel);
        }

        try {
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            Log.e("Notification Error", "Error posting notification", e);  // Log the error (important!)

        }
    }

    private void subscribedPayment() {

        Date currentDate = Calendar.getInstance().getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(currentDate);

        System.out.println("Current DateTime: " + formattedDate);


        Map<String, Object> subscriptionPayments = new HashMap<>();
        subscriptionPayments.put("payment_no", PAYMENT_NO);
        subscriptionPayments.put("mentor_id", userId);
        subscriptionPayments.put("subscription_id", selectedSubscriptionDocumentId);
        subscriptionPayments.put("date_time", formattedDate);

        firestore.collection("subscription_payments").add(subscriptionPayments)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        SharedPreferences sharedPref = getActivity().getSharedPreferences("SkilliXPref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString("ACCOUNT_STATUS", "updated");
                        editor.apply();

                        //Toast.makeText(getContext(), "Subscription Purchasing Successful.", Toast.LENGTH_SHORT).show();
                        ToastUtils.showSuccessToast(getActivity(), "Subscription Purchasing Successful!");

                        Map<String,Object> accStatusUpdate = new HashMap<>();
                        accStatusUpdate.put("account_status","active");
                        accStatusUpdate.put("subscription_status","active");
                        accStatusUpdate.put("onceUpdated", "updated");
                        firestore.collection("users").document(userId).update(accStatusUpdate)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.i("SkilliXLog","Successfully Updated");
                                            }
                                        });

                        showNotification();

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragmentContainerView1, new MentorHomeFragment(), null);
                        fragmentTransaction.commit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("SkilliXLog", "Error updating user data", e);
                    }
                });

    }

    private void makePayherePayment() {
        InitRequest req = new InitRequest();
        req.setMerchantId("1229554");
        req.setCurrency("LKR");
        req.setAmount(Double.parseDouble(selectedPrice));
        req.setOrderId(selectedSubscriptionDocumentId);
        req.setItemsDescription(selectedTitle);
        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName(first_name);
        req.getCustomer().setLastName(last_name);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress(address);
        req.getCustomer().getAddress().setCity("Not Defined");
        req.getCustomer().getAddress().setCountry(country);
        req.setNotifyUrl("xxxx");
        req.getCustomer().getDeliveryAddress().setAddress("No.2, Kandy Road");
        req.getCustomer().getDeliveryAddress().setCity("Kadawatha");
        req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");
        req.getItems().add(new Item(null, title, 1, Double.parseDouble(price)));

        Intent intent = new Intent(getActivity(), PHMainActivity.class); // Use getActivity() instead of this
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST); // Send unique request ID
    }

}