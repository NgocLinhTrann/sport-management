package com.example.dangki;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Admin.KhachHang.Menu;
import com.example.dangki.KhachHang.ChonSan;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Welcome extends AppCompatActivity {
    TextView currentTime;
    SpinKitView loadingBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_welcome);

        // Khởi tạo các thành phần của Layout
        FindViewByIds();

        /*
        Xử lý ngày giờ để đưa lên màn hình
         */
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentTimeString = dateFormat.format(new Date());
        currentTime.setText(currentTimeString);

        /*
        Delay 2 giây trước khi vào màn hình login
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingBar.setVisibility(View.INVISIBLE);
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();

                if (currentUser != null) {
                    String userID = currentUser.getUid();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("User")
                            .document(userID)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        String roleID = documentSnapshot.getString("role_id");
                                        if (roleID != null && roleID.equals("admin")) {
                                            Intent intent = new Intent(getApplicationContext(), Menu.class);
                                            intent.putExtra("userID", userID);
                                            startActivity(intent);

                                        } else {
                                            // Trường hợp vai trò không xác định, xử lý tương ứng
                                            Intent intent = new Intent(getApplicationContext(), ChonSan.class);
                                            intent.putExtra("userID", userID);
                                            startActivity(intent);
                                        }
                                    }
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xảy ra lỗi khi truy vấn tài liệu người dùng
                                    startActivity(new Intent(getApplicationContext(), Login.class));
                                    finish();
                                }
                            });
                } else {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
            }
        }, 2250);

    }
    void FindViewByIds(){
        currentTime = findViewById(R.id.textViewCurrentTime);
        loadingBar = findViewById(R.id.progressBar);
        loadingBar.setVisibility(View.VISIBLE);

    }
}
