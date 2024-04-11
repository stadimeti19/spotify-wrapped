package com.example.spotify_wrapped;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spotify_wrapped.databinding.HomePageBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class HomePage extends Fragment {

    private HomePageBinding binding;
    private static final String TAG = "HomePage";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = HomePageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize firebase authentication
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // onClickListener for the login button
        binding.loginButton.setOnClickListener(v -> {
            // Handle login button click
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();
            // check if input fields are valid
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                // sign in user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity(), task -> {
                            if (task.isSuccessful()) {
                                // successful sign-in
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                // navigate to spotify wrapped page
                                if (user != null) {
                                    navigateToWrappedActivity();
                                } else {
                                    Log.w(TAG, "User is null after successful sign-in");
                                    Toast.makeText(getContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // failed sign-in
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(getContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // case where input fields are invalid
                Toast.makeText(getContext(), "Please enter email and password.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // onClickListener for the sign up button
        binding.signupButton.setOnClickListener(v -> showSignUpDialog(mAuth));
    }

    // method to show sign up dialog
    private void showSignUpDialog(FirebaseAuth mAuth) {
        AlertDialog.Builder signUpBuilder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.user_signup_dialog, null);
        signUpBuilder.setView(dialogView);
        AlertDialog dialog = signUpBuilder.create();

        // find the edit text views in the dialog
        EditText editFirstName = dialogView.findViewById(R.id.editFirstName);
        EditText editLastName = dialogView.findViewById(R.id.editLastName);
        EditText editUsername = dialogView.findViewById(R.id.editUsername);
        EditText editEmail = dialogView.findViewById(R.id.editEmailAddress);
        EditText editPassword = dialogView.findViewById(R.id.editPassword);
        Button addUserButton = dialogView.findViewById(R.id.addUserButton);
        Button cancelUserButton = dialogView.findViewById(R.id.cancelUserButton);

        // onClickListener for add user button
        addUserButton.setOnClickListener(v -> {
            String firstName = editFirstName.getText().toString();
            String lastName = editLastName.getText().toString();
            String username = editUsername.getText().toString();
            String email = editEmail.getText().toString();
            String password = editPassword.getText().toString();

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                    TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user in Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            // User creation in Firebase Authentication successful
                            // Get the current user
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            assert firebaseUser != null;

                            // create new user object
                            User newUser = new User(firstName, lastName, username, email);

                            // Add user data to cloud database
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(firstName + "_" + lastName)
                                    .set(newUser.toHashMap())
                                    .addOnSuccessListener(documentReference -> {
                                        // User data added successfully
                                        Toast.makeText(getContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error adding user data to FireStore
                                        Toast.makeText(getContext(), "Failed to add user", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // User creation failed
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // User with the same email already exists
                                Toast.makeText(getContext(), "User with this email already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                // Other errors
                                Toast.makeText(getContext(), "Failed to create user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        // onClickListener for cancel user button
        cancelUserButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // navigate to the spotify wrapped page
    private void navigateToWrappedActivity() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_HomePage_to_WrappedActivity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}