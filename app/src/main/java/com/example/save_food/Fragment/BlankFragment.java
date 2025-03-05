package com.example.save_food.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.save_food.R;
import com.example.save_food.adapter.CategoryAdapter;
import com.example.save_food.adapter.VPAdapter;
import com.example.save_food.adapter.ViewPagerItem;
import com.example.save_food.models.CategoryItem;
import com.example.save_food.models.KhoangCachLocation;
import com.example.save_food.models.ThongTin_UpLoadClass;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class BlankFragment extends Fragment {

    private ViewPager2 viewPager2;
    private ArrayList<ViewPagerItem> viewPagerItemArrayList;
    private ArrayList<KhoangCachLocation> khoangCachLocationList = new ArrayList<>();
    private VPAdapter vpAdapter;
    private HashSet<String> uniqueImageLinks = new HashSet<>();

    // Mã yêu cầu quyền truy cập vị trí
    private static final int LOCATION_PERMISSION_CODE = 1001;
    // Danh sách chứa tất cả bài đăng
    private List<ThongTin_UpLoadClass> fullPostList = new ArrayList<>();
    // Danh sách bài đăng được lọc (để hiển thị trong fragment)
    private List<ThongTin_UpLoadClass> filteredPostList = new ArrayList<>();
    private List<CategoryItem> categoryList = new ArrayList<>();
    private Map<String, Integer> categoryCountMap = new HashMap<>();
    private String selectedCategory = "---";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        viewPager2 = view.findViewById(R.id.viewPager2);

        // Khởi tạo danh sách và adapter cho ViewPager2
        viewPagerItemArrayList = new ArrayList<>();
        vpAdapter = new VPAdapter(viewPagerItemArrayList, getActivity());
        viewPager2.setAdapter(vpAdapter);

        // Thiết lập thuộc tính cho ViewPager2
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

        // Nếu danh sách vị trí rỗng, tức là không có dữ liệu vị trí,
        // thì tải toàn bộ bài đăng theo cách ngẫu nhiên.
        if (khoangCachLocationList.isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu vị trí, hiển thị bài đăng ngẫu nhiên.", Toast.LENGTH_LONG).show();
            loadDataRandom();
        } else {
            // Nếu có dữ liệu vị trí thì kiểm tra quyền truy cập vị trí.
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Không có quyền truy cập vị trí. Hiển thị bài đăng ngẫu nhiên.", Toast.LENGTH_LONG).show();
                loadDataRandom();
            } else {
                loadData();
            }
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
                // Nếu quyền được cấp, tải dữ liệu theo vị trí có sẵn
                loadData();
            } else {
                // Nếu quyền không được cấp, hiển thị thông báo và tải bài đăng ngẫu nhiên
                Toast.makeText(getContext(), "Không có quyền truy cập vị trí. Hiển thị bài đăng ngẫu nhiên.", Toast.LENGTH_LONG).show();
                loadDataRandom();
            }
        }
    }

    /**
     * Phương thức tải dữ liệu từ Firebase khi có quyền vị trí
     */
    private void loadData() {
        // Kiểm tra lại quyền trước khi tải dữ liệu (tránh trường hợp không có quyền)
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BlankFragment", "Không có quyền truy cập vị trí. Không tải dữ liệu.");
            return;
        }

        new Thread(() -> {
            for (KhoangCachLocation location : khoangCachLocationList) {
                String uid = location.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("ThongTin_UpLoad/" + uid);

                myRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        String name = snapshot.child("tenDonHang").getValue(String.class);
                        String diaChi = snapshot.child("diaChi").getValue(String.class);
                        String nganhHang = snapshot.child("nganhHang").getValue(String.class);
                        String thoiGianHetHan = snapshot.child("thoiGianHetHan").getValue(String.class);
                        String donViHetHan = snapshot.child("donViHetHan").getValue(String.class);
                        DataSnapshot imgSnapshot = snapshot.child("Ảnh");

                        ThongTin_UpLoadClass info = new ThongTin_UpLoadClass(name, diaChi, nganhHang, thoiGianHetHan, donViHetHan);
                        info.setUid(uid);
                        List<DataSnapshot> imageList = new ArrayList<>();
                        for (DataSnapshot imgChild : imgSnapshot.getChildren()) {
                            imageList.add(imgChild);
                        }
                        info.setImageSnapshots(imageList);
                        fullPostList.add(info);
                        if (nganhHang != null) {
                            int count = categoryCountMap.containsKey(nganhHang) ? categoryCountMap.get(nganhHang) : 0;
                            categoryCountMap.put(nganhHang, count + 1);
                        }
                        if (selectedCategory.equals("---") || selectedCategory.equals("All") || (nganhHang != null && nganhHang.equals(selectedCategory))) {
                            filteredPostList.add(info);
                            requireActivity().runOnUiThread(() -> {
                                vpAdapter.notifyItemInserted(filteredPostList.size() - 1);
                            });
                        }

                        for (DataSnapshot imgChild : imgSnapshot.getChildren()) {
                            String linkhinh = imgChild.child("linkHinh").getValue(String.class);
                            if (!uniqueImageLinks.contains(linkhinh)) {
                                uniqueImageLinks.add(linkhinh);
                                ViewPagerItem viewPagerItem = new ViewPagerItem(linkhinh, name, diaChi, uid);
                                requireActivity().runOnUiThread(() -> {
                                    viewPagerItemArrayList.add(viewPagerItem);
                                    vpAdapter.notifyItemInserted(viewPagerItemArrayList.size() - 1);
                                });
                            }
                        }
                    }

                    @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                    @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
                    @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error loading data", error.toException());
                    }
                });
            }
        }).start();
    }

    /**
     * Phương thức tải toàn bộ bài đăng từ Firebase và hiển thị theo thứ tự ngẫu nhiên
     */
    private void loadDataRandom() {
        // Xóa dữ liệu cũ
        fullPostList.clear();
        filteredPostList.clear();
        viewPagerItemArrayList.clear();
        uniqueImageLinks.clear();
        categoryCountMap.clear();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("ThongTin_UpLoad");

        Log.d("loadDataRandom", "Bắt đầu loadDataRandom...");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Duyệt qua từng UID (người dùng)
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Duyệt qua từng bài đăng của người dùng
                    for (DataSnapshot postSnapshot : userSnapshot.getChildren()) {
                        ThongTin_UpLoadClass post = postSnapshot.getValue(ThongTin_UpLoadClass.class);
                        if (post != null) {
                            post.setUid(userSnapshot.getKey());
                            fullPostList.add(post);

                            // Cập nhật số đếm ngành hàng nếu có
                            String nganhHang = post.getNganhHang();
                            if (nganhHang != null) {
                                int count = categoryCountMap.containsKey(nganhHang) ? categoryCountMap.get(nganhHang) : 0;
                                categoryCountMap.put(nganhHang, count + 1);
                            }

                            // Tạo danh sách ảnh cho bài đăng
                            List<DataSnapshot> imageList = new ArrayList<>();
                            if (postSnapshot.hasChild("Ảnh")) {
                                DataSnapshot imgSnapshot = postSnapshot.child("Ảnh");
                                for (DataSnapshot imgChild : imgSnapshot.getChildren()) {
                                    imageList.add(imgChild);
                                    String linkHinh = imgChild.child("linkHinh").getValue(String.class);
                                    if (linkHinh != null) {
                                        // Thêm ảnh của bài đăng vào ViewPagerItem (không kiểm tra unique để đảm bảo tất cả bài được hiển thị)
                                        viewPagerItemArrayList.add(new ViewPagerItem(linkHinh, post.getTenDonHang(), post.getDiaChi(), post.getUid()));
                                    }
                                }
                            } else {
                                Log.d("loadDataRandom", "Bài đăng không có key 'Ảnh': " + post.getTenDonHang());
                            }
                            // Lưu danh sách ảnh vào đối tượng post (để hỗ trợ chức năng lọc sau này)
                            post.setImageSnapshots(imageList);

                            // Nếu đang ở chế độ lọc (selectedCategory khác "Toàn bộ") thì thêm bài theo điều kiện
                            if (selectedCategory.equals("Toàn bộ") || selectedCategory.equals("---") || (nganhHang != null && nganhHang.equals(selectedCategory))) {
                                filteredPostList.add(post);
                            }
                        }
                    }
                }
                Log.d("loadDataRandom", "Số lượng bài đăng: " + fullPostList.size());
                Log.d("loadDataRandom", "Số lượng ảnh hiển thị: " + viewPagerItemArrayList.size());

                // Xáo trộn danh sách hiển thị bài đăng
                Collections.shuffle(viewPagerItemArrayList);
                requireActivity().runOnUiThread(() -> vpAdapter.notifyDataSetChanged());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("loadDataRandom", "Firebase error: " + error.getMessage());
            }
        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        // Nếu bạn cần cập nhật danh sách ngành hàng cho bộ lọc, có thể gọi loadDataFromFirebase() hoặc tích hợp
        loadDataFromFirebase();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.category_item, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.category_item) {
            showCategoryDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDataFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ThongTin_UpLoad");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot userSnapshot, @Nullable String previousChildName) {
                for (DataSnapshot postSnapshot : userSnapshot.getChildren()) {
                    ThongTin_UpLoadClass post = postSnapshot.getValue(ThongTin_UpLoadClass.class);
                    if (post != null) {
                        fullPostList.add(post);
                        String category = post.getNganhHang();
                        if (category != null && !category.isEmpty()) {
                            int count = categoryCountMap.containsKey(category) ? categoryCountMap.get(category) : 0;
                            categoryCountMap.put(category, count + 1);
                        }
                    }
                }
                updateCategoryList();
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void updateCategoryList() {
        if (!categoryList.isEmpty()) {
            categoryList.clear();
        }
        categoryList.add(new CategoryItem("Toàn bộ", fullPostList.size()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            categoryList.add(new CategoryItem("Quần áo", categoryCountMap.getOrDefault("Quần áo", 0)));
            categoryList.add(new CategoryItem("Đồ vật học tập", categoryCountMap.getOrDefault("Đồ vật học tập", 0)));
            categoryList.add(new CategoryItem("Đồ vật sinh hoạt", categoryCountMap.getOrDefault("Đồ vật sinh hoạt", 0)));
            categoryList.add(new CategoryItem("Đồ vật nghề nghiệp", categoryCountMap.getOrDefault("Đồ vật nghề nghiệp", 0)));
        }
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_category, null);
        builder.setView(dialogView);
        ListView lvCategories = dialogView.findViewById(R.id.lvCategories);

        CategoryAdapter adapter = new CategoryAdapter(getContext(), categoryList);
        lvCategories.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        lvCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoryItem selectedItem = categoryList.get(position);
                filterPosts(selectedItem.getName());
                dialog.dismiss();
            }
        });
    }

    private void filterPosts(String category) {
        selectedCategory = category; // Lưu ngành hàng đã chọn
        filteredPostList.clear();
        viewPagerItemArrayList.clear();
        uniqueImageLinks.clear();

        // Duyệt danh sách bài đăng đầy đủ và lọc theo ngành hàng
        for (ThongTin_UpLoadClass post : fullPostList) {
            if (category.equals("Toàn bộ") || (post.getNganhHang() != null && post.getNganhHang().equals(category))) {
                filteredPostList.add(post);
                // Thêm ảnh của bài đăng vào ViewPagerItem chỉ một lần (ví dụ: ảnh đầu tiên)
                List<DataSnapshot> snapshots = post.getImageSnapshots();
                if (snapshots != null && !snapshots.isEmpty()) {
                    // Lấy ảnh đầu tiên
                    DataSnapshot firstImg = snapshots.get(0);
                    String linkHinh = firstImg.child("linkHinh").getValue(String.class);
                    if (linkHinh != null && !uniqueImageLinks.contains(linkHinh)) {
                        uniqueImageLinks.add(linkHinh);
                        viewPagerItemArrayList.add(new ViewPagerItem(linkHinh, post.getTenDonHang(), post.getDiaChi(), post.getUid()));
                    }
                }
            }
        }
        vpAdapter.notifyDataSetChanged();
    }
}
