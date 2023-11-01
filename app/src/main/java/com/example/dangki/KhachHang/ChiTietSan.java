package com.example.dangki.KhachHang;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Calendar.CalendarActivity;
import com.example.dangki.Model.San;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietSan extends AppCompatActivity {
    static final int REQUEST_DATSAN_CODE =1;
    Button btnDatSan;
    TextView tv_tenSan, tv_chonphut, tv_giaSan, tv_tinhTrangSan, btn_chonGio;
    ImageView btn_goback, imv_anhsan;
    Date selectedStartDate,selectedEndDate;
    LocalTime selected_StartTime_final, selected_EndTime_final;
    int gioChoi=0;
    double totalDb=0.0, gia_san=0.0;
    String sanId="", img_url, tenSan, userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.khachhang_chonsan_chitietsan);

        FindViewByIds();
        LoadDuLieuSan();

        btn_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        tv_chonphut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showBottomDialog();
//            }
//        });

        imv_anhsan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imgUrl = (String) imv_anhsan.getTag();

                Dialog dialog = new Dialog(ChiTietSan.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.dialog_fullscreen_image);

                ImageView imageViewFullscreen = dialog.findViewById(R.id.imageView_fullscreen);
                Picasso.get().load(imgUrl).into(imageViewFullscreen);

                dialog.show();
            }
        });

        btnDatSan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(), CalendarActivity.class);
                intent.putExtra("idSan_intent", sanId);
                intent.putExtra("stadium_price", gia_san);
                intent.putExtra("userID", userID);
                startActivityForResult(intent, REQUEST_DATSAN_CODE);
            }
        });

//        btnDatSan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                checkSanAvailability(sanId, selectedStartDate, selectedEndDate);
////                Toast.makeText(ChiTietSan.this, "starttime: "+ selectedStartDate.toString()
////                        +"             end_time: "+ selectedEndDate.toString(), Toast.LENGTH_LONG).show();
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_DATSAN_CODE && resultCode ==1){
            totalDb =data.getDoubleExtra("totalDb", 0.0);
            selected_StartTime_final = LocalTime.parse(data.getStringExtra("start_time"));
            selected_StartTime_final = LocalTime.parse(data.getStringExtra("end_time"));
            gioChoi = data.getIntExtra("rental_time", 0);

            finish();
        }
    }

    private String calculateEndTime(String startTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(startTime));
            calendar.add(Calendar.MINUTE, Integer.parseInt(tv_chonphut.getText().toString().split(" ")[0]));
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

//    private void checkSanAvailability(String sanId, Date selectedStartTime, Date selectedEndTime) {
//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//
//        // Chuyển đổi selectedStartTime và selectedEndTime từ định dạng chuỗi sang timestamp
//
//        firestore.collection("Stadium_Rental")
//                .whereEqualTo("stadium_id", sanId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            if (task.getResult() != null && !task.getResult().isEmpty()) {
//                                boolean isBook= false;
//                                List<DocumentSnapshot> documents =task.getResult().getDocuments();
//                                for(DocumentSnapshot documentSnapshot : documents){
//                                    Date start_time = documentSnapshot.getDate("start_time");
//                                    Date end_time = documentSnapshot.getDate("end_time");
//                                    if(selectedStartTime.compareTo(start_time) >=0
//                                            && selectedStartTime.compareTo(end_time) <=0){
//                                        isBook = true;
//                                        Toast.makeText(ChiTietSan.this, "Đã có lịch đặt từ: "
//                                                + start_time.toString() +"  -  "+end_time.toString(),
//                                                Toast.LENGTH_SHORT).show();
//                                    } else if (selectedEndTime.compareTo(start_time) >=0
//                                            && selectedEndTime.compareTo(end_time) <=0) {
//                                        isBook = true;
//                                        Toast.makeText(ChiTietSan.this, "Đã có lịch đặt từ: "
//                                                        + start_time.toString() +"  -  "+end_time.toString(),
//                                                Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                                if(isBook == false){
//                                    Toast.makeText(ChiTietSan.this, "Lịch trống: " ,
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            } else {
//                                Toast.makeText(ChiTietSan.this, "Đã xảy ra lỗi: " +
//                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                task.getException().printStackTrace();
//                                Log.d("Lôi",task.getException().getMessage());
//                            }
//                    }
//                    }
//
//                });
//    }

    private void ShowDateTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ChiTietSan.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(Calendar.YEAR, year);
                selectedCalendar.set(Calendar.MONTH, month);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (selectedCalendar.before(Calendar.getInstance())) {
                    selectedCalendar = Calendar.getInstance();
                    Toast.makeText(ChiTietSan.this, "Không thể chọn thời gian trong quá khứ",
                            Toast.LENGTH_SHORT).show();
                }

                ShowTimePickerDialog(selectedCalendar);
            }
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void ShowTimePickerDialog(Calendar selectedCalendar) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(ChiTietSan.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (hourOfDay >= 23 || hourOfDay < 5) {
                    Toast.makeText(ChiTietSan.this, "Không thể chọn thời gian từ 11PM đến 5AM",
                            Toast.LENGTH_SHORT).show();
                    btnDatSan.setEnabled(false);
                    btnDatSan.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C5C5C5")));
                } else {
                    Calendar currentCalendar = Calendar.getInstance();
                    if (selectedCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            selectedCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            selectedCalendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH)) {
                        if (hourOfDay < currentCalendar.get(Calendar.HOUR_OF_DAY) ||
                                (hourOfDay == currentCalendar.get(Calendar.HOUR_OF_DAY)
                                        && minute < currentCalendar.get(Calendar.MINUTE))) {
                            Toast.makeText(ChiTietSan.this, "Không thể chọn thời gian " +
                                    "trong quá khứ của ngày hiện tại", Toast.LENGTH_SHORT).show();
                            btnDatSan.setEnabled(false);
                            btnDatSan.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C5C5C5")));
                            return;
                        }
                    }

                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCalendar.set(Calendar.MINUTE, minute);

                    selectedStartDate = selectedCalendar.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String selectedDateTime = sdf.format(selectedCalendar.getTime());

                    btn_chonGio.setText(selectedDateTime);

                    btnDatSan.setEnabled(true);
                    btnDatSan.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3CB371")));
                }
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void FindViewByIds() {
        tv_chonphut = findViewById(R.id.tv_khachhang_chitietsan_chonphut);
        tv_giaSan = findViewById(R.id.tv_khachhang_chitietsan_gia);
        tv_tenSan = findViewById(R.id.tv_khachhang_chitietsan_tensan);
        tv_tinhTrangSan = findViewById(R.id.tv_khachhang_chitietsan_tinhtrang);
        btn_goback = findViewById(R.id.btn_khachhang_chitietsan_goback);
        imv_anhsan = findViewById(R.id.imv_khachhang_chitietsan_anhsan);
        btn_chonGio = findViewById(R.id.tv_khachhang_chitietsan_ChonGio);
        btnDatSan = findViewById(R.id.btn_khachhang_chitietsan_datsan);
    }

    private void LoadDuLieuSan() {
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        sanId = intent.getStringExtra("idSan_intent");
        gia_san = intent.getDoubleExtra("price", 0.0);
        img_url = intent.getStringExtra("img_url");
        tenSan = intent.getStringExtra("name");

        tv_tenSan.setText(tenSan);
        tv_giaSan.setText(String.valueOf(gia_san));
        Picasso.get().load(img_url).into(imv_anhsan);
        imv_anhsan.setTag(img_url);

//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//        firestore.collection("Stadium").document(sanId).get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    String idSan = documentSnapshot.getId();
//                    String tenSan = documentSnapshot.getString("name");
//                    double giaSan = documentSnapshot.getDouble("price");
//                    String img_url = documentSnapshot.getString("img_url");
//                    boolean isDelete = documentSnapshot.getBoolean("isDelete");
//                    San san = new San(idSan, img_url, tenSan, giaSan, isDelete);
//
//                    tv_tenSan.setText(tenSan);
//                    tv_giaSan.setText(String.valueOf(giaSan));
//                    Picasso.get().load(img_url).into(imv_anhsan);
//                    imv_anhsan.setTag(img_url);
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(ChiTietSan.this, "Đã xảy ra lỗi: " + e.getMessage(),
//                            Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                });
    }

    private void showBottomDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ChiTietSan.this);
        bottomSheetDialog.setContentView(R.layout.bottom_chonphut);

        NumberPicker numberPicker = bottomSheetDialog.findViewById(R.id.numberpicker_khachhang_chitietsan_sophut);
        Button btnHuy = bottomSheetDialog.findViewById(R.id.btn_khachhang_chitietsan_huy);
        Button btnChon = bottomSheetDialog.findViewById(R.id.btn_khachhang_chitietsan_chon);

        String[] phutValues = {"30", "60", "90", "120", "150", "180"};
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(phutValues.length - 1);
        numberPicker.setDisplayedValues(phutValues);

        btnChon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_chonphut.setText(phutValues[numberPicker.getValue()] + " phút");

                // Tạo một đối tượng Calendar từ selectedStartDate
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedStartDate);

                // Thêm số phút được chọn vào selectedStartDate
                int phut = Integer.parseInt(phutValues[numberPicker.getValue()]);
                calendar.add(Calendar.MINUTE, phut);

                // Lưu selectedEndDate
                selectedEndDate = calendar.getTime();

                bottomSheetDialog.dismiss();
            }
        });

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }
}
