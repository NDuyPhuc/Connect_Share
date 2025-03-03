package com.example.save_food.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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

public class BlankFragment extends Fragment {

    private ViewPager2 viewPager2;
    private ArrayList<ViewPagerItem> viewPagerItemArrayList;
    private ArrayList<KhoangCachLocation> khoangCachLocationList = new ArrayList<>();
    private VPAdapter vpAdapter;
    private HashSet<String> uniqueImageLinks = new HashSet<>();

    // Mã yêu cầu quyền truy cập vị trí
    private static final int LOCATION_PERMISSION_CODE = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout cho fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        viewPager2 = view.findViewById(R.id.viewPager2);

        // Khởi tạo danh sách và adapter cho ViewPager2
        viewPagerItemArrayList = new ArrayList<>();
        vpAdapter = new VPAdapter(viewPagerItemArrayList, getActivity());
        viewPager2.setAdapter(vpAdapter);

        // Thiết lập các thuộc tính cho ViewPager2
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Lấy dữ liệu truyền từ Bundle nếu có
        Bundle bundle = getArguments();
        if (bundle != null) {
            ArrayList<KhoangCachLocation> myList = bundle.getParcelableArrayList("my_list");
            if (myList != null) {
                khoangCachLocationList.addAll(myList);
            }
        }

        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền, yêu cầu quyền từ người dùng
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            // Nếu đã có quyền, tiến hành tải dữ liệu từ Firebase
            loadData();
        }

        return view;
    }

    /**
     * Xử lý kết quả yêu cầu cấp quyền
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Nếu quyền được cấp, tải dữ liệu
                loadData();
            } else {
                // Nếu quyền không được cấp, hiển thị thông báo cho người dùng
                Log.w("BlankFragment", "Không có quyền truy cập vị trí. Bài đăng sẽ không hiển thị.");
                Toast.makeText(getContext(), "Không có quyền truy cập vị trí. Bài đăng sẽ không hiển thị.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Phương thức tải dữ liệu từ Firebase và cập nhật ViewPager2
     */
    private void loadData() {
        // Kiểm tra lại quyền trước khi tải dữ liệu (tránh trường hợp không có quyền)
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BlankFragment", "Không có quyền truy cập vị trí. Không tải dữ liệu.");
            return;
        }

        // Sử dụng background thread để xử lý dữ liệu
        new Thread(() -> {
            for (KhoangCachLocation location : khoangCachLocationList) {
                String uid = location.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("ThongTin_UpLoad/" + uid);

                // Sử dụng ChildEventListener để lắng nghe thay đổi dữ liệu tại đường dẫn Firebase
                myRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        // Lấy thông tin bài đăng: tên đơn hàng và địa chỉ
                        String name = snapshot.child("tenDonHang").getValue(String.class);
                        String diaChi = snapshot.child("diaChi").getValue(String.class);
                        DataSnapshot imgSnapshot = snapshot.child("Ảnh");

                        // Duyệt qua từng ảnh được tải lên
                        for (DataSnapshot imgChild : imgSnapshot.getChildren()) {
                            String linkhinh = imgChild.child("linkHinh").getValue(String.class);

                            // Kiểm tra nếu ảnh chưa tồn tại trong tập hợp, thì thêm vào danh sách
                            if (!uniqueImageLinks.contains(linkhinh)) {
                                uniqueImageLinks.add(linkhinh);
                                ViewPagerItem viewPagerItem = new ViewPagerItem(linkhinh, name, diaChi, uid);

                                // Cập nhật danh sách và thông báo cho adapter trên luồng chính
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
