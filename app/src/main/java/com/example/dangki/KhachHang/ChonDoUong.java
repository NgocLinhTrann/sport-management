package com.example.dangki.KhachHang;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Model.DoUong;
import com.example.dangki.Model.San;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ChonDoUong extends AppCompatActivity {

    long backPressedTime;
    Button btnDatlich;
    SearchView searchView;
    GridView gridView;
    DoUongGridAdapter doUongGridAdapter;
    List<DoUong> doUongList;

    String rentalID, userID;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_chondouong);

        FindViewByIds();
        SetupGridView();
        LoadDoUongData();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                SearchDoUong(queryText);
                return true;
            }
        });

        btnDatlich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ThanhToan.class);
                intent.putExtra("rentalID", rentalID);
                intent.putExtra("userID", userID);
                startActivityForResult(intent, 1);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        LoadDoUongData();
    }

    private void FindViewByIds() {
        searchView = findViewById(R.id.searchView_khachhang_chondouong);
        gridView = findViewById(R.id.gridView_khachhang_chondouong);
        btnDatlich = findViewById(R.id.btn_khachhang_tienhanhDatLich);

        rentalID = getIntent().getStringExtra("rentalID");
        userID = getIntent().getStringExtra("userID");
    }
    void SearchDoUong(String queryText){
        List<DoUong> filteredList = new ArrayList<>();
        String normalizedSearchText = normalizeString(queryText);
        for (DoUong doUong : doUongList) {
            String normalizedName = normalizeString(doUong.getName());
            if (normalizedName.contains(normalizedSearchText)) {
                filteredList.add(doUong);
            }
        }
        doUongGridAdapter.setDoUongList(filteredList);
    }
    private String normalizeString(String text) {
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalizedText.replaceAll("\\p{M}", "").toLowerCase();
    }
    private void SetupGridView() {
        doUongList = new ArrayList<>();
        doUongGridAdapter = new DoUongGridAdapter(this, doUongList, rentalID, userID);
        gridView.setAdapter(doUongGridAdapter);
    }
    private void LoadDoUongData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Drink")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            doUongList = new ArrayList<>();
                            for (DocumentSnapshot document : documents) {
                                String id_douong = document.getId();
                                String img_url = document.getString("img_url");
                                String ten_doUong = document.getString("name");
                                boolean isDelete = document.getBoolean("isDelete");
                                double price = document.getDouble("price");
                                int soLuong = Math.toIntExact(document.getLong("remain"));
                                if(isDelete == false){
                                    DoUong doUong = new DoUong(id_douong, ten_doUong, img_url,
                                            price, soLuong,isDelete);
                                    doUongList.add(doUong);
                                }
                            }
                            doUongGridAdapter.setDoUongList(doUongList);
                        } else {
                            Log.d("Load fail", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
