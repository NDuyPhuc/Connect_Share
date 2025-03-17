package com.example.save_food.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.save_food.R;
import com.example.save_food.adapter.TabAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalPost extends Fragment {

    private CircleImageView imgAvatar;
    private TextView tvUserName;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    private String currentUserId;

    public PersonalPost() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_post, container, false);

        // Khởi tạo các view
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        // Thiết lập ViewPager2 và TabLayout
        TabAdapter tabAdapter = new TabAdapter(getActivity());
        viewPager.setAdapter(tabAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Bài đăng");
                    break;
                case 1:
                    tab.setText("Yêu cầu nhận sản phẩm");
                    break;
                case 2:
                    tab.setText("Kết quả phản hồi");
                    break;
            }
        }).attach();

        // Tải thông tin người dùng
        loadUserProfile();

        return view;
    }

    private void loadUserProfile() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String image = snapshot.child("image").getValue(String.class);
                    tvUserName.setText(name);
                    Glide.with(getActivity()).load(image).placeholder(R.drawable.person).into(imgAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }
}