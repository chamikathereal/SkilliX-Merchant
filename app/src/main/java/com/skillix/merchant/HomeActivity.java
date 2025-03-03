package com.skillix.merchant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillix.merchant.utils.ToastUtils;
import com.squareup.picasso.Picasso;


public class HomeActivity extends AppCompatActivity {



    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userId = auth.getCurrentUser().getUid();


        Intent intent = getIntent();
        String userName = intent.getStringExtra("USER_NAME");
        String userEmail = intent.getStringExtra("USER_EMAIL");
        String accountType = intent.getStringExtra("ACCOUNT_TYPE");
        String accountStatus = intent.getStringExtra("ACCOUNT_STATUS");

        //Log.i("SkilliXLog",accountStatus);


        DrawerLayout drawerLayout1 = findViewById(R.id.drawerLayout1);
        Toolbar toolbar1 = findViewById(R.id.toolbar1);
        NavigationView navigationView1 = findViewById(R.id.navigationView1);


        View headerView = navigationView1.getHeaderView(0);
        TextView headerTextView1 = headerView.findViewById(R.id.headerTextView1);
        TextView headerTextView2 = headerView.findViewById(R.id.headerTextView2);


        headerTextView1.setText(userName);
        headerTextView2.setText(userEmail);
        ShapeableImageView userHeaderImage = headerView.findViewById(R.id.userHeaderImage);
        firestore.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String imageUrl = documentSnapshot.getString("profile_image");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).into(userHeaderImage);
                } else {
                    userHeaderImage.setImageResource(R.drawable.profile);
                }

            }
        });

        navigationView1.getMenu().clear();


        if (accountType.equals("Mentee")) {
            navigationView1.inflateMenu(R.menu.navigation_menu);
        } else if (accountType.equals("Mentor")) {
            navigationView1.inflateMenu(R.menu.mentor_navigation_menu);
        }

        // Set the appropriate fragment based on the user type
        if (savedInstanceState == null) {
            if (accountType.equals("Mentee")) {

                if (accountStatus.equals("updated")) {
                    loadFragment(new MenteeHomeFragment());
                    navigationView1.setCheckedItem(R.id.menteeHomeFragment);
                } else {
                    loadFragment(new MenteeAccountFragment());
                    navigationView1.setCheckedItem(R.id.menteeAccountFragment);
                }

            } else if (accountType.equals("Mentor")) {
                if (accountStatus.equals("updated")) {
                    loadFragment(new MentorHomeFragment());
                    navigationView1.setCheckedItem(R.id.mentorHomeFragment);
                }else{
                    loadFragment(new MentorAccountFragment());
                    navigationView1.setCheckedItem(R.id.mentorAccountFragment);
                }

            }
        }

        navigationView1.setNavigationItemSelectedListener(item -> {

            if (accountType.equals("Mentee")) {
                // Handle mentee-specific menu items
                if (item.getItemId() == R.id.menteeHomeFragment) {
                    loadFragment(new MenteeHomeFragment());
                } else if (item.getItemId() == R.id.menteeAccountFragment) {
                    loadFragment(new MenteeAccountFragment());
                } else if (item.getItemId() == R.id.messageFragment) {
                    loadFragment(new MessageFragment());
                }else if (item.getItemId() == R.id.viewHiredMentor) {
                    loadFragment(new ViewHiredMentorFragment());
                }else if (item.getItemId() == R.id.signOut) {
                    Log.i("SkilliXSignoutLog","Signed Out!");
                    signOutProcess();
                }else if (item.getItemId() == R.id.addNote) {
                    loadFragment(new NoteBookFragment());
                }else if (item.getItemId() == R.id.termsAndCondition) {
                    loadFragment(new TermsAndConditionsFragment());
                }

            } else if (accountType.equals("Mentor")) {

                if (item.getItemId() == R.id.mentorHomeFragment) {
                    loadFragment(new MentorHomeFragment());
                } else if (item.getItemId() == R.id.mentorAccountFragment) {
                    loadFragment(new MentorAccountFragment());
                } else if (item.getItemId() == R.id.mentorServicesFragment) {
                    loadFragment(new MentorServicesFragment());
                } else if (item.getItemId() == R.id.messageFragment) {
                    loadFragment(new MessageFragment());
                }else if (item.getItemId() == R.id.signOut) {
                    Log.i("SkilliXSignoutLog","Signed Out!");
                    signOutProcess();
                }else if (item.getItemId() == R.id.termsAndCondition) {
                    loadFragment(new TermsAndConditionsFragment());
                }else if (item.getItemId() == R.id.mentorViewFeedbackFragment) {
                    loadFragment(new ViewFeedbackFragment());
                }
            }


            drawerLayout1.closeDrawers();
            //toolbar1.setTitle(item.getTitle());
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView1, fragment, null)
                .setReorderingAllowed(true)
                .commit();
    }

    private void signOutProcess(){
        Log.i("SkilliXSignoutLog", "Signed Out!");


        SharedPreferences sharedPref = getSharedPreferences("SkilliXPref", MODE_PRIVATE);


        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
        startActivity(intent);

        finish();


        ToastUtils.showSuccessToast(HomeActivity.this, "Signed out successfully!");
    }

}

//        Button button = findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.fragmentContainerView1, new ViewMentorProfileFragment(),null);
//                fragmentTransaction.addToBackStack(null); // Optional: Enables back navigation
//                fragmentTransaction.commit();
//
//
//            }
//        });