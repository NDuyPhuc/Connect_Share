package com.example.save_food.Fragment;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.save_food.BeautifulProgressDialog;
import com.example.save_food.MapsActivity;
import com.example.save_food.R;
import com.example.save_food.UploadActivity;
import com.example.save_food.models.KhoangCachLocaitonSort;
import com.example.save_food.models.KhoangCachLocation;
import com.example.save_food.models.UserLocation;
import com.example.save_food.registerActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class homeFragment extends Fragment {
    Location location;
    UserLocation userLocation;
    BeautifulProgressDialog dialog;
    FirebaseAuth auth;
    ArrayList<UserLocation> userLocations = new ArrayList<>();
    Location currentLocation;
    ArrayList<KhoangCachLocation> khoangCachLocationList = new ArrayList<>();
    public List<KhoangCachLocaitonSort> khoangCachLocaitonSorts = new ArrayList<>();
    FusedLocationProviderClient fusedLocationProviderClient;
    double latitude, longitude;
    FirebaseAuth firebaseAuth;
    private final int FINE_PERMISSION_CODE = 1;
    CardView showmap, post;
    ProgressDialog progressDialog;
    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new BeautifulProgressDialog(getActivity(), BeautifulProgressDialog.withGIF, "Please wait");
        Uri myUri = Uri.fromFile(new File("//android_asset/gif_food_and_smile.gif"));
        auth = FirebaseAuth.getInstance();
        dialog.setGifLocation(myUri);
        dialog.setLayoutColor(getResources().getColor(R.color.BeautifulProgressDialogBg));
        dialog.setMessageColor(getResources().getColor(R.color.white));

        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
            NavigationView navigationView = getActivity().findViewById(R.id.Nav_view);

            // Xóa trạng thái chọn mặc định
            bottomNavigationView.setSelectedItemId(R.id.none);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
        boolean k = true;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.hongcogihet) {
                    //openFragment(new BlankFragment());
                    // Tạo ra Bundle để đính kèm vào Fragment
                    navigationView.setCheckedItem(R.id.none);
                    k=false;
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("my_list", khoangCachLocationList);
                    BlankFragment fragment = new BlankFragment();
                    // Thiết lập Bundle cho BlankFragment
                    fragment.setArguments(bundle);

                    // Chuyển sang BlankFragment
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, fragment);
//                    transaction.addToBackStack(null);
                    transaction.commit();
                    getLastLocation();
                    FirebaseUser user = auth.getCurrentUser();
                    updateLocationOnLogin(user);
//                    // Ẩn fragment_container
//                    View fragmentContainer = getView().findViewById(R.id.container_home);
//                    fragmentContainer.setVisibility(View.GONE);


                    return true;
                }
                if (itemId == R.id.Users_nav) {
                    navigationView.setCheckedItem(R.id.none);
                    openFragment(new UsersFragment());
                    return true;
                }
                else if (itemId == R.id.upload_post_item) {
                    navigationView.setCheckedItem(R.id.none);
                    Intent intent = new Intent(getActivity(), UploadActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (itemId == R.id.chat_nav) {
                    navigationView.setCheckedItem(R.id.none);
                    openFragment(new ChatListFragment());
                    return true;
                }
                else if (itemId == R.id.personal_post) {
                    navigationView.setCheckedItem(R.id.none);
                    openFragment(new PersonalPost());
                    return true;
                }
                return false;
            }
        });
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        showmap = view.findViewById(R.id.showMap);
        post = view.findViewById(R.id.upload);
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setBackground(null);
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            // Thêm giá trị trả về ở đây nếu cần
        } else {
            // Code xử lý khi có quyền vị trí
        }




        khoangCachLocationList.clear();
        userLocations.clear();
        GetSumUID();
        for(int i=0;i<khoangCachLocationList.size();i++){
            Log.d("khoangcach", khoangCachLocationList.get(i).getDistance() + " - " + khoangCachLocationList.get(i).getUid() );
        }
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sử dụng biến khoangCachLocationList ở đây
                KetQua();
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                startActivity(intent);
            }
        });

        return view;


    }
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Quyền vị trí đã được cấp phép
            return true;
        } else {
            // Quyền vị trí chưa được cấp phép, yêu cầu người dùng cấp phép
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        }
    }
    private void KetQua() {
        for(int i=0;i<khoangCachLocationList.size();i++){
            Log.d("khoangcach", khoangCachLocationList.get(i).getDistance() + " - " + khoangCachLocationList.get(i).getUid() );
        }
    }

    private void GetSumUID() {
        ArrayList<String> uidList = new ArrayList<>();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String uid = userSnapshot.getKey();
                    // do something with uid
                    uidList.add(uid);
                }
                // Log toàn bộ các phần tử trong ArrayList uidList
                for (int i = 0; i < uidList.size(); i++) {
                    Log.d("UID", uidList.get(i));
                }
                KiemtraFireBase(uidList);
            }

            private void KiemtraFireBase(ArrayList<String> uidList) {
                ArrayList<String> uidListupload = new ArrayList<>();
                for (int i = 0; i < uidList.size(); i++) {
                    int userid = i;
                    DatabaseReference databaseReference = FirebaseDatabase
                            .getInstance().getReference("ThongTin_UpLoad");
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Kiểm tra xem nút con với ID của người dùng có tồn tại hay không
                            if (dataSnapshot.hasChild(uidList.get(userid))) {
                                uidListupload.add(uidList.get(userid));
                                    GetToaDo(uidListupload);
                                Log.d("EEE", uidList.get(userid));
                                // Thực hiện các thuật toán khác
                                // ...
                            } else {
                                Log.d("CCC", "không có nút " + uidList.get(userid));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Xử lý lỗi
                            Log.d("CCC", "không có nút " + uidList.get(userid));


                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle error
            }
        });

    }

    private void GetToaDo(ArrayList<String> uidListupload) {
        ArrayList<UserLocation> userLocationsCopy = new ArrayList<>();
        for (int i = 0; i < uidListupload.size(); i++) {
            int uiduser = i;
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users/" + uidListupload.get(i));
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (isAdded()) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("Latitude") && dataSnapshot.hasChild("Longitude")) {
                            double latitude = dataSnapshot.child("Latitude").getValue(Double.class);
                            double longitude = dataSnapshot.child("Longitude").getValue(Double.class);
                            String url = dataSnapshot.child("image").getValue(String.class);
                            userLocation = new UserLocation(uidListupload.get(uiduser), latitude, longitude, url);
                            userLocations.add(userLocation);
                            userLocationsCopy.add(userLocation);
                            if (uiduser == uidListupload.size() - 1) {
                                showmap.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Kiểm tra quyền vị trí trước khi thực hiện hoạt động
                                        if (checkLocationPermission()) {
                                            // Quyền vị trí đã được cấp phép, thực hiện các hoạt động liên quan đến vị trí
                                            processUserLocations(getActivity(), userLocations);
                                        } else {
                                            // Người dùng chưa cấp phép quyền vị trí
                                            // Có thể hiển thị thông báo hoặc thực hiện các hành động khác tùy thuộc vào yêu cầu của bạn
//                                        Toast.makeText(getActivity(), "Vui lòng cấp phép quyền vị trí để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                if (isAdded() && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                    // Yêu cầu cấp quyền truy cập vị trí từ fragment
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                                    return;
                                }
                                else if (!isAdded()) {
                                    Log.w("homeFragment", "Fragment không còn attached, không thể kiểm tra quyền.");
                                }


                                fusedLocationProviderClient.getLastLocation()
                                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                if (location != null) {
                                                    // Lấy được vị trí hiện tại của người dùng
                                                    currentLocation = location;
                                                    if (currentLocation == null) {
//                                                    Toast.makeText(getActivity(), "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                                                        // Sử dụng dữ liệu mặc định hoặc ẩn một số chức năng
                                                    } else {
                                                        TinhKhoangCach(userLocations, currentLocation);
                                                    }


                                                }
                                            }
                                        });

                            }

                            // Di chuyển lệnh Log.d vào bên trong phương thức onDataChange()
                            //Log.d("AAA" + uiduser + " ", userLocations.get(uiduser).getLatitude() + " - " + userLocations.get(uiduser).getLongitude() + " - " + userLocations.get(uiduser).getUid() );
                            Log.d("Size", String.valueOf(userLocations.size()));

                        } else {
                            Log.d("CCC", "Lỗi!!!!");
//                        Intent intent = new Intent(getActivity(), MapsActivity.class);
//                        startActivity(intent);
                        }
                    }
                    else{
                        Log.w("homeFragment", "Fragment không còn attached, bỏ qua xử lý.");
                    }
                }

//                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // handle error
                    Log.d("BBB", "Lỗi!!!");
                }
            });
        }
        Log.d("Size", String.valueOf(userLocations.size()));
    }

    private void TinhKhoangCach(ArrayList<UserLocation> userLocations, Location currentLocation) {
            for(int i = 0; i < userLocations.size(); i++){
                    double khoangcach = Math.sqrt(Math.pow(userLocations.get(i).getLatitude() - currentLocation.getLatitude(), 2) + Math.pow(userLocations.get(i).getLongitude() - currentLocation.getLongitude(), 2));
                    khoangCachLocationList.add(new KhoangCachLocation(khoangcach, userLocations.get(i).getUid()));


            }
            Collections.sort(khoangCachLocationList, new Comparator<KhoangCachLocation>() {
                public int compare(KhoangCachLocation o1, KhoangCachLocation o2) {
                    return Double.compare(o1.getDistance(), o2.getDistance());
                }
            });


    }
    //
    private void processUserLocations(Context context, ArrayList<UserLocation> userLocations) {
        Log.d("homeFragment", "Mở MapsActivity");
        Intent intent = new Intent(context, MapsActivity.class);
        Gson gson = new Gson();
        String userLocationsJson = gson.toJson(userLocations);
        intent.putExtra("userLocationsJson", userLocationsJson);
        context.startActivity(intent);
    }
    // Phương thức lấy vị trí cuối cùng đã biết (last known location)
    private void getLastLocation(){
        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa được cấp quyền, yêu cầu quyền
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        // Lấy vị trí cuối cùng đã biết từ FusedLocationProviderClient
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Nếu lấy được vị trí, gán cho currentLocation
                    currentLocation = location;
                } else {
                    // Nếu không lấy được vị trí, gọi hàm requestLocationUpdate để yêu cầu cập nhật vị trí mới
                    requestLocationUpdate();
                }
            }
        });
    }
    // Phương thức cập nhật vị trí của người dùng lên Firebase
    private void updateLocationOnLogin(FirebaseUser user) {
        // Kiểm tra xem user có hợp lệ không
        if (user != null) {
            // Kiểm tra xem currentLocation đã được khởi tạo chưa
            if (currentLocation != null) {
                // Nếu currentLocation không null, lấy tọa độ hiện tại
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();

                // Lấy reference đến nút người dùng trong Firebase Realtime Database
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

                // Tạo HashMap chứa dữ liệu cần cập nhật (tọa độ)
                HashMap<String, Object> locationMap = new HashMap<>();
                locationMap.put("Latitude", latitude);
                locationMap.put("Longitude", longitude);

                // Thực hiện cập nhật dữ liệu lên Firebase
                userRef.updateChildren(locationMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Nếu cập nhật then công, log thông báo thành công
                                    Log.d(TAG, "Location updated successfully");
                                    //TODO
                                    refreshData();
                                } else {
                                    // Nếu cập nhật thất bại, log thông báo lỗi
                                    Log.w(TAG, "Error updating location", task.getException());
                                }
                            }
                        });
            } else {
                // Nếu currentLocation đang null thì log thông báo và gọi hàm requestLocationUpdate()
                Log.w(TAG, "currentLocation is null. Unable to update location.");
                requestLocationUpdate();

            }
        }
    }
    // Trong homeFragment, thêm phương thức refreshData() để làm mới dữ liệu
    private void refreshData() {
        // Xóa các danh sách dữ liệu cũ
        khoangCachLocationList.clear();
        userLocations.clear();
        // Gọi lại hàm lấy dữ liệu từ Firebase
        GetSumUID();
    }

    // Phương thức yêu cầu cập nhật vị trí mới nếu không lấy được vị trí hiện tại
    private void requestLocationUpdate() {
        // Kiểm tra quyền truy cập vị trí, nếu chưa được cấp thì yêu cầu cấp quyền
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        // Yêu cầu lấy vị trí mới với độ chính xác cao
        fusedLocationProviderClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Cập nhật currentLocation nếu lấy được vị trí mới
                            currentLocation = location;
                            Log.d(TAG, "New location obtained: " + location.getLatitude() + ", " + location.getLongitude());
//                            Toast.makeText(getActivity(), "Vị trí đã được bật, vui lòng đợi vài giây để loading...và truy cập lại", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "Unable to obtain new location update");
//                            Toast.makeText(getActivity(), "Vui lòng bật vị trí để sử dụng", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
