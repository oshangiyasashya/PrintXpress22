package com.printxpress.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.textfield.TextInputLayout;
import com.printxpress.R;

public class RegisterFragment extends Fragment {

    private EditText etFullName, etEmail, etPhone, etPassword;
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ImageView ivMascot;
    private View cardRegisterContent;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        tilFullName = view.findViewById(R.id.tilFullName);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPhone = view.findViewById(R.id.tilPhone);
        tilPassword = view.findViewById(R.id.tilPassword);

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvLogin = view.findViewById(R.id.tvLogin);
        ivMascot = view.findViewById(R.id.ivRegisterMascot);
        cardRegisterContent = view.findViewById(R.id.cardRegisterContent);

        // Entrance animation
        if (cardRegisterContent != null) {
            cardRegisterContent.setTranslationY(150f);
            cardRegisterContent.setAlpha(0f);
            cardRegisterContent.animate().translationY(0f).alpha(1f).setDuration(500).start();
        }

        // Mascot Eye-Covering Swap on Password Focus
        if (etPassword != null && ivMascot != null) {
            etPassword.setOnFocusChangeListener((v, hasFocus) ->
                ivMascot.setImageResource(hasFocus ? R.drawable.ic_mascot_shy : R.drawable.ic_mascot_normal)
            );
        }

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getAuthSuccessUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Toast.makeText(getContext(), "Account created successfully! Welcome, " + user.fullName, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.catalogFragment);
            }
        });

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            btnRegister.setEnabled(!loading);
            btnRegister.setText(loading ? "Creating account..." : "Create Account");
        });

        btnRegister.setOnClickListener(v -> validateAndRegister());
        tvLogin.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.loginFragment));
    }

    private void validateAndRegister() {
        if (tilFullName != null) tilFullName.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPhone != null) tilPhone.setError(null);
        if (tilPassword != null) tilPassword.setError(null);

        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(name) || name.length() < 2) {
            if (tilFullName != null) tilFullName.setError("Enter full name");
            else etFullName.setError("Enter full name");
            valid = false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEmail != null) tilEmail.setError("Enter valid email address");
            else etEmail.setError("Enter valid email address");
            valid = false;
        }

        if (TextUtils.isEmpty(phone) || !phone.matches("^(\\+94|0)?7[0-9]{8}$")) {
            if (tilPhone != null) tilPhone.setError("Enter valid phone (e.g. 0712345678)");
            else etPhone.setError("Enter valid phone");
            valid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            if (tilPassword != null) tilPassword.setError("Password must be at least 6 characters");
            else etPassword.setError("Password must be at least 6 characters");
            valid = false;
        }

        if (valid) {
            authViewModel.register(name, email, phone, password, password);
        }
    }
}
