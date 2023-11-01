package com.example.dangki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.example.dangki.Admin.KhachHang.Menu;
import com.example.dangki.Calendar.CalendarActivity;
import com.example.dangki.KhachHang.ChiTietSan;
import com.example.dangki.KhachHang.ChonDoUong;
import com.example.dangki.KhachHang.ChonSan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        startActivity(new Intent(this, Welcome.class));
        finish();
//        setContentView(R.layout.toolbar);
    }
}