package com.example.save_food;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class activity_form_view extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_view);

        // Ánh xạ các TextView
        TextView tvFullname = findViewById(R.id.tv_fullname);
        TextView tvPhone = findViewById(R.id.tv_phonenumber);
        TextView tvCity = findViewById(R.id.tv_city);
        TextView tvDistrict = findViewById(R.id.tv_district);
        TextView tvWard = findViewById(R.id.tv_ward);
        TextView tvStreet = findViewById(R.id.tv_street);
        TextView tvNotes = findViewById(R.id.tv_notes);

        TextView tvProductName = findViewById(R.id.tv_product_name_view);
        TextView tvProductInfo = findViewById(R.id.tv_product_info_view);
        ImageView ivProduct = findViewById(R.id.iv_product_view);

        // Nhận dữ liệu từ Intent
        String fullname = getIntent().getStringExtra("fullname");
        String phone = getIntent().getStringExtra("phone");
        String city = getIntent().getStringExtra("city");
        String district = getIntent().getStringExtra("district");
        String ward = getIntent().getStringExtra("ward");
        String street = getIntent().getStringExtra("street");
        String notes = getIntent().getStringExtra("notes");

        String productName = getIntent().getStringExtra("productName");
        String productInfo = getIntent().getStringExtra("productInfo");
        String productImage = getIntent().getStringExtra("productImage");

        // Gán dữ liệu vào TextView (chỉ đọc)
        tvFullname.setText("Họ và tên: " + (fullname != null ? fullname : ""));
        tvPhone.setText("Số điện thoại: " + (phone != null ? phone : ""));
        tvCity.setText("Thành phố: " + (city != null ? city : ""));
        tvDistrict.setText("Quận/Huyện: " + (district != null ? district : ""));
        tvWard.setText("Xã/Thị trấn: " + (ward != null ? ward : ""));
        tvStreet.setText("Đường/Số nhà: " + (street != null ? street : ""));
        tvNotes.setText("Ghi chú: " + (notes != null ? notes : ""));

        tvProductName.setText("Tên sản phẩm: " + (productName != null ? productName : ""));
        tvProductInfo.setText("Thông tin sản phẩm: " + (productInfo != null ? productInfo : ""));
        if (productImage != null && !productImage.isEmpty()) {
            Glide.with(this).load(productImage).into(ivProduct);
        }
    }
}
