package com.example.dangki.KhachHang;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class ChiTietDoUong extends AppCompatActivity {
    Button btnThemDoUong;
    ProgressBar progressBar;
    ImageButton btn_add, btn_sub;
    ImageView btn_goback, imv_anhDoUong;
    TextView tv_tenDoUong, tv_giaDoUong, tv_sl;

    String id_doUong, img_url, ten_doUong, rentalID, userID, drink_rental_id;
    double gia_doUong, totalDB=0.0;
    int sl_conLai, currentQuantity = 0, sl_cu=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_chondouong_chitiet);

        FindViewByIds();
        LoadRentalTotal();
        LoadDuLieuDoUong();

        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentQuantity < sl_conLai){
                    currentQuantity++;
                    tv_sl.setText(String.valueOf(currentQuantity));
                }
            }
        });

        btn_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentQuantity > 1){
                    currentQuantity--;
                    tv_sl.setText(String.valueOf(currentQuantity));
                }
            }
        });

        imv_anhDoUong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(ChiTietDoUong.this, android.R.style
                        .Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.dialog_fullscreen_image);

                ImageView imageViewFullscreen = dialog.findViewById(R.id.imageView_fullscreen);
                Picasso.get().load(img_url).into(imageViewFullscreen);

                dialog.show();
            }
        });

        btnThemDoUong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check nếu sl được chọn là 0

                if(Integer.parseInt(tv_sl.getText().toString()) > 0 ){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChiTietDoUong.this);
                    builder.setTitle("Xác nhận");
                    builder.setMessage("Quý khách vui lòng xác nhận mua đồ uống?");

                    builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            progressBar.setVisibility(View.VISIBLE);
                            CompletableFuture<Boolean> firstAddtoDb = CheckDoUongLanDau();
                            firstAddtoDb.thenAccept(isFirstAdd ->{
                                if(isFirstAdd){
                                    ThemDoUongVaoDbLanDau();
                                }else{
                                    ThemDoUongVaoDb();
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }

    private void LoadRentalTotal() {
        loadRentalTotalAsync().thenAccept(total -> {
            totalDB = total;
        });
    }
    private CompletableFuture<Double> loadRentalTotalAsync() {
        CompletableFuture<Double> future = new CompletableFuture<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Rental")
                .document(rentalID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    double total = document.getDouble("total");
                    future.complete(total); // Hoàn thành CompletableFuture với giá trị total
                } else {
                    future.completeExceptionally(new RuntimeException("Document does not exist"));
                }
            } else {
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }

    CompletableFuture<Boolean> CheckDoUongLanDau(){
        progressBar.setVisibility(View.VISIBLE);
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Drink_Rental")
                .whereEqualTo("rental_id", rentalID)
                .whereEqualTo("drink_id", id_doUong)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().isEmpty()){
                            future.complete(true); // Không có dữ liệu
                        } else {
                            drink_rental_id = task.getResult().getDocuments().get(0).getId();
                            sl_cu = Math.toIntExact(task.getResult().getDocuments().get(0)
                                    .getLong("quantity"));
                            future.complete(false); // Có dữ liệu
                        }
                    } else {
                        future.complete(true); // Lỗi trong quá trình tìm kiếm
                    }
                });
        return future;
    }

    void ThemDoUongVaoDb(){
        double newTotal = totalDB + (gia_doUong * Integer.parseInt(tv_sl.getText().toString()));

        int sl_moi = sl_cu + Integer.parseInt(tv_sl.getText().toString());
        Map<String, Object> doUongData = new HashMap<>();
        doUongData.put("quantity", sl_moi);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Thêm dữ liệu vào collection "Drink_Rental"
        DocumentReference docRefDrinkRental = db.collection("Drink_Rental").document(drink_rental_id);
        batch.update(docRefDrinkRental, doUongData);

        // Cập nhật dữ liệu trong collection "Rental"
        DocumentReference docRefRental = db.collection("Rental").document(rentalID);
        batch.update(docRefRental, "total", newTotal);

        // Cập nhật sl đồ uống còn lại trong "Drink"
        int newRemain = sl_conLai - Integer.parseInt(tv_sl.getText().toString());
        DocumentReference refDrink = db.collection("Drink").document(id_doUong);
        batch.update(refDrink, "remain", newRemain);

        // Commit giao dịch batch
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }
    void ThemDoUongVaoDbLanDau() {
        double newTotal = totalDB + (gia_doUong * Integer.parseInt(tv_sl.getText().toString()));

        Map<String, Object> doUongData = new HashMap<>();
        doUongData.put("drink_id", id_doUong);
        doUongData.put("quantity", Integer.parseInt(tv_sl.getText().toString()));
        doUongData.put("rental_id", rentalID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Thêm dữ liệu vào collection "Drink_Rental"
        DocumentReference docRefDrinkRental = db.collection("Drink_Rental").document();
        batch.set(docRefDrinkRental, doUongData);

        // Cập nhật dữ liệu trong collection "Rental"
        DocumentReference docRefRental = db.collection("Rental").document(rentalID);
        batch.update(docRefRental, "total", newTotal);

        // Cập nhật sl đồ uống còn lại trong "Drink"
        int newRemain = sl_conLai - Integer.parseInt(tv_sl.getText().toString());
        DocumentReference refDrink = db.collection("Drink").document(id_doUong);
        batch.update(refDrink, "remain", newRemain);

        // Commit giao dịch batch
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }



    private void LoadDuLieuDoUong() {
        tv_tenDoUong.setText(ten_doUong);
        tv_giaDoUong.setText(String.valueOf(gia_doUong) + "VND");
        Picasso.get().load(img_url).into(imv_anhDoUong);
    }

    private void FindViewByIds() {
        tv_tenDoUong = findViewById(R.id.tv_khachhang_chitietdouong_tendouong);
        tv_giaDoUong = findViewById(R.id.tv_khachhang_chitietdouong_gia);
        tv_sl = findViewById(R.id.tv_khachhang_chitietdouong_sl);
        btn_add = findViewById(R.id.btn_khachang_chitietdouong_add);
        btn_sub= findViewById(R.id.btn_khachang_chitietdouong_sub);
        btn_goback = findViewById(R.id.btn_khachhang_chitietdouong_goback);
        btnThemDoUong = findViewById(R.id.btn_khachang_chitietdouong_ThemDoUong);
        imv_anhDoUong = findViewById(R.id.imv_khachhang_chitetdouong_anhdouong);

        Intent intent = getIntent();
        ten_doUong = intent.getStringExtra("name");
        id_doUong = intent.getStringExtra("idDoUong_intent");
        img_url = intent.getStringExtra("img_url");
        gia_doUong = intent.getDoubleExtra("price", 0.0);
        sl_conLai = intent.getIntExtra("remain",0);
        userID = intent.getStringExtra("userID");
        rentalID = intent.getStringExtra("rentalID");

        progressBar = findViewById(R.id.progressBar_khachhang_themDoUong);

    }
}
