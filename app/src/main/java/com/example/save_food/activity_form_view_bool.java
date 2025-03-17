package com.example.save_food;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.save_food.models.ThongTin_UpLoadClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class activity_form_view_bool extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_view_bool);
        TextView tvFullname = findViewById(R.id.tv_fullname);
        TextView tvPhone = findViewById(R.id.tv_phonenumber);
        TextView tvCity = findViewById(R.id.tv_city);
        TextView tvDistrict = findViewById(R.id.tv_district);
        TextView tvWard = findViewById(R.id.tv_ward);
        TextView tvStreet = findViewById(R.id.tv_street);
        TextView tvNotes = findViewById(R.id.tv_notes);
        TextView tvProductName = findViewById(R.id.tv_product_name_view);
        TextView tvProductInfo = findViewById(R.id.tv_product_info_view);
        TextView tvProductInfo_more = findViewById(R.id.tv_product_info_view_more);
        ImageView ivProduct = findViewById(R.id.iv_product_view);
        Button back_view = findViewById(R.id.btn_back_view);
        Button back_view_tuchoi = findViewById(R.id.btn_tuchoi);
        Button back_view_chapnhan = findViewById(R.id.btn_chapnhan);
        back_view.setOnClickListener(view -> onBackPressed());
        back_view_tuchoi.setOnClickListener(view -> updateRequestStatus("rejected"));
        back_view_chapnhan.setOnClickListener(view -> updateRequestStatus("accepted"));
        String uid = getIntent().getStringExtra("UID_sender");
        if (uid != null && !uid.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String fullname = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);
                    String district = snapshot.child("district").getValue(String.class);
                    String ward = snapshot.child("ward").getValue(String.class);
                    String street = snapshot.child("street").getValue(String.class);
                    String notes = snapshot.child("notes").getValue(String.class);
                    tvFullname.setText("Họ và tên: " + (fullname != null ? fullname : ""));
                    tvPhone.setText("Số điện thoại: " + (phone != null ? phone : ""));
                    tvCity.setText("Thành phố: " + (city != null ? city : ""));
                    tvDistrict.setText("Quận/Huyện: " + (district != null ? district : ""));
                    tvWard.setText("Xã/Thị trấn: " + (ward != null ? ward : ""));
                    tvStreet.setText("Đường/Số nhà: " + (street != null ? street : ""));
                    tvNotes.setText("Ghi chú: " + (notes != null ? notes : ""));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            Log.d("UID_View", "UID bị NULL");
        }
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
    private void updateRequestStatus(String newStatus) {
        String chatId = getIntent().getStringExtra("chatId");
        if (chatId != null) {
            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
            chatRef.child("status").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(activity_form_view_bool.this, "Yêu cầu đã " + (newStatus.equals("accepted") ? "chấp nhận" : "từ chối"), Toast.LENGTH_SHORT).show();
                        if(newStatus.equals("accepted")) {
                            ThongTin_UpLoadClass product = new ThongTin_UpLoadClass();
                            product.setTenDonHang(getIntent().getStringExtra("productName"));
                            product.setThongTinChiTiet(getIntent().getStringExtra("productInfo_more"));
                            product.setDiaChi("Địa chỉ mẫu");
                            product.setUid(getIntent().getStringExtra("UID_sender"));
                            String acceptedUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String productImageUrl = getIntent().getStringExtra("productImageUrl");
                            if(productImageUrl == null || productImageUrl.isEmpty()){
                                productImageUrl = getIntent().getStringExtra("productImage");
                            }
                            saveReview(product, acceptedUserId, productImageUrl);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(activity_form_view_bool.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(activity_form_view_bool.this, "Không tìm thấy thông tin yêu cầu", Toast.LENGTH_SHORT).show();
        }
    }
    public void saveReview(ThongTin_UpLoadClass product, String acceptedUserId, String productImageUrl) {
        String productOwnerUid = product.getUid();
        String productId = product.getPostId();
        if (productOwnerUid == null || productOwnerUid.isEmpty()) {
            productOwnerUid = getIntent().getStringExtra("UID_sender");
            product.setUid(productOwnerUid);
        }
        if (productId == null || productId.isEmpty()) {
            productId = getIntent().getStringExtra("postId");
        }
        DatabaseReference productImageRef = FirebaseDatabase.getInstance()
                .getReference("ThongTin_UpLoad").child(productOwnerUid).child(productId).child("Ảnh");
        productImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = null;
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        url = ds.child("linkHinh").getValue(String.class);
                        if (url != null && !url.isEmpty()) {
                            break;
                        }
                    }
                }
                final String finalImageUrl = (url != null && !url.isEmpty()) ? url : productImageUrl;
                String reviewerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("Reviews").child(reviewerUid);
                String reviewId = reviewRef.push().getKey();
                product.setStatus("accepted");
                product.setAcceptedUserId(acceptedUserId);
                product.setPostId(reviewId);
                Log.d("Activty_Form_View", "IMAGE" + finalImageUrl);
                reviewRef.child(reviewId).setValue(product)
                        .addOnSuccessListener(aVoid -> {
                            if (finalImageUrl != null && !finalImageUrl.isEmpty()) {
                                DatabaseReference imageRef = reviewRef.child(reviewId).child("Ảnh").push();
                                Map<String, Object> imageMap = new HashMap<>();
                                imageMap.put("linkHinh", finalImageUrl);
                                imageRef.setValue(imageMap)
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(activity_form_view_bool.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(activity_form_view_bool.this, "Lỗi lưu ảnh đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(activity_form_view_bool.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(activity_form_view_bool.this, "Lỗi lưu đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity_form_view_bool.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
