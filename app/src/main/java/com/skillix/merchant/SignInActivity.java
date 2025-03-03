package com.skillix.merchant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillix.merchant.utils.ToastUtils;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button signInButton = findViewById(R.id.updateMentorAccountButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText signInEmailEditText = findViewById(R.id.userLastNameEditText);
                EditText signInEditTextPassword = findViewById(R.id.signInEditTextPassword);


                String email = signInEmailEditText.getText().toString().trim();
                String password = signInEditTextPassword.getText().toString().trim();


                if (email.isEmpty()) {
                    //Toast.makeText(SignInActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignInActivity.this, "Please enter your email address!");
                    return;
                }

                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //Toast.makeText(SignInActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignInActivity.this, "Invalid email format!");
                    return;
                }

                else if (password.isEmpty()) {
                    //Toast.makeText(SignInActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    ToastUtils.showWarningToast(SignInActivity.this, "Please enter your password!");
                    return;
                } else {

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignInActivity.this, task -> {
                                if (task.isSuccessful()) {

                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    String userUID = currentUser.getUid();


                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                    DocumentReference userRef = firestore.collection("users").document(userUID);

                                    userRef.get().addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {

                                            String firstName = documentSnapshot.getString("first_name");
                                            String lastName = documentSnapshot.getString("last_name");
                                            String userEmail = documentSnapshot.getString("email");
                                            String accountType = documentSnapshot.getString("account_type");
                                            String onceUpdated = documentSnapshot.getString("onceUpdated");


                                            SharedPreferences sharedPref = getSharedPreferences("SkilliXPref", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPref.edit();

                                            editor.putBoolean("isLoggedIn", true);

                                            editor.putString("USER_NAME", firstName + " " + lastName);
                                            editor.putString("USER_EMAIL", userEmail);
                                            editor.putString("USER_UID", userUID);
                                            editor.putString("ACCOUNT_TYPE", accountType);
                                            editor.putString("ACCOUNT_STATUS", onceUpdated);

                                            editor.apply();

                                            Intent signedInIntent = new Intent(SignInActivity.this, HomeActivity.class);
                                            signedInIntent.putExtra("USER_NAME", firstName + " " + lastName);
                                            signedInIntent.putExtra("USER_EMAIL", userEmail);
                                            signedInIntent.putExtra("USER_UID", userUID);
                                            signedInIntent.putExtra("ACCOUNT_TYPE", accountType);
                                            signedInIntent.putExtra("ACCOUNT_STATUS", onceUpdated);

                                            Log.i("SkilliXLog","Signed In");


                                            startActivity(signedInIntent);
                                            finish();
                                        } else {
                                            //Toast.makeText(SignInActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                            ToastUtils.showWarningToast(SignInActivity.this, "User data not found!");
                                            Log.i("SkilliXLog","User data not found");
                                        }
                                    }).addOnFailureListener(e -> {
                                        //Toast.makeText(SignInActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                                        ToastUtils.showErrorToast(SignInActivity.this, "Error fetching user data!");
                                        Log.i("SkilliXLog","Error fetching user data");
                                    });

                                } else {

                                    //Toast.makeText(SignInActivity.this, "Sign-In Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    ToastUtils.showErrorToast(SignInActivity.this, "Sign-In Failed!");
                                    Log.e("SignInErrorLog","Sign-In Failed: " + task.getException().getMessage());
                                }
                            });
                }
            }
        });


        TextView registerNowButton = findViewById(R.id.registerNowButton);
        registerNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SklliXLog", "Registration");

                Intent signedUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(signedUpIntent);

            }
        });
    }

}