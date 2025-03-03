package com.skillix.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();


        EditText firstNameEditText = findViewById(R.id.userFirstNameEditText);
        EditText lastNameEditText = findViewById(R.id.userLastNameEditText);
        EditText emailEditText = findViewById(R.id.userEmailEditText);
        Spinner countrySpinner = findViewById(R.id.userCountrySpinner);
        EditText mobileEditText = findViewById(R.id.userMobileEditText);
        Spinner genderSpinner = findViewById(R.id.genderSpinner);
        Spinner menteeSelectSpinner = findViewById(R.id.userCareerSpinner);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        EditText reTypePasswordEditText = findViewById(R.id.repasswordEditText);

        // Spinner For Country
        ArrayList<String> country = new ArrayList<>();
        country.add("Select Country");

        firestore.collection("country").orderBy("country_name").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                country.add(document.getString("country_name"));
                            }

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                    SignUpActivity.this,
                                    R.layout.country_spinner_item,
                                    country
                            );

                            countrySpinner.setAdapter(arrayAdapter);
                        } else {
                            Log.d("FirestoreCountrLog", "Error getting documents: ", task.getException());
                        }
                    }
                });

        // Spinner For Gender
        ArrayList<String> gender = new ArrayList<>();
        gender.add("Select Gender");

        firestore.collection("gender").orderBy("gender_name").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                gender.add(document.getString("gender_name"));
                            }

                            ArrayAdapter<String> genderArrayAdapter = new ArrayAdapter<>(
                                    SignUpActivity.this,
                                    R.layout.country_spinner_item,
                                    gender
                            );

                            genderSpinner.setAdapter(genderArrayAdapter);
                        } else {
                            Log.d("FirestoreGenderLog", "Error getting documents: ", task.getException());
                        }
                    }
                });

        // Spinner For Account Type
        ArrayList<String> menteeOrmentor = new ArrayList<>();
        menteeOrmentor.add("Select Account");

        firestore.collection("account_type").orderBy("account_type_name").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                menteeOrmentor.add(document.getString("account_type_name"));
                            }

                            ArrayAdapter<String> menteeOrmentorArrayAdapter = new ArrayAdapter<>(
                                    SignUpActivity.this,
                                    R.layout.country_spinner_item,
                                    menteeOrmentor
                            );

                            menteeSelectSpinner.setAdapter(menteeOrmentorArrayAdapter);

                        } else {
                            Log.d("FirestoreAccountTypeLog", "Error getting documents: ", task.getException());
                        }
                    }
                });


        Button signUpButton = findViewById(R.id.updateMentorAccountButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input
                String firstName = firstNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String selectCountry = countrySpinner.getSelectedItem().toString();
                String mobile = mobileEditText.getText().toString().trim();
                String selectedGender = genderSpinner.getSelectedItem().toString();
                String selectedAccountType = menteeSelectSpinner.getSelectedItem().toString();
                String password = passwordEditText.getText().toString().trim();
                String reTypePassword = reTypePasswordEditText.getText().toString().trim();

                String onceUpdated = "";


                if (firstName.isEmpty()) {
                    //Toast.makeText(SignUpActivity.this, "First name is required", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "First name is required!");
                } else if (lastName.isEmpty()) {
                    //Toast.makeText(SignUpActivity.this, "Last name is required", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "Last name is required!");

                } else if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //Toast.makeText(SignUpActivity.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "Enter a valid email!");

                } else if (selectCountry.equals("Select Country")) {
                    //Toast.makeText(SignUpActivity.this, "Please select a country", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "Please select a country!");

                } else if (mobile.isEmpty() || mobile.length() != 10) {
                    //Toast.makeText(SignUpActivity.this, "Enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "Enter a valid 10-digit mobile number!");

                } else if (selectedGender.equals("Select Gender")) {
                    //Toast.makeText(SignUpActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "Please select your gender!");

                } else if (selectedAccountType.equals("Select Account")) {
                    //Toast.makeText(SignUpActivity.this, "Please select an account type", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "Please select an account type!");

                } else if (password.isEmpty() || password.length() < 6) {
                    //Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "Password must be at least 6 characters!");

                } else if (!password.equals(reTypePassword)) {
                    //Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignUpActivity.this, "Passwords do not match!");

                } else {

                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        String userId = firebaseAuth.getCurrentUser().getUid();

                                        //store in db
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("user_id", userId);
                                        user.put("first_name", firstName);
                                        user.put("last_name", lastName);
                                        user.put("email", email);
                                        user.put("country", selectCountry);
                                        user.put("mobile", mobile);
                                        user.put("gender", selectedGender);
                                        user.put("password", password);
                                        user.put("account_type", selectedAccountType);
                                        user.put("onceUpdated", onceUpdated);
                                        user.put("registered", "yes");

                                        firestore.collection("users").document(userId)
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                        ToastUtils.showSuccessToast(SignUpActivity.this, "Registration successful!");
                                                        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //Toast.makeText(SignUpActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                                                        ToastUtils.showErrorToast(SignUpActivity.this, "Error saving data!");
                                                    }
                                                });
                                    } else {
                                        //Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        ToastUtils.showErrorToast(SignUpActivity.this, "Sign Up Failed, Email has been already registered!");
                                    }
                                }
                            });
                }
            }
        });

    }


}

