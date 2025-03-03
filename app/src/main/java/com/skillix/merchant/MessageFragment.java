package com.skillix.merchant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.adapter.MessageModelAdapter;
import com.skillix.merchant.model.MentorCard;
import com.skillix.merchant.model.MessageModel;

import java.util.ArrayList;
import java.util.List;


public class MessageFragment extends Fragment {

    String userId = "1111";
    String admin_Id = "1111";
    FirebaseFirestore firestore;
    String chatId;
    private FirebaseAuth auth;
    private String loggedId, otherId;
    ArrayList<MessageModel> messageModels;
    String searchQuery = "";
    EditText searchUserEditText;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_message, container, false);


        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loggedId = auth.getCurrentUser().getUid();

        chatId = loggedId + "_" + otherId;
        Log.i("SkilliXLog", chatId);

        Log.i("SkilliXLog", "loggedId: " + loggedId);

        recyclerView = view.findViewById(R.id.loadMessageCardRecyclerView);
        searchUserEditText = view.findViewById(R.id.searchUserEditText);

        loadUsers();

        searchUserEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                searchQuery = charSequence.toString().toLowerCase();
                loadUsers();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        // or getActivity() depending on your context

//        ArrayList<MessageModel> messageModels = new ArrayList<>();
//        messageModels.add(new MessageModel("https://example.com/profile1.jpg", "John", "Doe", "Software Engineer"));
//        messageModels.add(new MessageModel("https://example.com/profile2.jpg", "Jane", "Smith", "Product Manager"));


        //chatId = generateChatId(admin_Id, getUserId);
        getUsersWithChats();


        return view;

    }

    private String generateChatId(String senderId, String receiverId) {
        if (senderId.compareTo(receiverId) < 0) {
            return senderId + "_" + receiverId;
        } else {
            return receiverId + "_" + senderId;
        }
    }

    private void getUsersWithChats() {
        // Query for chats where admin is the sender
        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            QuerySnapshot querySnapshot = task.getResult();

                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Iterate through each document in the collection
                                for (DocumentSnapshot document : querySnapshot) {
                                    Log.i("SkilliXLog", String.valueOf(document.getData()));
                                }
                            }

                        } else {
                            Log.e("SkilliXLog", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void loadUsers() {

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageModels = new ArrayList<>();
        MessageModelAdapter messageAdapter = new MessageModelAdapter(getActivity(), messageModels);
        recyclerView.setAdapter(messageAdapter);


        firestore.collection("users").document(loggedId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();
                            String loggedUserAccountType = document.getString("account_type");
                            Log.i("SkilliXLog", "loggedUserAccountType: " + loggedUserAccountType);

                            if (loggedUserAccountType.equals("Mentor")) {
                                firestore.collection("users").whereEqualTo("account_type", "Mentee")
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {

                                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {

                                                    String menteeId = document.getId();
                                                    Log.i("SkilliXLog", "menteesId: " + menteeId);

                                                    //make the chat id
                                                    String mentorGeneratedChatId = menteeId + "_" + loggedId;
                                                    Log.i("SkilliXLog", "mentorGeneratedChatId: " + mentorGeneratedChatId);

                                                    firestore.collection("chats").document(mentorGeneratedChatId).collection("messages")
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        QuerySnapshot messageSnapshots = task.getResult();

                                                                        for (DocumentSnapshot message : messageSnapshots) {

                                                                            String msgData = String.valueOf(message.getData());
                                                                            String senderId = message.getString("senderId");
                                                                            String receiverId = message.getString("receiverId");

                                                                            Log.i("SkilliXLog", "get message data from mentorGeneratedChatId: " + msgData);
                                                                            Log.i("SkilliXLog", "get senderId from mentorGeneratedChatId: " + senderId);
                                                                            Log.i("SkilliXLog", "get receiverId from mentorGeneratedChatId: " + receiverId);

                                                                            if (receiverId.equals(loggedId) || senderId.equals(loggedId)) {

                                                                                Log.i("SkilliXLog", "Yes Receiver Id or Sender Id equals to your logging id: " + loggedId);

                                                                                if (receiverId.equals(loggedId)) {

                                                                                    Log.i("SkilliXLog", "Yes Receiver is equals to your logging id: " + loggedId);

                                                                                    firestore.collection("users")
                                                                                            .document(senderId)
                                                                                            .get()
                                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        DocumentSnapshot mentorSnapshot = task.getResult();

                                                                                                        if (mentorSnapshot.exists()) {
                                                                                                            // Get the details from Firestore
                                                                                                            String theId = senderId; // Use the receiverId directly
                                                                                                            String proIMG = String.valueOf(mentorSnapshot.get("profile_image"));
                                                                                                            String firstName = mentorSnapshot.getString("first_name");
                                                                                                            String lastName = mentorSnapshot.getString("last_name");
                                                                                                            String career = mentorSnapshot.getString("career");

                                                                                                            Log.i("SkilliXLog", "Checking user with ID: " + theId); // Log user ID
                                                                                                            Log.i("SkilliXLog", "First Name: " + firstName);
                                                                                                            Log.i("SkilliXLog", "Last Name: " + lastName);
                                                                                                            Log.i("SkilliXLog", "Career: " + career);

                                                                                                            // Avoid duplication: check if this receiverId is already in the list
                                                                                                            boolean userExists = false;
                                                                                                            for (MessageModel model : messageModels) {
                                                                                                                Log.i("SkilliXLog", "Checking in list for user with ID: " + model.getId()); // Log each model id being checked
                                                                                                                if (model.getId().equals(theId)) { // Check if the id already exists
                                                                                                                    Log.i("SkilliXLog", "Found duplicate user with ID: " + theId); // Log when duplicate found
                                                                                                                    userExists = true;
                                                                                                                    break;
                                                                                                                }
                                                                                                            }

                                                                                                            // If the user doesn't exist, add them to the list
                                                                                                            if (!userExists) {
                                                                                                                Log.i("SkilliXLog", "Adding new user with ID: " + theId); // Log when adding new user
                                                                                                                messageModels.add(new MessageModel(theId, proIMG, firstName, lastName, career));
                                                                                                                messageAdapter.notifyDataSetChanged();
                                                                                                            }

                                                                                                        } else {
                                                                                                            Log.i("SkilliXLog", "No such document");
                                                                                                        }
                                                                                                    } else {
                                                                                                        Log.e("SkilliXLog", "Task failed with exception: ", task.getException());
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }

                                                                                if (senderId.equals(loggedId)) {

                                                                                    Log.i("SkilliXLog", "Yes Sender Id is equals to your logging id: " + loggedId);

                                                                                    firestore.collection("users")
                                                                                            .document(receiverId)
                                                                                            .get()
                                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        DocumentSnapshot mentorSnapshot = task.getResult();

                                                                                                        if (mentorSnapshot.exists()) {
                                                                                                            // Get the details from Firestore
                                                                                                            String theId = receiverId; // Use the receiverId directly
                                                                                                            String proIMG = String.valueOf(mentorSnapshot.get("profile_image"));
                                                                                                            String firstName = mentorSnapshot.getString("first_name");
                                                                                                            String lastName = mentorSnapshot.getString("last_name");
                                                                                                            String career = mentorSnapshot.getString("career");

                                                                                                            Log.i("SkilliXLog", "Checking user with ID: " + theId); // Log user ID
                                                                                                            Log.i("SkilliXLog", "First Name: " + firstName);
                                                                                                            Log.i("SkilliXLog", "Last Name: " + lastName);
                                                                                                            Log.i("SkilliXLog", "Career: " + career);

                                                                                                            // Avoid duplication: check if this receiverId is already in the list
                                                                                                            boolean userExists = false;
                                                                                                            for (MessageModel model : messageModels) {
                                                                                                                Log.i("SkilliXLog", "Checking in list for user with ID: " + model.getId()); // Log each model id being checked
                                                                                                                if (model.getId().equals(theId)) { // Check if the id already exists
                                                                                                                    Log.i("SkilliXLog", "Found duplicate user with ID: " + theId); // Log when duplicate found
                                                                                                                    userExists = true;
                                                                                                                    break;
                                                                                                                }
                                                                                                            }

                                                                                                            // If the user doesn't exist, add them to the list
                                                                                                            if (!userExists) {
                                                                                                                Log.i("SkilliXLog", "Adding new user with ID: " + theId); // Log when adding new user
                                                                                                                messageModels.add(new MessageModel(theId, proIMG, firstName, lastName, career));
                                                                                                                messageAdapter.notifyDataSetChanged();
                                                                                                            }

                                                                                                        } else {
                                                                                                            Log.i("SkilliXLog", "No such document");
                                                                                                        }
                                                                                                    } else {
                                                                                                        Log.e("SkilliXLog", "Task failed with exception: ", task.getException());
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }

                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });


                                                }

                                            }
                                        });
                            }

                            if (loggedUserAccountType.equals("Mentee")) {
                                firestore.collection("users").whereEqualTo("account_type", "Mentor")
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {

                                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {

                                                    String mentorId = document.getId();
                                                    Log.i("SkilliXLog", "mentorsId: " + mentorId);

                                                    //make the chat id
                                                    String menteeGeneratedChatId = mentorId + "_" + loggedId;
                                                    Log.i("SkilliXLog", "menteeGeneratedChatId: " + menteeGeneratedChatId);

                                                    firestore.collection("chats").document(menteeGeneratedChatId).collection("messages")
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        QuerySnapshot messageSnapshots = task.getResult();

                                                                        for (DocumentSnapshot message : messageSnapshots) {

                                                                            String msgData = String.valueOf(message.getData());
                                                                            String senderId = message.getString("senderId");
                                                                            String receiverId = message.getString("receiverId");

                                                                            Log.i("SkilliXLog", "get message data from mentorGeneratedChatId: " + msgData);
                                                                            Log.i("SkilliXLog", "get senderId from mentorGeneratedChatId: " + senderId);
                                                                            Log.i("SkilliXLog", "get receiverId from mentorGeneratedChatId: " + receiverId);

                                                                            if (receiverId.equals(loggedId) || senderId.equals(loggedId)) {

                                                                                Log.i("SkilliXLog", "Yes Receiver Id or Sender Id equals to your logging id: " + loggedId);

                                                                                if (receiverId.equals(loggedId)) {

                                                                                    Log.i("SkilliXLog", "Yes Receiver is equals to your logging id: " + loggedId);

                                                                                    firestore.collection("users")
                                                                                            .document(senderId)
                                                                                            .get()
                                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        DocumentSnapshot mentorSnapshot = task.getResult();

                                                                                                        if (mentorSnapshot.exists()) {
                                                                                                            // Get the details from Firestore
                                                                                                            String theId = senderId; // Use the receiverId directly
                                                                                                            String proIMG = String.valueOf(mentorSnapshot.get("profile_image"));
                                                                                                            String firstName = mentorSnapshot.getString("first_name");
                                                                                                            String lastName = mentorSnapshot.getString("last_name");
                                                                                                            String career = mentorSnapshot.getString("career");

                                                                                                            Log.i("SkilliXLog", "Checking user with ID: " + theId); // Log user ID
                                                                                                            Log.i("SkilliXLog", "First Name: " + firstName);
                                                                                                            Log.i("SkilliXLog", "Last Name: " + lastName);
                                                                                                            Log.i("SkilliXLog", "Career: " + career);

                                                                                                            // Avoid duplication: check if this receiverId is already in the list
                                                                                                            boolean userExists = false;
                                                                                                            for (MessageModel model : messageModels) {
                                                                                                                Log.i("SkilliXLog", "Checking in list for user with ID: " + model.getId()); // Log each model id being checked
                                                                                                                if (model.getId().equals(theId)) { // Check if the id already exists
                                                                                                                    Log.i("SkilliXLog", "Found duplicate user with ID: " + theId); // Log when duplicate found
                                                                                                                    userExists = true;
                                                                                                                    break;
                                                                                                                }
                                                                                                            }


                                                                                                            if (!userExists) {
                                                                                                                // Check if firstName and lastName are not null before calling toLowerCase()
                                                                                                                if ((firstName != null && firstName.toLowerCase().contains(searchQuery)) ||
                                                                                                                        (lastName != null && lastName.toLowerCase().contains(searchQuery))) {
                                                                                                                    Log.i("SkilliXLog", "Adding new user with ID: " + theId); // Log when adding new user
                                                                                                                    messageModels.add(new MessageModel(theId, proIMG, firstName, lastName, career));
                                                                                                                    messageAdapter.notifyDataSetChanged();
                                                                                                                }
                                                                                                            }

                                                                                                        } else {
                                                                                                            Log.i("SkilliXLog", "No such document");
                                                                                                        }
                                                                                                    } else {
                                                                                                        Log.e("SkilliXLog", "Task failed with exception: ", task.getException());
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }

                                                                                if (senderId.equals(loggedId)) {

                                                                                    Log.i("SkilliXLog", "Yes Sender Id is equals to your logging id: " + loggedId);

                                                                                    firestore.collection("users")
                                                                                            .document(receiverId)
                                                                                            .get()
                                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        DocumentSnapshot mentorSnapshot = task.getResult();

                                                                                                        if (mentorSnapshot.exists()) {
                                                                                                            // Get the details from Firestore
                                                                                                            String theId = receiverId; // Use the receiverId directly
                                                                                                            String proIMG = String.valueOf(mentorSnapshot.get("profile_image"));
                                                                                                            String firstName = mentorSnapshot.getString("first_name");
                                                                                                            String lastName = mentorSnapshot.getString("last_name");
                                                                                                            String career = mentorSnapshot.getString("career");

                                                                                                            Log.i("SkilliXLog", "Checking user with ID: " + theId); // Log user ID
                                                                                                            Log.i("SkilliXLog", "First Name: " + firstName);
                                                                                                            Log.i("SkilliXLog", "Last Name: " + lastName);
                                                                                                            Log.i("SkilliXLog", "Career: " + career);

                                                                                                            // Avoid duplication: check if this receiverId is already in the list
                                                                                                            boolean userExists = false;
                                                                                                            for (MessageModel model : messageModels) {
                                                                                                                Log.i("SkilliXLog", "Checking in list for user with ID: " + model.getId()); // Log each model id being checked
                                                                                                                if (model.getId().equals(theId)) { // Check if the id already exists
                                                                                                                    Log.i("SkilliXLog", "Found duplicate user with ID: " + theId); // Log when duplicate found
                                                                                                                    userExists = true;
                                                                                                                    break;
                                                                                                                }
                                                                                                            }

                                                                                                            // If the user doesn't exist, add them to the list
                                                                                                            if (!userExists) {
                                                                                                                // Check if firstName and lastName are not null before calling toLowerCase()
                                                                                                                if ((firstName != null && firstName.toLowerCase().contains(searchQuery)) ||
                                                                                                                        (lastName != null && lastName.toLowerCase().contains(searchQuery))) {
                                                                                                                    Log.i("SkilliXLog", "Adding new user with ID: " + theId); // Log when adding new user
                                                                                                                    messageModels.add(new MessageModel(theId, proIMG, firstName, lastName, career));
                                                                                                                    messageAdapter.notifyDataSetChanged();
                                                                                                                }
                                                                                                            }


                                                                                                        } else {
                                                                                                            Log.i("SkilliXLog", "No such document");
                                                                                                        }
                                                                                                    } else {
                                                                                                        Log.e("SkilliXLog", "Task failed with exception: ", task.getException());
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }

                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });


                                                }

                                            }
                                        });

                            }

                        }

                    }
                });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(messageAdapter);
    }


}