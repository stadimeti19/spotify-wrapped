package com.example.spotify_wrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsPage extends AppCompatActivity {
    private EditText currentEmail;
    private EditText currentPassword;
    private Button editEmailButton;
    private Button editPasswordButton;
    private Button deleteAccountButton;
    private ImageView imageViewHome;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private static final String TAG = "SettingsPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);

        currentEmail = findViewById(R.id.currentEmail);
        currentPassword = findViewById(R.id.currentPassword);
        editEmailButton = findViewById(R.id.editEmailButton);
        editPasswordButton = findViewById(R.id.editPasswordButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        imageViewHome = findViewById(R.id.home_button);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsPage.this, startActivity.class));
            }
        });

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Set current user info
        FirebaseUser currentUser = mAuth.getCurrentUser();
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
    }

    // Called when the edit email button is clicked
    private void onEditEmailClicked() {
        FirebaseUser user = mAuth.getCurrentUser();

        // update email
        assert user != null;
        user.verifyBeforeUpdateEmail(currentEmail.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingsPage.this, "Check new email for verification", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsPage.this, "Failed to update email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Called when the edit password button is clicked
    public void onEditPasswordClicked() {
        // Update password in Firebase Authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String newPassword = currentPassword.getText().toString();
            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(SettingsPage.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SettingsPage.this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();

        // delete user
        assert user != null;
        firestore.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");

                        // Once Firestore document is deleted, delete the user account
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsPage.this, "User account deleted.", Toast.LENGTH_SHORT).show();
                                            // Finish this activity and return to the previous one
                                            startActivity(new Intent(SettingsPage.this, MainActivity.class));
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

}
