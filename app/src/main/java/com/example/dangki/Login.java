package com.example.dangki;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dangki.Admin.KhachHang.Menu;
import com.example.dangki.KhachHang.ChonSan;
import com.example.dangki.KhachHang.DatSan;
import com.example.dangki.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    EditText email, password;
    TextView btnDangKy;
    Button loginBtn, loginGoogle;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        fAuth = FirebaseAuth.getInstance();
        db =FirebaseFirestore.getInstance();

        if (fAuth.getCurrentUser() != null) {
            fAuth.signOut();
        }
        email = findViewById(R.id.editTextEmailAddress);
        password = findViewById(R.id.editTextPass);

        loginBtn = findViewById(R.id.btDN);
        loginGoogle = findViewById(R.id.btDNGoogle);
        btnDangKy = findViewById(R.id.txtDangKi2);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this,gso);

        /*
        Xử lý sự kiện nhấn nút đăng ký
         */
        btnDangKy.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), Register.class));
            finish();
        });
        /*
        xử lý sự kiện đăng nhập bằng Google
         */
        loginGoogle.setOnClickListener(view -> {
            LoginGoogle();
        });

        /*
        xử lý sự kiện đăng nhập bằng email, password
         */
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkEmpty()) {
                    String uEmail = email.getText().toString().trim();
                    String uPass = password.getText().toString().trim();

                    ProgressDialog progressDialog = new ProgressDialog(Login.this);
                    progressDialog.setTitle("Loading");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();

                    fAuth.signInWithEmailAndPassword(uEmail, uPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference userRef = db
                                        .collection("User")
                                        .document(fAuth.getUid());
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String role = document.getString("role_id");
                                                if (role != null && role.equals("admin")) {
                                                    Toast.makeText(Login.this,
                                                            "Đăng nhập thành công",
                                                            Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(),
                                                            Menu.class);
                                                    intent.putExtra("userID",
                                                            fAuth.getCurrentUser().getUid());
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(Login.this, "Đăng nhập thành công",
                                                            Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(),
                                                            ChonSan.class);
                                                    intent.putExtra("userID",
                                                            fAuth.getCurrentUser().getUid());
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        } else {
                                            Toast.makeText(Login.this,
                                                    "Lỗi trong quá trình truy vấn người dùng",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(Login.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


    }
    public boolean checkEmpty(){
        String mEmail = email.getText().toString().trim();
        String mPass = password.getText().toString().trim();
        if(TextUtils.isEmpty(mEmail)) {
            email.setError("Email không được trống");
            return false;
        }else if(TextUtils.isEmpty(mPass)){
            password.setError("Mật khẩu không được trống");
            return false;
        }
        return true;
    }
    int RC_SIGN_IN =40;
    void LoginGoogle(){
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(Login.this, "Bạn chưa chọn tài khoản", Toast.LENGTH_SHORT).show();
                return;
            }

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void firebaseAuth(String idToken) {
        ProgressDialog progressDialog = new ProgressDialog(Login.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressDialog.dismiss();
                            FirebaseUser fuser = fAuth.getCurrentUser();
                            User dbUser = new User();
                            dbUser.setId(fuser.getUid());
                            dbUser.setName(fuser.getDisplayName());
                            dbUser.setImg_url(fuser.getPhotoUrl().toString());
                            dbUser.setPhoneNumber(fuser.getPhoneNumber());
                            dbUser.setEmail(fuser.getEmail());
                            dbUser.setRole_id("customer");
                            AddUserToFirestore(dbUser);
                        }
                    }
                });
    }

    private void AddUserToFirestore(User dbUser) {
        DocumentReference userRef = db.collection("User").document(dbUser.getId());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Người dùng đã tồn tại trong Firestore
                        // Thực hiện các hành động cần thiết sau khi đăng nhập bằng Google
                        Toast.makeText(Login.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ChonSan.class);
                        intent.putExtra("userID", dbUser.getId());
                        startActivity(intent);
                        finish();
                    } else {
                        // Người dùng chưa tồn tại trong Firestore
                        // Thêm người dùng mới vào Firestore

                        // Thực hiện thêm dữ liệu vào Firestore
                        Map<String, Object> newUser = new HashMap<>();
                        newUser.put("name", dbUser.getName());
                        newUser.put("phoneNumber", dbUser.getPhoneNumber());
                        newUser.put("gender", dbUser.getGender());
                        newUser.put("email", dbUser.getEmail());
                        newUser.put("img_url", dbUser.getImg_url());
                        newUser.put("password", dbUser.getPassword());
                        newUser.put("birthdate", dbUser.getBirthdate());
                        newUser.put("role_id", "customer");
                        db.collection("User")
                                .document(dbUser.getId())
                                .set(newUser)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Thực hiện các hành động cần thiết sau khi thêm người dùng mới vào Firestore
                                        Toast.makeText(Login.this, "Đăng nhập thành công",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), ChonSan.class);
                                        intent.putExtra("userID", dbUser.getId());
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Login.this, "Thêm người dùng vào " +
                                                "Firestore thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(Login.this, "Lỗi khi truy cập Firestore",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
