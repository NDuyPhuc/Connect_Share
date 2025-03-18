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

import com.example.save_food.R;
import com.example.save_food.adapter.PersonalPostAdapter;
import com.example.save_food.models.ThongTin_UpLoadClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PostsFragment extends Fragment {

    private RecyclerView rvPosts;
    private TextView tvNoPosts;
    private ArrayList<ThongTin_UpLoadClass> postList;
    private PersonalPostAdapter adapter;
    private DatabaseReference postsRef;
    private String currentUserId;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        // Khởi tạo các view
        rvPosts = view.findViewById(R.id.rvPosts);
        tvNoPosts = view.findViewById(R.id.tvNoPosts);

        // Khởi tạo Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postsRef = FirebaseDatabase.getInstance().getReference("ThongTin_UpLoad").child(currentUserId);

        // Thiết lập RecyclerView
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

        // Tải danh sách bài đăng
        loadUserPosts();

        return view;
    }

    private void loadUserPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ThongTin_UpLoadClass post = ds.getValue(ThongTin_UpLoadClass.class);
                    if (post == null) continue;
                    post.setPostId(ds.getKey());
                    if (ds.hasChild("Ảnh")) {
                        List<DataSnapshot> imageSnapshots = new ArrayList<>();
                        for (DataSnapshot imgSnap : ds.child("Ảnh").getChildren()) {
                            imageSnapshots.add(imgSnap);
                        }
                        post.setImageSnapshots(imageSnapshots);
                    }
                    postList.add(post);
                }
                adapter.notifyDataSetChanged();
                Collections.sort(postList, (o1, o2) -> o2.getPostId().compareTo(o1.getPostId()));
                if (postList.isEmpty()) {
                    tvNoPosts.setVisibility(View.VISIBLE);
                    rvPosts.setVisibility(View.GONE);
                } else {
                    tvNoPosts.setVisibility(View.GONE);
                    rvPosts.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    private void showEditPostDialog(final ThongTin_UpLoadClass post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Post");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

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
        etExpiry.setHint("Detailed information");
        etExpiry.setText(post.getThongTinChiTiet());
        layout.addView(etExpiry);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newAddress = etAddress.getText().toString().trim();
            String newCategory = etCategory.getText().toString().trim();
            String newExpiry = etExpiry.getText().toString().trim();

            if (!TextUtils.isEmpty(newTitle) && !TextUtils.isEmpty(newAddress) &&
                    !TextUtils.isEmpty(newCategory) && !TextUtils.isEmpty(newExpiry)) {
                HashMap<String, Object> updateMap = new HashMap<>();
                updateMap.put("tenDonHang", newTitle);
                updateMap.put("diaChi", newAddress);
                updateMap.put("nganhHang", newCategory);
                updateMap.put("thongTinChiTiet", newExpiry);

                postsRef.child(post.getPostId()).updateChildren(updateMap)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
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

    private void deletePost(final ThongTin_UpLoadClass post) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    postsRef.child(post.getPostId()).removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
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