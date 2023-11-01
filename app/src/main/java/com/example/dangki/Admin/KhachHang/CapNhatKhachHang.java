package com.example.dangki.Admin.KhachHang;


import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Model.User;
import com.example.dangki.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class CapNhatKhachHang extends AppCompatActivity {
    EditText edt_tenkh, edt_sdt, edt_email, edt_birthdate;
    CircleImageView imv_anhkh;
    User user;
    ImageView btn_goback;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.admin_khachhang_info);
        
        FindViewByIds();
        LoadData();
        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void LoadData() {
        edt_email.setEnabled(false);
        edt_tenkh.setEnabled(false);
        edt_sdt.setEnabled(false);
        edt_birthdate.setEnabled(false);

        edt_email.setText(user.getEmail());
        edt_tenkh.setText(user.getName());
        edt_sdt.setText(user.getPhoneNumber());

        if(!user.getImg_url().isEmpty()){
            Picasso.get().load(user.getImg_url()).into(imv_anhkh);
        }
        if(user.getBirthdate() != null){
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = dateFormat.format(user.getBirthdate());
            edt_birthdate.setText(formattedDate);
        }
    }

    private void FindViewByIds() {
        edt_tenkh = findViewById(R.id.edt_admin_khachhang_info_tenkh);
        edt_sdt = findViewById(R.id.edt_admin_khachhang_info_sdt);
        edt_email = findViewById(R.id.edt_admin_khachhang_info_email);
        edt_birthdate = findViewById(R.id.edt_admin_khachhang_info_birthdate);
        imv_anhkh = findViewById(R.id.imv_admin_khachhang_info_anh);
        btn_goback = findViewById(R.id.btn_admin_khachhang_info_goback);

        user = (User) getIntent().getSerializableExtra("user");

    }
}
