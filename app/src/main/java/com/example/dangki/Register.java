package com.example.dangki;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class Register extends AppCompatActivity {
    EditText  password, fullName, phoneNumber, email,birthday;
    TextView btnLogin;
    RadioGroup gender;
    Button BtnGegister;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        FindViewByIds();

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(Register.this);

        if (fAuth.getCurrentUser() != null) {
//            startActivity(new Intent(getApplicationContext(), Login.class));
//            finish();
            fAuth.signOut();
        }

        /*
        Nếu người dùng đã có tài khoản thì đăng nhập
         */
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });
        /*
        Kiểm tra có thành phần nào điền thiếu hay không
         */
        ErrorChecking();
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Hiển thị DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(Register.this, new DatePickerDialog.OnDateSetListener() {
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
                            birthday.setText(formattedDate);
                        } else {
                            // Ngày sinh không hợp lệ
//                            birthday.setError("Ngày sinh không hợp lệ");
                            birthday.setText("");
                            Toast.makeText(Register.this, "Ngày sinh không hợp lệ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, year, month, day);

                datePickerDialog.show();
            }
        });
        // Kiểm tra lỗi khi EditText userName mất focus

        BtnGegister.setOnClickListener(view ->  {
                if(validateFields()){
                    String uEmail = email.getText().toString().trim();
                    String uPass = password.getText().toString().trim();
                    String uName = fullName.getText().toString().trim();
                    String uPhone = phoneNumber.getText().toString().trim();

                    String uBirthday = birthday.getText().toString().trim();
                    SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");
                    Date uBirthday_date;
                    try {
                        uBirthday_date = formatter1.parse(uBirthday);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }


                    int selected_gender_id = gender.getCheckedRadioButtonId();
                    RadioButton selected_gender_radio = findViewById(selected_gender_id);
                    String uGender = selected_gender_radio.getText().toString();

                    // Hiển thị dialog loading

                    progressDialog.setTitle("Loading");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCancelable(true); // Cho phép hủy
                    progressDialog.show();

                    CompletableFuture<Boolean> isDuplicateFuture = CheckDuplicateDataAndRegister(uPhone, uEmail);

                    isDuplicateFuture.thenAccept(isDuplicate ->{
                       if(isDuplicate){
                           progressDialog.dismiss();
                       }else{
                           fAuth.createUserWithEmailAndPassword(uEmail,uPass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                               @Override
                               public void onSuccess(AuthResult authResult) {
                                   AddUserToDb(uName, uPhone, uGender,uBirthday_date, uEmail, uPass);
                               }
                           })
                                   .addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                           Log.d("Register", e.getMessage());
                                       }
                                   });
                       }

                    });
//                    fAuth.createUserWithEmailAndPassword(uEmail,uPass).addOnCompleteListener(task -> {
//                       if(task.isSuccessful()){
//                           checkDuplicateDataAndRegister(uName, uPhone, uGender,uBirthday_date, uEmail, uPass);
//                       }
//                    });

                }

        });

    }

    private void AddUserToDb(String uName, String uPhone, String uGender, Date uBirthday_date,
                             String uEmail, String uPass) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("name", uName);
        newUser.put("phoneNumber", uPhone);
        newUser.put("gender", uGender);
        newUser.put("email", uEmail);
        newUser.put("img_url", "");
        newUser.put("password", uPass);
        newUser.put("birthdate", uBirthday_date);
        newUser.put("role_id", "customer");
        String userID = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        db.collection("User").document(userID).set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Register.this,
                                "Tạo tài khoản thành công",
                                Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        startActivity(
                                new Intent(getApplicationContext(),
                                        Login.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Register.this,
                                "Error " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    CompletableFuture<Boolean> CheckDuplicateDataAndRegister(String uPhone, String uEmail) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .whereEqualTo("email", uEmail)
                .get()
                .addOnCompleteListener(queryTask -> {
                    if (queryTask.isSuccessful()) {
                        QuerySnapshot querySnapshot = queryTask.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // Lấy thông tin chi tiết về số điện thoại hoặc email bị trùng
                            Toast.makeText(Register.this, "Email  đã được sử dụng ",
                                    Toast.LENGTH_SHORT).show();

                            future.complete(true);
                        }
                        // Tiếp tục quá trình đăng ký
                        // ...
//                           future.complete(false);
                    }
                    db.collection("User")
                            .whereEqualTo("phoneNumber", uPhone)
                            .get()
                            .addOnCompleteListener(queryTask1 -> {
                                if (queryTask1.isSuccessful()) {
                                    QuerySnapshot querySnapshot = queryTask1.getResult();
                                    if (!querySnapshot.isEmpty()) {
                                        // Lấy thông tin chi tiết về số điện thoại hoặc email bị trùng
                                        Toast.makeText(Register.this, "SDT  đã được sử dụng ",
                                                Toast.LENGTH_SHORT).show();

                                        future.complete(true);
                                    }
                                    // Tiếp tục quá trình đăng ký
                                    // ...
                            future.complete(false);
                                }
                            });
                });

//        future.complete(false);
        return future;
    }


    private void FindViewByIds() {
        gender = findViewById(R.id.genderRadio);
        fullName = findViewById(R.id.HoTen);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPass);
        phoneNumber = findViewById(R.id.editTextSdt);
        birthday = findViewById(R.id.editTextDate);
        btnLogin = findViewById(R.id.txtDangNhap);

        BtnGegister = findViewById(R.id.btDangKi);
    }

    private void checkDuplicateDataAndRegister(String uName, String uPhone, String uGender,
                                               Date uBirthday_date, String uEmail, String uPass) {
        // Kiểm tra trùng lặp số điện thoại
        db.collection("User")
                .whereEqualTo("phoneNumber", uPhone)
                .get()
                .addOnCompleteListener(phoneQueryTask -> {
                    if (phoneQueryTask.isSuccessful()) {
                        if (!phoneQueryTask.getResult().isEmpty()) {
                            // Số điện thoại đã tồn tại
                            progressDialog.dismiss();
                            Toast.makeText(Register.this, "Số điện thoại đã được sử dụng",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Kiểm tra trùng lặp tên người dùng
                            db.collection("User")
                                    .whereEqualTo("email", uEmail)
                                    .get()
                                    .addOnCompleteListener(userNameQueryTask -> {
                                        if (userNameQueryTask.isSuccessful()) {
                                            if (!userNameQueryTask.getResult().isEmpty()) {
                                                // Tên người dùng đã tồn tại
                                                progressDialog.dismiss();
                                                Toast.makeText(Register.this, "Email này " +
                                                        "đã được sử dụng", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Tạo người dùng mới và lưu vào Firestore
                                                String userID = FirebaseAuth.getInstance()
                                                        .getCurrentUser().getUid();
//                                                User newUser = new User(uName, uPhone, uGender,
//                                                        uUserName, uBirthday_date, uEmail, uPass);
                                                Map<String, Object> newUser = new HashMap<>();
                                                newUser.put("name", uName);
                                                newUser.put("phoneNumber", uPhone);
                                                newUser.put("gender", uGender);
                                                newUser.put("email", uEmail);
                                                newUser.put("img_url", "");
                                                newUser.put("password", uPass);
                                                newUser.put("birthdate", uBirthday_date);
                                                newUser.put("role_id", "customer");

                                                db.collection("User").document(userID).set(newUser)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(Register.this,
                                                                        "User created",
                                                                        Toast.LENGTH_SHORT).show();
                                                                startActivity(
                                                                        new Intent(getApplicationContext(),
                                                                                Login.class));
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(Register.this,
                                                                        "Error " + e.getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(Register.this, "Error " +
                                                    userNameQueryTask.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(Register.this, "Error " +
                                phoneQueryTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void ErrorChecking(){

// Kiểm tra lỗi khi EditText password mất focus
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String uPass = password.getText().toString().trim();
                    if (TextUtils.isEmpty(uPass)) {
                        password.setError("Mật khẩu không được trống");
                    }
                }
            }
        });

// Kiểm tra lỗi khi EditText email mất focus
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String uEmail = email.getText().toString().trim();
                    if (TextUtils.isEmpty(uEmail)) {
                        email.setError("Email không được trống");
                    }
                }
            }
        });

// Kiểm tra lỗi khi EditText birthday mất focus
        birthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String uBirthday = birthday.getText().toString().trim();
                    if (TextUtils.isEmpty(uBirthday)) {
                        birthday.setError("Ngày sinh không được trống");
                    }
                }
            }
        });
        fullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String uName = fullName.getText().toString().trim();
                    if (TextUtils.isEmpty(uName)) {
                        fullName.setError("Họ tên không được trống");
                    }
                }
            }
        });
        phoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String uPhone = phoneNumber.getText().toString().trim();
                    if (TextUtils.isEmpty(uPhone)) {
                        phoneNumber.setError("Số điện thoại không được bỏ trống");
                    } else if (uPhone.length() <10 || uPhone.length() > 10) {
                        phoneNumber.setError("SĐT phải là 10 số");
                    }
                }
            }
        });

    }
    private boolean validateFields() {
        String uPass = password.getText().toString().trim();
        String uEmail = email.getText().toString().trim();
        String uBirthday = birthday.getText().toString().trim();
        String uPhone = phoneNumber.getText().toString().trim();
        String uName = fullName.getText().toString().trim();

        if (TextUtils.isEmpty(uPass)) {
            password.setError("Mật khẩu không được trống");
            return false;
        } else if (TextUtils.isEmpty(uEmail)) {
            email.setError("Email không được trống");
            return false;
        } else if (TextUtils.isEmpty(uBirthday)) {
            birthday.setError("Ngày sinh không được trống");
            return false;
        } else if (gender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(Register.this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(uPhone)){
            phoneNumber.setError("Số điện thoại không được trống");
            return false;
        }else if(TextUtils.isEmpty(uName)){
            fullName.setError("Họ tên không được trống");
            return false;
        } else if (uPhone.length() <10 || uPhone.length() >10) {
            phoneNumber.setError("SĐT phải là 10 số");
            return false;
        }
        return true;
    }

}
