package com.example.habittracker.activities.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.habittracker.DatabaseManager;
import com.example.habittracker.R;
import com.example.habittracker.utils.SharedInfo;
import com.example.habittracker.utils.StringCallback;

/**
 * HabitInputFragment prompts the user to enter details about a Habit.
 */
public class SharingInputFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // get the fragment view
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_sharing_input, null);
        Button sendButton = view.findViewById(R.id.sendButton);
        EditText inputName = view.findViewById(R.id.sharing_input_edit_text);

        // build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = builder
                .setView(view)
                .setTitle("")
                .setNegativeButton("Cancel", null)
                .create();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the text entered by the user
                String followUserid = inputName.getText().toString();
                if (followUserid.length() == 0) {
                    Toast.makeText(getContext(), "Username field cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(), "Processing your request!", Toast.LENGTH_SHORT).show();
                DatabaseManager.get().sendFollowRequest(
                        SharedInfo.getInstance().getCurrentUser().getUsername(),
                        followUserid,
                        new StringCallback() {
                            @Override
                            public void onCallbackSuccess(String msg) {
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }

                            @Override
                            public void onCallbackFailure(String reason) {
                                Toast.makeText(getContext(), reason, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });
        return alertDialog;
    }
}