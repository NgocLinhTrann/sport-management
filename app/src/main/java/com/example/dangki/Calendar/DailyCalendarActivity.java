package com.example.dangki.Calendar;

import static android.content.ContentValues.TAG;

import static com.example.dangki.Calendar.CalendarUtils.selectedDate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.CaseMap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.dangki.Dangnhapthanhcong;
import com.example.dangki.KhachHang.ChiTietSan;
import com.example.dangki.KhachHang.ChonSan;
import com.example.dangki.KhachHang.DatLichThanhCong;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.checkerframework.checker.units.qual.A;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class DailyCalendarActivity extends AppCompatActivity
{
    static final int REQUEST_THANHCONG =1;
    static final int RESULT_DATSAN_SUCCESS =2;
    private boolean isFirstBookingComplete = false;

    private TextView monthDayText, tv_chonGioChoi;
    private TextView dayOfWeekTV;
    private ListView hourListView;

    List<LocalTime> start_time_list;
    List<LocalTime> end_time_list;
    String sanID = "", rentalID ="", userID;
    Button btn_datlich, btn_themLich;
    LocalTime selected_StartTime_final, selected_EndTime_final;
    Date selected_EndTime;
    double stadium_price = 0.0, totalDb = 0.0;
    int gioChoi = 0;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_daily_calendar);
        initWidgets();
        GetData();



        btn_themLich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DailyCalendarActivity.this);
                builder.setTitle("Xác nhận thêm lich đặt sân")
                        .setMessage("Quý khách vui lòng xác nhận thêm lịch đặt sân?")
                        .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CompletableFuture<Boolean> firstBookingFuture = checkFirstBookingAsync();

                                firstBookingFuture.thenAccept(isFirstBooking -> {
                                    if (isFirstBooking) {
                                        // Thực hiện công việc sau khi là first booking
                                        AddFirstBooking();
                                    } else {
                                        // Thực hiện công việc khi không phải first booking
                                        AddBooking();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Đóng dialog
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }


    /*
    Phương thức này dùng để thêm vào collection "Stadium_Rental" sau khi đã check là khách hàng đã
    có đặt lịch (Đã chọn 1 sân), đồng thời sẽ cập nhật lại "total" trong "Rental"
     */
    private void AddBooking() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        Date start_time_add_to_db = new Date();
        Date end_time_add_to_db = new Date();
        start_time_add_to_db = ConvertLocalDateTimeToDate(selectedDate, selected_StartTime_final);
        end_time_add_to_db = ConvertLocalDateTimeToDate(selectedDate, selected_EndTime_final);

        // Dữ liệu cần thêm vào bảng Stadium_Rental
        Map<String, Object> stadiumRentalData = new HashMap<>();
        stadiumRentalData.put("rental_id", rentalID);
        stadiumRentalData.put("start_time", start_time_add_to_db);
        stadiumRentalData.put("end_time", end_time_add_to_db);
        stadiumRentalData.put("rental_time", gioChoi);
        stadiumRentalData.put("stadium_id", sanID);

        // Thêm dữ liệu vào bảng Stadium_Rental
        DocumentReference stadiumRentalRef = db.collection("Stadium_Rental").document();
        batch.set(stadiumRentalRef, stadiumRentalData);

        // Cập nhật dữ liệu trong bảng Rental
        double total_add = totalDb + (stadium_price / 60) * gioChoi;
        Map<String, Object> updateTotal= new HashMap<>();
        updateTotal.put("total", total_add);

        // Cập nhật dữ liệu trong bảng Rental
        DocumentReference rentalRef = db.collection("Rental").document(rentalID);
        batch.update(rentalRef, updateTotal);

        // Thực hiện tác vụ Batch
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressBar.setVisibility(View.GONE);

                        Intent intent = new Intent(getApplicationContext(), DatLichThanhCong.class);
                        intent.putExtra("userID", userID);
                        intent.putExtra("rentalID", rentalID);

                        startActivityForResult(intent, REQUEST_THANHCONG);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Có lỗi xảy ra khi thêm dữ liệu",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*
    Phương thức này dùng để thêm lịch đặt sân vào Firestore khi khách hàng lần đầu đặt lịch, lần đầu
    ở đây không có nghĩa là lần đầu đặt trên app, mà là đơn hàng mới, vì 1 lần ta có thể đặt nhiều
    sân, nhiều đồ uống, nên nếu lần đầu ta sẽ tạo một Document ở Collection "Rental" với status là
    "Booking", sau đó thêm dữ liệu vào Collection "Drink_Rental"
     */
    public void AddFirstBooking() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Dữ liệu cần thêm vào bảng Rental
        double total_add = (stadium_price / 60) * gioChoi;
        int total_add_int = (int) total_add;

        Map<String, Object> rentalData = new HashMap<>();
        rentalData.put("date", Timestamp.now());
        rentalData.put("total", total_add_int);
        rentalData.put("user_id", userID);
        rentalData.put("status", "Booking");

        // Thêm dữ liệu vào bảng Rental
        DocumentReference rentalRef = db.collection("Rental").document();

        // Lấy documentID của Rental để dùng thêm vào "Drink_Rental" hoặc để truyền qua Intent
        // để sau đặt đồ uống hay thanh toán
        rentalID = rentalRef.getId();
        batch.set(rentalRef, rentalData);


        // Dữ liệu cần thêm vào bảng Stadium_Rental
        Date start_time_add_to_db = new Date();
        Date end_time_add_to_db = new Date();
        start_time_add_to_db = ConvertLocalDateTimeToDate(selectedDate, selected_StartTime_final);
        end_time_add_to_db = ConvertLocalDateTimeToDate(selectedDate, selected_EndTime_final);

        Map<String, Object> stadiumRentalData = new HashMap<>();
        stadiumRentalData.put("rental_id", rentalID);
        stadiumRentalData.put("stadium_id", sanID);
//        stadiumRentalData.put("stadium_id", 1);
        stadiumRentalData.put("start_time", start_time_add_to_db);
        stadiumRentalData.put("end_time", end_time_add_to_db);
        stadiumRentalData.put("rental_time", gioChoi);

        // Thêm dữ liệu vào bảng Stadium_Rental
        DocumentReference stadiumRentalRef = db.collection("Stadium_Rental").document();
        batch.set(stadiumRentalRef, stadiumRentalData);

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);

                        Intent intent = new Intent(getApplicationContext(), DatLichThanhCong.class);
                        intent.putExtra("userID", userID);
                        intent.putExtra("rentalID", rentalID);
                        startActivityForResult(intent, REQUEST_THANHCONG);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Có lỗi xảy ra khi thêm dữ liệu",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_THANHCONG && resultCode == 1){
            setResult(RESULT_DATSAN_SUCCESS);
            finish();
        }
    }


    /*
    Hàm này dùng để check xem liệu khách hàng này có đang Booking hay không
    Booking là dùng để cho khách hàng đặt nhiều sân cùng lúc, khi khách hàng Xác nhận cuối cùng
    ở màn hình chọn đồ uống, trạng thái status sẽ chuyển sang "Booked", sau khi thanh toán sẽ chuyển
    sang "Done"
     */
    CompletableFuture<Boolean> checkFirstBookingAsync() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Rental")
                .whereEqualTo("user_id", userID)
                .whereEqualTo("status", "Booking")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            rentalID = documentSnapshot.getId();
                            totalDb = documentSnapshot.getDouble("total");
                            future.complete(false); // Trả về kết quả false
                        } else {
                            future.complete(true); // Trả về kết quả true
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        future.completeExceptionally(task.getException()); // Trả về ngoại lệ khi có lỗi
                    }
                });

        return future;
    }

    private void initWidgets()
    {
        monthDayText = findViewById(R.id.monthDayText);
        dayOfWeekTV = findViewById(R.id.dayOfWeekTV);
        hourListView = findViewById(R.id.hourListView);
        btn_datlich = findViewById(R.id.btn_khachhang_datlichngay);

        tv_chonGioChoi = findViewById(R.id.tv_khachhang_chitietsan_giochoi);
        btn_themLich = findViewById(R.id.btn_khachhang_chitietsan_themLich);
        progressBar = findViewById(R.id.progressBar_khachhang_themlichdatsan);

        Intent intent = getIntent();
        sanID = intent.getStringExtra("idSan_intent");
        stadium_price = intent.getDoubleExtra("stadium_price", 0.0);
        userID = intent.getStringExtra("userID");
    }


    private void setDayView()
    {
        monthDayText.setText(CalendarUtils.monthDayFromDate(selectedDate));
        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        dayOfWeekTV.setText(dayOfWeek);
        setHourAdapter();
    }

    private void setHourAdapter()
    {
        HourAdapter hourAdapter = new HourAdapter(getApplicationContext(), hourEventList());
        hourListView.setAdapter(hourAdapter);
    }

    private ArrayList<HourEvent> hourEventList()
    {
        ArrayList<HourEvent> list = new ArrayList<>();

        for(int hour = 0; hour < 24; hour++)
        {
            LocalTime time = LocalTime.of(hour, 0);
            ArrayList<Event> events = Event.eventsForDateAndTime(selectedDate, time);
            HourEvent hourEvent = new HourEvent(time, events);
            list.add(hourEvent);
        }

        return list;
    }
    private void GetData()
    {
        Event.eventsList = new ArrayList<>();
        start_time_list = new ArrayList<>();
        end_time_list = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Stadium_Rental")
                .whereEqualTo("stadium_id", sanID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Lấy dữ liệu từ mỗi document trong collection
                                String id = document.getId();
                                Date start_time = document.getDate("start_time");
                                Date end_time = document.getDate("end_time");

                                LocalDate localDate = start_time.toInstant().atZone(ZoneId.systemDefault())
                                        .toLocalDate();

                                if(localDate.equals(selectedDate)){
                                    LocalTime localTimeStart_time = start_time.toInstant()
                                            .atZone(ZoneId.systemDefault()).toLocalTime();

                                    LocalTime localTimeEnd_time = end_time.toInstant()
                                            .atZone(ZoneId.systemDefault()).toLocalTime();
                                    LocalTime iTime = localTimeStart_time;

                                    start_time_list.add(localTimeStart_time);
                                    end_time_list.add(localTimeEnd_time);



                                        while (!iTime.isAfter(localTimeEnd_time)){
                                            Event newEvent = new Event(id, selectedDate, iTime);
                                            Event.eventsList.add(newEvent);
                                            iTime = iTime.plusHours(1);
                                    }
                                }
                            }
                            setDayView();
                            if(selectedDate.isBefore(LocalDate.now())){
                                btn_datlich.setEnabled(false);
                                btn_datlich.setBackgroundTintList(ContextCompat.getColorStateList(
                                        DailyCalendarActivity.this, R.color.lightGray));
                            }else{
                                btn_datlich.setEnabled(true);
                                btn_datlich.setBackgroundTintList(ContextCompat.getColorStateList(
                                        DailyCalendarActivity.this, R.color.green));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

//        setDayView();
    }
    private Date getDateAtStartOfDay(LocalDate localDate) {
        LocalDateTime startOfDay = localDate.atStartOfDay();
        Instant instant = startOfDay.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    // Phương thức để lấy thời điểm bắt đầu của ngày tiếp theo
    private Date getDateAtStartOfNextDay(LocalDate localDate) {
        LocalDateTime startOfNextDay = localDate.plusDays(1).atStartOfDay();
        Instant instant = startOfNextDay.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
    public void previousDayAction(View view)
    {
        selectedDate = selectedDate.minusDays(1);
        GetData();
    }

    public void nextDayAction(View view)
    {
        selectedDate = selectedDate.plusDays(1);
        GetData();
    }

    public void newEventAction(View view)
    {
//        startActivity(new Intent(this, EventEditActivity.class));

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                // Lấy giờ và phút được chọn
                LocalTime selectedTime = LocalTime.of(hourOfDay, minute);

                // Khởi tạo biến date1 và date2 với giá trị LocalTime tương ứng
                boolean isBetween = false;
                for (int i = 0; i < start_time_list.size(); i++) {
                    LocalTime start_Time = start_time_list.get(i);
                    LocalTime end_Time = end_time_list.get(i);

                    if (selectedTime.isAfter(start_Time) && selectedTime.isBefore(end_Time)) {
                        isBetween = true;
                        break;
                    }
                }
                LocalDateTime selectedDateTime = selectedDate.atTime(selectedTime);

                // Kiểm tra nếu giờ được chọn là khung giờ từ 11:PM - 5:PM
                if(selectedTime.isBefore(LocalTime.of(5,30))){
                    Toast.makeText(getApplicationContext(), "11PM - 5:30AM chưa phục vụ. Quý khách" +
                            " vui lòng chọn giờ khác", Toast.LENGTH_SHORT).show();

                    ResetTrangThaiNeuKhongHopLe();

                }else if (selectedTime.isAfter(LocalTime.of(22,59))) {
                    Toast.makeText(getApplicationContext(), "11PM - 5:30AM chưa phục vụ. Quý khách" +
                            " vui lòng chọn giờ khác", Toast.LENGTH_SHORT).show();

                    ResetTrangThaiNeuKhongHopLe();
                    //Kiểm tra nếu Giờ được chọn là giờ quá khứ
                }else if(selectedDateTime.isBefore(LocalDateTime.now())){
                    Toast.makeText(getApplicationContext(), "Giờ không hợp lệ"
                            , Toast.LENGTH_SHORT).show();

                    ResetTrangThaiNeuKhongHopLe();

                } else if (isBetween) {
                    Toast.makeText(getApplicationContext(), "Giờ đã được chọn, vui lòng chọn " +
                                    "lại giờ khác"
                            , Toast.LENGTH_SHORT).show();

                    ResetTrangThaiNeuKhongHopLe();

                } else {
                    // Giờ hợp lệ, tiếp tục xử lý
                    // ...
                    selected_StartTime_final = selectedTime;
                    ShowBottomDialog();
                }
            }
        }, 0, 0, false);

        timePickerDialog.show();
    }
    private void ShowBottomDialog() {
        Dialog bottomSheetDialog = new Dialog(DailyCalendarActivity.this);
//        bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomSheetDialog.setContentView(R.layout.bottom_chonphut);
        // Đặt các thuộc tính của dialog
        Window dialogWindow = bottomSheetDialog.getWindow();
        if (dialogWindow != null) {
            // Đặt độ trasparent cho nền của dialog
            dialogWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            // Đặt độ trasparent cho phần nền trên cùng của dialog (bo góc trên)
            dialogWindow.setDimAmount(0.5f);

            // Đặt các thuộc tính về kích thước và vị trí
            WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
            layoutParams.gravity = Gravity.BOTTOM; // Hiển thị ở dưới cùng
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Chiều rộng tối đa
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Chiều cao tự động
            dialogWindow.setAttributes(layoutParams);
        }

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
//                tv_chonphut.setText(phutValues[numberPicker.getValue()] + " phút");

                // Tạo một đối tượng Calendar từ selectedStartDate
                Date selectedStartDate = ConvertLocalDateTimeToDate(selectedDate, selected_StartTime_final);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedStartDate);

                // Thêm số phút được chọn vào selectedStartDate
                int phut = Integer.parseInt(phutValues[numberPicker.getValue()]);

                calendar.add(Calendar.MINUTE, phut);
                selected_EndTime = calendar.getTime();

                LocalTime selected_EndTime_LocalTime = ConvertDateToLocalTime(selected_EndTime);
                // Lưu selectedEndDate
                boolean isBetween = false;
                LocalTime start_Time_db = null;
                LocalTime end_Time_db = null;
                for (int i = 0; i < start_time_list.size(); i++) {
                    start_Time_db = start_time_list.get(i);
                    end_Time_db = end_time_list.get(i);

                    if (selected_EndTime_LocalTime.isAfter(start_Time_db) &&
                            selected_EndTime_LocalTime.isBefore(end_Time_db)) {
                        isBetween = true;
                        break;
                    } else if ((selected_StartTime_final.isBefore(start_Time_db) ||
                                selected_StartTime_final.equals(start_Time_db))
                            && (selected_EndTime_LocalTime.isAfter(end_Time_db) ||
                                selected_EndTime_LocalTime.equals(end_Time_db))) {
                        isBetween = true;
                        break;
                    }
                }

                /*
                TH cuối cùng (hợp lệ)
                 */
                if(!isBetween){
                    bottomSheetDialog.dismiss();
                    tv_chonGioChoi.setText("Bạn vừa chọn: " + selected_StartTime_final + " - "
                            + selected_EndTime_LocalTime );

                    GetData();
                    LocalTime iTime = selected_StartTime_final;
                    selected_EndTime_LocalTime = selected_EndTime_LocalTime.withSecond(0);
                        while (!iTime.isAfter(selected_EndTime_LocalTime)){
                            Event newEvent = new Event("", selectedDate, iTime);
                            Event.eventsList.add(newEvent);
                            iTime = iTime.plusHours(1);
                        }

                    btn_datlich.setText("Chọn giờ khác");
                    setDayView();
                    btn_themLich.setVisibility(View.VISIBLE);
                    selected_EndTime_final = selected_EndTime_LocalTime;
                    gioChoi =phut;

                }else{
                    Toast.makeText(DailyCalendarActivity.this, "Khung giờ bạn chọn đã " +
                            "có lịch đặt khác (" + start_Time_db.getHour() + ":"
                            + start_Time_db.getMinute() + " - " + end_Time_db.getHour()
                            + ":" + end_Time_db.getMinute() + "). Vui lòng chọn giờ khác!",
                            Toast.LENGTH_SHORT).show();

                    ResetTrangThaiNeuKhongHopLe();
                }
            }
        });

        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

//        bottomSheetDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        bottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
//        bottomSheetDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        bottomSheetDialog.show();
    }
    Date ConvertLocalDateTimeToDate(LocalDate localDate, LocalTime localTime){

        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

        ZoneId zoneId = ZoneId.systemDefault(); // Use a specific time zone, or specify a desired time zone

        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

        Instant instant = zonedDateTime.toInstant();

        Date date = Date.from(instant);
        return date;
    }
    LocalTime ConvertDateToLocalTime(Date date){
        LocalTime localTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        return localTime;
    }
    void ResetTrangThaiNeuKhongHopLe(){
        tv_chonGioChoi.setText("");
        GetData();
        btn_themLich.setVisibility(View.GONE);
        btn_datlich.setText("Đặt lịch ngay");
    }
//    void ShowSuccessScreen(){
//        Dialog successSreen = new Dialog(DailyCalendarActivity.this);
//        successSreen.setContentView(R.layout.success_screen_themlichdatsan);
//        Button btn_tieptuc = successSreen.findViewById(R.id.btn_khachang_datsanthanhcong_tieptuc);
//        Button btn_sankhac = successSreen.findViewById(R.id.btn_khachang_datsanthanhcong_sankhac);
//        successSreen.show();
//
//        btn_tieptuc.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(DailyCalendarActivity.this, Dangnhapthanhcong.class));
//                finish();
//            }
//        });
//
//        btn_sankhac.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                setResult(RESULT_DATSAN_SUCCESS);
//                finish();
//            }
//        });
//    }
}