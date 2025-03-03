package com.skillix.merchant;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class TermsAndConditionsFragment extends Fragment {

    FirebaseFirestore firestore;
    TextView termsAndCOndtionTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_terms_and_conditions, container, false);

        firestore = FirebaseFirestore.getInstance();

        termsAndCOndtionTextView = view.findViewById(R.id.termsAndCOndtionTextView);

        if (firestore != null) {
            firestore.collection("terms_and_conditions").document("termsDoc")
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                String tmsAndcnd = documentSnapshot.getString("text");
                                Log.i("SkilliXLogAdmin", tmsAndcnd);
                                termsAndCOndtionTextView.setText(tmsAndcnd);
                            }
                        }
                    });
        } else {
            Log.e("FirestoreError", "Firestore is not initialized.");
        }

        return view;
    }
}
