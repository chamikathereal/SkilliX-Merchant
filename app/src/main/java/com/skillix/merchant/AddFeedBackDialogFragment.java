package com.skillix.merchant;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillix.merchant.utils.ToastUtils;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddFeedBackDialogFragment extends DialogFragment {

    private TextView feedbackUserName, feedbackUserCareer;
    private EditText enterFeedbackContext;
    private ShapeableImageView feedbackUserProfileImage;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userId;
    private String mentorId;
    private String feedBackContext;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_add_feed_back_dialog, null);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        if (getArguments() != null) {
            mentorId = getArguments().getString("mentor_Id");
        }

        // Ensure mentorId is not null
        if (mentorId == null) {
            Log.e("AddFeedBackDialog", "mentorId is null!");
            return builder.create();
        }

        Log.i("SkilliXLog", mentorId);

        feedbackUserName = dialogView.findViewById(R.id.feedbackUserName);
        feedbackUserCareer = dialogView.findViewById(R.id.feedbackUserCareer);
        feedbackUserProfileImage = dialogView.findViewById(R.id.feedbackUserProfileImage);

        enterFeedbackContext = dialogView.findViewById(R.id.enterFeedbackContext);

        loadMenteeDataToAddFeedBackDialogView();

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton(R.string.dialog_save_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        feedBackContext = enterFeedbackContext.getText().toString().trim();
                        Log.i("SkilliXLog", feedBackContext);
                        if (!feedBackContext.isBlank()){
                            enterMenteeFeedBack();
                            //Toast.makeText(getActivity(), "Feedback Successfully Saved!", Toast.LENGTH_SHORT).show();
                            ToastUtils.showSuccessToast(getActivity(), "Feedback Successfully Saved!");

                        }else {
                            //Toast.makeText(getActivity(), "Please Enter Your Feedback!", Toast.LENGTH_SHORT).show();
                            ToastUtils.showWarningToast(getActivity(), "Please Enter Your Feedback!");
                        }



                       // Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                        //Log.i("SkilliXLog", "Feedback Successfully Saved!");
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    private void loadMenteeDataToAddFeedBackDialogView() {
        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String userImage = document.getString("profile_image");
                                String fullName = document.getString("first_name") + " " + document.getString("last_name");
                                String career = document.getString("career");

                                // Log data to check if it's correctly fetched
                                Log.i("SkilliXLog", "User Image: " + userImage);
                                Log.i("SkilliXLog", "Full Name: " + fullName);
                                Log.i("SkilliXLog", "Career: " + career);

                                if (userImage != null && !userImage.isEmpty()) {
                                    Picasso.get()
                                            .load(userImage)
                                            .placeholder(R.drawable.profile)  // Optional: Placeholder image
                                            .into(feedbackUserProfileImage);
                                } else {
                                    feedbackUserProfileImage.setImageResource(R.drawable.profile); // Default image if null or empty
                                }

                                feedbackUserName.setText(fullName != null ? fullName : "No name available");
                                feedbackUserCareer.setText(career != null ? career : "No career info available");
                            } else {
                                Log.e("SkilliXLog", "No document found");
                            }
                        } else {
                            Log.e("SkilliXLog", "Task failed: " + task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("SkilliXLog", "Failed to load mentee data", e);
                    }
                });
    }


    private void enterMenteeFeedBack() {

        Date currentDate = Calendar.getInstance().getTime();

        // Format date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = formatter.format(currentDate);

        System.out.println("Current DateTime: " + formattedDate);


        Map<String, Object> feedbackDocument = new HashMap<>();
        feedbackDocument.put("mentee_id", userId);
        feedbackDocument.put("mentor_id", mentorId);
        feedbackDocument.put("feedback_text", feedBackContext);
        feedbackDocument.put("date_time", formattedDate);

        // Log feedback data before sending
        Log.i("Feedback", "Feedback Document: " + feedbackDocument);

        firestore.collection("feedback").add(feedbackDocument)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (getActivity() != null) {
                            //Toast.makeText(getActivity(), "Please Try Again!", Toast.LENGTH_SHORT).show();
                            ToastUtils.showErrorToast(getActivity(), "Please Try Again!");
                        }
                        Log.e("SkilliXLog", "Failed to add feedback", e);
                    }
                });
    }

}