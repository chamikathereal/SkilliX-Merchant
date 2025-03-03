package com.skillix.merchant;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.adapter.ChatAdapter;
import com.skillix.merchant.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;
import com.skillix.merchant.utils.ToastUtils;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ChatFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<Message> messages = new ArrayList<>();
    private String getUserId;
    private String adminId;
    private ShapeableImageView parternProfileIcon;
    private TextView partnerNameTextView;

    public ChatFragment(String getUserId) {
        this.getUserId = getUserId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        adminId = auth.getCurrentUser().getUid();

        partnerNameTextView = rootView.findViewById(R.id.partnerNameTextView);
        parternProfileIcon = rootView.findViewById(R.id.parternProfileIcon);

        recyclerView = rootView.findViewById(R.id.recyclerViewMessages);
        etMessage = rootView.findViewById(R.id.etMessage);
        btnSend = rootView.findViewById(R.id.btnSend);

        Log.i("SkilliXLog","menteeID" + getUserId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(messages, getUserId);
        recyclerView.setAdapter(chatAdapter);


        btnSend.setOnClickListener(v -> sendMessage());

        loadChatMessages();

        //loaduser
        firestore.collection("users").document(getUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()){

                            DocumentSnapshot documentSnapshot = task.getResult();

                            if (documentSnapshot.exists()) {

                                String imageUrl = documentSnapshot.getString("profile_image");
                                if (imageUrl != null) {
                                    Uri mentorImg = Uri.parse(imageUrl);
                                    Picasso.get()
                                            .load(mentorImg)
                                            .placeholder(R.drawable.profile)
                                            //.error(R.drawable.error_image)
                                            .into(parternProfileIcon);
                                }


                                partnerNameTextView.setText(documentSnapshot.getString("first_name") + " " +documentSnapshot.getString("last_name") );
                            }

                        }

                    }
                });

        return rootView;
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString();
        if (!messageText.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            Message newMessage = new Message(adminId, getUserId, messageText, timestamp);
            messages.add(newMessage);
            insertMessageToFirestore(newMessage);
            chatAdapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
            etMessage.setText("");
        }
    }


    private void insertMessageToFirestore(Message message) {

        String chatId = generateChatId(adminId, getUserId);
        Log.i("Firestore", "Message chat id: " + chatId);
        Log.i("Firestore", "Message chat id: " + adminId);


        CollectionReference messagesRef = firestore.collection("chats").document(chatId).collection("messages");


        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", message.getSenderId());
        messageData.put("receiverId", message.getReceiverId());
        messageData.put("message", message.getMessage());
        messageData.put("timestamp", message.getTimestamp());
        Log.i("Firestore", "message.getSenderId(): " + message.getSenderId());


        messagesRef.add(messageData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i("Firestore", "Message sent successfully: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (getActivity() != null) {
                            //Toast.makeText(getActivity(), "Please Try Again!", Toast.LENGTH_SHORT).show();
                            ToastUtils.showErrorToast(getActivity(), "Please Try Again!");
                        }
                        Log.e("Firestore", "Failed to send message", e);
                    }
                });
    }


    private String generateChatId(String senderId, String receiverId) {
        if (senderId.compareTo(receiverId) < 0) {
            return senderId + "_" + receiverId;
        } else {
            return receiverId + "_" + senderId;
        }
    }


    private void loadChatMessages() {

        String chatId = generateChatId(adminId,getUserId);
        CollectionReference messagesRef = firestore.collection("chats").document(chatId).collection("messages");
        Query query = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING);


        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("Firestore", "Error fetching chat messages: ", e);
                    return;
                }


                messages.clear();


                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String senderId = doc.getString("senderId");
                        String receiverId = doc.getString("receiverId");
                        String messageText = doc.getString("message");
                        long timestamp = doc.getLong("timestamp");

                        Message message = new Message(senderId, receiverId, messageText, timestamp);
                        messages.add(message);
                    }


                    chatAdapter.notifyDataSetChanged();


                    recyclerView.scrollToPosition(messages.size() - 1);
                }
            }
        });
    }
}