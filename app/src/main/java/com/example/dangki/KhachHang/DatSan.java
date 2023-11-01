package com.example.dangki.KhachHang;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.R;

public class DatSan extends AppCompatActivity {
    FrameLayout f1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_datsan);

        f1 = findViewById(R.id.frame1);

        f1.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ChiTietSan.class));
            finish();
        });
    }
}
