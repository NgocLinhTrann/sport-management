package com.example.dangki.Admin.San;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Admin.Douong.CapNhatDoUong;
import com.example.dangki.Admin.Douong.Menu;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CapNhatSan extends AppCompatActivity {
    static int RESULT_UPDATE_SUCCESS =1;
    static int RESULT_DELETE_SUCCESS =2;
    ImageView btn_goback;
    Button btn_capNhat, btn_xoa;
    EditText edt_tenSan, edt_giaSan, edt_idSan;
    ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.admin_san_update);

        FindViewByIds();


        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*
        Lấy dữ liệu intent truyền từ Menu item
         */
        Intent intent = getIntent();
        if(intent != null){
            edt_idSan.setHint(intent.getStringExtra("Id"));
            edt_tenSan.setHint(intent.getStringExtra("name"));
            edt_giaSan.setHint(intent.getDoubleExtra("price", 0.0) +"");
        }

        /*
        Xử lý sự kiện cập nhật thông tin sân
         */
        btn_capNhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> updateData = new HashMap<>();

                if(!TextUtils.isEmpty(edt_tenSan.getText().toString().trim())){
                    updateData.put("name", edt_tenSan.getText().toString());
                }
                if(!TextUtils.isEmpty(edt_giaSan.getText().toString().trim())){
                    updateData.put("price", Double.parseDouble(edt_giaSan.getText().toString()));
                }
                if(updateData.size() >0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(CapNhatSan.this);
                    builder.setTitle("Xác nhận cập nhật");
                    builder.setMessage("Bạn có chắc chắn muốn cập nhật?");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            progressBar.setVisibility(View.VISIBLE);
                            firestore.collection("Stadium").document(intent.getStringExtra("Id"))
                                    .update(updateData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(CapNhatSan.this, "Cập nhật thành công",
                                                    Toast.LENGTH_SHORT).show();
                                            setResult(RESULT_UPDATE_SUCCESS);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(CapNhatSan.this, e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                        }
                    });
                    builder.setNegativeButton("Hủy", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }else {
                    Toast.makeText(CapNhatSan.this, "Vui lòng nhập các giá trị để cập nhật",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*
        Xử lý sự kiện xóa sân
         */
        btn_xoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CapNhatSan.this);
                builder.setTitle("Xác nhận xóa");
                builder.setMessage("Bạn có chắc chắn muốn xóa?");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                        Map<String, Object> xoaData = new HashMap<>();
                        xoaData.put("isDelete", true);

                        progressBar.setVisibility(View.VISIBLE);

                        firestore.collection("Stadium").document(intent.getStringExtra("Id"))
                                .update(xoaData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CapNhatSan.this, "Xóa thành công",
                                                Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_DELETE_SUCCESS);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CapNhatSan.this, e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                    }
                });
                builder.setNegativeButton("Hủy", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void FindViewByIds() {
        btn_goback = findViewById(R.id.btn_admin_san_update_goback);
        btn_capNhat = findViewById(R.id.btn_admin_san_update_capnhat);
        btn_xoa = findViewById(R.id.btn_admin_san_update_xoa);
        edt_idSan = findViewById(R.id.edt_admin_san_update_id);
        edt_giaSan = findViewById(R.id.edt_admin_san_update_giatien);
        edt_tenSan = findViewById(R.id.edt_admin_san_update_tensan);
        progressBar = findViewById(R.id.progressBar_admin_san_update);
    }
}
