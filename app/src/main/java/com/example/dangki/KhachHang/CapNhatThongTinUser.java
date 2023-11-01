package com.example.dangki.KhachHang;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Admin.Douong.ThemDoUong;
import com.example.dangki.R;
import com.example.dangki.Register;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.SimpleFormatter;

import de.hdodenhof.circleimageview.CircleImageView;

public class CapNhatThongTinUser extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    CircleImageView imv_user;
    EditText edt_ten, edt_birthdate, edt_email, edt_sdt;
    RadioGroup radioGroup_gioitinh;
    ProgressBar progressBar;
    String userID, img_url;
    Uri mImageUri;
    RadioGroup genderRadioGroup;
    RadioButton radioButtonNam, radioButtonNu;
    ImageView btn_addimg;
    Button btn_capnhat;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_capnhatthongtin);

        FindViewByIds();
        LoadData();
        imv_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(CapNhatThongTinUser.this, android.R.style
                        .Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.dialog_fullscreen_image);

                ImageView imageViewFullscreen = dialog.findViewById(R.id.imageView_fullscreen);

                if(imv_user.getTag() != null ){
                    Picasso.get().load((String) imv_user.getTag()).into(imageViewFullscreen);

                }else {
                    Picasso.get().load(R.drawable.profile).into(imageViewFullscreen);
                }
                dialog.show();
            }
        });
        btn_addimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenFileExplorer();
            }
        });
        edt_birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Hiển thị DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(CapNhatThongTinUser.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        // Xử lý ngày sinh được chọn
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        Calendar currentDate = Calendar.getInstance();
                        currentDate.set(Calendar.HOUR_OF_DAY, 0);
                        currentDate.set(Calendar.MINUTE, 0);
                        currentDate.set(Calendar.SECOND, 0);

                        if (selectedDate.before(currentDate)) {
                            // Ngày sinh hợp lệ
                            String formattedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                            edt_birthdate.setText(formattedDate);
                        } else {
                            // Ngày sinh không hợp lệ
//                            birthday.setError("Ngày sinh không hợp lệ");
                            edt_birthdate.setText("");
                            Toast.makeText(CapNhatThongTinUser.this, "Ngày sinh không hợp lệ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, year, month, day);

                datePickerDialog.show();
            }
        });
        btn_capnhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mImageUri != null){
                    CapNhatAnh();
                }else {
                    Map<String, Object> updateData = CapNhatDuLieu(null);
                    if(updateData.size() >0){
                        CapNhatDB(updateData);
                    }
                }
            }
        });
    }

    private void OpenFileExplorer() {
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

            Picasso.get().load(mImageUri).into(imv_user);
            imv_user.setTag(mImageUri.toString());
        }
    }
    String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void LoadData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        edt_ten.setHint(documentSnapshot.getString("name"));
                        edt_email.setHint(documentSnapshot.getString("email"));
                        edt_sdt.setHint(documentSnapshot.getString("phoneNumber"));
                        Date birthDate = documentSnapshot.getDate("birthdate");
                        String img_url_db = documentSnapshot.getString("img_url");
                        String gioiTinh = documentSnapshot.getString("gender");
                        if(gioiTinh !=null){
                            if(gioiTinh.equals("Nam")){
                                radioButtonNam.setChecked(true);
                            }else{
                                radioButtonNu.setChecked(true);
                            }
                        }

                        if(birthDate != null){
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            String birthDateString = dateFormat.format(birthDate);
                            edt_birthdate.setHint(birthDateString);
                        }
                        if(img_url_db != null){
                            Picasso.get().load(img_url_db).into(imv_user);
                            imv_user.setTag((img_url_db));
                        }
                    }
                });
    }
    private void CapNhatAnh() {
        if(mImageUri !=null){
            progressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageRef = firebaseStorage.getReference();

            StorageReference imgStorageRef = storageRef.child("User/" + System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            imgStorageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imgStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    Map<String, Object> updateData =CapNhatDuLieu(imageUrl);
                                    CapNhatDB(updateData);
                                }
                            });
                        }
                    });
        }
    }

    private void CapNhatDB(Map<String, Object> updateData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .document(userID)
                .update(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CapNhatThongTinUser.this, "Cập nhật thông tin " +
                                "thành công", Toast.LENGTH_SHORT).show();
                        setResult(1);
                        finish();
                    }
                });
    }

    private Map<String, Object> CapNhatDuLieu(String imageUrl) {
        Map<String, Object> updateData = new HashMap<>();
        if(!TextUtils.isEmpty(edt_ten.getText().toString())){
            updateData.put("name", edt_ten.getText().toString());
        }
        if(!TextUtils.isEmpty(edt_sdt.getText().toString())){
            updateData.put("phoneNumber", edt_sdt.getText().toString());
        }
        if(!TextUtils.isEmpty(edt_birthdate.getText().toString())){
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date birthDate = null;
            try {
                birthDate = dateFormat.parse(edt_birthdate.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (birthDate != null) {
                updateData.put("birthdate", birthDate);
            }
        }
        if(radioButtonNam.isChecked()){
            updateData.put("gender", "Nam");
        }else{
            updateData.put("gender", "Nữ");
        }

        if(imageUrl != null){
            updateData.put("img_url", imageUrl);
        }
        return updateData;
    }

    private void FindViewByIds() {
        imv_user = findViewById(R.id.imv_khachhang_capnhatthongtin);
        edt_ten = findViewById(R.id.edt_khachhang_capnhatthongtin_hoten);
        edt_email = findViewById(R.id.edt_khachhang_capnhatthongtin_email);
        edt_birthdate = findViewById(R.id.edt_khachhang_capnhatthongtin_ngaysinh);
        edt_sdt = findViewById(R.id.edt_khachhang_capnhatthongtin_sdt);
        edt_email.setEnabled(false);
        genderRadioGroup = findViewById(R.id.genderRadio);
        radioButtonNam = findViewById(R.id.radioBtNam);
        radioButtonNu = findViewById(R.id.radioBtNu);
        btn_addimg = findViewById(R.id.btn_khachhang_capnhatthongtin_addimg);
        btn_capnhat = findViewById(R.id.btn_khachhang_capnhatthongtin_capnhat);
        progressBar = findViewById(R.id.progressBar_khachhang_capnhatthongtin);

        userID = getIntent().getStringExtra("userID");
    }
}
