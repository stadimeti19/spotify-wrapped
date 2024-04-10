package com.example.spotify_wrapped;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsPage extends Fragment {

    private TextView currentUsername;
    private TextView currentEmail;
    private TextView currentPassword;
    private Button editEmailButton;
    private Button editPasswordButton;
    private Button deleteAccountButton;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_page, container, false);

        currentUsername = view.findViewById(R.id.currentUsername);
        currentEmail = view.findViewById(R.id.currentEmail);
        currentPassword = view.findViewById(R.id.currentPassword);
        editEmailButton = view.findViewById(R.id.editEmailButton);
        editPasswordButton = view.findViewById(R.id.editPasswordButton);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        mAuth = FirebaseAuth.getInstance();

        // Set current user info
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUsername.setText(currentUser.getDisplayName());
            currentEmail.setText(currentUser.getEmail());
        }

        editEmailButton.setOnClickListener(v -> {
            // Handle edit email button click
            // Show dialog to edit email
            showEditEmailDialog();
        });

        editPasswordButton.setOnClickListener(v -> {
            // Handle edit password button click
            // Show dialog to edit password
            showEditPasswordDialog();
        });

        deleteAccountButton.setOnClickListener(v -> {
            // Handle delete account button click
            deleteAccount();
        });

        return view;
    }

    private void showEditEmailDialog() {
        // Implement dialog to edit email here
        // For example, use AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Add your dialog layout and logic here
        // ...
    }

    private void showEditPasswordDialog() {
        // Implement dialog to edit password here
        // For example, use AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Add your dialog layout and logic here
        // ...
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            navigateToHomePage();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // navigate to home page
    private void navigateToHomePage() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_SettingsPage_to_HomePage);
    }
}
