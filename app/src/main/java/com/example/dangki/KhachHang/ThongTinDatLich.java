package com.example.dangki.KhachHang;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Login;
import com.example.dangki.Model.ThongTinDatLichModel;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ThongTinDatLich extends AppCompatActivity {
    static final long DOUBLE_BACK_PRESS_DURATION = 2000; // Thời gian giới hạn giữa 2 lần nhấn "Back" (2 giây trong trường hợp này)
    long backPressedTime; // Thời gian người dùng nhấn nút "Back" lần cuối
    RecyclerView recyclerView;
    ThongTinDatLichAdapter thongTinDatLichAdapter;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton btn_filter;
    String userID;
    List<ThongTinDatLichModel> thongTinDatLichModelList;
    ImageView btn_nofilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_lichsu);

        FindViewByIds();
        BottomNavigation();
        SetupAdapter();
        LoadData();

        btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowBottomDialog();
            }
        });

        btn_nofilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thongTinDatLichAdapter.setThongTinDatLichList(thongTinDatLichModelList);
                btn_nofilter.setVisibility(View.GONE);
            }
        });
        thongTinDatLichAdapter.setOnItemClickListener(new ThongTinDatLichAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ThongTinDatLichModel thongTinDatLichModel) {
                Intent intent = new Intent(ThongTinDatLich.this, ChiTietDatLich.class);
                intent.putExtra("rentalID", thongTinDatLichModel.getRentalID());
                intent.putExtra("status", thongTinDatLichModel.getStatus());
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 && resultCode ==1){
            LoadData();
        }
    }

    private void ShowBottomDialog() {
        Dialog bottomDialog = new Dialog(ThongTinDatLich.this);

        // Đặt layout cho dialog
        bottomDialog.setContentView(R.layout.bottom_khachhang_lichsu_filter);

        RadioGroup radioGroup = bottomDialog.findViewById(R.id.radiogr_khachang_lichsu_filter);
        Button filter_btn = bottomDialog.findViewById(R.id.btn_khachhang_lichsu_filter);

        // Đặt các thuộc tính của dialog
        Window dialogWindow = bottomDialog.getWindow();
        if (dialogWindow != null) {
            // Đặt độ trasparent cho nền của dialog
            dialogWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            // Đặt độ trasparent cho phần nền trên cùng của dialog (bo góc trên)
            dialogWindow.setDimAmount(0.5f);

            // Đặt các thuộc tính về kích thước và vị trí
            WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
            layoutParams.gravity = Gravity.BOTTOM; // Hiển thị ở dưới cùng
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Chiều rộng tối đa
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Chiều cao tự động
            dialogWindow.setAttributes(layoutParams);
        }

        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedRadio = radioGroup.getCheckedRadioButtonId();
                List<ThongTinDatLichModel> filteredList = new ArrayList<>();

                switch (selectedRadio){
                    case R.id.radio_booking:
                        for(ThongTinDatLichModel item : thongTinDatLichModelList){
                            if(item.getStatus().equals("Booking")){
                                filteredList.add(item);
                            }
                        }
                        break;
                    case R.id.radio_booked:
                        for(ThongTinDatLichModel item : thongTinDatLichModelList){
                            if(item.getStatus().equals("Booked")){
                                filteredList.add(item);
                            }
                        }
                        break;
                    case R.id.radio_done:
                        for(ThongTinDatLichModel item : thongTinDatLichModelList){
                            if(item.getStatus().equals("Done")){
                                filteredList.add(item);
                            }
                        }
                        break;
                    default:
                        filteredList.addAll(thongTinDatLichModelList);
                        break;
                }
                thongTinDatLichAdapter.setThongTinDatLichList(filteredList);
                btn_nofilter.setVisibility(View.VISIBLE);
                bottomDialog.dismiss();
            }
        });
        // Hiển thị dialog
        bottomDialog.show();
    }

    private void LoadData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        thongTinDatLichModelList = new ArrayList<>();

        db.collection("Rental")
                .whereEqualTo("user_id", userID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String rentalID = documentSnapshot.getId();
                        String status = documentSnapshot.getString("status");
                        CompletableFuture<Void> future = new CompletableFuture<>();

                        db.collection("Stadium_Rental")
                                .whereEqualTo("rental_id", rentalID)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.isEmpty()) {
                                        QueryDocumentSnapshot documentSnapshot1 =
                                                (QueryDocumentSnapshot) queryDocumentSnapshots1.getDocuments().get(0);
                                        String stadium_id = documentSnapshot1.getString("stadium_id");
                                        Date start_time = documentSnapshot1.getDate("start_time");
                                        Date end_time = documentSnapshot1.getDate("end_time");

                                        db.collection("Stadium")
                                                .document(stadium_id)
                                                .get()
                                                .addOnSuccessListener(documentSnapshot2 -> {
                                                    String img_url = documentSnapshot2.getString("img_url");
                                                    String name = documentSnapshot2.getString("name");

                                                    ThongTinDatLichModel thongTinDatLichModel =
                                                            new ThongTinDatLichModel(rentalID, img_url,
                                                            name, status, start_time, end_time);
                                                    thongTinDatLichModelList.add(thongTinDatLichModel);

                                                    future.complete(null);
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Xử lý ngoại lệ nếu có lỗi xảy ra trong quá trình truy vấn Firestore
                                                    future.completeExceptionally(e);
                                                });
                                    } else {
                                        future.complete(null);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Xử lý ngoại lệ nếu có lỗi xảy ra trong quá trình truy vấn Firestore
                                    future.completeExceptionally(e);
                                });

                        futures.add(future);
                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenRun(() -> {
                                thongTinDatLichAdapter.setThongTinDatLichList(thongTinDatLichModelList);
                            })
                            .exceptionally(e -> {
                                // Xử lý ngoại lệ nếu có lỗi xảy ra trong quá trình truy vấn Firestore
                                return null;
                            });
                })
                .addOnFailureListener(e -> {
                    // Xử lý ngoại lệ nếu có lỗi xảy ra trong quá trình truy vấn Firestore
                });
    }


//    void LoadData(){
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        thongTinDatLichModelList = new ArrayList<>(); // Thêm dòng này để khởi tạo danh sách
//
//        db.collection("Rental")
//                .whereEqualTo("user_id", userID)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<CompletableFuture<Void>> futures = new ArrayList<>();
//
//                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                        String rentalID = documentSnapshot.getId();
//                        String status = documentSnapshot.getString("status");
//                        CompletableFuture<Void> future = new CompletableFuture<>();
//
//                        db.collection("Stadium_Rental")
//                                .whereEqualTo("rental_id", rentalID)
//                                .limit(1)
//                                .get()
//                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
//                                    if (!queryDocumentSnapshots1.isEmpty()) {
//                                        QueryDocumentSnapshot documentSnapshot1= (QueryDocumentSnapshot) queryDocumentSnapshots1.getDocuments();
//
//                                        Date start_time = documentSnapshot1.getDate("start_time");
//                                        Date end_time = documentSnapshot1.getDate("end_time");
//
//                                        ThongTinDatLichModel thongTinDatLichModel = new ThongTinDatLichModel()
//                                    } else {
//                                        future.complete(null);
//                                    }
//                                });
//
//                        futures.add(future);
//                    }
//
//                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
//                        thongTinDatLichAdapter.setThongTinDatLichList(thongTinDatLichModelList);
//                    });
//                });
//    }
    private void SetupAdapter() {
        thongTinDatLichAdapter = new ThongTinDatLichAdapter(new ArrayList<>());
        recyclerView.setAdapter(thongTinDatLichAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + DOUBLE_BACK_PRESS_DURATION > System.currentTimeMillis()){
            super.onBackPressed();
        }else{
            Toast.makeText(this, "Nhấn back một lần nữa để thoát ứng dụng", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis(); // Cập nhật thời gian người dùng nhấn nút "Back" lần cuối
    }

    private void FindViewByIds() {
        bottomNavigationView = findViewById(R.id.bottom_khachhang_lichsu);
        recyclerView = findViewById(R.id.rcv_khachhang_lichsu);
        btn_filter = findViewById(R.id.btn_khachhang_lichsu_btnfilter);
        btn_nofilter = findViewById(R.id.btn_khachhang_lichsu_nofilter);

        userID = getIntent().getStringExtra("userID");
    }
    private void BottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_khachhang_history);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_khachhang_history:
                    return true;
                case R.id.bottom_khachhang_menu_info:
                    Intent intent= new Intent(getApplicationContext(), ThongTinUser.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.bottom_khachhang_home:
                    Intent intent1= new Intent(getApplicationContext(), ChonSan.class);
                    intent1.putExtra("userID", userID);
                    startActivity(intent1);
                    finish();
                    return true;
            }
            return false;
        });
    }
}
