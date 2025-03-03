package com.skillix.merchant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nitish.typewriterview.TypeWriterView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        SharedPreferences sharedPref = getSharedPreferences("SkilliXPref", MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {

            Intent signedInIntent = new Intent(MainActivity.this, HomeActivity.class);


            String userName = sharedPref.getString("USER_NAME", "Default Name");
            String userEmail = sharedPref.getString("USER_EMAIL", "default@email.com");
            String userUID = sharedPref.getString("USER_UID", "defaultUID");
            String accountType = sharedPref.getString("ACCOUNT_TYPE", "Default Type");
            String accountStatus = sharedPref.getString("ACCOUNT_STATUS", "Default Status");


            signedInIntent.putExtra("USER_NAME", userName);
            signedInIntent.putExtra("USER_EMAIL", userEmail);
            signedInIntent.putExtra("USER_UID", userUID);
            signedInIntent.putExtra("ACCOUNT_TYPE", accountType);
            signedInIntent.putExtra("ACCOUNT_STATUS", accountStatus);

            startActivity(signedInIntent);
            finish();

            Log.i("SkilliXLog", "Get Started");

        }

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TypeWriterView typeWriterView = findViewById(R.id.typeWriterView);
        typeWriterView.animateText("Skillix connects Mentors with Mentees for learning and professional growth.");

        Button getStartButton = findViewById(R.id.updateMentorAccountButton);
        getStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SkilliXLog","Get Started");

                Intent getStartIntent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(getStartIntent);
                finish();


            }
        });

    }
}