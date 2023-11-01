package com.example.dangki.Admin.DatLich;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Admin.UserInfo;
import com.example.dangki.Model.PurchasedService;
import com.example.dangki.Model.ThongTinDatLichModel;
import com.example.dangki.Model.User;
import com.example.dangki.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.common.subtyping.qual.Bottom;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Menu extends AppCompatActivity {
    static final long DOUBLE_BACK_PRESS_DURATION = 2000; // Thời gian giới hạn giữa 2 lần nhấn "Back" (2 giây trong trường hợp này)
    long backPressedTime; // Thời gian người dùng nhấn nút "Back" lần cuối
    RecyclerView recyclerView;
    DatLichAdapter datLichAdapter;
    List<DatLichModel> datLichModelList;
    BottomNavigationView bottomNavigationView;
    String userID;
    SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.admin_datlich);
        FindViewByIds();
        BottomNavigation();
        SetupAdapter();
        LoadData();
        datLichAdapter.setOnItemClickListener(new DatLichAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DatLichModel datLichModel) {
                Intent intent = new Intent(Menu.this, ChiTietDatLichAdmin.class);
                intent.putExtra("rentalID", datLichModel.getRentalID());
                intent.putExtra("status", datLichModel.getStatus());
                startActivityForResult(intent, 1);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                SearchDatLich(searchText);
                return true;
            }
        });
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == 1){
            LoadData();
        }
    }

    private void SearchDatLich(String searchText) {
        List<DatLichModel> filteredList = new ArrayList<>();
        String normalizedSearchText = normalizeString(searchText);
        for (DatLichModel datLichModel : datLichModelList) {
            String normalizedName = normalizeString(datLichModel.getUserName());
            if (normalizedName.contains(normalizedSearchText)) {
                filteredList.add(datLichModel);
            }
        }
        datLichAdapter.setDatLichModelList(filteredList);
    }
    private String normalizeString(String text) {
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalizedText.replaceAll("\\p{M}", "").toLowerCase();
    }

    private void BottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_admin_datlich_layout);
        bottomNavigationView.setSelectedItemId(R.id.bottom_admin_datlich);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_admin_datlich:
                    return true;
                case R.id.bottom_admin_profile:
                    Intent intent = new Intent(getApplicationContext(), UserInfo.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.bottom_admin_khachhang:
                    Intent intent3 = new Intent(getApplicationContext(), com.example.dangki.Admin.KhachHang.Menu.class);
                    intent3.putExtra("userID", userID);
                    startActivity(intent3);
                    finish();
                    return true;
                case R.id.bottom_admin_douong:
                    Intent intent1 = new Intent(getApplicationContext(), com.example.dangki.Admin.Douong.Menu.class);
                    intent1.putExtra("userID", userID);
                    startActivity(intent1);
                    finish();
                    return true;
                case R.id.bottom_admin_san:
                    Intent intent2 = new Intent(getApplicationContext(), com.example.dangki.Admin.San.Menu.class);
                    intent2.putExtra("userID", userID);
                    startActivity(intent2);
                    finish();
                    return true;
            }
            return false;
        });
    }

    private void LoadData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        datLichModelList = new ArrayList<>();
        db.collection("Rental")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String rentalID = documentSnapshot.getId();
                        String status = documentSnapshot.getString("status");
                        String userID = documentSnapshot.getString("user_id");

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
                                                    String img_url_san = documentSnapshot2.getString("img_url");
                                                    String ten_san = documentSnapshot2.getString("name");

                                                    // Lấy dữ liệu từ collection "User"
                                                    db.collection("User")
                                                            .document(userID)
                                                            .get()
                                                            .addOnSuccessListener(documentSnapshotUser -> {
                                                                String cus_name = documentSnapshotUser.getString("name");

                                                                DatLichModel datLichModel
                                                                        = new DatLichModel(rentalID,
                                                                        cus_name, ten_san, status,
                                                                        img_url_san, start_time,
                                                                        end_time); // Thay ... bằng các thuộc tính khác

                                                                datLichModelList.add(datLichModel);
                                                                future.complete(null);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                // Xử lý ngoại lệ nếu có lỗi xảy ra trong quá trình truy vấn Firestore
                                                                future.completeExceptionally(e);
                                                            });
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
                                datLichAdapter.setDatLichModelList(datLichModelList);
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

    private void SetupAdapter() {
         datLichAdapter = new DatLichAdapter(new ArrayList<>());
         recyclerView.setAdapter(datLichAdapter);
         recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void FindViewByIds() {
        recyclerView = findViewById(R.id.rcv_admin_datlich);
        searchView = findViewById(R.id.search_admin_datlich);

        userID = getIntent().getStringExtra("userID");
    }
}
