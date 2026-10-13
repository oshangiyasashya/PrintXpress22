package com.printxpress.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.printxpress.R;
import com.printxpress.data.entity.User;
import com.printxpress.data.repository.UserRepository;
import com.printxpress.util.SessionManager;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        userRepository = new UserRepository(getApplication());
        sessionManager = new SessionManager(this);

        etEmail = findViewById(R.id.etAdminEmail);
        etPassword = findViewById(R.id.etAdminPassword);
        btnLogin = findViewById(R.id.btnAdminLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid admin email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

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
