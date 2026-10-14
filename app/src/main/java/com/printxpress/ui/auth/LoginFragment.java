package com.printxpress.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.printxpress.R;
import com.printxpress.ui.admin.AdminDashboardActivity;
import com.printxpress.ui.admin.AdminLoginActivity;
import com.printxpress.util.SessionManager;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private MaterialButton btnAdminPortal;
    private ImageView ivMascot;
    private View cardLoginContent;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegister = view.findViewById(R.id.tvRegister);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        btnAdminPortal = view.findViewById(R.id.btnAdminPortal);
        ivMascot = view.findViewById(R.id.ivMascot);
        cardLoginContent = view.findViewById(R.id.cardLoginContent);

        // Entrance animation
        if (cardLoginContent != null) {
            cardLoginContent.setTranslationY(150f);
            cardLoginContent.setAlpha(0f);
            cardLoginContent.animate().translationY(0f).alpha(1f).setDuration(500).start();
        }

        // Interactive Mascot Eye-Covering Swap on Password Focus
        if (etPassword != null && ivMascot != null) {
            etPassword.setOnFocusChangeListener((v, hasFocus) -> {
                ivMascot.setImageResource(hasFocus ? R.drawable.ic_mascot_shy : R.drawable.ic_mascot_normal);
            });
        }

        // Pre-fill remembered email
        SessionManager session = new SessionManager(requireContext());
        String remembered = session.getRememberedEmail();
        if (remembered != null && !remembered.isEmpty()) {
            etEmail.setText(remembered);
        }

        // Observe ViewModel states
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getAuthSuccessUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                session.saveRememberedEmail(user.email);
                if ("SUPER_ADMIN".equalsIgnoreCase(user.role) || "STAFF".equalsIgnoreCase(user.role)) {
                    Toast.makeText(getContext(), "Welcome Admin, " + user.fullName, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireContext(), AdminDashboardActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Welcome back, " + user.fullName + "!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.catalogFragment);
                }
            }
        });

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            btnLogin.setEnabled(!loading);
            btnLogin.setText(loading ? "Signing in..." : "Login");
        });

        btnLogin.setOnClickListener(v -> validateAndLogin());

        tvRegister.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.registerFragment));
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        if (btnAdminPortal != null) {
            btnAdminPortal.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AdminLoginActivity.class);
                startActivity(intent);
            });
        }
    }

    private void validateAndLogin() {
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPassword != null) tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEmail != null) tilEmail.setError("Enter a valid email address");
            else etEmail.setError("Enter a valid email address");
            valid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            if (tilPassword != null) tilPassword.setError("Password must be at least 6 characters");
            else etPassword.setError("Password must be at least 6 characters");
            valid = false;
        }

        if (valid) {
            authViewModel.login(email, password);
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reset Password");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputEmail = new EditText(requireContext());
        inputEmail.setHint("Registered Email");
        inputEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(inputEmail);

        final EditText inputNewPass = new EditText(requireContext());
        inputNewPass.setHint("New Password (min 6 chars, letters & numbers)");
        inputNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputNewPass);

        final EditText inputConfirmPass = new EditText(requireContext());
        inputConfirmPass.setHint("Confirm New Password");
        inputConfirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputConfirmPass);

        builder.setView(layout);

        builder.setPositiveButton("Reset Password", (dialog, which) -> {
            String email = inputEmail.getText().toString();
            String newPass = inputNewPass.getText().toString();
            String confirmPass = inputConfirmPass.getText().toString();
            authViewModel.resetPassword(email, newPass, confirmPass);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
