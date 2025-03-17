// ProductAdapter.java
package com.example.save_food.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save_food.R;
import com.example.save_food.models.Product;
import com.example.save_food.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ReviewViewHolder> {
    private Context context;
    private List<Product> productList;
    private String currentUserId;
    private String profileUid; // UID of the profile from Reviews node

    // Updated constructor with profileUid
    public ProductAdapter(Context context, List<Product> productList, String profileUid) {
        this.context = context;
        this.productList = productList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.profileUid = profileUid;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reiview_profile_pesonal_post, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvPostTitleReview.setText(product.getTitle());
        if (product.getImageLink() != null && !product.getImageLink().isEmpty()) {
            Picasso.get().load(product.getImageLink()).into(holder.imgPostReview);
        } else {
            holder.imgPostReview.setImageResource(R.drawable.a);
        }
        holder.btnReview.setVisibility(View.GONE);
        holder.tvReviewText.setVisibility(View.GONE);
        // Hide individual rating text views if any
        holder.tvReviewHaiLong.setVisibility(View.GONE);
        holder.tvReviewBinhThuong.setVisibility(View.GONE);
        holder.tvReviewKhongHaiLong.setVisibility(View.GONE);

        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("Reviews");
        reviewRef.orderByChild("productId").equalTo(product.getProductId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean reviewFound = false;
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Review review = ds.getValue(Review.class);
                            if (review != null && review.getUserId().equals(product.getUserId())) {
                                reviewFound = true;
                                // Show the review text (e.g. "Hài lòng" or "Không hài lòng")
                                holder.btnReview.setVisibility(View.GONE);
                                holder.tvReviewText.setVisibility(View.VISIBLE);
                                holder.tvReviewText.setText(review.getReview());
                                break;
                            }
                        }
                        // If no review exists and the current user is the accepted user,
                        // show the review button for them to submit a review.
                        if (!reviewFound
                                && product.getRatingType() == 0
                                && "accepted".equals(product.getStatus())
                                && currentUserId.equals(product.getUserId())) {
                            holder.btnReview.setVisibility(View.VISIBLE);
                            holder.btnReview.setOnClickListener(v -> showReviewDialog(product));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void showReviewDialog(Product product) {
        String[] options = {"Hài lòng", "Bình thường", "Không hài lòng"};
        new AlertDialog.Builder(context)
                .setTitle("Đánh giá sản phẩm: " + product.getTitle())
                .setItems(options, (dialog, which) -> {
                    int ratingType = which + 1;
                    String reviewText = options[which];
                    saveReviewToFirebase(product.getProductId(), ratingType, reviewText);
                    Toast.makeText(context, "Cảm ơn đánh giá của bạn!", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void saveReviewToFirebase(String productId, int ratingType, String reviewText) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("Reviews");
        String reviewId = reviewRef.push().getKey();
        Review review = new Review(productId, currentUserId, (long) ratingType, System.currentTimeMillis(), reviewText);
        reviewRef.child(reviewId).setValue(review)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Cảm ơn đánh giá của bạn!", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi lưu đánh giá!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPostReview;
        TextView tvPostTitleReview, tvReviewHaiLong, tvReviewBinhThuong, tvReviewKhongHaiLong, tvReviewText;
        Button btnReview;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPostReview = itemView.findViewById(R.id.img_post_review);
            tvPostTitleReview = itemView.findViewById(R.id.tv_post_title_review);
            btnReview = itemView.findViewById(R.id.btnReview);
            tvReviewHaiLong = itemView.findViewById(R.id.tv_review_hai_long);
            tvReviewBinhThuong = itemView.findViewById(R.id.tv_review_binh_thuong);
            tvReviewKhongHaiLong = itemView.findViewById(R.id.tv_review_khong_hai_long);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
        }
    }
}
