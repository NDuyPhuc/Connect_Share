package com.example.save_food.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.save_food.R;
import com.example.save_food.adapter.PersonalPostAdapter;
import com.example.save_food.models.ThongTin_UpLoadClass;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalPost extends Fragment {

    private CircleImageView imgAvatar;
    private TextView tvUserName;
    private RecyclerView rvPosts;
    private ArrayList<ThongTin_UpLoadClass> postList;
    private PersonalPostAdapter adapter;
    private DatabaseReference postsRef, usersRef;
    private FirebaseAuth auth;
    private String currentUserId;

    public PersonalPost() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_personal_post, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        rvPosts = view.findViewById(R.id.rvPosts);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        postsRef = FirebaseDatabase.getInstance().getReference("ThongTin_UpLoad").child(currentUserId);

        // Setup RecyclerView
        rvPosts.setLayoutManager(new LinearLayoutManager(getActivity()));
        postList = new ArrayList<>();
        adapter = new PersonalPostAdapter(getActivity(), postList, new PersonalPostAdapter.OnPostActionListener() {
            @Override
            public void onEdit(ThongTin_UpLoadClass post) {
                showEditPostDialog(post);
            }

            @Override
            public void onDelete(ThongTin_UpLoadClass post) {
                deletePost(post);
            }
        });
        rvPosts.setAdapter(adapter);

        loadUserProfile();
        loadUserPosts();

        return view;
    }

    private void loadUserProfile(){
        usersRef.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name = snapshot.child("name").getValue(String.class);
                    String image = snapshot.child("image").getValue(String.class);
                    tvUserName.setText(name);
                    Glide.with(getActivity()).load(image).placeholder(R.drawable.person).into(imgAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadUserPosts(){
        // Lắng nghe thay đổi tại nút ThongTin_UpLoad/{uid}
        postsRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ThongTin_UpLoadClass post = ds.getValue(ThongTin_UpLoadClass.class);
                    if (post == null) continue;
                    // Lưu lại key của bài đăng
                    post.setPostId(ds.getKey());

                    // Nếu có child "Ảnh", lấy danh sách ảnh
                    if(ds.hasChild("Ảnh")){
                        List<DataSnapshot> imageSnapshots = new ArrayList<>();
                        for(DataSnapshot imgSnap : ds.child("Ảnh").getChildren()){
                            imageSnapshots.add(imgSnap);
                        }
                        post.setImageSnapshots(imageSnapshots);
                    }
                    postList.add(post);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    private void showEditPostDialog(final ThongTin_UpLoadClass post){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Post");

        // Sử dụng LinearLayout để chứa nhiều EditText
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16,16,16,16);

        final EditText etTitle = new EditText(getActivity());
        etTitle.setHint("Post Title");
        etTitle.setText(post.getTenDonHang());
        layout.addView(etTitle);

        final EditText etAddress = new EditText(getActivity());
        etAddress.setHint("Address");
        etAddress.setText(post.getDiaChi());
        layout.addView(etAddress);

        final EditText etCategory = new EditText(getActivity());
        etCategory.setHint("Category");
        etCategory.setText(post.getNganhHang());
        layout.addView(etCategory);

        final EditText etExpiry = new EditText(getActivity());
        etExpiry.setHint("Expiry Time");
        etExpiry.setText(post.getThoiGianHetHan());
        layout.addView(etExpiry);

        final EditText etUnit = new EditText(getActivity());
        etUnit.setHint("Unit");
        etUnit.setText(post.getDonViHetHan());
        layout.addView(etUnit);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newAddress = etAddress.getText().toString().trim();
            String newCategory = etCategory.getText().toString().trim();
            String newExpiry = etExpiry.getText().toString().trim();
            String newUnit = etUnit.getText().toString().trim();
            if(!TextUtils.isEmpty(newTitle) && !TextUtils.isEmpty(newAddress) &&
                    !TextUtils.isEmpty(newCategory) && !TextUtils.isEmpty(newExpiry) && !TextUtils.isEmpty(newUnit)){
                HashMap<String, Object> updateMap = new HashMap<>();
                updateMap.put("tenDonHang", newTitle);
                updateMap.put("diaChi", newAddress);
                updateMap.put("nganhHang", newCategory);
                updateMap.put("thoiGianHetHan", newExpiry);
                updateMap.put("donViHetHan", newUnit);
                postsRef.child(post.getPostId()).updateChildren(updateMap)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "Post updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deletePost(final ThongTin_UpLoadClass post){
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    postsRef.child(post.getPostId()).removeValue()
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    Toast.makeText(getActivity(), "Post deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Deletion failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }
}
