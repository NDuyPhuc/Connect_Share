package com.example.save_food;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.save_food.models.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private final int FINE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(MapsActivity.this);
                    }
                }
                else{
                    Toast.makeText(MapsActivity.this, "Vui lòng bật vị trí và truy cập lại!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;

        // Di chuyển camera đến vị trí hiện tại
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        this.gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

        // Lấy dữ liệu từ Intent và hiển thị các Marker
        getUserLocationsFromIntent();

        // Cập nhật vị trí hiện tại lên Firebase
        updateCurrentLocationToFirebase();
    }

    private void getUserLocationsFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String userLocationsJson = intent.getStringExtra("userLocationsJson");
            if (userLocationsJson != null) {
                Gson gson = new Gson();
                Type userListType = new TypeToken<ArrayList<UserLocation>>() {}.getType();
                ArrayList<UserLocation> userLocations = gson.fromJson(userLocationsJson, userListType);
                if (userLocations != null) {
                    for (UserLocation location : userLocations) {
                        LatLng uploadmaps = new LatLng(location.getLatitude(), location.getLongitude());
                        loadImageAndAddMarker(uploadmaps, location.getImage());
                    }
                }
            }
        }
    }

    private void loadImageAndAddMarker(LatLng position, String imageUrl) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        Bitmap circularBitmap = getCircularBitmap(resource);
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(circularBitmap);
                        gMap.addMarker(new MarkerOptions().position(position).icon(icon));
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // Xử lý khi hình ảnh bị xóa khỏi view
                    }
                });
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = 120;
        int height = 120;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        Bitmap circularBitmap = Bitmap.createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, rect, rect, paint);
        return circularBitmap;
    }

    private void updateCurrentLocationToFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("Latitude", currentLocation.getLatitude());
            hashMap.put("Longitude", currentLocation.getLongitude());

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("Users");
            databaseReference.child(user.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getApplicationContext(), "Cập nhật vị trí thành công", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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