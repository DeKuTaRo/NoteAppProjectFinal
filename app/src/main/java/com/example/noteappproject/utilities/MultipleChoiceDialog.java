package com.example.noteappproject.utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.noteappproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultipleChoiceDialog extends DialogFragment {
    public interface IOnMultipleChoiceListener{
        void onPositiveButtonClicked(List<String> selectedLabel);
        void onNeutralButtonClicked();
    }
    private IOnMultipleChoiceListener iOnMultipleChoiceListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            this.iOnMultipleChoiceListener = (IOnMultipleChoiceListener) context;
        } catch (Exception e){
            throw new ClassCastException(getActivity().toString()+ "IOnMultipleChoiceListener must be implement !");
        }
    }

    private String[] listLabel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DatabaseSetup();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> selectedLabel = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        builder.setTitle("Note Label!")
                .setMultiChoiceItems(this.listLabel, null, (dialogInterface, i, b) -> {
                    if (b) {
                        selectedLabel.add(listLabel[i]);
                    } else {
                        selectedLabel.remove(listLabel[i]);
                    }
                }).setPositiveButton("Add", (dialogInterface, i) -> iOnMultipleChoiceListener.onPositiveButtonClicked(selectedLabel))
                .setNegativeButton("Later", (dialogInterface, i) -> dialogInterface.dismiss()).setNeutralButton("Add more label", (dialogInterface, i) -> iOnMultipleChoiceListener.onNeutralButtonClicked());

        builder.setMessage("Add labels to your note !");
        builder.setCancelable(false);

        return builder.create();
    }

    private void DatabaseSetup() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userEmail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

        if (userEmail == null){
            return;
        }

        assert userEmail != null;
        userEmail = StringUlti.getSubEmailName(userEmail);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userEmail).child("Label");

        databaseReference.get().addOnCompleteListener(task -> {
            if ( !task.isSuccessful() ){
                return;
            }

            String labelListFormat = (String) task.getResult().getValue(String.class);
            String[] availableLabel = getActivity().getResources().getStringArray(R.array.available_label);

            if ( labelListFormat == null ){
                this.listLabel = availableLabel;
                return;
            }

            String[] labelListFromDatabase = labelListFormat.split("\\|");
            String[] total = new String[labelListFromDatabase.length + availableLabel.length];

            int i = 0;
            for (String label: availableLabel){
                total[i] = label;
                i++;
            }

            for (String label: labelListFromDatabase){
                total[i] = label;
                i++;
            }

            this.listLabel = total;
        });
    }
}
