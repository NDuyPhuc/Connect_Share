package com.example.save_food;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.save_food.Fragment.BlankFragment;
import com.example.save_food.models.MyClusterItem;
import com.example.save_food.models.MyClusterRenderer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
        // Gán sự kiện cho button quay về MainActivity
        Button btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(v -> {
            // Nếu MainActivity là activity gọi MapsActivity, finish() là đủ.
            finish();

            // Nếu muốn khởi tạo lại MainActivity, bạn có thể sử dụng:
//             Intent intent = new Intent(MapsActivity.this, MainActivity.class);
//             startActivity(intent);
        });
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
    public void onBackPressed() {
        finish();
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Move camera to current location
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

        // Always enable "My Location" layer (blue dot)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        // Initialize ClusterManager
        clusterManager = new ClusterManager<>(this, mMap);
        MyClusterRenderer renderer = new MyClusterRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer(renderer);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        // Load initial markers
        loadUserLocationsFromFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean showLocation = prefs.getBoolean("showUserLocation", true);
        Log.d("MapsActivity", "showLocation in onResume: " + showLocation); // Thêm log để kiểm tra
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(showLocation);
                mMap.getUiSettings().setMyLocationButtonEnabled(showLocation);
            } else {
                mMap.setMyLocationEnabled(false);
            }
        }
    }

    private void loadUserLocationsFromFirebase() {
        clusterManager.clearItems();
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("ThongTin_UpLoad");
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uidSet = new HashSet<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    uidSet.add(userSnapshot.getKey());
                }
                int totalUids = uidSet.size();
                if (totalUids == 0) {
                    clusterManager.cluster();
                    return;
                }
                AtomicInteger counter = new AtomicInteger(0);
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
                for (String uid : uidSet) {
                    usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            Double latitude = userSnapshot.child("Latitude").getValue(Double.class);
                            Double longitude = userSnapshot.child("Longitude").getValue(Double.class);
                            String imageUrl = userSnapshot.child("image").getValue(String.class);
                            Boolean showLocation = userSnapshot.child("showLocation").getValue(Boolean.class);
                            if (latitude != null && longitude != null && imageUrl != null && (showLocation == null || showLocation)) {
                                LatLng position = new LatLng(latitude, longitude);
                                MyClusterItem item = new MyClusterItem(position, imageUrl);
                                clusterManager.addItem(item);
                            }
                            if (counter.incrementAndGet() == totalUids) {
                                clusterManager.cluster();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("MapsActivity", "Lỗi khi lấy dữ liệu người dùng: " + error.getMessage());
                            if (counter.incrementAndGet() == totalUids) {
                                clusterManager.cluster();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MapsActivity", "Lỗi khi lấy bài đăng: " + error.getMessage());
                clusterManager.cluster();
            }
        });
    }    private void updateCurrentLocationToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && currentLocation != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            userRef.child("Latitude").setValue(currentLocation.getLatitude());
            userRef.child("Longitude").setValue(currentLocation.getLongitude());
            Toast.makeText(getApplicationContext(), "Cập nhật vị trí thành công", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Vui lòng cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BroadcastReceiver locationToggleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("LOCATION_TOGGLE".equals(intent.getAction())) {
                loadUserLocationsFromFirebase();
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("LOCATION_TOGGLE");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            registerReceiver(locationToggleReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(locationToggleReceiver, filter);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(locationToggleReceiver);
    }
}