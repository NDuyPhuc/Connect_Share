package com.example.save_food;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.save_food.Fragment.homeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String storagepath = "Users_Profile_Cover_image/";
    String uid;
    CircleImageView set;
    TextView editname,user_email,user_phone;
    BeautifulProgressDialog pd;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    // Các biến lưu giá trị ban đầu (đã load từ Firebase)
    private String originalName = "", originalEmail = "", originalPhone = "", originalCity = "", originalDistrict = "", originalWard = "", originalStreet = "", originalNotes = "";
    private String originalAvatarURL = "";
    // Biến lưu URL avatar mới nếu người dùng cập nhật (nếu chưa cập nhật thì null)
    private String updatedAvatarURL = null;
    // Cờ đánh dấu có thay đổi chưa lưu (được set bằng TextWatcher)
    private boolean unsavedChanges = false;

    private  int storagePermisson = 1;
    String cameraPermission[];
    String storagePermission[];
    Uri imageuri;
    String profileOrCoverPhoto;
    Button updateProfile;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");


        editname = findViewById(R.id.name);
        set = findViewById(R.id.img_avatar);
        user_email = findViewById(R.id.user_email);
        user_phone = findViewById(R.id.user_phone);
        updateProfile = findViewById(R.id.updateButton);
        pd = new BeautifulProgressDialog(profileActivity.this, BeautifulProgressDialog.withGIF, "Please wait");
        Uri myUri = Uri.fromFile(new File("//android_asset/gif_food_and_smile.gif"));
        pd.setGifLocation(myUri);
        pd.setLayoutColor(getResources().getColor(R.color.BeautifulProgressDialogBg));
        pd.setMessageColor(getResources().getColor(R.color.white));
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        set = findViewById(R.id.img_avatar);
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = firebaseDatabase.getReference("Users");
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    String strname = "" + dataSnapshot1.child("name").getValue();
                    String image = "" + dataSnapshot1.child("image").getValue();
                    String email = "" + dataSnapshot1.child("email").getValue();
                    String phone = "" + dataSnapshot1.child("phone").getValue();
                    ((EditText) findViewById(R.id.name)).setText(strname);
                    ((EditText) findViewById(R.id.user_email)).setText(email);
                    ((EditText) findViewById(R.id.user_phone)).setText(phone);

                    // Lưu giá trị ban đầu
                    originalName = strname;
                    originalEmail = email;
                    originalPhone = phone;
                    originalCity = "" + dataSnapshot1.child("city").getValue();
                    originalDistrict = "" + dataSnapshot1.child("district").getValue();
                    originalWard = "" + dataSnapshot1.child("ward").getValue();
                    originalStreet = "" + dataSnapshot1.child("street").getValue();
                    originalNotes = "" + dataSnapshot1.child("notes").getValue();
                    ((EditText) findViewById(R.id.et_city)).setText(originalCity);
                    ((EditText) findViewById(R.id.et_district)).setText(originalDistrict);
                    ((EditText) findViewById(R.id.et_ward)).setText(originalWard);
                    ((EditText) findViewById(R.id.et_street)).setText(originalStreet);
                    ((EditText) findViewById(R.id.et_notes)).setText(originalNotes);

                    originalAvatarURL = image;

                    try {
                        if (!isDestroyed()) {
                            Glide.with(profileActivity.this).load(image).into(set);
                        }
                    } catch (Exception e) {
                        if (!isDestroyed()) {
                            Glide.with(profileActivity.this).load(R.drawable.person).into(set);
                        }
                    }
                }
                // Khi load dữ liệu thành công, chưa có thay đổi nào
                unsavedChanges = false;
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi: Chuyển người dùng sang homeFragment
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container_blank, new homeFragment()); // Thay thế fragment_container bằng ID của container trong layout của bạn
                transaction.addToBackStack(null); // Thêm vào back stack để người dùng có thể quay lại
                transaction.commit();
                // Hiển thị thông báo yêu cầu người dùng vào lại "Favorites"
                Toast.makeText(profileActivity.this, "Vui lòng truy cập lại để cập nhật vị trí!.", Toast.LENGTH_SHORT).show();
            }
        });


        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfileData();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
        // Khi ấn vào avatar, hiển thị dialog chọn nguồn ảnh
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đánh dấu cập nhật ảnh profile (ở đây key là "image")
                profileOrCoverPhoto = "image";
                showImagePicDialog();
            }
        });
        setupTextWatchers();

    }

    private void setupTextWatchers() {
        int[] editTextIds = { R.id.name, R.id.user_email, R.id.user_phone, R.id.et_city, R.id.et_district, R.id.et_ward, R.id.et_street, R.id.et_notes };
        for (int id : editTextIds) {
            ((EditText) findViewById(id)).addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    unsavedChanges = true;
                }
            });
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                    .setTitle("Bạn chưa lưu thông tin")
                    .setMessage("Bạn có muốn lưu thay đổi trước khi thoát?")
                    .setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateUserProfileDataAndFinish();
                        }
                    })
                    .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Nếu "Hủy" thì nếu avatar đã thay đổi, revert lại avatar gốc
                            revertAvatarIfChanged();
                            unsavedChanges = false;
                            finish();
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
        return super.onSupportNavigateUp();
    }

    private boolean hasUnsavedChanges() {
        EditText etName = findViewById(R.id.name);
        EditText etEmail = findViewById(R.id.user_email);
        EditText etPhone = findViewById(R.id.user_phone);
        EditText etCity = findViewById(R.id.et_city);
        EditText etDistrict = findViewById(R.id.et_district);
        EditText etWard = findViewById(R.id.et_ward);
        EditText etStreet = findViewById(R.id.et_street);
        EditText etNotes = findViewById(R.id.et_notes);

        boolean changed = false;
        if (!etName.getText().toString().trim().equals(originalName)) changed = true;
        if (!etEmail.getText().toString().trim().equals(originalEmail)) changed = true;
        if (!etPhone.getText().toString().trim().equals(originalPhone)) changed = true;
        if (!etCity.getText().toString().trim().equals(originalCity)) changed = true;
        if (!etDistrict.getText().toString().trim().equals(originalDistrict)) changed = true;
        if (!etWard.getText().toString().trim().equals(originalWard)) changed = true;
        if (!etStreet.getText().toString().trim().equals(originalStreet)) changed = true;
        if (!etNotes.getText().toString().trim().equals(originalNotes)) changed = true;
        // Kiểm tra avatar: nếu updatedAvatarURL đã được cập nhật và khác với originalAvatarURL
        if (updatedAvatarURL != null && !updatedAvatarURL.equals(originalAvatarURL)) changed = true;
        return changed;
    }


    public abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }

    private void showImagePicDialog() {
        String options[] = {"Chụp ảnh", "Ảnh từ thư viện"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn nguồn ảnh");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Lựa chọn "Chụp ảnh"
                    checkCameraPermission();
                } else if (which == 1) {
                    // Lựa chọn "Ảnh từ thư viện"
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    private void checkCameraPermission() {
        // Kiểm tra quyền CAMERA và WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa cấp quyền, yêu cầu cấp quyền
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CAMERA_REQUEST);
        } else {
            // Nếu quyền đã được cấp, gọi hàm pickFromCamera() để chụp ảnh
            pickFromCamera();
        }
    }


    private void updateUserProfileData() {
        // Lấy dữ liệu từ các EditText (lưu ý: trong layout activity_profile.xml bạn đã chuyển các TextView thành EditText)
        String strName = ((EditText) findViewById(R.id.name)).getText().toString().trim();
        String strEmail = ((EditText) findViewById(R.id.user_email)).getText().toString().trim();
        String strPhone = ((EditText) findViewById(R.id.user_phone)).getText().toString().trim();
        String strCity = ((EditText) findViewById(R.id.et_city)).getText().toString().trim();
        String strDistrict = ((EditText) findViewById(R.id.et_district)).getText().toString().trim();
        String strWard = ((EditText) findViewById(R.id.et_ward)).getText().toString().trim();
        String strStreet = ((EditText) findViewById(R.id.et_street)).getText().toString().trim();
        String strNotes = ((EditText) findViewById(R.id.et_notes)).getText().toString().trim();

        // Kiểm tra bắt buộc nếu cần (ví dụ: tên không được để trống)
        if (TextUtils.isEmpty(strName)) {
            Toast.makeText(profileActivity.this, "Hãy nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }
        // Nếu cần bạn có thể kiểm tra các trường khác...

        // Tạo HashMap chứa dữ liệu cần cập nhật
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", strName);
        hashMap.put("email", strEmail);
        hashMap.put("phone", strPhone);
        hashMap.put("city", strCity);
        hashMap.put("district", strDistrict);
        hashMap.put("ward", strWard);
        hashMap.put("street", strStreet);
        hashMap.put("notes", strNotes);

        // Hiển thị progress dialog
        pd.show();

        // Cập nhật dữ liệu vào node "Users" với key là uid của người dùng hiện tại
        databaseReference.child(firebaseUser.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(profileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(profileActivity.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //    private void showEditProfileDialog() {
//        String options[] = {"Chỉnh sửa ảnh", "Chỉnh sửa tên","Số điện thoại"};
//        AlertDialog.Builder b = new AlertDialog.Builder(this);
//        b.setTitle("Chọn sự thay đổi");
//        b.setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if (i == 0){
//                    pd.setMessage("Cập nhật ảnh đại diện");
//                    profileOrCoverPhoto = "image";
//                    showImagePicDialog();
//                } else if (i == 1){
//                    pd.setMessage("Cập nhật tên của bạn");
//                    showNamephoneupdate("name");
//                } else if (i == 2){
//                    pd.setMessage("Cập nhật số điện thoại của bạn");
//                    showNamephoneupdate("phone");}
//            }
//        });
//        b.create().show();
//    }
    private void checkPermissions(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    1052);

        }

    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                    .setTitle("Bạn chưa lưu thông tin")
                    .setMessage("Bạn có muốn lưu thay đổi trước khi thoát?")
                    .setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateUserProfileDataAndFinish();
                        }
                    })
                    .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Nếu "Hủy" thì nếu avatar đã thay đổi, revert lại avatar gốc
                            revertAvatarIfChanged();
                            unsavedChanges = false;
                            finish();
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void updateUserProfileDataAndFinish() {
        // Lấy dữ liệu từ các EditText
        String strName = ((EditText) findViewById(R.id.name)).getText().toString().trim();
        String strEmail = ((EditText) findViewById(R.id.user_email)).getText().toString().trim();
        String strPhone = ((EditText) findViewById(R.id.user_phone)).getText().toString().trim();
        String strCity = ((EditText) findViewById(R.id.et_city)).getText().toString().trim();
        String strDistrict = ((EditText) findViewById(R.id.et_district)).getText().toString().trim();
        String strWard = ((EditText) findViewById(R.id.et_ward)).getText().toString().trim();
        String strStreet = ((EditText) findViewById(R.id.et_street)).getText().toString().trim();
        String strNotes = ((EditText) findViewById(R.id.et_notes)).getText().toString().trim();

        if (TextUtils.isEmpty(strName)) {
            Toast.makeText(profileActivity.this, "Hãy nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", strName);
        hashMap.put("email", strEmail);
        hashMap.put("phone", strPhone);
        hashMap.put("city", strCity);
        hashMap.put("district", strDistrict);
        hashMap.put("ward", strWard);
        hashMap.put("street", strStreet);
        hashMap.put("notes", strNotes);

        pd.show();
        databaseReference.child(firebaseUser.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(profileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                        unsavedChanges = false;
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(profileActivity.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void revertAvatarIfChanged() {
        if (updatedAvatarURL != null && !updatedAvatarURL.equals(originalAvatarURL)) {
            HashMap<String, Object> revertMap = new HashMap<>();
            revertMap.put("image", originalAvatarURL);
            databaseReference.child(firebaseUser.getUid()).updateChildren(revertMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Cập nhật lại giao diện avatar về giá trị ban đầu
                            Glide.with(profileActivity.this)
                                    .load(originalAvatarURL)
                                    .placeholder(R.drawable.person)
                                    .into(set);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Nếu revert thất bại, có thể hiển thị Toast
                            Toast.makeText(profileActivity.this, "Lỗi hoàn nguyên ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String image = "" + dataSnapshot1.child("image").getValue();

                    if (!profileActivity.this.isFinishing() && !profileActivity.this.isDestroyed()) {
                        Glide.with(profileActivity.this).load(image).into(set);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseUser == null) {
                     Intent intent = new Intent(this, loginActivity.class);
                     startActivity(intent);
                     finish();
        } else {
            // Xử lý khi user đã đăng nhập
            String email = firebaseUser.getEmail();
            // Tiếp tục xử lý thông tin khác với FirebaseUser
        }
        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String image = "" + dataSnapshot1.child("image").getValue();

                    if (!profileActivity.this.isFinishing() && !profileActivity.this.isDestroyed()) {
                        Glide.with(profileActivity.this).load(image).into(set);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void showNamephoneupdate(String key) {
        pd.show();
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Cập nhật");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setPadding(10,10,10,10);

        EditText editText = new EditText(this);
        editText.setHint(key);
        linearLayout.addView(editText);

        b.setView(linearLayout);
        b.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = editText.getText().toString().trim();
                if  (!TextUtils.isEmpty(value)){
                    HashMap<String, Object> r = new HashMap<>();
                    r.put(key, value);

                    databaseReference.child(firebaseUser.getUid()).updateChildren(r).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), "Cập nhật....", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                } else {
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(), "hãy nhập "+key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        b.create().show();
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                if (data != null) {
                    imageuri = data.getData();
                    uploadProfileCoverPhoto(imageuri);
                }
            }
            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                uploadProfileCoverPhoto(imageuri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){

                    // permission was granted.
                    pickFromGallery();
                } else {


                    // Permission denied - Show a message to inform the user that this app only works
                    // with these permissions granted

                }
                }
            break;
            }

        }




    // Here we will click a photo and then go to startactivityforresult for updating data
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageuri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent camerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(camerIntent, IMAGE_PICKCAMERA_REQUEST);
    }

    // We will select an image from gallery
    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        pd.show();
        String filepathname = storagepath + profileOrCoverPhoto + "_" + firebaseUser.getUid();
        StorageReference storageReference1 = storageReference.child(filepathname);
        storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                final Uri downloadUri = uriTask.getResult();
                if (uriTask.isSuccessful()) {
                    // Cập nhật URL ảnh vào Firebase Database (key "image")
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(profileOrCoverPhoto, downloadUri.toString());
                    databaseReference.child(firebaseUser.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(profileActivity.this, "Cập nhật ảnh thành công", Toast.LENGTH_LONG).show();
                                    // Lưu URL mới vào biến updatedAvatarURL
                                    updatedAvatarURL = downloadUri.toString();
                                    // Load ảnh mới cập nhật lên avatar
                                    Glide.with(profileActivity.this)
                                            .load(downloadUri.toString())
                                            .placeholder(R.drawable.person)
                                            .into(set);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(profileActivity.this, "Lỗi cập nhật ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    pd.dismiss();
                    Toast.makeText(profileActivity.this, "Lỗi tải ảnh", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(profileActivity.this, "Lỗi upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}