// Profile_Personal_Activity.java
package com.example.save_food;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.save_food.adapter.ProductAdapter;
import com.example.save_food.models.HinhAnh_Upload;
import com.example.save_food.models.Product;
import com.example.save_food.models.ThongTin_UpLoadClass;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Profile_Personal_Activity extends AppCompatActivity {
    public static ArrayList<PostsFragment.Post> sharedPosts;
    private ImageButton btnBack;
    private Button btnContact;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfilePagerAdapter pagerAdapter;
    private String profileUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_personal_activity);
        profileUid = getIntent().getStringExtra("USER_ID");
        if (profileUid == null) {
            profileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(profileUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                String userAvatar = snapshot.child("image").getValue(String.class);
                TextView tvUsername = findViewById(R.id.tv_username);
                ImageView imgAvatar = findViewById(R.id.img_avatar);
                tvUsername.setText(userName != null ? userName : "Không có tên");
                if (userAvatar != null && !userAvatar.isEmpty()) {
                    Picasso.get().load(userAvatar).placeholder(R.drawable.person).into(imgAvatar);
                } else {
                    imgAvatar.setImageResource(R.drawable.person);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        btnContact = findViewById(R.id.btn_contact);
        btnContact.setOnClickListener(v -> {
            Intent intent = new Intent(Profile_Personal_Activity.this, chat.class);
            intent.putExtra("hisUid", profileUid);
            startActivity(intent);
        });
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Bài đăng" : "Đánh giá từ khách hàng");
        }).attach();
    }

    private class ProfilePagerAdapter extends FragmentStateAdapter {
        public ProfilePagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? PostsFragment.newInstance(profileUid) : ReviewsFragment.newInstance(profileUid);
        }
        @Override
        public int getItemCount() {
            return 2;
        }
    }

    public static class PostsFragment extends Fragment {
        private RecyclerView recyclerView;
        private PostsAdapter postsAdapter;
        private List<Post> postList;
        private String profileUid;

        public static PostsFragment newInstance(String uid) {
            PostsFragment fragment = new PostsFragment();
            Bundle args = new Bundle();
            args.putString("USER_ID", uid);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_profile_personal_posts, container, false);
            recyclerView = view.findViewById(R.id.recycler_view_profile_posts);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            postList = new ArrayList<>();
            if (getArguments() != null) {
                profileUid = getArguments().getString("USER_ID");
            }
            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("ThongTin_UpLoad").child(profileUid);
            postsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postList.clear();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        ThongTin_UpLoadClass upload = postSnapshot.getValue(ThongTin_UpLoadClass.class);
                        if (upload != null) {
                            String imageLink = "";
                            DataSnapshot imageNode = postSnapshot.child("Ảnh");
                            if (imageNode.exists()) {
                                for (DataSnapshot child : imageNode.getChildren()) {
                                    HinhAnh_Upload hinhAnh = child.getValue(HinhAnh_Upload.class);
                                    if (hinhAnh != null && hinhAnh.getLinkHinh() != null && !hinhAnh.getLinkHinh().isEmpty()) {
                                        imageLink = hinhAnh.getLinkHinh();
                                        break;
                                    }
                                }
                            }
                            Post post = new Post(upload.getTenDonHang(), upload.getDiaChi(), upload.getThongTinChiTiet(), imageLink);
                            postList.add(post);
                        }
                    }
                    Profile_Personal_Activity.sharedPosts = new ArrayList<>(postList);
                    if (getActivity() != null) {
                        TabLayout tabLayout = getActivity().findViewById(R.id.tab_layout);
                        if (tabLayout != null && tabLayout.getTabAt(0) != null) {
                            tabLayout.getTabAt(0).setText("Bài đăng (" + postList.size() + ")");
                        }
                    }
                    if (postsAdapter != null)
                        postsAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
            postsAdapter = new PostsAdapter(postList);
            recyclerView.setAdapter(postsAdapter);
            return view;
        }

        public static class Post {
            String title, location, thongTinChiTiet, imageLink;
            public Post(String title, String location, String thongTinChiTiet, String imageLink) {
                this.title = title;
                this.location = location;
                this.thongTinChiTiet = thongTinChiTiet;
                this.imageLink = imageLink;
            }
        }

        public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {
            private List<Post> posts;
            public PostsAdapter(List<Post> posts) {
                this.posts = posts;
            }
            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_pesonal_post, parent, false);
                return new PostViewHolder(view);
            }
            @Override
            public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
                Post post = posts.get(position);
                holder.bind(post);
            }
            @Override
            public int getItemCount() {
                return posts.size();
            }
            class PostViewHolder extends RecyclerView.ViewHolder {
                TextView tvPostTitle, tvPostLocation, tvPostThongTin;
                ImageView imgPost;
                public PostViewHolder(@NonNull View itemView) {
                    super(itemView);
                    tvPostTitle = itemView.findViewById(R.id.tv_post_title);
                    tvPostLocation = itemView.findViewById(R.id.tv_post_location);
                    tvPostThongTin = itemView.findViewById(R.id.tv_post_thongtin);
                    imgPost = itemView.findViewById(R.id.img_post);
                }
                public void bind(Post post) {
                    tvPostTitle.setText(post.title);
                    tvPostLocation.setText(post.location);
                    tvPostThongTin.setText(post.thongTinChiTiet);
                    if (post.imageLink != null && !post.imageLink.isEmpty())
                        Picasso.get().load(post.imageLink).placeholder(R.drawable.a).into(imgPost);
                    else
                        imgPost.setImageResource(R.drawable.a);
                }
            }
        }
    }

    // ReviewsFragment inside Profile_Personal_Activity.java
    public static class ReviewsFragment extends Fragment {
        private RecyclerView recyclerView;
        private ProductAdapter adapter;
        private List<Product> productList;
        private String profileUid;

        public static ReviewsFragment newInstance(String uid) {
            ReviewsFragment fragment = new ReviewsFragment();
            Bundle args = new Bundle();
            args.putString("USER_ID", uid);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_profile_personal_reviews, container, false);
            recyclerView = view.findViewById(R.id.recycler_view_profile_reviews);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            productList = new ArrayList<>();
            if (getArguments() != null) {
                profileUid = getArguments().getString("USER_ID");
            }
            adapter = new ProductAdapter(getContext(), productList, profileUid);
            recyclerView.setAdapter(adapter);
            loadReviews();
            return view;
        }

        private void loadReviews() {
            DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews").child(profileUid);
            reviewsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String, Product> reviewMap = new HashMap<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        String title = ds.child("tenDonHang").getValue(String.class);
                        if (title == null || title.isEmpty()) continue;
                        String postId = ds.child("postId").getValue(String.class);
                        if (postId == null || postId.isEmpty()) postId = key;
                        String imageLink = null;
                        DataSnapshot imagesNode = ds.child("Ảnh");
                        if (imagesNode.exists()) {
                            for (DataSnapshot imageSnapshot : imagesNode.getChildren()) {
                                imageLink = imageSnapshot.child("linkHinh").getValue(String.class);
                                if (imageLink != null && !imageLink.isEmpty()) break;
                            }
                        }
                        String status = ds.child("status").getValue(String.class);
                        String acceptedUserId = ds.child("uid").getValue(String.class);
                        if (status == null || !status.equals("accepted")) continue;
                        Product product = new Product(postId, title, imageLink, status, acceptedUserId);
                        String ratingStr = ds.child("ratingType").getValue(String.class);
                        int ratingType = 0;
                        try {
                            ratingType = ratingStr != null ? Integer.parseInt(ratingStr) : 0;
                        } catch (NumberFormatException e) { }
                        product.setRatingType(ratingType);
                        reviewMap.put(title, product);
                        Log.d("ReviewsFragment", "Review node key: " + key + ", Title: " + title +
                                ", postId: " + postId + ", ImageLink: " + imageLink +
                                ", Status: " + status + ", acceptedUserId: " + acceptedUserId +
                                ", ratingType: " + ratingType);
                    }
                    productList.clear();
                    productList.addAll(reviewMap.values());
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ReviewsFragment", "Error loading reviews: " + error.getMessage());
                }
            });
        }
    }


}
