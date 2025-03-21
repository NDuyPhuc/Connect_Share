package com.example.save_food;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class activity_form_view extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_view);

        // Ánh xạ các TextView cho thông tin cá nhân
        TextView tvFullname = findViewById(R.id.tv_fullname);
        TextView tvPhone = findViewById(R.id.tv_phonenumber);
        TextView tvCity = findViewById(R.id.tv_city);
        TextView tvDistrict = findViewById(R.id.tv_district);
        TextView tvWard = findViewById(R.id.tv_ward);
        TextView tvStreet = findViewById(R.id.tv_street);
        TextView tvNotes = findViewById(R.id.tv_notes);

        // Ánh xạ các view cho thông tin sản phẩm
        TextView tvProductName = findViewById(R.id.tv_product_name_view);
        TextView tvProductInfo = findViewById(R.id.tv_product_info_view);
        TextView tvProductInfo_more = findViewById(R.id.tv_product_info_view_more);
        ImageView ivProduct = findViewById(R.id.iv_product_view);
        Button back_view = findViewById(R.id.btn_back_view);

        back_view.setOnClickListener(view -> {
            onBackPressed();
        });

        // Kiểm tra xem có dữ liệu tự nhập được truyền qua hay không
        String fullname = getIntent().getStringExtra("fullname");
        if (fullname != null && !fullname.isEmpty()) {
            // Nếu có, hiển thị dữ liệu tự nhập
            tvFullname.setText("Họ và tên: " + fullname);
            tvPhone.setText("Số điện thoại: " + getIntent().getStringExtra("phone"));
            tvCity.setText("Thành phố: " + getIntent().getStringExtra("city"));
            tvDistrict.setText("Quận/Huyện: " + getIntent().getStringExtra("district"));
            tvWard.setText("Xã/Thị trấn: " + getIntent().getStringExtra("ward"));
            tvStreet.setText("Đường/Số nhà: " + getIntent().getStringExtra("street"));
            tvNotes.setText("Ghi chú: " + getIntent().getStringExtra("notes"));
        } else {
            // Nếu không có dữ liệu tự nhập, fallback về lấy từ Firebase theo UID_sender như cũ
            String uid = getIntent().getStringExtra("UID_sender");
            if (uid != null && !uid.isEmpty()) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String city = snapshot.child("city").getValue(String.class);
                        String district = snapshot.child("district").getValue(String.class);
                        String ward = snapshot.child("ward").getValue(String.class);
                        String street = snapshot.child("street").getValue(String.class);
                        String notes = snapshot.child("notes").getValue(String.class);

                        tvFullname.setText("Họ và tên: " + (name != null ? name : ""));
                        tvPhone.setText("Số điện thoại: " + (phone != null ? phone : ""));
                        tvCity.setText("Thành phố: " + (city != null ? city : ""));
                        tvDistrict.setText("Quận/Huyện: " + (district != null ? district : ""));
                        tvWard.setText("Xã/Thị trấn: " + (ward != null ? ward : ""));
                        tvStreet.setText("Đường/Số nhà: " + (street != null ? street : ""));
                        tvNotes.setText("Ghi chú: " + (notes != null ? notes : ""));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Xử lý lỗi nếu cần
                    }
                });
            } else {
                Log.d("UID_View", "UID bị NULL");
            }
        }



        // Lấy thông tin sản phẩm từ Intent (nếu được truyền)
        String productName = getIntent().getStringExtra("productName");
        String productInfo = getIntent().getStringExtra("productInfo");
        String productInfo_more = getIntent().getStringExtra("productInfo_more");
        String productImage = getIntent().getStringExtra("productImage");

        tvProductName.setText((productName != null ? productName : ""));
        tvProductInfo.setText((productInfo != null ? productInfo : ""));
        tvProductInfo_more.setText((productInfo_more != null ? productInfo_more : ""));
        if (productImage != null && !productImage.isEmpty()) {
            Glide.with(this).load(productImage).into(ivProduct);
        }
    }
}
