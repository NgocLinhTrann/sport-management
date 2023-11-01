package com.example.dangki.KhachHang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Calendar.DailyCalendarActivity;
import com.example.dangki.Dangnhapthanhcong;
import com.example.dangki.R;

public class DatLichThanhCong extends AppCompatActivity {
    static int RESULT_DATLICH_THANHCONG = 1;
    Button btn_tieptuc, btn_sankhac;
    String userID, rentalID;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.success_screen_themlichdatsan);

        FindViewByIds();

        btn_tieptuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChonDoUong.class);
                intent.putExtra("userID", userID);
                intent.putExtra("rentalID", rentalID);
                setResult(RESULT_DATLICH_THANHCONG);
                startActivity(intent);
                finish();
            }
        });

        btn_sankhac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChonSan.class);
                intent.putExtra("userID", userID);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                setResult(RESULT_DATLICH_THANHCONG);
                startActivity(intent);
                finish();
            }
        });
    }

    private void FindViewByIds() {
        btn_tieptuc = findViewById(R.id.btn_khachang_datsanthanhcong_tieptuc);
        btn_sankhac = findViewById(R.id.btn_khachang_datsanthanhcong_sankhac);

        rentalID = getIntent().getStringExtra("rentalID");
        userID = getIntent().getStringExtra("userID");
    }
}
