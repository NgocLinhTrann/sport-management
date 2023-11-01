package com.example.dangki.Admin.Douong;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Model.DoUong;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ThemDoUong extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    static final int RESULT_ADD_SUCCESS = 2;
    Uri mImageUri;
    ProgressBar progressBar;
    ImageView btn_goback, img_chooser;
    EditText edt_gia, edt_sl, edt_ten;
    Button btn_themDoUong;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.admin_douong_add);

        findViewByIds();

        FocusChecking();
        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        img_chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        btn_themDoUong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkMissingField() == false){
                    ThemHinhAnhDoUong();
                }
            }
        });
    }

    String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void ThemDoUong(String img_url) {
        String ten_douong_db = edt_ten.getText().toString();
        double gia_do_uong_db = Double.parseDouble(edt_gia.getText().toString());
        int sl_conlai_db = Integer.parseInt(edt_sl.getText().toString());

//        DoUong doUong= new DoUong(ten_douong_db, img_url, gia_do_uong_db, sl_conlai_db, false);
        Map<String, Object> doUong = new HashMap<>();
        doUong.put("img_url", img_url);
        doUong.put("isDelete", false);
        doUong.put("name", ten_douong_db);
        doUong.put("remain", sl_conlai_db);
        doUong.put("price", gia_do_uong_db);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference drinksRef = db.collection("Drink");
        drinksRef.add(doUong).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                Toast.makeText(ThemDoUong.this, "Thêm đồ uống thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_ADD_SUCCESS);
                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                        Toast.makeText(ThemDoUong.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void ThemHinhAnhDoUong() {
        if(mImageUri !=null){
            progressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageRef = firebaseStorage.getReference();

            StorageReference imgStorageRef = storageRef.child("Drink/" + System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            imgStorageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imgStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    ThemDoUong(imageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                            Toast.makeText(ThemDoUong.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
        btn_goback = findViewById(R.id.btn_admin_douong_themdouong_goback);
        img_chooser = findViewById(R.id.img_chooser);
        edt_gia = findViewById(R.id.edt_admin_khachhang_info_email);
        edt_sl = findViewById(R.id.edt_admin_douong_themdouong_sl);
        edt_ten = findViewById(R.id.edt_admin_douong_themdouong_tendouong);
        btn_themDoUong = findViewById(R.id.btn_admin_douong_themdouong_add);
        progressBar = findViewById(R.id.progressBar_admin_douong_themdouong);
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(img_chooser);
        }
    }

    void FocusChecking(){
        edt_ten.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    if(TextUtils.isEmpty(edt_ten.getText().toString().trim())){
                        edt_ten.setError("Bạn chưa nhập tên đồ uống");
                    }
                }
            }
        });
        edt_sl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    if(TextUtils.isEmpty(edt_sl.getText().toString().trim())){
                        edt_sl.setError("Bạn chưa nhập số lượng đồ uống");
                    }
                }
            }
        });
        edt_gia.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    if(TextUtils.isEmpty(edt_gia.getText().toString().trim())){
                        edt_gia.setError("Bạn chưa nhập giá đồ uống");
                    }
                }
            }
        });
    }
}
