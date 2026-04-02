package com.example.shoppingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppingapp.database.AppDatabase;
import com.example.shoppingapp.database.entity.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etPassword, etConfirmPassword, etPhone;
    private TextInputLayout tilFullName, tilUsername, tilPassword, tilConfirmPassword, tilPhone;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);

        // TextInputLayouts
        tilFullName = findViewById(R.id.tilFullName);
        tilPhone = findViewById(R.id.tilPhone);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        // TextInputEditTexts
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        btnRegister.setOnClickListener(v -> doRegister());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean hasError = false;

        if (fullName.isEmpty()) {
            tilFullName.setError("Vui lòng nhập họ tên");
            hasError = true;
        } else {
            tilFullName.setError(null);
        }

        if (phone.length() < 10) {
            tilPhone.setError("Số điện thoại không hợp lệ");
            hasError = true;
        } else {
            tilPhone.setError(null);
        }

        if (username.length() < 3) {
            tilUsername.setError("Tên đăng nhập quá ngắn");
            hasError = true;
        } else {
            tilUsername.setError(null);
        }

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải ít nhất 6 ký tự");
            hasError = true;
        } else {
            tilPassword.setError(null);
        }

        if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            hasError = true;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (hasError) return;

        AppDatabase.databaseExecutor.execute(() -> {
            User existing = db.userDao().getUserByUsername(username);
            if (existing != null) {
                runOnUiThread(() -> Toast.makeText(this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show());
                return;
            }

            User newUser = new User(username, password, fullName, phone);
            db.userDao().insert(newUser);

            runOnUiThread(() -> {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
