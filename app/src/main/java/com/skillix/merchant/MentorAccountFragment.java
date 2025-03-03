package com.skillix.merchant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


public class MentorAccountFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String userId;

    private TextView userFullName, userGenderTextView;
    private EditText userFirstNameEditText, userLastNameEditText, userEmailEditText, userMobileEditText, userBioEditText, userAddressEditText, userAboutEditText, userEducationEditText,portfolioEditText;
    private Spinner userCountrySpinner, userCareerSpinner, userCertifiedCategorySpinner;
    private Button updateMentorAccountButton, addLocationButton;

    private ArrayList<String> userCountryList = new ArrayList<>();
    private ArrayList<String> careerList = new ArrayList<>();
    private ArrayList<String> certifiedCategoryArrayList = new ArrayList<>();

    private ArrayAdapter<String> countryAdapter;
    private ArrayAdapter<String> careerAdapter;
    private ArrayAdapter<String> certifiedCategoryAdapter;

    private ImageView mentorCoverImage;
    private ShapeableImageView mentorProfileImage;
    private Uri profileImageUri, coverImageUri;
    private boolean isProfileImage;

    private double latitude;
    private double longitude;
    private String address,AccountStatus;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    try {
                        String mimeType = getActivity().getContentResolver().getType(result);
                        if (mimeType != null && (mimeType.equals("image/jpeg") || mimeType.equals("image/png"))) {
                            if (isProfileImage) {
                                mentorProfileImage.setImageURI(result);
                                profileImageUri = result;
                            } else {
                                mentorCoverImage.setImageURI(result);
                                coverImageUri = result;
                            }
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View rootView = getView();
        if (rootView != null) {
            rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    Rect r = new Rect();
                    rootView.getWindowVisibleDisplayFrame(r);
                    int screenHeight = rootView.getHeight();
                    int keypadHeight = screenHeight - r.bottom;

                    if (keypadHeight > screenHeight * 0.01) {

                        rootView.setTranslationY(-keypadHeight / 3);
                    } else {

                        rootView.setTranslationY(0);
                    }
                    return true;
                }
            });
        }
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mentor_account, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userId = auth.getCurrentUser().getUid();

        Log.i("SkilliXLog", userId);

        userFirstNameEditText = view.findViewById(R.id.userFirstNameEditText);
        userLastNameEditText = view.findViewById(R.id.userLastNameEditText);
        userEmailEditText = view.findViewById(R.id.userEmailEditText);
        userMobileEditText = view.findViewById(R.id.userMobileEditText);
        userBioEditText = view.findViewById(R.id.userBioEditText);
        userAboutEditText = view.findViewById(R.id.userAboutEditText);
        userEducationEditText = view.findViewById(R.id.userEducationEditText);
        userAddressEditText = view.findViewById(R.id.userAddressEditText);
        portfolioEditText = view.findViewById(R.id.portfolioEditText);

        userCountrySpinner = view.findViewById(R.id.userCountrySpinner);
        userCareerSpinner = view.findViewById(R.id.userCareerSpinner);
        userCertifiedCategorySpinner = view.findViewById(R.id.userCertifiedCategorySpinner);

        userGenderTextView = view.findViewById(R.id.userGenderTextView);

        userFullName = view.findViewById(R.id.userFullName);

        mentorProfileImage = view.findViewById(R.id.mentorProfileImage);
        mentorProfileImage.setImageResource(R.drawable.profile);

        mentorCoverImage = view.findViewById(R.id.mentorCoverImage);
        mentorCoverImage.setImageResource(R.drawable.addcoverimage);


        loadUserData();

        loadCountries();
        loadCareers();
        loadCertifiedCategory();

        Button addLocationButton = view.findViewById(R.id.addLocationButton);
        addLocationButton.setOnClickListener(v -> {

            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGpsEnabled) {
                // GPS is not enabled, show dialog to enable it
                new AlertDialog.Builder(getContext())
                        .setTitle("Enable GPS")
                        .setMessage("Your GPS is disabled. Please enable it to continue.")
                        .setCancelable(false)
                        .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Open location settings
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                                // After enabling GPS, check location again.
                                //openMap();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // GPS is enabled, get the location directly
                openMap();
            }



        });

        getParentFragmentManager().setFragmentResultListener("locationData", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                latitude = result.getDouble("latitude");
                longitude = result.getDouble("longitude");
                address = result.getString("address");

                userAddressEditText.setText(address);

                // Store these as variables
                Log.i("SkilliXLog", "Received Location: " + latitude + ", " + longitude + " - " + address);

                // Check if address is already saved in Firestore or not
                firestore.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                // If address is saved in Firestore, use that
                                String savedAddress = documentSnapshot.getString("address");
                                if (savedAddress != null && !savedAddress.isEmpty()) {
                                    userAddressEditText.setText(savedAddress);  // Show address from Firestore
                                } else {
                                    userAddressEditText.setText(address);  // Show address from map if Firestore is empty
                                }
                            } else {
                                // If no document exists, show the map address
                                userAddressEditText.setText(address);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle errors (optional)
                            Log.e("SkilliXLog", "Error getting document: ", e);
                            userAddressEditText.setText(address);  // Fallback to map address
                        });
            }
        });

        Button uploadMentorProfileImageButton = view.findViewById(R.id.uploadMentorProfileImageButton);
        uploadMentorProfileImageButton.setOnClickListener(v -> {


            if (getActivity() != null) {
                isProfileImage = true; // Set flag for profile image
                pickImageLauncher.launch("image/*");
            } else {
                //Toast.makeText(requireContext(), "Activity is not available", Toast.LENGTH_SHORT).show();
                ToastUtils.showErrorToast(getActivity(), "Activity is not available!");
            }
        });

        Button uploadMentorCoverImageButton = view.findViewById(R.id.uploadMentorCoverImageButton);
        uploadMentorCoverImageButton.setOnClickListener(v -> {


            if (getActivity() != null) {
                isProfileImage = false; // Set flag for cover image
                pickImageLauncher.launch("image/*");
            } else {
                //Toast.makeText(requireContext(), "Activity is not available", Toast.LENGTH_SHORT).show();
                ToastUtils.showErrorToast(getActivity(), "Activity is not available!");
            }
        });

        updateMentorAccountButton = view.findViewById(R.id.updateMentorAccountButton);
        updateMentorAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if AccountStatus is null before logging
                if (AccountStatus != null) {
                    Log.i("SkilliXStatusLog", AccountStatus);
                } else {
                    Log.e("SkilliXStatusLog", "AccountStatus is null");
                }

                if ("updated".equals(AccountStatus)) {
                    updateUserData();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragmentContainerView1, new MentorHomeFragment(), null);
                    //fragmentTransaction.addToBackStack(null); // Optional: Enables back navigation
                    fragmentTransaction.commit();
                } else {
                    updateUserData();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragmentContainerView1, new PurchaseSubscriptionFragment(), null);
                    fragmentTransaction.addToBackStack(null); // Optional: Enables back navigation
                    fragmentTransaction.commit();
                }
            }
        });



        return view;
    }

    private void openMap(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView1, new GoogleMapFragment(), null);
        fragmentTransaction.addToBackStack(null); // Optional: Enables back navigation
        fragmentTransaction.commit();
    }

    private void loadUserData() {
        firestore.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        userFirstNameEditText.setText(documentSnapshot.getString("first_name"));
                        userLastNameEditText.setText(documentSnapshot.getString("last_name"));
                        userEmailEditText.setText(documentSnapshot.getString("email"));
                        userMobileEditText.setText(documentSnapshot.getString("mobile"));
                        userGenderTextView.setText(documentSnapshot.getString("gender"));
                        userBioEditText.setText(documentSnapshot.getString("bio"));
                        userAboutEditText.setText(documentSnapshot.getString("about"));
                        userEducationEditText.setText(documentSnapshot.getString("education"));
                        portfolioEditText.setText(documentSnapshot.getString("portfolio_url"));


                        AccountStatus = documentSnapshot.getString("onceUpdated");

                        String fname = documentSnapshot.getString("first_name");
                        String lname = documentSnapshot.getString("last_name");
                        userFullName.setText(fname + " " + lname);

                        String savedAddress = documentSnapshot.getString("address");
                        if (savedAddress != null && !savedAddress.isEmpty()) {
                            userAddressEditText.setText(savedAddress);
                        }

                        String profileImageUrl = documentSnapshot.getString("profile_image");
                        Log.i("SkilliXLog", "profileImageUrl: " + profileImageUrl);
                        String coverImageUrl = documentSnapshot.getString("cover_image");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile)
                                    .into(mentorProfileImage);
                        } else {
                            mentorProfileImage.setImageResource(R.drawable.profile);
                        }

                        if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(coverImageUrl)
                                    .placeholder(R.drawable.addcoverimage)
                                    .error(R.drawable.addcoverimage)
                                    .into(mentorCoverImage);
                        } else {
                            mentorCoverImage.setImageResource(R.drawable.addcoverimage);
                        }

                        if (getActivity() != null) {
                            NavigationView navigationView = getActivity().findViewById(R.id.navigationView1);
                            if (navigationView != null) {
                                View navHeaderView = navigationView.getHeaderView(0);
                                TextView headerTextView1 = navHeaderView.findViewById(R.id.headerTextView1);
                                TextView headerTextView2 = navHeaderView.findViewById(R.id.headerTextView2);
                                headerTextView1.setText(fname + " " + lname);
                                headerTextView2.setText(documentSnapshot.getString("email"));

                                ShapeableImageView userHeaderImage = navHeaderView.findViewById(R.id.userHeaderImage);
                                Picasso.get().load(profileImageUrl).into(userHeaderImage);
                            }
                        }
                    }
                });
    }

    private void loadCountries() {

        firestore.collection("country").orderBy("country_name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                // Check if the Fragment is attached to an Activity before proceeding
                if (!isAdded()) {
                    return; // Fragment is not attached, return early to avoid accessing context
                }

                if (task.isSuccessful()) {

                    userCountryList.add("Select Country");

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        userCountryList.add(document.getString("country_name"));
                    }

                    Log.d("SpinnerDebug", "Country List Loaded: " + userCountryList);

                    // Use getActivity() if you're not inside a Fragment method
                    countryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, userCountryList);
                    userCountrySpinner.setAdapter(countryAdapter);

                    firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {

                        // Check if the Fragment is still attached when this async callback is executed
                        if (!isAdded()) {
                            return; // Fragment is not attached, return early
                        }

                        if (documentSnapshot.exists()) {
                            String country = documentSnapshot.getString("country");
                            Log.d("SpinnerDebug", "User Country: " + country);

                            int index = userCountryList.indexOf(country);
                            Log.d("SpinnerDebug", "Country Index: " + index);

                            if (index != -1) {
                                userCountrySpinner.setSelection(index);
                            }
                        }

                    });

                } else {
                    Log.e("FirestoreError", "Error getting countries", task.getException());
                }

            }
        });

    }

    private void loadCareers() {
        firestore.collection("mentor_career").orderBy("name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // Check if the fragment is attached before proceeding
                if (!isAdded()) {
                    return; // Fragment is not attached to the activity, return early
                }

                if (task.isSuccessful()) {
                    careerList.clear();  // Ensure the list is fresh
                    careerList.add("Select Career");  // Default option

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        careerList.add(document.getString("name"));
                    }

                    Log.d("SpinnerDebug", "Career List Loaded: " + careerList);

                    // Use requireContext() only after ensuring the fragment is attached
                    careerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, careerList);
                    userCareerSpinner.setAdapter(careerAdapter);

                    // Load user's career if it exists
                    firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                        // Again, check if the fragment is attached before updating UI elements
                        if (!isAdded()) {
                            return; // Fragment is not attached, return early
                        }

                        if (documentSnapshot.exists() && documentSnapshot.contains("career")) {
                            String career = documentSnapshot.getString("career");
                            Log.d("SpinnerDebug", "User Career: " + career);

                            int index = careerList.indexOf(career);
                            Log.d("SpinnerDebug", "Career Index: " + index);

                            if (index != -1) {
                                userCareerSpinner.setSelection(index);
                            }
                        }
                    });
                } else {
                    Log.e("FirestoreError", "Error getting careers", task.getException());
                }
            }
        });
    }

    private void loadCertifiedCategory() {
        firestore.collection("certified_category").orderBy("name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // Check if the Fragment is still attached to the Activity
                if (!isAdded()) {
                    return; // Fragment is not attached, return early
                }

                if (task.isSuccessful()) {
                    certifiedCategoryArrayList.clear();  // Ensure the list is fresh
                    certifiedCategoryArrayList.add("Select Category");  // Default option

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        certifiedCategoryArrayList.add(document.getString("name"));
                    }

                    Log.d("SpinnerDebug", "Category List Loaded: " + certifiedCategoryArrayList);

                    // Only use requireContext() if the Fragment is still attached
                    certifiedCategoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, certifiedCategoryArrayList);
                    userCertifiedCategorySpinner.setAdapter(certifiedCategoryAdapter);

                    // Load user's certified category if it exists
                    firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                        // Again, check if the Fragment is still attached before interacting with UI
                        if (!isAdded()) {
                            return; // Fragment is not attached, return early
                        }

                        if (documentSnapshot.exists() && documentSnapshot.contains("certified_category")) {
                            String certifiedCategory = documentSnapshot.getString("certified_category");
                            Log.d("SpinnerDebug", "User certifiedCategory: " + certifiedCategory);

                            int index = certifiedCategoryArrayList.indexOf(certifiedCategory);
                            Log.d("SpinnerDebug", "Certified Category Index: " + index);

                            if (index != -1) {
                                userCertifiedCategorySpinner.setSelection(index);
                            }
                        }
                    });
                } else {
                    Log.e("FirestoreError", "Error getting certified categories", task.getException());
                }
            }
        });
    }


    private void updateUserData() {

        String firstName = userFirstNameEditText.getText().toString().trim();
        String lastName = userLastNameEditText.getText().toString().trim();
        String email = userEmailEditText.getText().toString().trim();
        String mobileNumber = userMobileEditText.getText().toString().trim();
        String bio = userBioEditText.getText().toString().trim();
        String userAddress = userAddressEditText.getText().toString().trim();
        String about = userAboutEditText.getText().toString().trim();
        String education = userEducationEditText.getText().toString().trim();
        String portfolioUrl = portfolioEditText.getText().toString().trim();


        //String gender = userGenderTextView.getText().toString().trim();
        String country = userCountrySpinner.getSelectedItem().toString().trim();
        String career = userCareerSpinner.getSelectedItem().toString().trim();
        String category = userCertifiedCategorySpinner.getSelectedItem().toString().trim();


        double userLatitude = latitude;
        double userLongitude = longitude;


        Log.i("SkilliXLog", "First Name: " + firstName);
        Log.i("SkilliXLog", "Last Name: " + lastName);
        Log.i("SkilliXLog", "Email: " + email);
        Log.i("SkilliXLog", "Mobile Number: " + mobileNumber);
        Log.i("SkilliXLog", "Bio: " + bio);
        Log.i("SkilliXLog", "Address: " + userAddress);
        Log.i("SkilliXLog", "userLatitude: " + userLatitude);
        Log.i("SkilliXLog", "userLongitude: " + userLongitude);
        Log.i("SkilliXLog", "About: " + about);
        Log.i("SkilliXLog", "Education: " + education);

        //Log.i("SkilliXLog", "Gender: " + gender);
        Log.i("SkilliXLog", "Country: " + country);
        Log.i("SkilliXLog", "Career: " + career);

        Map<String, Object> userData = new HashMap<>();
        userData.put("first_name", firstName);
        userData.put("last_name", lastName);
        userData.put("email", email);
        userData.put("mobile", mobileNumber);
        userData.put("bio", bio);
        userData.put("address", userAddress);
        userData.put("portfolio_url", portfolioUrl);

        if (userLatitude != 0 && userLongitude != 0) {
            userData.put("latitude", userLatitude);
            userData.put("longitude", userLongitude);
        }

        userData.put("about", about);
        userData.put("education", education);

        userData.put("country", country);

        if (!career.equals("Select Career")) {
            userData.put("career", career);
        }

        if (!category.equals("Select Category")) {
            userData.put("certified_category", category);
        }

        firestore.collection("users").document(userId).update(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Check if the context is not null
                        Context context = getContext();
                        if (context == null) {
                            context = getActivity();
                        }


                        if (context != null) {
                            //Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show();
                            ToastUtils.showSuccessToast(getActivity(), "Profile Updated!");
                        }

                        if (profileImageUri != null) {
                            uploadImage(profileImageUri, "profile");
                        }

                        if (coverImageUri != null) {
                            uploadImage(coverImageUri, "cover");
                        }
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirestoreError", "Error updating user data", e);
                    }
                });

    }

    private void uploadImage(Uri imageUri, String type) {
        StorageReference storageRef = storage.getReference();

        String fileName = userId + (type.equals("profile") ? ".jpg" : "_cover.jpg");
        StorageReference imageRef = storageRef.child(type.equals("profile") ? "profile_images/" + fileName : "cover_images/" + fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveImageUrl(uri.toString(), type)))
                .addOnFailureListener(e ->
                        //Toast.makeText(getActivity(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        ToastUtils.showErrorToast(getActivity(), "Image upload failed!")
                );
    }

    private void saveImageUrl(String imageUrl, String type) {
        Map<String, Object> userData = new HashMap<>();

        if (type.equals("profile")) {
            userData.put("profile_image", imageUrl);
        } else {
            userData.put("cover_image", imageUrl);
        }

        firestore.collection("users").document(userId).update(userData)
                .addOnSuccessListener(aVoid -> {
                    if (getActivity() != null) { // Ensure the activity is not null
                        if (type.equals("profile")) {
                            mentorProfileImage.setImageURI(Uri.parse(imageUrl));
                            ToastUtils.showSuccessToast(getActivity(), "Profile image updated!");
                        } else {
                            mentorCoverImage.setImageURI(Uri.parse(imageUrl));
                            ToastUtils.showSuccessToast(getActivity(), "Cover image updated!");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (getActivity() != null) {
                        ToastUtils.showErrorToast(getActivity(), "Error saving, Please Try again!");
                    }
                });
    }



}