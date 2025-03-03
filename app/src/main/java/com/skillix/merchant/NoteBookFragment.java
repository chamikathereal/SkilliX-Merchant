package com.skillix.merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.skillix.merchant.model.NoteDatabaseHelper;
import com.skillix.merchant.utils.ToastUtils;

public class NoteBookFragment extends Fragment {

    private ImageView saveButtonImageView;
    private EditText notePadEditText;
    private NoteDatabaseHelper noteDatabaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_note_book, container, false);


        notePadEditText = view.findViewById(R.id.notePadEditText);
        saveButtonImageView = view.findViewById(R.id.saveButtonImageView);


        noteDatabaseHelper = new NoteDatabaseHelper(getContext());


        final String savedNote = noteDatabaseHelper.getNote();
        if (savedNote != null) {
            notePadEditText.setText(savedNote);
        }

        saveButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String noteText = notePadEditText.getText().toString();


                if (savedNote == null) {

                    long result = noteDatabaseHelper.insertNote(noteText);
                    if (result != -1) {

                        if (noteText.isEmpty()) {
                            ToastUtils.showWarningToast(getActivity(), "Note saved as empty!");
                        } else {
                            ToastUtils.showSuccessToast(getActivity(), "Note saved!!");
                        }
                    } else {
                        ToastUtils.showErrorToast(getActivity(), "Error saving note!");
                    }
                } else {

                    int rowsAffected = noteDatabaseHelper.updateNote(noteText);
                    if (rowsAffected > 0) {

                        if (noteText.isEmpty()) {
                            ToastUtils.showWarningToast(getActivity(), "Note updated as empty!");
                        } else {
                            ToastUtils.showSuccessToast(getActivity(), "Note updated!");
                        }
                    } else {
                        ToastUtils.showErrorToast(getActivity(), "Error updating note!");
                    }
                }
            }
        });

        return view;
    }
}
