package com.example.dangki.Admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Admin.KhachHang.Menu;
import com.example.dangki.KhachHang.ThongTinDatLich;
import com.example.dangki.KhachHang.ThongTinUser;
import com.example.dangki.Login;
import com.example.dangki.Model.User;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfo extends AppCompatActivity {

    Button btn_logout;
    String userID;
    User user;
    BottomNavigationView bottomNavigationView;
    CircleImageView imv_user;
    TextView tv_ten, tv_email;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.layout_userinfo_admin);

        FindViewByIds();
        BottomNavigation();
        LoadData();
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogOut();
            }
        });
    }

    private void BottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_admin_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_admin_profile:
                    return true;
                case R.id.bottom_admin_khachhang:
                    Intent intent= new Intent(getApplicationContext(), Menu.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.bottom_admin_san:
                    Intent intent1= new Intent(getApplicationContext(), com.example.dangki.Admin.San.Menu.class);
                    intent1.putExtra("userID", userID);
                    startActivity(intent1);
                    finish();
                    return true;
                case R.id.bottom_admin_douong:
                    Intent intent2= new Intent(getApplicationContext(), com.example.dangki.Admin.Douong.Menu.class);
                    intent2.putExtra("userID", userID);
                    startActivity(intent2);
                    finish();
                    return true;
                case R.id.bottom_admin_datlich:
                    Intent intent3= new Intent(getApplicationContext(), com.example.dangki.Admin.DatLich.Menu.class);
                    intent3.putExtra("userID", userID);
                    startActivity(intent3);
                    finish();
                    return true;
            }
            return false;
        });
    }

    private void LogOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng xuất");
        builder.setMessage("Xác nhận đăng xuất");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void FindViewByIds() {
        btn_logout = findViewById(R.id.btn_admin_userinfo_logOut);
        bottomNavigationView = findViewById(R.id.bottom_admin_info);
        tv_ten = findViewById(R.id.tv_admin_info_name);
        tv_email = findViewById(R.id.tv_admin_info_email);
        imv_user = findViewById(R.id.imv_admin_info_anh);
        userID = getIntent().getStringExtra("userID");
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
}
