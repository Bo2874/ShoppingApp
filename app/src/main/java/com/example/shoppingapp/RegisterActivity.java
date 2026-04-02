package com.example.shoppingapp;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etUsername, etPassword, etConfirmPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        TextView btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> doRegister());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean hasError = false;

        // Full name validation
        if (fullName.length() < 2) {
            etFullName.setError("Họ tên phải có ít nhất 2 ký tự");
            hasError = true;
        }

        // Email validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            hasError = true;
        }

        // Phone validation
        if (!phone.startsWith("0") || phone.length() < 10 || phone.length() > 11 || !phone.matches("\\d+")) {
            etPhone.setError("Số điện thoại không hợp lệ");
            hasError = true;
        }

        // Username validation
        if (username.length() < 3 || !username.matches("[a-zA-Z0-9]+")) {
            etUsername.setError("Tên đăng nhập phải có ít nhất 3 ký tự");
            hasError = true;
        }

        // Password validation
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            hasError = true;
        }

        // Confirm password validation
        if (!confirmPassword.equals(password)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            hasError = true;
        }

        if (hasError) return;

        AppDatabase.databaseExecutor.execute(() -> {
            // Check if username already exists
            User existing = db.userDao().getUserByUsername(username);
            if (existing != null) {
                runOnUiThread(() -> etUsername.setError("Tên đăng nhập đã tồn tại"));
                return;
            }

            // Create user
            User user = new User(username, password, fullName, phone);
            user.setEmail(email);
            db.userDao().insert(user);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
