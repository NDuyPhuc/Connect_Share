package com.example.save_food;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.save_food.models.MyClusterItem;
import com.example.save_food.models.MyClusterRenderer;
import com.example.save_food.models.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private final int FINE_PERMISSION_CODE = 1;
    private ClusterManager<MyClusterItem> clusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            SupportMapFragment mapFragment = (SupportMapFragment)
                                    getSupportFragmentManager().findFragmentById(R.id.map);
                            if (mapFragment != null) {
                                mapFragment.getMapAsync(MapsActivity.this);
                            }
                        } else {
                            Toast.makeText(MapsActivity.this,
                                    "Vui lòng bật vị trí và truy cập lại!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Di chuyển camera đến vị trí hiện tại
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

        // Khởi tạo ClusterManager
        clusterManager = new ClusterManager<>(this, mMap);
        // Sử dụng renderer tùy chỉnh để load ảnh marker qua Glide
        MyClusterRenderer renderer = new MyClusterRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer(renderer);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        // Load vị trí người dùng từ Firebase
        loadUserLocationsFromFirebase();

        // Cập nhật vị trí hiện tại của user lên Firebase
        updateCurrentLocationToFirebase();
    }

    private void loadUserLocationsFromFirebase() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        // Lấy dữ liệu một lần, có thể thay bằng addValueEventListener() nếu cần tự động cập nhật
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clusterManager.clearItems();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Double latitude = userSnapshot.child("Latitude").getValue(Double.class);
                    Double longitude = userSnapshot.child("Longitude").getValue(Double.class);
                    String imageUrl = userSnapshot.child("image").getValue(String.class); // giả sử trường ảnh tên "image"
                    if (latitude != null && longitude != null) {
                        LatLng position = new LatLng(latitude, longitude);
                        MyClusterItem item = new MyClusterItem(position, imageUrl);
                        clusterManager.addItem(item);
                    }
                }
                clusterManager.cluster();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this,
                        "Lỗi load dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentLocationToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && currentLocation != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            userRef.child("Latitude").setValue(currentLocation.getLatitude());
            userRef.child("Longitude").setValue(currentLocation.getLongitude());
            // Nếu có thêm trường image, bạn có thể cập nhật ở đây
            Toast.makeText(getApplicationContext(), "Cập nhật vị trí thành công", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Gọi super để đảm bảo xử lý đúng override
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Vui lòng cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
