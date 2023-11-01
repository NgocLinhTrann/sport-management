package com.example.dangki.Admin.Douong;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Admin.San.ThemSan;
import com.example.dangki.Admin.UserInfo;
import com.example.dangki.Dangnhapthanhcong;
import com.example.dangki.Model.DoUong;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Menu extends AppCompatActivity {
    static final int REQUEST_CODE_CAPNHAT = 1; // Mã yêu cầu cập nhật
    static final int REQUEST_CODE_ADD = 2; // Mã yêu cầu cập nhật
    static final long DOUBLE_BACK_PRESS_DURATION = 2000; // Thời gian giới hạn giữa 2 lần nhấn "Back" (2 giây trong trường hợp này)
    long backPressedTime; // Thời gian người dùng nhấn nút "Back" lần cuối
    BottomNavigationView bottomNavigationView;
    SearchView searchView;
    RecyclerView recyclerView;
    DoUongMenuAdapter doUongMenuAdapter;
    FloatingActionButton btn_admin_douong_them;
    List<DoUong> doUongList;
    String userID;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.admin_douong);

        FindViewByIds();
        BottomNavigation();

        doUongMenuAdapter = new DoUongMenuAdapter(new ArrayList<>());
        recyclerView.setAdapter(doUongMenuAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadDoUong();

        btn_admin_douong_them.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ThemDoUong.class);
                startActivityForResult(intent, REQUEST_CODE_ADD);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                searchDoUong(searchText);
                return true;
            }
        });

        doUongMenuAdapter.setOnItemClickListener(new DoUongMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DoUong doUong) {
                Intent intent = new Intent(Menu.this, CapNhatDoUong.class);
                intent.putExtra("Id", doUong.getId());
                intent.putExtra("sl", doUong.getRemain());
                intent.putExtra("gia", doUong.getPrice());
                intent.putExtra("ten", doUong.getName());

                startActivityForResult(intent, REQUEST_CODE_CAPNHAT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==  REQUEST_CODE_CAPNHAT && resultCode == 1){
            loadDoUong();
        }
        // Sự kiện xóa
        if(requestCode == REQUEST_CODE_CAPNHAT && resultCode ==2){
            loadDoUong();
        }
        if(requestCode == REQUEST_CODE_ADD && resultCode ==2){
            loadDoUong();
        }
    }

    /*
    Search đồ uống
     */
    private void searchDoUong(String searchText) {
        List<DoUong> filteredList = new ArrayList<>();
        String normalizedSearchText = normalizeString(searchText);
        for (DoUong doUong : doUongList) {
            String normalizedName = normalizeString(doUong.getName());
            if (normalizedName.contains(normalizedSearchText)) {
                filteredList.add(doUong);
            }
        }
        doUongMenuAdapter.setDoUongList(filteredList);
    }
    private String normalizeString(String text) {
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalizedText.replaceAll("\\p{M}", "").toLowerCase();
    }

    /*
    Sự kiện thoát ứng dụng
     */
    @Override
    public void onBackPressed() {
        if(backPressedTime + DOUBLE_BACK_PRESS_DURATION > System.currentTimeMillis()){
            super.onBackPressed();
        }else{
            Toast.makeText(this, "Nhấn back một lần nữa để thoát ứng dụng", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis(); // Cập nhật thời gian người dùng nhấn nút "Back" lần cuối
    }

    /*
    Load đồ uống từ Firestore lên View
     */
    void loadDoUong(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("Drink");
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                doUongList = new ArrayList<>();
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    String id = documentSnapshot.getId();
                    String tenDoUong = documentSnapshot.getString("name");
                    String img_url = documentSnapshot.getString("img_url");
                    Boolean isDelete = documentSnapshot.getBoolean("isDelete");
                    int soLuong = Math.toIntExact(documentSnapshot.getLong("remain"));
                    double gia = documentSnapshot.getDouble("price");

                    if(isDelete == false){
                        DoUong doUong = new DoUong(id, tenDoUong, img_url, gia, soLuong, false);
                        doUongList.add(doUong);
                    }
                }
                doUongMenuAdapter.setDoUongList(doUongList);
            };
        });
    }

    /*
    Find View By Id
     */
    void FindViewByIds(){
        bottomNavigationView = findViewById(R.id.admin_navigation);
        btn_admin_douong_them = findViewById(R.id.btn_themDoUong);
        recyclerView = findViewById(R.id.rcv_admin_douong);
        searchView = findViewById(R.id.search_admin_douong);

        userID = getIntent().getStringExtra("userID");
    }

    /*
    Xử lý BottomNavigation
     */
    void BottomNavigation(){

        bottomNavigationView.setSelectedItemId(R.id.bottom_admin_douong);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_admin_douong:
                    return true;

                case R.id.bottom_admin_khachhang:
                    Intent intent = new Intent(getApplicationContext(), com.example.dangki.Admin.KhachHang.Menu.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.bottom_admin_san:
                    Intent intent1 = new Intent(getApplicationContext(), com.example.dangki.Admin.San.Menu.class);
                    intent1.putExtra("userID", userID);
                    startActivity(intent1);
                    finish();
                    return true;
                case R.id.bottom_admin_profile:
                    Intent intent2 = new Intent(getApplicationContext(), UserInfo.class);
                    intent2.putExtra("userID", userID);
                    startActivity(intent2);
                    finish();
                    return true;
                case R.id.bottom_admin_datlich:
                    Intent intent3 = new Intent(getApplicationContext(), com.example.dangki.Admin.DatLich.Menu.class);
                    intent3.putExtra("userID", userID);
                    startActivity(intent3);
                    finish();
                    return true;
            }
            return false;
        });
    }

}
