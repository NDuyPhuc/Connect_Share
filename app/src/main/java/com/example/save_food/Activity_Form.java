package com.example.save_food;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.save_food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Activity_Form extends AppCompatActivity {

    private EditText etFullname, etPhone, etCity, etDistrict, etWard, etStreet, etNotes;
    private ImageView ivProduct;
    private TextView tvProductName, tvProductInfo;
    private Button quay_lai, gui_sp;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        // Ánh xạ các view thông tin cá nhân
        etFullname = findViewById(R.id.et_fullname);
        etPhone = findViewById(R.id.phonenumber);
        etCity = findViewById(R.id.et_city);
        etDistrict = findViewById(R.id.et_district);
        etWard = findViewById(R.id.et_ward);
        etStreet = findViewById(R.id.et_street);
        etNotes = findViewById(R.id.et_notes);
        // Ánh xạ các view thông tin sản phẩm
        ivProduct = findViewById(R.id.iv_product);
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductInfo = findViewById(R.id.tv_product_info);
        quay_lai = findViewById(R.id.btn_back);
        gui_sp = findViewById(R.id.btn_submit);
        // Lấy extra từ Intent
        String infoOption = getIntent().getStringExtra("info_option");
        String productName = getIntent().getStringExtra("product_name");
        String productInfo = getIntent().getStringExtra("product_info");
        String productImage = getIntent().getStringExtra("product_image");

        // Cập nhật thông tin sản phẩm trong TableLayout
        if (productName != null) {
            tvProductName.setText(productName);
        }
        if (productInfo != null) {
            tvProductInfo.setText(productInfo);
        }
        if (productImage != null) {
            Glide.with(this)
                    .load(productImage)
                    .into(ivProduct);
        }

        // Nếu người dùng chọn "preset" thì load thông tin từ Firebase
        // Nếu chọn "empty" thì để các EditText trống
        if ("preset".equals(infoOption)) {
            loadUserDataFromFirebase();
        } else {
            etFullname.setText("");
            etPhone.setText("");
            etCity.setText("");
            etDistrict.setText("");
            etWard.setText("");
            etStreet.setText("");
            etNotes.setText("");
        }
        quay_lai.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void loadUserDataFromFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        // Lấy thông tin người dùng theo UID
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String city = snapshot.child("city").getValue(String.class);
                        String district = snapshot.child("district").getValue(String.class);
                        String ward = snapshot.child("ward").getValue(String.class);
                        String street = snapshot.child("street").getValue(String.class);
                        String notes = snapshot.child("notes").getValue(String.class);

                        etFullname.setText(name != null ? name : "");
                        etPhone.setText(phone != null ? phone : "");
                        etCity.setText(city != null ? city : "");
                        etDistrict.setText(district != null ? district : "");
                        etWard.setText(ward != null ? ward : "");
                        etStreet.setText(street != null ? street : "");
                        etNotes.setText(notes != null ? notes : "");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Activity_Form.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
