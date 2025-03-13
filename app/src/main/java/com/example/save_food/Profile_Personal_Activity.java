package com.example.save_food;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.save_food.models.HinhAnh_Upload;
import com.example.save_food.models.ThongTin_UpLoadClass;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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

        // Lấy UID của người dùng từ intent (nếu có) hoặc dùng UID hiện tại
        profileUid = getIntent().getStringExtra("USER_ID");
        if (profileUid == null) {
            profileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Lấy thông tin người dùng (avatar, tên) từ Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(profileUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String userAvatar = snapshot.child("image").getValue(String.class);
                    TextView tvUsername = findViewById(R.id.tv_username);
                    ImageView imgAvatar = findViewById(R.id.img_avatar);
                    if (userName != null) {
                        tvUsername.setText(userName);
                    } else {
                        tvUsername.setText("Không có tên");
                    }
                    if (userAvatar != null && !userAvatar.isEmpty()) {
                        Picasso.get().load(userAvatar)
                                .placeholder(R.drawable.person) // ảnh mặc định nếu load thất bại
                                .into(imgAvatar);
                    } else {
                        imgAvatar.setImageResource(R.drawable.person);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Xử lý nút "Liên hệ"
        btnContact = findViewById(R.id.btn_contact);
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        Intent intent = new Intent(Profile_Personal_Activity.this, chat.class);
                        intent.putExtra("hisUid", profileUid);
                        Profile_Personal_Activity.this.startActivity(intent);
            }
        });

        // Thiết lập ViewPager2 và TabLayout để chuyển giữa Fragment
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Bài đăng");
            } else {
                tab.setText("Đánh giá từ khách hàng");
            }
        }).attach();
    }

    // Adapter cho ViewPager2
    private class ProfilePagerAdapter extends FragmentStateAdapter {
        public ProfilePagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return PostsFragment.newInstance(profileUid);
            } else {
                return ReviewsFragment.newInstance(profileUid);
            }
        }
        @Override
        public int getItemCount() {
            return 2;
        }
    }

    // Fragment hiển thị danh sách bài đăng
    public static class PostsFragment extends Fragment {

        private RecyclerView recyclerView;
        private PostsAdapter postsAdapter;
        private List<Post> postList;
        private String profileUid;

        // Factory method để truyền UID vào Fragment
        public static PostsFragment newInstance(String uid) {
            PostsFragment fragment = new PostsFragment();
            Bundle args = new Bundle();
            args.putString("USER_ID", uid);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_profile_personal_posts, container, false);
            recyclerView = view.findViewById(R.id.recycler_view_profile_posts);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            postList = new ArrayList<>();

            if (getArguments() != null) {
                profileUid = getArguments().getString("USER_ID");
                Log.d("PostsFragment", "Profile UID: " + profileUid);
            }

            // Sửa tên node: sử dụng "ThongTin_UpLoad" (chữ L viết hoa) để khớp với dữ liệu đã upload
            DatabaseReference postsRef = FirebaseDatabase.getInstance()
                    .getReference("ThongTin_UpLoad").child(profileUid);
            postsRef.addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("PostsFragment", "onDataChange: children count = " + snapshot.getChildrenCount());
                    postList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()){
                            ThongTin_UpLoadClass upload = postSnapshot.getValue(ThongTin_UpLoadClass.class);
                            if (upload != null) {
                                // Lấy link ảnh từ node "Ảnh"
                                String imageLink = "";
                                DataSnapshot imageNode = postSnapshot.child("Ảnh");
                                if (imageNode.exists()){
                                    for (DataSnapshot child : imageNode.getChildren()){
                                        // Sử dụng model HinhAnh_Upload để lấy link ảnh
                                        HinhAnh_Upload hinhAnh = child.getValue(HinhAnh_Upload.class);
                                        if (hinhAnh != null && hinhAnh.getLinkHinh() != null && !hinhAnh.getLinkHinh().isEmpty()){
                                            imageLink = hinhAnh.getLinkHinh();
                                            break;
                                        }
                                    }
                                }
                                Log.d("PostsFragment", "Post: " + upload.toStringg() + ", imageLink: " + imageLink);
                                Post post = new Post(upload.getTenDonHang(), upload.getDiaChi(), upload.getThongTinChiTiet(), imageLink);
                                postList.add(post);
                            } else {
                                Log.d("PostsFragment", "upload is null for key: " + postSnapshot.getKey());
                            }
                        }
                    } else {
                        Log.d("PostsFragment", "No posts found for UID: " + profileUid);
                    }
                    // Cập nhật biến sharedPosts để ReviewsFragment có thể sử dụng
                    Profile_Personal_Activity.sharedPosts = new ArrayList<>(postList);
                    // Cập nhật tiêu đề Tab với số lượng bài đăng
                    if(getActivity() != null) {
                        TabLayout tabLayout = getActivity().findViewById(R.id.tab_layout);
                        if (tabLayout != null && tabLayout.getTabAt(0) != null) {
                            tabLayout.getTabAt(0).setText("Bài đăng (" + postList.size() + ")");
                        }
                    }
                    if (postsAdapter != null) {
                        postsAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("PostsFragment", "onCancelled: " + error.getMessage());
                }
            });


            postsAdapter = new PostsAdapter(postList);
            recyclerView.setAdapter(postsAdapter);
            return view;
        }

        // Model cho bài đăng, bao gồm trường imageLink để hiển thị ảnh
        public static class Post {
            String title;
            String location;
            String thongTinChiTiet;
            String imageLink;
            public Post(String title, String location, String thongTinChiTiet, String imageLink) {
                this.title = title;
                this.location = location;
                this.thongTinChiTiet = thongTinChiTiet;
                this.imageLink = imageLink;
            }
        }

        // Adapter cho RecyclerView bài đăng
        public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {
            private List<Post> posts;
            public PostsAdapter(List<Post> posts) {
                this.posts = posts;
            }
            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_profile_pesonal_post, parent, false);
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
                    if (post.imageLink != null && !post.imageLink.isEmpty()) {
                        Picasso.get().load(post.imageLink)
                                .placeholder(R.drawable.a)
                                .into(imgPost);
                    } else {
                        imgPost.setImageResource(R.drawable.a);
                    }
                }
            }
        }
    }

    // Fragment hiển thị danh sách đánh giá từ khách hàng (dữ liệu mẫu)
    public static class ReviewsFragment extends Fragment {

        private RecyclerView recyclerView;
        private ReviewsAdapter reviewsAdapter;
        private List<Review> reviewList;
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
            reviewList = new ArrayList<>();
            if(getArguments() != null) {
                profileUid = getArguments().getString("USER_ID");
            }
            // Nếu có dữ liệu bài đăng đã được chia sẻ từ PostsFragment, chuyển đổi chúng thành review
            if(Profile_Personal_Activity.sharedPosts != null && !Profile_Personal_Activity.sharedPosts.isEmpty()){
                for(PostsFragment.Post post : Profile_Personal_Activity.sharedPosts){
                    // Giả sử reviewType mặc định là 1, và dùng ảnh của bài đăng
                    reviewList.add(new Review(post.title, 1, post.imageLink));
                }
            } else {
                // Nếu chưa có dữ liệu, bạn có thể thêm dữ liệu mẫu
                reviewList.add(new Review("Bài đăng 1", 1, ""));
                reviewList.add(new Review("Bài đăng 2", 3, ""));
                reviewList.add(new Review("Bài đăng 3", 2, ""));
            }
            reviewsAdapter = new ReviewsAdapter(reviewList);
            recyclerView.setAdapter(reviewsAdapter);
            return view;
        }

        // Mở rộng lớp Review để có thêm trường imageLink
        public static class Review {
            String postTitle;
            int reviewType; // 1 = Hài lòng, 2 = Bình thường, 3 = Không hài lòng
            String imageLink;
            public Review(String postTitle, int reviewType, String imageLink) {
                this.postTitle = postTitle;
                this.reviewType = reviewType;
                this.imageLink = imageLink;
            }
        }

        public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
            private List<Review> reviews;
            public ReviewsAdapter(List<Review> reviews) {
                this.reviews = reviews;
            }
            @NonNull
            @Override
            public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_reiview_profile_pesonal_post, parent, false);
                return new ReviewViewHolder(view);
            }
            @Override
            public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
                Review review = reviews.get(position);
                holder.bind(review);
            }
            @Override
            public int getItemCount() {
                return reviews.size();
            }
            class ReviewViewHolder extends RecyclerView.ViewHolder {
                TextView tvPostTitleReview;
                ImageView imgPostReview;
                TextView tvReviewHaiLong, tvReviewBinhThuong, tvReviewKhongHaiLong;
                public ReviewViewHolder(@NonNull View itemView) {
                    super(itemView);
                    tvPostTitleReview = itemView.findViewById(R.id.tv_post_title_review);
                    imgPostReview = itemView.findViewById(R.id.img_post_review);
                    tvReviewHaiLong = itemView.findViewById(R.id.tv_review_hai_long);
                    tvReviewBinhThuong = itemView.findViewById(R.id.tv_review_binh_thuong);
                    tvReviewKhongHaiLong = itemView.findViewById(R.id.tv_review_khong_hai_long);
                }
                public void bind(Review review) {
                    tvPostTitleReview.setText(review.postTitle);
                    tvReviewHaiLong.setVisibility(View.GONE);
                    tvReviewBinhThuong.setVisibility(View.GONE);
                    tvReviewKhongHaiLong.setVisibility(View.GONE);
                    if (review.reviewType == 1) {
                        tvReviewHaiLong.setVisibility(View.VISIBLE);
                    } else if (review.reviewType == 2) {
                        tvReviewBinhThuong.setVisibility(View.VISIBLE);
                    } else if (review.reviewType == 3) {
                        tvReviewKhongHaiLong.setVisibility(View.VISIBLE);
                    }
                    // Hiển thị ảnh nếu imageLink hợp lệ
                    if (review.imageLink != null && !review.imageLink.isEmpty()) {
                        Picasso.get().load(review.imageLink)
                                .placeholder(R.drawable.a) // ảnh mặc định nếu load thất bại
                                .into(imgPostReview);
                    } else {
                        imgPostReview.setImageResource(R.drawable.a);
                    }
                }
            }
        }
    }

}
