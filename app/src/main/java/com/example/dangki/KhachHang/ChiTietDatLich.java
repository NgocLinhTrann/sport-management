package com.example.dangki.KhachHang;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Model.PurchasedService;
import com.example.dangki.R;
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

import java.util.ArrayList;
import java.util.List;

public class ChiTietDatLich extends AppCompatActivity {

    String rentalID, status;
    TextView tv_tongTien;
    ImageView btn_goback;
    RecyclerView recyclerView;
    PurchasedServiceAdapter purchasedServiceAdapter;
    ProgressBar progressBar;
    List<PurchasedService> purchasedServices;
    Button btn_huy;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_lichsu_chitiet);

        FindViewByIds();
        SetupAdapter();
        LoadData();

        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_huy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HuyDatSan();
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
                        HuyDatSan();
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
                                        LoadData();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
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
                                            LoadData();
                                        });
                            }
                        });
            }
        });
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void HuyDatSan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChiTietDatLich.this);
        builder.setTitle("Hủy đặt lịch");
        builder.setMessage("Quý khách xác nhận hủy đặt lịch");
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressBar.setVisibility(View.VISIBLE);
                HuyDatSanDB();
            }
        });
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void HuyDatSanDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Task<?>> tasks = new ArrayList<>();

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

                    // Finish Activity hiện tại
                    setResult(1);
                    progressBar.setVisibility(View.GONE);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Xảy ra lỗi trong quá trình thực hiện

                    // Hiển thị thông báo hoặc xử lý lỗi tùy theo yêu cầu của bạn
                });
    }


    private void SetupAdapter() {
        purchasedServiceAdapter = new PurchasedServiceAdapter(new ArrayList<>());
        purchasedServiceAdapter.setStatus(status);
        recyclerView.setAdapter(purchasedServiceAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void LoadData() {
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

    private void FindViewByIds() {
        recyclerView = findViewById(R.id.rcv_admin_datlich_chitiet);
        btn_huy = findViewById(R.id.btn_khachang_lichsu_huy);
        tv_tongTien = findViewById(R.id.tv_khachhang_lichsu_tongtien);
        btn_goback = findViewById(R.id.btn_admin_datlich_chitiet_goback);
        progressBar = findViewById(R.id.progressBar_khachhang_lichsu);

        rentalID = getIntent().getStringExtra("rentalID");
        status = getIntent().getStringExtra("status");
        if(status.equals("Done")){
            btn_huy.setVisibility(View.GONE);
        }
    }
}
