package com.example.spotify_wrapped;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsPage extends Fragment {
    private EditText currentEmail;
    private EditText currentPassword;
    private Button editEmailButton;
    private Button editPasswordButton;
    private Button deleteAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_page, container, false);

        currentEmail = view.findViewById(R.id.currentEmail);
        currentPassword = view.findViewById(R.id.currentPassword);
        editEmailButton = view.findViewById(R.id.editEmailButton);
        editPasswordButton = view.findViewById(R.id.editPasswordButton);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Set current user info
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Get email and password from Firebase Authentication
            String currEmail = currentUser.getEmail();
            String currPassword = "********"; // Firebase doesn't expose passwords for security reasons

            // Populate EditText fields with current email and password
            currentEmail.setText(currEmail);
            currentPassword.setText(currPassword);
        }

        editEmailButton.setOnClickListener(v -> onEditEmailClicked());
        editPasswordButton.setOnClickListener(v -> onEditPasswordClicked());
        deleteAccountButton.setOnClickListener(v -> deleteAccount());

        return view;
    }

    // Called when the edit email button is clicked
    public void onEditEmailClicked() {
        // Update email in Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String newEmail = currentEmail.getText().toString();
            currentUser.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed to update email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Called when the edit password button is clicked
    public void onEditPasswordClicked() {
        // Update password in Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String newPassword = currentPassword.getText().toString();
            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            // Delete user from Firebase Authentication
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // User deleted from Authentication, now delete from Firestore
                    firestore.collection("users").document(uid).delete()
                            .addOnSuccessListener(aVoid -> {
                                // User deleted from Firestore successfully
                                Toast.makeText(SettingsPage.this.getActivity(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                navigateToHomePage();
                            })
                            .addOnFailureListener(e -> {
                                // Failed to delete user from Firestore
                                Toast.makeText(SettingsPage.this.getActivity(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Failed to delete user from Authentication
                    Toast.makeText(SettingsPage.this.getActivity(), "Failed to delete account", Toast.LENGTH_SHORT).show();
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
