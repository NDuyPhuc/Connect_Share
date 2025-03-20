package com.example.save_food;

import static android.content.ContentValues.TAG;
import static android.telephony.CellLocation.requestLocationUpdate;

import static com.example.save_food.R.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.save_food.Fragment.BlankFragment;
import com.example.save_food.Fragment.ChangePasswordFragment;
import com.example.save_food.Fragment.ChatListFragment;
import com.example.save_food.Fragment.UsersFragment;
import com.example.save_food.Fragment.homeFragment;
import com.example.save_food.notification.Token;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button showMap;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser user;
    FirebaseAuth mAuth;
    DrawerLayout drawerLayout;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private NavigationView mNavigationView;
    CircularImageView imgAvatar;
    TextView tvname;
    Toolbar toolbar;
    FragmentManager fragmentManager = getSupportFragmentManager();

    BottomNavigationView bottomNavigationView;
    String mUID;
    public static String SHARED_PREFS = "sharedPrefs";
    public static int RC_NOTIFICATIONS = 99;
    public static int LOCATIONS = 991;
    private Location currentLocation;
    private final int FINE_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        Location currentLocation;
        mNavigationView = findViewById(R.id.Nav_view);
        imgAvatar = mNavigationView.getHeaderView(0).findViewById(R.id.img_avatar);
        tvname = mNavigationView.getHeaderView(0).findViewById(R.id.tvName);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_draw_open, R.string.navigation_draw_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);
        mNavigationView.setNavigationItemSelectedListener(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationView navigationView = findViewById(R.id.Nav_view);
        // Chọn item đầu tiên khi mở app
//        navigationView.setCheckedItem(R.id.google_map);
        // Xóa trạng thái chọn mặc định
        bottomNavigationView.setSelectedItemId(R.id.none);
        // Kiểm tra quyền truy cập vị trí
        checkLocationPermission();
        checkUserStatus();
        FirebaseUser user = mAuth.getCurrentUser();
        getLastLocation();
        updateLocationOnLogin(user);
        setupNavigationSwitch();

        if (isNetworkAvailable()) {
            loadDataFromFirebase();
        } else {
            Toast.makeText(this, "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
        }
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();

                    tvname.setText(name);
                    try {
                        Glide.with(MainActivity.this)
                                .load(image)
                                .placeholder(R.drawable.person) // Hình ảnh mặc định khi đang tải
                                .error(R.drawable.person) // Hình ảnh mặc định nếu tải thất bại
                                .into(imgAvatar);
                    } catch (Exception e) {
                        Log.e("GlideError", "Lỗi khi tải hình ảnh: " + e.getMessage());
                        Picasso.get().load(R.drawable.person).into(imgAvatar); // Sử dụng Picasso nếu Glide thất bại
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi khi đọc dữ liệu: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.hongcogihet) {
                    bottomNavigationView.setSelectedItemId(R.id.none);
                    openFragment(new BlankFragment());
                    return true;
                } else if (id == R.id.Users_nav) {
                    bottomNavigationView.setSelectedItemId(R.id.none);
                    openFragment(new UsersFragment());
                    return true;
                } else if (id == R.id.chat_nav) {
                    bottomNavigationView.setSelectedItemId(R.id.none);
                    openFragment(new ChatListFragment());
                    return true;
                }
                return false;
            }
        });

        // Kiểm tra xem homeFragment đã tồn tại chưa
        Fragment homeFragment = fragmentManager.findFragmentByTag("HOME_FRAGMENT_TAG");
        if (homeFragment == null) {
            homeFragment = new homeFragment();
            // Add fragment mà không gán container cho nó (headless fragment) hoặc ẩn nó đi
            fragmentManager.beginTransaction().add(homeFragment, "HOME_FRAGMENT_TAG").commit();
        }
        // Tiếp tục mở BlankFragment cho giao diện chính
        fragmentManager.beginTransaction().replace(R.id.content_frame, new BlankFragment()).commit();


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("FCM Token", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Lấy token thành công
                String token = task.getResult();
                updateToken(token);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATIONS);
        }

    }

    private void setupNavigationSwitch() {
        NavigationView navigationView = findViewById(R.id.Nav_view);
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.menu_toggle_location);
        View actionView = menuItem.getActionView();
        if (actionView != null) {
            SwitchCompat switchLocation = actionView.findViewById(R.id.switch_location);
            if (switchLocation != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                userRef.child("showLocation").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean showLocation = snapshot.getValue(Boolean.class) != null ? snapshot.getValue(Boolean.class) : true;
                        switchLocation.setChecked(showLocation);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MainActivity", "Lỗi khi lấy showLocation: " + error.getMessage());
                    }
                });

                switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    userRef.child("showLocation").setValue(isChecked);
                    Intent intent = new Intent("LOCATION_TOGGLE");
                    intent.putExtra("showUserLocation", isChecked);
                    sendBroadcast(intent);
                    Toast.makeText(this, isChecked ? "Đã bật vị trí" : "Đã tắt vị trí", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
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
                                    Toast.makeText(MainActivity.this, "Đã bật vị trí, vui lòng thoát ra truy cập lại!", Toast.LENGTH_SHORT).show();
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

    private void getLastLocation() {
        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa được cấp quyền, yêu cầu quyền
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
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
    private void requestLocationUpdate() {
        // Kiểm tra quyền truy cập vị trí, nếu chưa được cấp thì yêu cầu cấp quyền
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
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
                            Toast.makeText(MainActivity.this, "Vị trí đã được bật, vui lòng đợi vài giây để loading...và truy cập lại", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "Unable to obtain new location update");
//                            Toast.makeText(MainActivity.this, "Vui lòng bật vị trí để sử dụng", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Cho phép", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "Không cho phép", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == LOCATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent i = getIntent();
                finish();
                startActivity(i);
            } else {
                Toast.makeText(this, "Không cho phép", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            Log.e("NetworkUtils", "Không thể lấy ConnectivityManager");
            return false;
        }

        // Kiểm tra kết nối mạng cho các phiên bản Android từ 6.0 (Marshmallow) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Lấy thông tin về mạng hiện tại
            android.net.Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                Log.e("NetworkUtils", "Không có mạng nào được kết nối");
                return false;
            }

            // Lấy thông tin chi tiết về mạng
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities == null) {
                Log.e("NetworkUtils", "Không thể lấy NetworkCapabilities");
                return false;
            }

            // Kiểm tra xem mạng có kết nối Internet không
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            // Kiểm tra kết nối mạng cho các phiên bản Android cũ hơn
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null) {
                Log.e("NetworkUtils", "Không có mạng nào được kết nối");
                return false;
            }

            // Kiểm tra xem mạng có đang kết nối và có thể truy cập Internet không
            return activeNetworkInfo.isConnected() && activeNetworkInfo.isAvailable();
        }
    }


    private void loadDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Xử lý dữ liệu từ Firebase
                Log.d("FirebaseData", "Dữ liệu đã được tải: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
                Log.e("FirebaseError", "Lỗi khi tải dữ liệu từ Firebase: " + databaseError.getMessage());
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Bạn cần cấp quyền vị trí để sử dụng ứng dụng")
                        .setMessage("Chúng tôi cần vị trí hiện tại của bạn để chạy ứng dụng")
                        .setPositiveButton("Ok", (dialog, which) -> {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    LOCATIONS);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            finishAffinity();
                        })
                        .show();
            } else {
                // Yêu cầu quyền mà không cần giải thích
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATIONS);
            }
        } else {
            // Quyền đã được cấp, thực hiện các tác vụ liên quan đến vị trí
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
        checkLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.Nav_view);
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.none); // Reset trạng thái checked
        }
        registerReceiver(locationSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        // Kiểm tra quyền và cập nhật lại vị trí khi quay lại ứng dụng
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Sử dụng getCurrentLocation thay cho getLastLocation để lấy vị trí mới nhất
            fusedLocationProviderClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                            Log.d(TAG, "Location updated in onResume: "
                                    + location.getLatitude() + ", " + location.getLongitude());
                        } else {
                            Log.w(TAG, "No location available in onResume, requesting update");
                            requestLocationUpdate();
                        }
                    });
        } else {
            // Nếu chưa có quyền, yêu cầu quyền
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATIONS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(locationSwitchStateReceiver);
    }

    private void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.e("FirebaseError", "Lỗi khi cập nhật token: " + task.getException());
                }
            }
        });
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.google_map) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
        if (itemId == R.id.AI_BOT) {
            Intent intent = new Intent(this, ChatBotAIActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_myprofile) {
            startActivity(new Intent(MainActivity.this, profileActivity.class));
        } else if (itemId == R.id.changepass) {
            bottomNavigationView.setSelectedItemId(R.id.none);
            openFragment(new ChangePasswordFragment());
        } else if (itemId == R.id.report) {
            startActivity(new Intent(MainActivity.this, reportActivity.class));
        } else if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", "");
            editor.apply();
            startActivity(new Intent(MainActivity.this, loginActivity.class));
            finish();
        }
        // Remove the menu_toggle_location handling from here as it's now in setupNavigationSwitch
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();
    }
    private void openFragment(Fragment fragment) {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);
        if (currentFragment == null || !currentFragment.getClass().equals(fragment.getClass())) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content_frame, fragment);
            transaction.commit();
        }
    }

    private void checkUserStatus() {
        user = mAuth.getCurrentUser();
        if (user != null) {
            mUID = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
        } else {
            startActivity(new Intent(MainActivity.this, loginActivity.class));
            finish();
        }
    }
    // Khai báo BroadcastReceiver để lắng nghe thay đổi trạng thái vị trí
    private BroadcastReceiver locationSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    // Nếu bất kỳ dịch vụ định vị nào được bật
                    if (isGpsEnabled || isNetworkEnabled) {
                        // Gọi hàm cập nhật vị trí
                        getLastLocation();
                    }
                }
            }
        }
    };

}