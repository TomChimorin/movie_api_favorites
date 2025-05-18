package com.example.assignment3.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment3.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.registerButton.setOnClickListener(v -> registerUser());
        binding.loginRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String email = binding.registerEmail.getText().toString().trim();
        String password = binding.registerPassword.getText().toString().trim();
        String confirmPassword = binding.registerConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.registerEmail.setError("Email cannot be empty");
            return;
        }
        if (password.isEmpty()) {
            binding.registerPassword.setError("Password cannot be empty");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.registerConfirmPassword.setError("Passwords must match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}