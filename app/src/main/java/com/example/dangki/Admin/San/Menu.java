package com.example.dangki.Admin.San;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Admin.Douong.CapNhatDoUong;
import com.example.dangki.Admin.UserInfo;
import com.example.dangki.Dangnhapthanhcong;
import com.example.dangki.Model.DoUong;
import com.example.dangki.Model.San;
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
    static final int REQUEST_ADD_STADIUM_CODE =1;
    static final int REQUEST_CODE_CAPNHAT = 2 ;// Mã yêu cầu cập nhật
    FloatingActionButton btn_themSan;
    BottomNavigationView bottomNavigationView;
    SearchView searchView;
    static final long DOUBLE_BACK_PRESS_DURATION = 2000; // Thời gian giới hạn giữa 2 lần nhấn "Back" (2 giây trong trường hợp này)
    long backPressedTime; // Thời gian người dùng nhấn nút "Back" lần cuối
    RecyclerView recyclerView;
    List<San> sanList;
    SanAdapter sanAdapter;
    String userID;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.admin_san);
        findViewByIds();
        BottomNavigation();

        sanAdapter = new SanAdapter(new ArrayList<>());
        recyclerView.setAdapter(sanAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LoadSan();
        /*
        Search san
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                SearchSan(searchText);
                return true;
            }
        });

        /*
        Thêm sân mới
         */
        btn_themSan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Menu.this, ThemSan.class);
                startActivityForResult(intent, REQUEST_ADD_STADIUM_CODE);
            }
        });

        /*
        Nhấn vào item sẽ load giao diện chi tiết sân
         */

        sanAdapter.setOnItemClickListener(new SanAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(San san) {
                Intent intent = new Intent(Menu.this, CapNhatSan.class);
                intent.putExtra("Id", san.getId());
                intent.putExtra("name", san.getName());
                intent.putExtra("price", san.getPrice());

                startActivityForResult(intent, REQUEST_CODE_CAPNHAT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ADD_STADIUM_CODE && resultCode == 2){
            LoadSan();
        }
        if(requestCode == REQUEST_CODE_CAPNHAT && resultCode ==1){
            LoadSan();
        }
        if(requestCode == REQUEST_CODE_CAPNHAT && resultCode == 2){
            LoadSan();
        }
    }

    /*
        Ấn 2 lần thoát ứng dụng
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
    Search Sân
     */
    private void SearchSan(String searchText) {
        List<San> filteredList = new ArrayList<>();
        String normalizedSearchText = normalizeString(searchText);
        for (San san : sanList) {
            String normalizedName = normalizeString(san.getName());
            if (normalizedName.contains(normalizedSearchText)) {
                filteredList.add(san);
            }
        }
        sanAdapter.setSanList(filteredList);
    }
    private String normalizeString(String text) {
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalizedText.replaceAll("\\p{M}", "").toLowerCase();
    }

    private void LoadSan() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("Stadium");
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                sanList = new ArrayList<>();
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    String id = documentSnapshot.getId();
                    String tenSan = documentSnapshot.getString("name");
                    String img_url = documentSnapshot.getString("img_url");
                    double giaSan = documentSnapshot.getDouble("price");
                    boolean isDelete = documentSnapshot.getBoolean("isDelete");

                    if(isDelete != true){
                        San san = new San(id,img_url,tenSan,giaSan,isDelete);
                        sanList.add(san);
                    }
                }
                sanAdapter.setSanList(sanList);
            }
        });
    }

    private void findViewByIds() {
        searchView = findViewById(R.id.search_admin_san);
        btn_themSan = findViewById(R.id.btn_admin_san_them);
        bottomNavigationView = findViewById(R.id.admin_san_navigation);
        recyclerView = findViewById(R.id.rcv_admin_san);

        userID = getIntent().getStringExtra("userID");
    }
    void BottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.bottom_admin_san);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.bottom_admin_san:
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
                case R.id.bottom_admin_datlich:
                    Intent intent2 = new Intent(getApplicationContext(), com.example.dangki.Admin.DatLich.Menu.class);
                    intent2.putExtra("userID", userID);
                    startActivity(intent2);
                    finish();
                    return true;
            }
            return false;
        });
    }
}
