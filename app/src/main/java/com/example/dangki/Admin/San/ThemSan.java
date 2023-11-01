package com.example.dangki.Admin.San;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.example.dangki.Admin.Douong.ThemDoUong;
import com.example.dangki.Model.DoUong;
import com.example.dangki.Model.San;
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

public class ThemSan extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    static  final int RESULT_ADD_SUCCESS =2;
    Button btn_themSan;
    ImageView imv_chonSan, btn_goback;
    Uri mImageUri;
    EditText edt_tenSan, edt_giaSan;
    ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.admin_san_add);

        findViewByIds();
        /*
        Check mất focus các EditText
         */
        FocusChecking();

        /*
        goback button
         */
        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*
        Load ảnh từ thư viện lên ImageView
         */
        imv_chonSan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        /*
        Thêm sân vào FireStore
         */
        btn_themSan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckMissingField() == false){
                    ThemHinhAnhSan();
                }
            }
        });
    }

    private void ThemHinhAnhSan() {
        if(mImageUri !=null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageRef = firebaseStorage.getReference();
            progressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar

            StorageReference imgStorageRef = storageRef.child("Stadium/" + System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            imgStorageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imgStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    ThemSan(imageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                            Toast.makeText(ThemSan.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    void ThemSan(String img_url){
        String tenSan = edt_tenSan.getText().toString();
        double giaSan = Double.parseDouble(edt_giaSan.getText().toString());

        Map<String, Object> addData = new HashMap<>();
        addData.put("img_url", img_url);
        addData.put("isDelete", false);
        addData.put("name", tenSan);
        addData.put("price", giaSan);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference sanRef = db.collection("Stadium");
        sanRef.add(addData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                        Toast.makeText(ThemSan.this, "Thêm sân thành công", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_ADD_SUCCESS);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                        Toast.makeText(ThemSan.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(imv_chonSan);
        }
    }
    private void FocusChecking() {
            edt_tenSan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        if(TextUtils.isEmpty(edt_tenSan.getText().toString().trim())){
                            edt_tenSan.setError("Tên sân không được trống");
                        }
                    }
                }
            });

        edt_giaSan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    if(TextUtils.isEmpty(edt_giaSan.getText().toString().trim())){
                        edt_giaSan.setError("Giá sân không được trống");
                    }
                }
            }
        });
    }

    boolean CheckMissingField(){
        boolean isMissing = false;
        if(TextUtils.isEmpty(edt_tenSan.getText().toString().trim())){
            isMissing = true;
        } else if (TextUtils.isEmpty((edt_giaSan.getText().toString().trim()))) {
            isMissing = true;
        }
        return isMissing;
    }
    private void findViewByIds() {
        btn_themSan = findViewById(R.id.btn_admin_douong_themdouong_add);
        btn_goback = findViewById(R.id.btn_admin_san_themsan_goback);
        edt_giaSan = findViewById(R.id.edt_admin_san_themsan_gia);
        edt_tenSan = findViewById(R.id.edt_admin_san_themsan_tensan);
        imv_chonSan = findViewById(R.id.imv_admin_san_themsan_anhsan);
        progressBar = findViewById(R.id.progressBar_admin_san_themsan);
    }
    String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}
