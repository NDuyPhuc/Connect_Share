package com.example.save_food.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.save_food.R;
import com.example.save_food.models.ThongTin_UpLoadClass;

import java.util.List;

public class PersonalPostAdapter extends RecyclerView.Adapter<PersonalPostAdapter.PostViewHolder> {

    public interface OnPostActionListener {
        void onEdit(ThongTin_UpLoadClass post);
        void onDelete(ThongTin_UpLoadClass post);
    }

    private Context context;
    private List<ThongTin_UpLoadClass> postList;
    private OnPostActionListener listener;

    public PersonalPostAdapter(Context context, List<ThongTin_UpLoadClass> postList, OnPostActionListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_personal_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        ThongTin_UpLoadClass post = postList.get(position);
        holder.tvPostTitle.setText(post.getTenDonHang());
        holder.tvPostContent.setText(post.getDiaChi());

        // Load ảnh bài đăng nếu có (lấy ảnh đầu tiên trong imageSnapshots)
        if (post.getImageSnapshots() != null && !post.getImageSnapshots().isEmpty()) {
            String imageUrl = post.getImageSnapshots().get(0).child("linkHinh").getValue(String.class);
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.ivPostImage);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(post));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(post));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvPostTitle, tvPostContent;
        ImageView ivPostImage;
        Button btnEdit, btnDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            btnEdit = itemView.findViewById(R.id.btnEditPost);
            btnDelete = itemView.findViewById(R.id.btnDeletePost);
        }
    }
}
