package com.example.save_food;

import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Activity_Form extends AppCompatActivity {

    private EditText etFullname, etPhone, etCity, etDistrict, etWard, etStreet, etNotes;
    private ImageView ivProduct;
    private TextView tvProductName, tvProductInfo, tvProductInfo_more;
    private Button quay_lai, gui_sp;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        // Khởi tạo Firebase Auth và lấy người dùng hiện tại ngay lập tức
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        tvProductInfo_more = findViewById(R.id.tv_product_info1);
        quay_lai = findViewById(R.id.btn_back);
        gui_sp = findViewById(R.id.btn_submit);

        // Lấy extra từ Intent
        String infoOption = getIntent().getStringExtra("info_option");
        String productName = getIntent().getStringExtra("product_name");
        String productInfo = getIntent().getStringExtra("product_info");
        String productInfo_more = getIntent().getStringExtra("product_info_more");
        String productImage = getIntent().getStringExtra("product_image");
        String receiverUid  = getIntent().getStringExtra("UID_personal");
        // Cập nhật thông tin sản phẩm trong TableLayout
        if (productName != null) {
            tvProductName.setText(productName);
        }
        if (productInfo != null) {
            tvProductInfo.setText(productInfo);
        }
        if (productInfo_more != null) {
            tvProductInfo_more.setText(productInfo_more);
        }
        if (productImage != null) {
            Glide.with(this)
                    .load(productImage)
                    .into(ivProduct);
        }

        // Nếu người dùng chọn "preset" thì load thông tin từ Firebase, còn nếu "empty" thì để trống
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

        quay_lai.setOnClickListener(view -> onBackPressed());
        gui_sp.setOnClickListener(view -> sendProductMessage(receiverUid));

        // Khởi tạo DatabaseReference và chatRef
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");
    }

    private void sendProductMessage(String receiverUid) {
        if (receiverUid == null || receiverUid.isEmpty()) {
            Toast.makeText(this, "Không có thông tin người nhận", Toast.LENGTH_SHORT).show();
            return;
        }
        String senderUid = firebaseUser.getUid(); // UID của người gửi
        long timestamp = System.currentTimeMillis();

        // Tạo đối tượng JSON chứa thông tin sản phẩm
        JSONObject productObject = new JSONObject();
        try {
            productObject.put("productName", tvProductName.getText().toString());
            productObject.put("productInfo", tvProductInfo.getText().toString());
            productObject.put("productInfo_more", tvProductInfo_more.getText().toString());
            productObject.put("productImage", getIntent().getStringExtra("product_image"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String productData = productObject.toString();

        // Tạo HashMap để gửi tin nhắn
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", senderUid);
        hashMap.put("receiver", receiverUid);
        hashMap.put("message", productData);
        hashMap.put("timestamp", String.valueOf(timestamp));
        hashMap.put("dilihat", false);
        hashMap.put("type", "product");

        chatRef.push().setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật ChatList cho cả 2 bên
                    updateChatList(receiverUid);
                    Toast.makeText(Activity_Form.this, "Đã gửi thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    // Truyền UID của người gửi sang Activity_Form_View
                    Intent intent = new Intent(Activity_Form.this, activity_form_view.class);
                    // Truyền thông tin cá nhân
                    intent.putExtra("fullname", etFullname.getText().toString());
                    intent.putExtra("phone", etPhone.getText().toString());
                    intent.putExtra("city", etCity.getText().toString());
                    intent.putExtra("district", etDistrict.getText().toString());
                    intent.putExtra("ward", etWard.getText().toString());
                    intent.putExtra("street", etStreet.getText().toString());
                    intent.putExtra("notes", etNotes.getText().toString());
                    // Truyền UID của người gửi
                    intent.putExtra("UID_sender", senderUid);
                    // Truyền thông tin sản phẩm
                    intent.putExtra("productName", tvProductName.getText().toString());
                    intent.putExtra("productInfo", tvProductInfo.getText().toString());
                    intent.putExtra("productInfo_more", tvProductInfo_more.getText().toString());
                    intent.putExtra("productImage", getIntent().getStringExtra("product_image"));

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(Activity_Form.this, "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateChatList(String receiverUid) {
        // Cập nhật cho người gửi: thêm người nhận vào ChatList của mình
        DatabaseReference chatListRefSender = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(receiverUid);
        chatListRefSender.child("id").setValue(receiverUid);

        // (Tùy chọn) Cập nhật cho người nhận: thêm người gửi vào ChatList của họ
        DatabaseReference chatListRefReceiver = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(receiverUid)
                .child(firebaseUser.getUid());
        chatListRefReceiver.child("id").setValue(firebaseUser.getUid());
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
