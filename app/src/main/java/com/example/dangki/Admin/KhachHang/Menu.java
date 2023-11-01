package com.example.dangki.Admin.KhachHang;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Admin.UserInfo;
import com.example.dangki.Dangnhapthanhcong;
import com.example.dangki.Model.DoUong;
import com.example.dangki.Model.User;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Menu extends AppCompatActivity {
    String userID;
    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerView;
    KhachHangMenuAdapter khachHangMenuAdapter;
    List<User> userList;
    SearchView searchView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.admin_khachhang);

        FindViewByIds();
        BottomNavigation();
        SetupAdapter();
        LoadData();

        khachHangMenuAdapter.setOnItemClickListener(new KhachHangMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                Intent intent = new Intent(Menu.this, CapNhatKhachHang.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                SearchKH(searchText);
                return true;
            }
        });
    }

    private void SearchKH(String searchText) {
        List<User> filteredList = new ArrayList<>();
        String normalizedSearchText = normalizeString(searchText);
        for (User user : userList) {
            String normalizedName = normalizeString(user.getName());
            if (normalizedName.contains(normalizedSearchText)) {
                filteredList.add(user);
            }
        }
        khachHangMenuAdapter.setUserList(filteredList);
    }
    private String normalizeString(String text) {
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalizedText.replaceAll("\\p{M}", "").toLowerCase();
    }

    private void LoadData() {
        userList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .whereNotEqualTo("role_id", "admin")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            String userID = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            String gender = documentSnapshot.getString("gender");
                            Date birthdate = documentSnapshot.getDate("birthdate");
                            String email = documentSnapshot.getString("email");
                            String img_url = documentSnapshot.getString("img_url");
                            String phoneNumber = documentSnapshot.getString("phoneNumber");
                            String password = documentSnapshot.getString("password");
                            User user = new User(userID, name, phoneNumber, gender, email, img_url,
                                                    password, "customer", birthdate);

                            userList.add(user);
                        }
                        khachHangMenuAdapter.setUserList(userList);
                    }
                });
    }

    private void SetupAdapter() {
        khachHangMenuAdapter = new KhachHangMenuAdapter(new ArrayList<>());
        recyclerView.setAdapter(khachHangMenuAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void BottomNavigation() {
        bottomNavigationView = findViewById(R.id.admin_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_admin_khachhang);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_admin_khachhang:
                    return true;
                case R.id.bottom_admin_profile:
                    Intent intent = new Intent(getApplicationContext(), UserInfo.class);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.bottom_admin_datlich:
                    Intent intent3 = new Intent(getApplicationContext(), com.example.dangki.Admin.DatLich.Menu.class);
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

    private void FindViewByIds() {
        recyclerView = findViewById(R.id.rcv_admin_khachhang);
        userID = getIntent().getStringExtra("userID");
        searchView = findViewById(R.id.search_admin_khachhang);
    }
}
