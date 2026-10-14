package com.printxpress.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.printxpress.R;
import com.printxpress.data.entity.User;
import com.printxpress.data.repository.UserRepository;
import com.printxpress.util.SessionManager;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private ImageView ivMascot;
    private View cardAdminLoginContent;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        userRepository = new UserRepository(getApplication());
        sessionManager = new SessionManager(this);

        tilEmail = findViewById(R.id.tilAdminEmail);
        tilPassword = findViewById(R.id.tilAdminPassword);
        etEmail = findViewById(R.id.etAdminEmail);
        etPassword = findViewById(R.id.etAdminPassword);
        btnLogin = findViewById(R.id.btnAdminLogin);
        ivMascot = findViewById(R.id.ivAdminMascot);
        cardAdminLoginContent = findViewById(R.id.cardAdminLoginContent);

        // Slide-up entrance animation
        if (cardAdminLoginContent != null) {
            cardAdminLoginContent.setTranslationY(150f);
            cardAdminLoginContent.setAlpha(0f);
            cardAdminLoginContent.animate().translationY(0f).alpha(1f).setDuration(500).start();
        }

        // Mascot Eye-Covering Swap on Password Focus
        if (etPassword != null && ivMascot != null) {
            etPassword.setOnFocusChangeListener((v, hasFocus) ->
                ivMascot.setImageResource(hasFocus ? R.drawable.ic_mascot_shy : R.drawable.ic_mascot_normal)
            );
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPassword != null) tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEmail != null) tilEmail.setError("Enter valid admin email address");
            else etEmail.setError("Enter valid admin email address");
            valid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            if (tilPassword != null) tilPassword.setError("Password must be at least 6 characters");
            else etPassword.setError("Password must be at least 6 characters");
            valid = false;
        }

        if (!valid) return;

        userRepository.authenticateUser(email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if ("SUPER_ADMIN".equalsIgnoreCase(user.role) || "STAFF".equalsIgnoreCase(user.role)) {
                        sessionManager.createLoginSession(user.id, user.role, user.email, user.fullName);
                        Toast.makeText(AdminLoginActivity.this, "Welcome Admin, " + user.fullName, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(AdminLoginActivity.this, "Access denied: Not an administrator account", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(AdminLoginActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
