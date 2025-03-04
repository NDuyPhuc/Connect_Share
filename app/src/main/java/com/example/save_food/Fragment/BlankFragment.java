package com.example.save_food.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
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

import java.util.ArrayList;
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
    private List<ThongTin_UpLoadClass>  filteredPostList = new ArrayList<>();
    private List<CategoryItem> categoryList = new ArrayList<>();
    private Map<String, Integer> categoryCountMap = new HashMap<>();
    private String selectedCategory = "---";


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
                        String nganhHang = snapshot.child("nganhHang").getValue(String.class);
                        String thoiGianHetHan = snapshot.child("thoiGianHetHan").getValue(String.class);
                        String donViHetHan = snapshot.child("donViHetHan").getValue(String.class);
                        DataSnapshot imgSnapshot = snapshot.child("Ảnh");
                        // Tạo đối tượng thông tin bài đăng
                        ThongTin_UpLoadClass info = new ThongTin_UpLoadClass(name, diaChi, nganhHang, thoiGianHetHan, donViHetHan);
                        info.setUid(uid);
                        // Lưu danh sách ảnh vào đối tượng bài đăng
                        List<DataSnapshot> imageList = new ArrayList<>();
                        for (DataSnapshot imgChild : imgSnapshot.getChildren()) {
                            imageList.add(imgChild);
                        }
                        info.setImageSnapshots(imageList);
                        // Lưu vào danh sách đầy đủ
                        fullPostList.add(info);
                        // Cập nhật số đếm ngành hàng
                        if (nganhHang != null) {
                            int count = categoryCountMap.containsKey(nganhHang) ? categoryCountMap.get(nganhHang) : 0;
                            categoryCountMap.put(nganhHang, count + 1);
                        }
                        // Nếu bài đăng này phù hợp với bộ lọc hiện tại, thêm vào danh sách hiển thị
                        if (selectedCategory.equals("---") || selectedCategory.equals("All") || (nganhHang != null && nganhHang.equals(selectedCategory))) {
                                filteredPostList.add(info);
                                // Chạy trên UI thread để cập nhật adapter
                                requireActivity().runOnUiThread(() -> {
                                vpAdapter.notifyItemInserted(filteredPostList.size() - 1);
                            });
                        }

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        loadDataFromFirebase();

    }

    @Override
    public  void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.category_item, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();
        if (id == R.id.category_item){
            showCategoryDialog();
            return true;

        }
        return  super.onOptionsItemSelected(item);
    }

    private void loadDataFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ThongTin_UpLoad");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot userSnapshot, @Nullable String previousChildName) {
                // userSnapshot đại diện cho mỗi UID (một người dùng)
                for (DataSnapshot postSnapshot : userSnapshot.getChildren()) {
                    // postSnapshot đại diện cho từng bài đăng, được lưu với key số như "1", "2",...
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
                // Cập nhật lại danh sách các ngành hàng khi có dữ liệu mới
                updateCategoryList();
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Hàm cập nhật danh sách ngành hàng dựa vào số liệu trong categoryCountMap
    private void updateCategoryList() {
        if(!categoryList.isEmpty()) {
            categoryList.clear();
        }
        // "Toàn bộ" hiển thị tổng số bài đăng
        categoryList.add(new CategoryItem("Toàn bộ", fullPostList.size()));
        // Cập nhật các ngành cụ thể, sử dụng giá trị đếm từ map (nếu chưa có thì mặc định là 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            categoryList.add(new CategoryItem("Quần áo", categoryCountMap.getOrDefault("Quần áo", 0)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            categoryList.add(new CategoryItem("Đồ vật học tập", categoryCountMap.getOrDefault("Đồ vật học tập", 0)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            categoryList.add(new CategoryItem("Đồ vật sinh hoạt", categoryCountMap.getOrDefault("Đồ vật sinh hoạt", 0)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            categoryList.add(new CategoryItem("Đồ vật nghề nghiệp", categoryCountMap.getOrDefault("Đồ vật nghề nghiệp", 0)));
        }
    }

    // Hàm hiển thị dialog danh sách ngành hàng
    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_category, null);
        builder.setView(dialogView);
        ListView lvCategories = dialogView.findViewById(R.id.lvCategories);

        // Sử dụng CategoryAdapter với danh sách ngành hàng đã có
        CategoryAdapter adapter = new CategoryAdapter(getContext(), categoryList);
        lvCategories.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        lvCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoryItem selectedItem = categoryList.get(position);
                filterPosts(selectedItem.getName()); // Gọi hàm lọc
                dialog.dismiss();
            }
        });

    }
    private void filterPosts(String category) {
        selectedCategory = category; // Lưu ngành hàng đã chọn

        // Xóa danh sách bài đăng và danh sách ViewPager
        filteredPostList.clear();
        viewPagerItemArrayList.clear();
        uniqueImageLinks.clear();

        // Duyệt danh sách bài đăng đầy đủ và lọc theo ngành hàng
        for (ThongTin_UpLoadClass post : fullPostList) {
            if (category.equals("Toàn bộ") || (post.getNganhHang() != null && post.getNganhHang().equals(category))) {
                filteredPostList.add(post);

                // Thêm ảnh của bài đăng vào ViewPager nếu chưa có
                for (DataSnapshot imgChild : post.getImageSnapshots()) {  // Giả sử bạn có một danh sách ảnh trong post
                    String linkHinh = imgChild.child("linkHinh").getValue(String.class);
                    if (!uniqueImageLinks.contains(linkHinh)) {
                        uniqueImageLinks.add(linkHinh);
                        viewPagerItemArrayList.add(new ViewPagerItem(linkHinh, post.getTenDonHang(), post.getDiaChi(), post.getUid()));
                    }
                }
            }
        }

        // Cập nhật lại ViewPager
        vpAdapter.notifyDataSetChanged();
    }

}
