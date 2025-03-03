package com.skillix.merchant;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.skillix.merchant.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MentorServicesDialogFragment extends DialogFragment {

    private Spinner servicePackageSpinner;
    private EditText serviceDescriptionEditText, servicePriceEditText;
    private FirebaseFirestore firestore;
    ArrayList<String> servicePackageArrayList;

    private FirebaseAuth auth;
    private String userId;
    private String serviceId;
    AlertDialog dialog;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_mentor_services_dialog, null);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        servicePackageSpinner = dialogView.findViewById(R.id.servicePackageSpinner);
        serviceDescriptionEditText = dialogView.findViewById(R.id.serviceDescriptionEditText);
        servicePriceEditText = dialogView.findViewById(R.id.servicePriceEditText);


        if (getArguments() != null) {
            serviceId = getArguments().getString("serviceId");
        }

        if (serviceId != null) {
            fetchServiceData(serviceId);
        }

        lordPackageNames();

        builder.setView(dialogView)
                .setPositiveButton(R.string.dialog_save_text, null) // Remove default behavior
                .setNegativeButton(R.string.dialog_cancel_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();  // Close the dialog if canceled
                    }
                });


        dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (serviceId != null) {
                            updateService();
                        } else {
                            addService();
                        }

                    }
                });
            }
        });

        return dialog;
    }

    private void lordPackageNames() {
        servicePackageArrayList = new ArrayList<>();
        servicePackageArrayList.add("Select Package"); // Ensure the default option exists

        firestore.collection("service_package").orderBy("name").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            servicePackageArrayList.clear();
                            servicePackageArrayList.add("Select Package");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                servicePackageArrayList.add(document.getString("name"));
                            }

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                    requireContext(),
                                    R.layout.country_spinner_item,
                                    servicePackageArrayList
                            );

                            servicePackageSpinner.setAdapter(arrayAdapter);

                            // Load user's career only if serviceId is not null
                            if (serviceId != null) {
                                firestore.collection("service_price").document(serviceId).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                                                String name = documentSnapshot.getString("name");
                                                int index = servicePackageArrayList.indexOf(name);
                                                if (index != -1) {
                                                    servicePackageSpinner.setSelection(index);
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d("FirestoreCountrLog", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private void addService() {

        String packageName = servicePackageSpinner.getSelectedItem().toString();
        String packageDescription = serviceDescriptionEditText.getText().toString();
        String packagePrice = servicePriceEditText.getText().toString();

        if (packageName.equals("Select Package")) {
            //Toast.makeText(getContext(), "Please select a valid package.", Toast.LENGTH_SHORT).show();
            ToastUtils.showWarningToast(getActivity(), "Please select a valid package!");
            return; // Exit the method if validation fails
        }

        if (packageDescription.isEmpty()) {
            //Toast.makeText(getContext(), "Please enter a description.", Toast.LENGTH_SHORT).show();
            ToastUtils.showWarningToast(getActivity(), "Please enter a description!");
            serviceDescriptionEditText.requestFocus();
            return;
        }

        if (packagePrice.isEmpty()) {
            //Toast.makeText(getContext(), "Please enter a price.", Toast.LENGTH_SHORT).show();
            ToastUtils.showWarningToast(getActivity(), "Please enter a price!");
            servicePriceEditText.requestFocus();
            return;
        }

        Log.i("SkilliXLog", "packageName " + packageName);
        Log.i("SkilliXLog", "packageDescription " + packageDescription);
        Log.i("SkilliXLog", "packagePrice " + packagePrice);

        Map<String, Object> addServiceDocument = new HashMap<>();
        addServiceDocument.put("name", packageName);
        addServiceDocument.put("description", packageDescription);
        addServiceDocument.put("price", packagePrice);
        addServiceDocument.put("mentor_id", userId);

        firestore.collection("service_price").add(addServiceDocument)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(getContext(), "Service successfully added!", Toast.LENGTH_SHORT).show();
                                ToastUtils.showSuccessToast(getActivity(), "Service successfully added!");
                                Log.i("SkilliXLog", "Service successfully added!");
                                dialog.dismiss(); // Close dialog only when operation is complete
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragmentContainerView1, new MentorServicesFragment(), null);
                                //fragmentTransaction.addToBackStack(null); // Optional: Enables back navigation
                                fragmentTransaction.commit();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Update UI after failure, ensure it's on the main thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(getContext(), "Service adding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                ToastUtils.showErrorToast(getActivity(), "Service adding error!");
                                Log.i("SkilliXLog", "Service adding error: " + e.getMessage());
                            }
                        });
                    }
                });
    }

    private void updateService() {


        String packageName = servicePackageSpinner.getSelectedItem().toString();
        String packageDescription = serviceDescriptionEditText.getText().toString();
        String packagePrice = servicePriceEditText.getText().toString();


        if (packageName.equals("Select Package")) {
            //Toast.makeText(getContext(), "Please select a valid package.", Toast.LENGTH_SHORT).show();
            ToastUtils.showWarningToast(getActivity(), "Please select a valid package!");
            return;
        }

        if (packageDescription.isEmpty()) {
            //Toast.makeText(getContext(), "Please enter a description.", Toast.LENGTH_SHORT).show();
            ToastUtils.showWarningToast(getActivity(), "Please enter a description!");
            serviceDescriptionEditText.requestFocus();
            return;
        }

        if (packagePrice.isEmpty()) {
            //Toast.makeText(getContext(), "Please enter a price.", Toast.LENGTH_SHORT).show();
            ToastUtils.showWarningToast(getActivity(), "Please enter a price!");
            servicePriceEditText.requestFocus();
            return;
        }

        Log.i("SkilliXLog", "packageName " + packageName);
        Log.i("SkilliXLog", "packageDescription " + packageDescription);
        Log.i("SkilliXLog", "packagePrice " + packagePrice);

        Map<String, Object> updatedServiceDocument = new HashMap<>();
        updatedServiceDocument.put("name", packageName);
        updatedServiceDocument.put("description", packageDescription);
        updatedServiceDocument.put("price", packagePrice);

        firestore.collection("service_price").document(serviceId)
                .update(updatedServiceDocument)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(getContext(), "Service successfully updated!", Toast.LENGTH_SHORT).show();
                                ToastUtils.showSuccessToast(getActivity(), "Service successfully updated!");
                                Log.i("SkilliXLog", "Service successfully updated!");
                                dialog.dismiss(); // Close dialog only when operation is complete
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragmentContainerView1, new MentorServicesFragment(), null);
                                //fragmentTransaction.addToBackStack(null); // Optional: Enables back navigation
                                fragmentTransaction.commit();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Update UI after failure, ensure it's on the main thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(getContext(), "Service update error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                ToastUtils.showErrorToast(getActivity(), "Service update error!");
                                Log.i("SkilliXLog", "Service update error: " + e.getMessage());
                            }
                        });
                    }
                });
    }


    private void fetchServiceData(String serviceId) {
        firestore.collection("service_price").document(serviceId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String packageName = document.getString("name");
                                String packageDescription = document.getString("description");
                                String packagePrice = document.getString("price");


                                serviceDescriptionEditText.setText(packageDescription);
                                servicePriceEditText.setText(packagePrice);

                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) servicePackageSpinner.getAdapter();
                                if (adapter != null) {
                                    int position = adapter.getPosition(packageName);
                                    servicePackageSpinner.setSelection(position);
                                }

                            } else {
                                Log.d("FirestoreLog", "No such document");
                            }
                        } else {
                            Log.d("FirestoreLog", "Error getting document: ", task.getException());
                        }
                    }
                });
    }

}

