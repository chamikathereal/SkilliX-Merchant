package com.skillix.merchant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.adapter.FeedbackAdapter;
import com.skillix.merchant.model.Feedback;
import com.skillix.merchant.utils.ToastUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewMentorProfileFragment extends Fragment {

    private ImageView mentorProfileCoverImageView;
    private ShapeableImageView mentorProfileImageView;
    private TextView mentorProfileNameTextView, mentorProfileBioTextView, mentorProfileAboutTextView, mentorProfileCareerTextView, mentorProfileEducationTextView, mentorProfileAddressView, mentorProfileCountryTextView, mentorProfileCertifiedTechnologyTextView;
    private Button callButton, mailButton,viewPortfolioButton, messageToMentorButton, hireMentorButton, addFeedBackToMentorButton;
    private String mentorId,menteeId;
    private String mentorFullName;
    private String mentorCategory;
    private String mentorCountry;
    private String mentorImg;
    private String mentorCoverImg;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    FeedbackAdapter feedbackAdapter;
    ArrayList<Feedback> feedbackArrayList;
    private String mobile;
    private String email,portfolioURL;
    private double latitude;
    private double longitude;
    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_view_mentor_profile, container, false);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        menteeId = auth.getCurrentUser().getUid();

        // Retrieve data from the arguments passed to the fragment
        if (getArguments() != null) {
            mentorId = getArguments().getString("mentorId");
            mentorFullName = getArguments().getString("mentorFullName");
        }

        //Hire Mentor Button
        hireMentorButton = view.findViewById(R.id.hireMentorButton);


        firestore.collection("mentor_hired")
                .whereEqualTo("mentor_Id", mentorId)
                .whereEqualTo("mentee_Id", menteeId)

                .whereEqualTo("status", "onGoing")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("SkilliXLog", "Listen failed.", e);
                            return;
                        }

                        if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                            // Iterate through the results in real-time as the data changes
                            for (DocumentSnapshot document : documentSnapshots) {

                                String servicePriceOrder = document.getString("service_price_Id");


                                if (document.exists()) {
                                    hireMentorButton.setText("Mentor Hired");
                                    hireMentorButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Log.i("SkilliXLog",servicePriceOrder);

                                            firestore.collection("service_price").document(servicePriceOrder)
                                                            .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                                            if (task.isSuccessful()){

                                                                                DocumentSnapshot document = task.getResult();

                                                                                        if (document.exists()){

                                                                                            String name = document.getString("name");
                                                                                            String price = document.getString("price");

                                                                                            ToastUtils.showWarningToast(getContext(), "Mentor is Hired! " + "("+name+ " for " + "Rs." + price + ".00");

                                                                                        }

                                                                            }

                                                                        }
                                                                    });


                                        }
                                    });
                                    //hireMentorButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.buttons_shape));
                                    //hireMentorButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_gray));

                                    String onGoingServiceId = document.getId();
                                    Log.i("SkilliXLog", "onGoingServiceId: " + onGoingServiceId);
                                    Log.i("SkilliXLog", "MenteeeeeID: " + menteeId);
                                }
                            }
                        } else {
                            // No results found
                            hireMentorButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.i("SkilliXLog", "Hired");
                                    MentorHiringDialogFragment dialog = new MentorHiringDialogFragment();

                                    // Create and set the arguments
                                    Bundle args = new Bundle();
                                    args.putString("mentor_Id", mentorId);
                                    dialog.setArguments(args); // Set the arguments before showing the dialog

                                    dialog.show(getParentFragmentManager(), "MentorHiringDialog");
                                }
                            });

                            Log.i("SkilliXLog", "No ongoing services found.");
                        }
                    }
                });


        loadMentorProfile();
        loadMentorFeedbacks();

        // Now you can use the data (mentorId, mentorFullName, etc.)
        Log.i("MentorProfile", "mentorId: " + mentorId);
        Log.i("MentorProfile", "mentorFullName: " + mentorFullName);

        //ImageViews
        mentorProfileCoverImageView = view.findViewById(R.id.mentorProfileCoverImageView);
        mentorProfileImageView = view.findViewById(R.id.mentorProfileImageView);

        //TextViews
        mentorProfileNameTextView = view.findViewById(R.id.mentorProfileNameTextView);
        mentorProfileBioTextView = view.findViewById(R.id.mentorProfileBioTextView);
        mentorProfileAboutTextView = view.findViewById(R.id.mentorProfileAboutTextView);
        mentorProfileCareerTextView = view.findViewById(R.id.mentorProfileCareerTextView);
        mentorProfileEducationTextView = view.findViewById(R.id.mentorProfileEducationTextView);
        mentorProfileAddressView = view.findViewById(R.id.mentorProfileAddressView);
        mentorProfileCountryTextView = view.findViewById(R.id.mentorProfileCountryTextView);
        mentorProfileCertifiedTechnologyTextView = view.findViewById(R.id.mentorProfileCertifiedTechnologyTextView);
        callButton = view.findViewById(R.id.callButton);
        mailButton = view.findViewById(R.id.mailButton);
        viewPortfolioButton = view.findViewById(R.id.viewPortfolioButton);

        addFeedBackToMentorButton = view.findViewById(R.id.addFeedBackToMentorButton);
        messageToMentorButton = view.findViewById(R.id.messageToMentorButton);

        //Call Button
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SkilliXLog", mobile);
                Intent mobilePhone = new Intent(Intent.ACTION_DIAL);
                Uri uri = Uri.parse("tel:" + mobile);  // Add 'tel:' prefix
                mobilePhone.setData(uri);
                startActivity(mobilePhone);
            }
        });

        //Mail Button
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SkilliXLog", email);

                // Create an Intent to send email
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                // Set the data (mailto:) and the recipient's email address
                Uri uri = Uri.parse("mailto:" + email);  // Add 'mailto:' prefix to the email address
                emailIntent.setData(uri);

                // Optionally, you can add subject, body, etc.
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body of the email here.");

                // Start the email activity
                startActivity(Intent.createChooser(emailIntent, "Choose an email client"));
            }
        });

        //Portfolio Button
        viewPortfolioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if portfolioURL is null
                if (portfolioURL != null) {
                    Log.i("SkilliXWebLog", portfolioURL); // Log only if portfolioURL is not null

                    // Proceed to the WebViewFragment if the URL is valid
                    Bundle bundle = new Bundle();
                    bundle.putString("url", portfolioURL); // Pass the URL to the WebViewFragment

                    WebViewFragment webViewFragment = new WebViewFragment();
                    webViewFragment.setArguments(bundle);

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragmentContainerView1, webViewFragment); // Make sure fragment_container is the correct container ID
                    transaction.addToBackStack(null); // This ensures the back button will return to the ViewProfileFragment
                    transaction.commit();
                } else {
                    ToastUtils.showWarningToast(getActivity(), "URL is not available!");
                }
            }
        });


        //msg to mentor
        messageToMentorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView1, new ChatFragment(mentorId), null);
                fragmentTransaction.addToBackStack(null);  // Optional: Enables back navigation
                fragmentTransaction.commit();
            }
        });


        //Address VIew From Google Map
        mentorProfileAddressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // Create the new ViewMapFragment and set arguments
                ViewMapFragment viewMapFragment = new ViewMapFragment();

                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", latitude);
                bundle.putDouble("longitude", longitude);
                bundle.putString("mentorFullName", mentorFullName);
                viewMapFragment.setArguments(bundle); // Set arguments before fragment transaction

                // Log before fragment transaction
                Log.i("SkilliXLog", "latitude getData: " + latitude);  // Correct log message
                Log.i("SkilliXLog", "longitude getData: " + longitude);  // Add space between "longitude:" and the value

                // Replace the fragment and add to backstack
                fragmentTransaction.replace(R.id.fragmentContainerView1, viewMapFragment);
                fragmentTransaction.addToBackStack(null);  // This allows the user to navigate back
                fragmentTransaction.commit();
            }
        });

        //Add Feed Back Button
        addFeedBackToMentorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddFeedBackDialogFragment feedBackDialogFragment = new AddFeedBackDialogFragment();

                Bundle args = new Bundle();
                args.putString("mentor_Id", mentorId);
                feedBackDialogFragment.setArguments(args);

                feedBackDialogFragment.show(getParentFragmentManager(), "addFeedbackDialog");

            }
        });


        return view;
    }



    private void loadMentorProfile() {

        firestore.collection("users").document(mentorId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {


                                mentorCoverImg = document.getString("cover_image");
                                mentorImg = document.getString("profile_image");

                                String firstName = document.getString("first_name");
                                String lastName = document.getString("last_name");
                                email = document.getString("email");
                                String bio = document.getString("bio");
                                String about = document.getString("about");
                                String career = document.getString("career");
                                String education = document.getString("education");
                                String address = document.getString("address");
                                String country = document.getString("country");
                                String certified_category = document.getString("certified_category");
                                 portfolioURL = document.getString("portfolio_url");

                                Double latitudeValue = document.getDouble("latitude");
                                if (latitudeValue != null) {
                                    latitude = latitudeValue;
                                } else {

                                    latitude = 0.0;
                                }

                                Double longitudeValue = document.getDouble("longitude");
                                if (longitudeValue != null) {
                                    longitude = longitudeValue;
                                } else {

                                    longitude = 0.0;
                                }


                                mobile = document.getString("mobile");

                                mentorProfileNameTextView.setText(firstName + " " + lastName);
                                mentorProfileBioTextView.setText(bio);
                                mentorProfileAboutTextView.setText(about);
                                mentorProfileCareerTextView.setText(career);
                                mentorProfileEducationTextView.setText(education);
                                mentorProfileAddressView.setText(address);
                                mentorProfileCountryTextView.setText(country);
                                mentorProfileCertifiedTechnologyTextView.setText(certified_category);

                                Picasso.get()
                                        .load(mentorCoverImg)
                                        .placeholder(R.drawable.coverimage)
                                        //.error(R.drawable.error_image)
                                        .into(mentorProfileCoverImageView);

                                Picasso.get()
                                        .load(mentorImg)
                                        .placeholder(R.drawable.profile)
                                        //.error(R.drawable.error_image)
                                        .into(mentorProfileImageView);


                            } else {
                                Log.e("SkilliXLog", "Document Not Found");
                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("SkilliXLog", "Unable to Process");
                    }
                });

    }

    private void loadMentorFeedbacks(){
        RecyclerView feedbackRecyclerView = view.findViewById(R.id.feedbackRecyclerView);

        feedbackArrayList = new ArrayList<>();

        firestore.collection("feedback").whereEqualTo("mentor_id", mentorId)
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
        feedbackRecyclerView.setLayoutManager(linearLayoutManager);
        feedbackRecyclerView.setAdapter(feedbackAdapter);
    }
}