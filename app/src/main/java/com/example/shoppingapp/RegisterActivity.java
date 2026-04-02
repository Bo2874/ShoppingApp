package com.example.shoppingapp;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etUsername, etPassword, etConfirmPassword;
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilUsername, tilPassword, tilConfirmPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

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

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void doRegister() {
        clearErrors();

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean hasError = false;

        // Full name
        if (fullName.isEmpty()) {
            tilFullName.setError("Vui lòng nhập họ tên");
            hasError = true;
        } else if (fullName.length() < 2) {
            tilFullName.setError("Họ tên phải có ít nhất 2 ký tự");
            hasError = true;
        }

        // Email
        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ (ví dụ: abc@gmail.com)");
            hasError = true;
        }

        // Phone
        if (phone.isEmpty()) {
            tilPhone.setError("Vui lòng nhập số điện thoại");
            hasError = true;
        } else if (!phone.matches("^0\\d{9,10}$")) {
            tilPhone.setError("SĐT phải bắt đầu bằng 0, gồm 10-11 chữ số");
            hasError = true;
        }

        // Username
        if (username.isEmpty()) {
            tilUsername.setError("Vui lòng nhập tên đăng nhập");
            hasError = true;
        } else if (username.length() < 3) {
            tilUsername.setError("Tên đăng nhập phải có ít nhất 3 ký tự");
            hasError = true;
        } else if (!username.matches("[a-zA-Z0-9]+")) {
            tilUsername.setError("Chỉ được dùng chữ cái và số, không có khoảng trắng");
            hasError = true;
        }

        // Password
        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            hasError = true;
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            hasError = true;
        }

        // Confirm password
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            hasError = true;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            hasError = true;
        }

        if (hasError) return;

        AppDatabase.databaseExecutor.execute(() -> {
            User existing = db.userDao().getUserByUsername(username);
            if (existing != null) {
                runOnUiThread(() -> tilUsername.setError("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác"));
                return;
            }

            User user = new User(username, password, fullName, phone);
            user.setEmail(email);
            db.userDao().insert(user);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
