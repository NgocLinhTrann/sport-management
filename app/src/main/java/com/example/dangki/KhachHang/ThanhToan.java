package com.example.dangki.KhachHang;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Dangnhapthanhcong;
import com.example.dangki.Model.CreateOrder;
import com.example.dangki.Model.PurchasedService;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ThanhToan extends AppCompatActivity {

    ImageView btn_goback;
    TextView tv_tongTien;
    String userID, rentalID;
    RecyclerView recyclerView;
    PurchasedServiceAdapter purchasedServiceAdapter;
    List<PurchasedService> purchasedServices;
    Button btn_pay;
    RadioGroup radioGroup_phuongthuc;
    RadioButton radioButtonCash, radioButtonZalo;
    ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_thanhtoan);
        FindViewByIds();
        SetupAdapter();

        LoadDanhSach();

        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_pay.setEnabled(false);
        btn_pay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.gray));
        radioGroup_phuongthuc.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == 0) {
                    // Không có radio nào được chọn
                    btn_pay.setEnabled(false);
                    btn_pay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(),
                            R.color.gray));
                } else {
                    // Có radio được chọn
                    btn_pay.setEnabled(true);
                    btn_pay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(),
                            R.color.green));                }
            }
        });
        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ThanhToan.this);
                builder.setTitle("Xác nhận thanh toán");
                builder.setMessage("Quý khách vui lòng xác nhận thanh toán?");

                builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(radioButtonZalo.isChecked()){
                            ThanhToanZalo();
                        } else if (radioButtonCash.isChecked()) {
                            progressBar.setVisibility(View.VISIBLE);
                            UpdateStatus("Booked");
                        }
                    }
                });
                builder.setNegativeButton("Húy", null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        purchasedServiceAdapter.setOnDeleteItemClickListener(new PurchasedServiceAdapter.OnDeleteItemClickListener() {
            @Override
            public void onDeleteItem(PurchasedService purchasedService) {
                if(purchasedService.getType().equals("Drink")){
                    XoaDoUong(purchasedService);
                }else{
                    int dem =0;
                    for (int i =0; i<purchasedServices.size(); i++ ){
                        if(purchasedServices.get(i).getType().equals("Stadium")){
                            dem +=1;
                        }
                    }
                    if(dem ==1){
                        HuyDatLich();
                    }else{
                        XoaSan(purchasedService);
                    }
                }
            }
        });
    }

    private void XoaSan(PurchasedService purchasedService) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận");
        builder.setMessage("Xác nhận xóa lịch đặt sân này");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                XoaSanDB(purchasedService);
            }
        });
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void XoaSanDB(PurchasedService purchasedService) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Stadium_Rental")
                .whereEqualTo("rental_id", rentalID)
                .whereEqualTo("stadium_id", purchasedService.getId())
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        db.collection("Stadium_Rental")
                                .document(documentSnapshot.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        LoadDanhSach();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                });
    }

    private void HuyDatLich() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hủy đặt lịch");
        builder.setMessage("Đây là lịch đặt sân cuối cùng, nếu bạn xóa thì đồng nghĩa với hủy đặt lịch" +
                ". Bạn chắc chắn?");
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HuyDatLichDB();
            }
        });
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void HuyDatLichDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Task<?>> tasks = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);

        // Cập nhật số lượng đồ uống trong collection "Drink"
        for (PurchasedService purchasedService : purchasedServices) {
            if (purchasedService.getType().equals("Drink")) {
                String drinkID = purchasedService.getId();
                int quantity = purchasedService.getQuantity();

                // Tăng số lượng đồ uống bằng quantity
                Task<Void> updateDrinkTask = db.collection("Drink")
                        .document(drinkID)
                        .update("remain", FieldValue.increment(quantity));
                tasks.add(updateDrinkTask);
            }
        }

        // Xóa dữ liệu trên các collection "Rental", "Drink_Rental" và "Stadium_Rental"
        Task<Void> deleteRentalTask = db.collection("Rental")
                .document(rentalID)
                .delete();
        tasks.add(deleteRentalTask);

        Task<Void> drinkRentalQueryTask = db.collection("Drink_Rental")
                .whereEqualTo("rental_id", rentalID)
                .get()
                .continueWithTask(querySnapshotTask -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();

                    QuerySnapshot querySnapshot = querySnapshotTask.getResult();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Task<Void> deleteTask = document.getReference().delete();
                        deleteTasks.add(deleteTask);
                    }

                    return Tasks.whenAll(deleteTasks);
                });
        tasks.add(drinkRentalQueryTask);

        Task<Void> stadiumRentalQueryTask = db.collection("Stadium_Rental")
                .whereEqualTo("rental_id", rentalID)
                .get()
                .continueWithTask(querySnapshotTask -> {
                    List<Task<Void>> deleteTasks = new ArrayList<>();

                    QuerySnapshot querySnapshot = querySnapshotTask.getResult();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Task<Void> deleteTask = document.getReference().delete();
                        deleteTasks.add(deleteTask);
                    }

                    return Tasks.whenAll(deleteTasks);
                });
        tasks.add(stadiumRentalQueryTask);

        // ...

        Task<?>[] taskArray = tasks.toArray(new Task[0]);

        // Lắng nghe sự kiện hoàn thành của tất cả các tác vụ
        Tasks.whenAllComplete(taskArray)
                .addOnSuccessListener(taskList -> {
                    // Tất cả các tác vụ đã hoàn thành thành công
                    progressBar.setVisibility(View.GONE);

                    // Finish Activity hiện tại
                    Intent intent = new Intent(ThanhToan.this, ChonSan.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("userID", userID);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Xảy ra lỗi trong quá trình thực hiện
                    // Hiển thị thông báo hoặc xử lý lỗi tùy theo yêu cầu của bạn
                });
    }

    private void XoaDoUong(PurchasedService purchasedService) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Xác nhận xóa khỏi danh sách");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressBar.setVisibility(View.VISIBLE);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                WriteBatch batch = db.batch();
                db.collection("Drink_Rental")
                        .whereEqualTo("rental_id", rentalID)
                        .whereEqualTo("drink_id", purchasedService.getId())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                DocumentReference Drink_Rental = documentSnapshot.getReference();
                                batch.delete(Drink_Rental);

                                DocumentReference Drink = db.collection("Drink")
                                        .document(purchasedService.getId());
                                batch.update(Drink, "remain", FieldValue.increment(purchasedService.getQuantity()));

                                DocumentReference Rental = db.collection("Rental")
                                        .document(rentalID);
                                double sub = purchasedService.getQuantity() * purchasedService.getPrice();
                                batch.update(Rental, "total", FieldValue.increment(-sub));

                                batch.commit()
                                        .addOnSuccessListener(unused -> {
                                            progressBar.setVisibility(View.GONE);
                                            LoadDanhSach();
                                        });
                            }
                        });
            }
        });
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void ThanhToanZalo() {
        CreateOrder orderApi = new CreateOrder();

        try {
            String tongTien = tv_tongTien.getText().toString(); // Chuỗi ban đầu: "650000.0 VND"

// Bỏ chữ "VND" trong chuỗi
            String chiTietTien = tongTien.replace(".0VND", "");

            JSONObject data = orderApi.createOrder(chiTietTien);
            Log.d("Amount", chiTietTien);
            String code = data.getString("returncode");
//            String code = data.getString("returncode");

            String token = data.getString("zptranstoken");

            if (code.equals("1")) {
                ZaloPaySDK.getInstance().payOrder(ThanhToan.this, token, "demozpdk://app",
                        new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(String s, String s1, String s2) {
                                UpdateStatus("Done");
                            }

                            @Override
                            public void onPaymentCanceled(String s, String s1) {

                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {

                            }
                        });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateStatus(String status) {
        FirebaseFirestore db= FirebaseFirestore.getInstance();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", status);

        db.collection("Rental")
                .document(rentalID)
                .update(updateData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ThanhToan.this, "Lịch đặt của bạn đã được hệ" +
                                    " thống ghi nhận", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(ThanhToan.this, ChonSan.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("userID", userID);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    private void LoadDanhSach() {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            purchasedServices = new ArrayList<>();

            // Lấy thông tin tổng tiền từ collection "Rental"
            db.collection("Rental").document(rentalID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        tv_tongTien.setText(documentSnapshot.getDouble("total").toString() + "VND");
                    });



            // Truy vấn danh sách đồ uống đã mua từ collection "Drink_Rental"
            db.collection("Drink_Rental")
                    .whereEqualTo("rental_id", rentalID)
                    .get()
                    .addOnSuccessListener(drinkRentalQueryDocumentSnapshots -> {

                        for (QueryDocumentSnapshot drinkRentalSnapshot : drinkRentalQueryDocumentSnapshots) {
                            String drinkID = drinkRentalSnapshot.getString("drink_id");
                            int quantity = drinkRentalSnapshot.getLong("quantity").intValue();

                            // Truy vấn thông tin về đồ uống từ collection "Drink"
                            db.collection("Drink")
                                    .document(drinkID)
                                    .get()
                                    .addOnSuccessListener(drinkSnapshot -> {
//                                        purchasedServices = new ArrayList<>()
                                        String drinkName = drinkSnapshot.getString("name");
                                        String img_url = drinkSnapshot.getString("img_url");
                                        double drinkPrice = drinkSnapshot.getDouble("price");

                                        PurchasedService purchasedService =
                                                new PurchasedService(drinkID, drinkName, "Drink",
                                                img_url, drinkPrice, quantity);
                                        purchasedServices.add(purchasedService);
                                        purchasedServiceAdapter.setPurchasedServiceList(purchasedServices);

                                    })
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                        } else {
                                        }
                                    });
                        }
                        if (drinkRentalQueryDocumentSnapshots.isEmpty()) {
                        }
                    });



            // Truy vấn danh sách sân đã thuê từ collection "Stadium_Rental"
            db.collection("Stadium_Rental")
                    .whereEqualTo("rental_id", rentalID)
                    .get()
                    .addOnSuccessListener(stadiumRentalQueryDocumentSnapshots -> {

                        for (QueryDocumentSnapshot stadiumRentalSnapshot : stadiumRentalQueryDocumentSnapshots) {
                            String stadiumID = stadiumRentalSnapshot.getString("stadium_id");
                            int rental_time = stadiumRentalSnapshot.getLong("rental_time").intValue();

                            // Truy vấn thông tin về sân từ collection "Stadium"
                            db.collection("Stadium")
                                    .document(stadiumID)
                                    .get()
                                    .addOnSuccessListener(stadiumSnapshot -> {
                                        String stadiumName = stadiumSnapshot.getString("name");
                                        String img_url = stadiumSnapshot.getString("img_url");
                                        double stadiumPrice = stadiumSnapshot.getDouble("price");

                                        PurchasedService purchasedService =
                                                new PurchasedService(stadiumID, stadiumName,
                                                "Stadium", img_url, stadiumPrice, rental_time);
                                        purchasedServices.add(purchasedService);
                                        purchasedServiceAdapter.setPurchasedServiceList(purchasedServices);
                                    })
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                        } else {
                                        }
                                    });
                        }

                        if (stadiumRentalQueryDocumentSnapshots.isEmpty()) {
                        }
                    });

            // Chờ hoàn thành của cả ba hàm bất đồng bộ

    }



    private void SetupAdapter() {
        purchasedServiceAdapter = new PurchasedServiceAdapter(new ArrayList<>());
        recyclerView.setAdapter(purchasedServiceAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }



    private void FindViewByIds() {
        btn_goback = findViewById(R.id.btn_khachhang_thanhtoan_goback);
        tv_tongTien = findViewById(R.id.tv_khachhang_thanhtoan_tongtien2);
        recyclerView = findViewById(R.id.rcv_khachhang_thanhtoan);
        progressBar = findViewById(R.id.progressBar_khachhang_thanhtoan);

        userID = getIntent().getStringExtra("userID");
        rentalID = getIntent().getStringExtra("rentalID");

        btn_pay = findViewById(R.id.btn_khachhang_thanhtoan);

        radioGroup_phuongthuc = findViewById(R.id.radioGroup_paymentMethod);
        radioButtonCash = findViewById(R.id.radioButton_cash);
        radioButtonZalo = findViewById(R.id.radioButton_zalo);


        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(554, Environment.SANDBOX);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}
