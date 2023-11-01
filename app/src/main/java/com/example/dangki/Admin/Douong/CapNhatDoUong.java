package com.example.dangki.Admin.Douong;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CapNhatDoUong extends AppCompatActivity {
    static final int RESULT_CODE_CAPNHAT =1;
    static  final int RESULT_CODE_XOA =2;
    Button btn_capnhat, btn_xoa;
    ProgressBar progressBar;
    ImageView btn_goback;
    TextView edt_gia, edt_ten, edt_sl, edt_id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.admin_douong_update);

        findViewByIds();
        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*
        Lấy dữ liệu từ Intent
         */
        Intent intent  =getIntent();
        if(intent !=null){
            edt_id.setHint(intent.getStringExtra("Id"));
            edt_ten.setHint(intent.getStringExtra("ten"));
            edt_sl.setHint(intent.getIntExtra("sl",0)+ "");
            edt_gia.setHint(intent.getDoubleExtra("gia", 0.0) +"");
        }

        /*
        Cập nhật đồ uống
         */
        btn_capnhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    Map<String, Object> updateData = new HashMap<>();

                    if(!TextUtils.isEmpty(edt_ten.getText().toString().trim())){
                        updateData.put("name", edt_ten.getText().toString());
                    }
                    if(!TextUtils.isEmpty(edt_gia.getText().toString().trim())){
                        updateData.put("price", Double.parseDouble(edt_gia.getText().toString()));
                    }
                    if(!TextUtils.isEmpty(edt_sl.getText().toString().trim())){
                        updateData.put("remain", Integer.parseInt(edt_sl.getText().toString()));
                    }

                    if(updateData.size() >0){
                        progressBar.setVisibility(View.VISIBLE);
                        firestore.collection("Drink").document(intent.getStringExtra("Id"))
                                .update(updateData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CapNhatDoUong.this, "Cập nhật thành công",
                                                Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_CODE_CAPNHAT);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CapNhatDoUong.this, "Cập nhật thất bại",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), Menu.class));
                                    }
                                });
                    }
                }

        });

        /*
        Xóa đồ uống
         */
        btn_xoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CapNhatDoUong.this);
                builder.setTitle("Xác nhận xóa đồ uống");
                builder.setMessage("Bạn có chắc chắn muốn xóa đồ uống này?");

                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        Map<String, Object> xoaData = new HashMap<>();
                        xoaData.put("isDelete", true);
                        progressBar.setVisibility(View.VISIBLE);
                        firestore.collection("Drink").document(intent.getStringExtra("Id"))
                                .update(xoaData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CapNhatDoUong.this,
                                                "Xóa đồ uống thành công", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_CODE_XOA);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(CapNhatDoUong.this,
                                                e.getMessage(), Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_CODE_XOA);
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

    private boolean checkMissingField() {
        boolean isMissing = false;
        if(TextUtils.isEmpty(edt_gia.getText().toString().trim())){
            edt_gia.setError("Bạn chưa nhập giá đồ uống");
            isMissing= true;
        }
        if (TextUtils.isEmpty(edt_sl.getText().toString().trim())) {
            edt_sl.setError("Bạn chưa nhập số lượng đồ uống");
            isMissing= true;
        }
        if (TextUtils.isEmpty(edt_ten.getText().toString().trim())) {
            edt_ten.setError("Bạn chưa nhập tên đồ uống");
            isMissing= true;
        }
        return isMissing;
    }
    private void findViewByIds() {
        btn_capnhat = findViewById(R.id.btn_admin_douong_update_capnhat);
        btn_xoa = findViewById(R.id.btn_admin_douong_update_xoa);
        btn_goback = findViewById(R.id.btn_admin_douong_update_goback);
        edt_gia = findViewById(R.id.edt_admin_douong_update_giatien);
        edt_sl = findViewById(R.id.edt_admin_douong_update_sl);
        edt_ten = findViewById(R.id.edt_admin_douong_update_tendouong);
        edt_id = findViewById(R.id.edt_admin_douong_update_id);
        progressBar = findViewById(R.id.progressBar_admin_douong_update);
    }
}
