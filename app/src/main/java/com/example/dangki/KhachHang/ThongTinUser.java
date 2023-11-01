package com.example.dangki.KhachHang;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.dangki.Login;
import com.example.dangki.Model.User;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ThongTinUser extends AppCompatActivity {
    static int REQUEST_CODE_CAPNHAT = 1;
    Button btn_logout;
    BottomNavigationView bottomNavigationView;
    String userID;
    CircleImageView imv_user;
    TextView tv_ten, tv_email;
    CardView btn_doimk, btn_chinhsua;
    User user;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_userinfo);

        FindViewByIds();
        LoadData();
        BottomNavigation();

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ThongTinUser.this);

                builder.setTitle("Xác nhận");
                builder.setMessage("Xác nhận đăng xuất?");
                builder.setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }
                });
                builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        imv_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(ThongTinUser.this, android.R.style
                        .Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.dialog_fullscreen_image);

                ImageView imageViewFullscreen = dialog.findViewById(R.id.imageView_fullscreen);
                if (!TextUtils.isEmpty(user.getImg_url())) {
                    Picasso.get().load(user.getImg_url()).into(imageViewFullscreen);
                } else {
                    Picasso.get().load(R.drawable.profile).into(imageViewFullscreen);
                }

                dialog.show();
            }
        });
        btn_doimk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DangPhatTrien();
            }
        });
        btn_chinhsua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CapNhatThongTinUser.class);
                intent.putExtra("userID", userID);
                startActivityForResult(intent, REQUEST_CODE_CAPNHAT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_CAPNHAT && resultCode == 1){
            LoadData();
        }
    }

    private void DangPhatTrien() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ThongTinUser.this);
        builder.setTitle("Comming soon");
        builder.setMessage("Tính năng này đang được phát triển");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
        }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void LoadData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String img_url = documentSnapshot.getString("img_url");
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String gender = documentSnapshot.getString("gender");
                        Date birthdate = documentSnapshot.getDate("birthdate");
                        String password = documentSnapshot.getString("password");
                        String role_id = documentSnapshot.getString("role_id");

                        user = new User(userID, name, phoneNumber, gender, email, img_url, password,
                                        role_id, birthdate);

                        tv_ten.setText(name);
                        tv_email.setText(email);

                        if(!img_url.isEmpty()){
                            Picasso.get().load(img_url).into(imv_user);
                        }
                    }
                });
    }

    private void FindViewByIds() {
        btn_logout = findViewById(R.id.btn_khachhang_userinfo_logout);
        bottomNavigationView = findViewById(R.id.bottom_khachhang_info);
        imv_user = findViewById(R.id.imv_khachhang_userinfo);
        tv_ten = findViewById(R.id.tv_khachhang_userinfo_FullName);
        tv_email = findViewById(R.id.tv_khachhang_userinfo_email);
        btn_doimk = findViewById(R.id.btn_khachhang_user_doimk);
        btn_chinhsua = findViewById(R.id.btn_khachhang_user_chinhsua);

        userID = getIntent().getStringExtra("userID");
    }
    private void BottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_khachhang_menu_info);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_khachhang_menu_info:
                    return true;
                case R.id.bottom_khachhang_home:
                    Intent intent= new Intent(getApplicationContext(), ChonSan.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.bottom_khachhang_history:
                    Intent intent1= new Intent(getApplicationContext(), ThongTinDatLich.class);
                    intent1.putExtra("userID", userID);
                    startActivity(intent1);
                    finish();
                    return true;
            }
            return false;
        });
    }
}
