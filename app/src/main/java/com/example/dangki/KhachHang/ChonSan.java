package com.example.dangki.KhachHang;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Dangnhapthanhcong;
import com.example.dangki.Model.San;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ChonSan extends AppCompatActivity {
    static final long DOUBLE_BACK_PRESS_DURATION = 2000; // Thời gian giới hạn giữa 2 lần nhấn "Back" (2 giây trong trường hợp này)
    long backPressedTime;
    SearchView searchView;
    GridView gridView;
    SanGridAdapter sanGridAdapter;
    List<San> sanList;
    String userID, rentalID;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton btn_douong;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_chonsan);

        FindViewByIds();
        CheckDatLichLan2();
        BottomNavigation();
        setupGridView();
        loadStadiumData();

        /*
        xử lý sự kiện Search sân
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                SearchSan(queryText);
                return true;
            }
        });
        btn_douong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChonDoUong.class);
                intent.putExtra("rentalID", rentalID);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckDatLichLan2();
    }

    private void CheckDatLichLan2() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Rental")
                .whereEqualTo("user_id", userID)
                .whereEqualTo("status", "Booking")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            rentalID = documentSnapshot.getId();
                            btn_douong.setVisibility(View.VISIBLE);
                        }
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

    private void BottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_khachhang_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_khachhang_home:
                    return true;
                case R.id.bottom_khachhang_menu_info:
                    Intent intent= new Intent(getApplicationContext(), ThongTinUser.class);
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

    private void SearchSan(String queryText) {
        List<San> filteredList = new ArrayList<>();
        String normalizedSearchText = normalizeString(queryText);
        for (San san : sanList) {
            String normalizedName = normalizeString(san.getName());
            if (normalizedName.contains(normalizedSearchText)) {
                filteredList.add(san);
            }
        }
        sanGridAdapter.setSanList(filteredList);
    }
    private String normalizeString(String text) {
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalizedText.replaceAll("\\p{M}", "").toLowerCase();
    }

    private void FindViewByIds() {
        searchView = findViewById(R.id.searchView_khachhang_chonsan);
        gridView = findViewById(R.id.gridView_khachhang_chonsan);
        bottomNavigationView = findViewById(R.id.bottom_khachhang_Home);
        btn_douong = findViewById(R.id.btn_khachhang_chonsan_douong);
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
    }

    private void setupGridView() {
        sanList = new ArrayList<>();
        sanGridAdapter = new SanGridAdapter(this, sanList, userID);
        gridView.setAdapter(sanGridAdapter);
    }

    private void loadStadiumData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Stadium")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            sanList = new ArrayList<>();
                            for (DocumentSnapshot document : documents) {
                                String id_san = document.getId();
                                String img_url = document.getString("img_url");
                                String ten_san = document.getString("name");
                                boolean isDelete = document.getBoolean("isDelete");
                                double price = document.getDouble("price");
                                if(isDelete == false){
                                    San san = new San(id_san,img_url,ten_san,price, false);
                                    sanList.add(san);
                                }
                            }
                            sanGridAdapter.setSanList(sanList);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
