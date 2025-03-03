package com.skillix.merchant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skillix.merchant.utils.ToastUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenteeAccountFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String userId;

    private TextView userNameInAccountFragment;
    private EditText userFirstNameEditText, userLastNameEditText, userEmailEditText, userMobileEditText;
    private Spinner userCountrySpinner, userCareerSpinner;
    private TextView userGenderTextView;
    private Button updateMenteeAccountButton;

    private ArrayList<String> userCountryList = new ArrayList<>();
    private ArrayList<String> careerList = new ArrayList<>();

    private ArrayAdapter<String> countryAdapter;
    private ArrayAdapter<String> careerAdapter;

    private ShapeableImageView menteeProfileIcon;
    private Uri profileImageUri;
    private static final String TAG = "SkilliXLog";

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    try {
                        String mimeType = getActivity().getContentResolver().getType(result);
                        if (mimeType != null && (mimeType.equals("image/jpeg") || mimeType.equals("image/png"))) {
                            menteeProfileIcon.setImageURI(result);
                            profileImageUri = result;  // Save the image URI for uploading
                        } else {
                            //Toast.makeText(getActivity(), "Please select a JPG or PNG image", Toast.LENGTH_SHORT).show();
                            ToastUtils.showWarningToast(getActivity(), "Please select a JPG or PNG image!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Toast.makeText(getActivity(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        ToastUtils.showErrorToast(getActivity(), "Failed to load image!");
                    }
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mentee_account, container, false);


        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userId = auth.getCurrentUser().getUid();


        userFirstNameEditText = view.findViewById(R.id.userFirstNameEditText);
        userLastNameEditText = view.findViewById(R.id.userLastNameEditText);
        userEmailEditText = view.findViewById(R.id.userEmailEditText);
        userMobileEditText = view.findViewById(R.id.userMobileEditText);
        userGenderTextView = view.findViewById(R.id.uerGenderTextView);
        userCountrySpinner = view.findViewById(R.id.userCountrySpinner);
        userCareerSpinner = view.findViewById(R.id.userCareerSpinner);
        updateMenteeAccountButton = view.findViewById(R.id.updateMenteeAccountButton);
        userNameInAccountFragment = view.findViewById(R.id.userNameInAccountFrgament);
        menteeProfileIcon = view.findViewById(R.id.menteeProfileIcon);
        menteeProfileIcon.setImageResource(R.drawable.profile);


        loadUserData();


        loadCountries();
        loadCareers();


        updateMenteeAccountButton.setOnClickListener(v -> updateUserData());


        Button uploadMenteeProfileButton = view.findViewById(R.id.uploadMenteeProfileButton);
        uploadMenteeProfileButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        return view;
    }

    private void loadUserData() {
        firestore.collection("users").document(userId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Error loading user data", e);
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Get user data and set in UI
                            userFirstNameEditText.setText(documentSnapshot.getString("first_name"));
                            userLastNameEditText.setText(documentSnapshot.getString("last_name"));
                            userEmailEditText.setText(documentSnapshot.getString("email"));
                            userMobileEditText.setText(documentSnapshot.getString("mobile"));
                            userGenderTextView.setText(documentSnapshot.getString("gender"));

                            String fname = documentSnapshot.getString("first_name");
                            String lname = documentSnapshot.getString("last_name");


                            userNameInAccountFragment.setText(fname + " " + lname);


                            String country = documentSnapshot.getString("country");
                            String career = documentSnapshot.getString("career");

                            userCountrySpinner.setSelection(userCountryList.indexOf(country));
                            userCareerSpinner.setSelection(careerList.indexOf(career));

                            NavigationView navigationView = getActivity().findViewById(R.id.navigationView1);
                            View navHeaderView = navigationView.getHeaderView(0);
                            TextView headerTextView1 = navHeaderView.findViewById(R.id.headerTextView1);
                            TextView headerTextView2 = navHeaderView.findViewById(R.id.headerTextView2);
                            headerTextView1.setText(fname + " " + lname);
                            headerTextView2.setText(documentSnapshot.getString("email"));


                            String imageUrl = documentSnapshot.getString("profile_image");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl).into(menteeProfileIcon); // Picasso for loading image
                            }
                        }
                    }
                });
    }


    private void loadCountries() {
        firestore.collection("country").orderBy("country_name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    userCountryList.add("Select Country");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        userCountryList.add(document.getString("country_name"));
                    }

                    Log.d(TAG, "Country List Loaded: " + userCountryList);

                    countryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, userCountryList);
                    userCountrySpinner.setAdapter(countryAdapter);

                    firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String country = documentSnapshot.getString("country");
                            Log.d(TAG, "User Country: " + country);

                            int index = userCountryList.indexOf(country);
                            Log.d(TAG, "Country Index: " + index);

                            if (index != -1) {
                                userCountrySpinner.setSelection(index);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Error getting countries", task.getException());
                }
            }
        });
    }


    private void loadCareers() {
        firestore.collection("career").orderBy("career_name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    careerList.clear();  // Ensure the list is fresh
                    careerList.add("Select Career");  // Default option

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        careerList.add(document.getString("career_name"));
                    }

                    Log.d(TAG, "Career List Loaded: " + careerList);

                    careerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, careerList);
                    userCareerSpinner.setAdapter(careerAdapter);

                    firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("career")) {
                            String career = documentSnapshot.getString("career");
                            Log.d(TAG, "User Career: " + career);

                            int index = careerList.indexOf(career);
                            Log.d(TAG, "Career Index: " + index);

                            if (index != -1) {
                                userCareerSpinner.setSelection(index);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Error getting careers", task.getException());
                }
            }
        });
    }

    private void updateUserData() {
        String firstName = userFirstNameEditText.getText().toString().trim();
        String lastName = userLastNameEditText.getText().toString().trim();
        String email = userEmailEditText.getText().toString().trim();
        String mobileNumber = userMobileEditText.getText().toString().trim();
        String gender = userGenderTextView.getText().toString().trim();
        String country = userCountrySpinner.getSelectedItem().toString();
        String career = userCareerSpinner.getSelectedItem().toString();

        Map<String, Object> userData = new HashMap<>();
        userData.put("first_name", firstName);
        userData.put("last_name", lastName);
        userData.put("email", email);
        userData.put("mobile", mobileNumber);
        userData.put("gender", gender);
        userData.put("country", country);
        userData.put("onceUpdated", "updated");

        if (!career.equals("Select Career")) {
            userData.put("career", career);
        }

        firestore.collection("users").document(userId).update(userData)
                .addOnSuccessListener(aVoid -> {
                    //Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                    ToastUtils.showSuccessToast(getActivity(), "Profile Updated!");

                    SharedPreferences sharedPref = getActivity().getSharedPreferences("SkilliXPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.putString("ACCOUNT_STATUS", "updated");
                    editor.apply();

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragmentContainerView1, new MenteeHomeFragment());
                    transaction.addToBackStack(null); // Optional: Add to back stack to allow back navigation
                    transaction.commit();



                    if (profileImageUri != null) {
                        uploadProfileImage(profileImageUri);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error updating user data", e));
    }

    private void uploadProfileImage(Uri imageUri) {
        StorageReference storageRef = storage.getReference();
        String fileName = userId + ".jpg";
        StorageReference profileImageRef = storageRef.child("profile_images/" + fileName);

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveProfileImageUrl(uri.toString())))
                .addOnFailureListener(e ->
                        //Toast.makeText(getActivity(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        ToastUtils.showErrorToast(getActivity(), "Image upload failed!")
                );
    }

    private void saveProfileImageUrl(String imageUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("profile_image", imageUrl);

        firestore.collection("users").document(userId).update(userData)
                .addOnSuccessListener(aVoid -> {
                    menteeProfileIcon.setImageURI(Uri.parse(imageUrl));
                    updateNavHeaderImage(imageUrl);
                    //Toast.makeText(getContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
                    ToastUtils.showSuccessToast(getActivity(), "Profile image updated!");
                })
                .addOnFailureListener(e ->
                        //Toast.makeText(getContext(), "Error saving profile image", Toast.LENGTH_SHORT).show()
                        ToastUtils.showErrorToast(getActivity(), "Error saving profile image!")
                );
    }

    private void updateNavHeaderImage(String imageUrl) {
        NavigationView navigationView = getActivity().findViewById(R.id.navigationView1);
        View navHeaderView = navigationView.getHeaderView(0);
        ShapeableImageView userHeaderImage = navHeaderView.findViewById(R.id.userHeaderImage);
        Picasso.get().load(imageUrl).into(userHeaderImage);
        Picasso.get().load(imageUrl).into(menteeProfileIcon);
    }
}


