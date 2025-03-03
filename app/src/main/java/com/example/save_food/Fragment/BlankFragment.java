package com.example.save_food.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.save_food.R;
import com.example.save_food.adapter.VPAdapter;
import com.example.save_food.adapter.ViewPagerItem;
import com.example.save_food.models.KhoangCachLocation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BlankFragment extends Fragment {

    private ViewPager2 viewPager2;
    private ArrayList<ViewPagerItem> viewPagerItemArrayList;
    private ArrayList<KhoangCachLocation> khoangCachLocationList = new ArrayList<>();
    private VPAdapter vpAdapter;
    private HashSet<String> uniqueImageLinks = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        viewPager2 = view.findViewById(R.id.viewPager2);

        // Khởi tạo adapter và thiết lập cho ViewPager2
        viewPagerItemArrayList = new ArrayList<>();
        vpAdapter = new VPAdapter(viewPagerItemArrayList, getActivity());
        viewPager2.setAdapter(vpAdapter);

        // Thiết lập các thuộc tính cho ViewPager2
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Nhận dữ liệu từ Bundle (nếu có)
        Bundle bundle = getArguments();
        if (bundle != null) {
            ArrayList<KhoangCachLocation> myList = bundle.getParcelableArrayList("my_list");
            if (myList != null) {
                khoangCachLocationList.addAll(myList);
            }
        }

        // Tải dữ liệu từ Firebase
        loadData();

        return view;
    }

    private void loadData() {
        // Sử dụng background thread để xử lý dữ liệu
        new Thread(() -> {
            for (KhoangCachLocation location : khoangCachLocationList) {
                String uid = location.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("ThongTin_UpLoad/" + uid);

                // Sử dụng ChildEventListener để lắng nghe thay đổi dữ liệu
                myRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        String name = snapshot.child("tenDonHang").getValue(String.class);
                        String diaChi = snapshot.child("diaChi").getValue(String.class);
                        DataSnapshot imgSnapshot = snapshot.child("Ảnh");

                        for (DataSnapshot imgChild : imgSnapshot.getChildren()) {
                            String linkhinh = imgChild.child("linkHinh").getValue(String.class);

                            // Kiểm tra xem ảnh đã tồn tại trong tập hợp chưa
                            if (!uniqueImageLinks.contains(linkhinh)) {
                                uniqueImageLinks.add(linkhinh);
                                ViewPagerItem viewPagerItem = new ViewPagerItem(linkhinh, name, diaChi, uid);

                                // Thêm item vào danh sách và cập nhật adapter trên luồng chính
                                requireActivity().runOnUiThread(() -> {
                                    viewPagerItemArrayList.add(viewPagerItem);
                                    vpAdapter.notifyItemInserted(viewPagerItemArrayList.size() - 1);
                                });
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        // Xử lý khi dữ liệu thay đổi (nếu cần)
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        // Xử lý khi dữ liệu bị xóa (nếu cần)
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        // Xử lý khi dữ liệu di chuyển (nếu cần)
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error loading data", error.toException());
                    }
                });
            }
        }).start();
    }
}