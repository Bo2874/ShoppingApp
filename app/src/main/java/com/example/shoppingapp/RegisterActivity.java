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

    private TextInputLayout tilFullName, tilPhone, tilUsername, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etPhone, etUsername, etPassword, etConfirmPassword;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);

        tilFullName = findViewById(R.id.tilFullName);
        tilPhone = findViewById(R.id.tilPhone);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvLoginLink);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Clear previous errors
        tilFullName.setError(null);
        tilPhone.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean valid = true;

        if (fullName.isEmpty()) {
            tilFullName.setError("Vui lòng nhập họ và tên");
            valid = false;
        }

        if (phone.length() < 10) {
            tilPhone.setError("Số điện thoại tối thiểu 10 ký tự");
            valid = false;
        }

        if (username.length() < 3) {
            tilUsername.setError("Tên đăng nhập tối thiểu 3 ký tự");
            valid = false;
        }

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            valid = false;
        }

        if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            valid = false;
        }

        if (!valid) return;

        AppDatabase.databaseExecutor.execute(() -> {
            User existing = db.userDao().getUserByUsername(username);
            if (existing != null) {
                runOnUiThread(() -> tilUsername.setError("Tên đăng nhập đã tồn tại"));
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
