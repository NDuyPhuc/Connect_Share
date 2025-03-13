package com.example.save_food.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.save_food.Activity_Form;
import com.example.save_food.Profile_Personal_Activity;
import com.example.save_food.R;
import com.example.save_food.chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VPAdapter extends RecyclerView.Adapter<VPAdapter.ViewHolder> {
    Context context;
    ArrayList<ViewPagerItem> viewPagerItems;
    private DatabaseReference likesRef;
    private DatabaseReference postsRef;
    boolean mProcesslike = false;
    String myUid;

    public VPAdapter(ArrayList<ViewPagerItem> viewPagerItems, Context context) {
        this.viewPagerItems = viewPagerItems;
        this.context = context;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postsRef = FirebaseDatabase.getInstance().getReference().child("ThongTin_UpLoad");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ViewPagerItem viewPagerItem = viewPagerItems.get(position);

        // Lấy thông tin người dùng từ Firebase dựa trên UID của người đăng
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(viewPagerItem.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userName = snapshot.child("name").getValue(String.class);
                    String userAvatar = snapshot.child("image").getValue(String.class);
                    // Cập nhật tên người dùng
                    if(userName != null) {
                        holder.username_view.setText(userName);
                    } else {
                        holder.username_view.setText("Không có tên");
                    }
                    // Cập nhật avatar người dùng (sử dụng Picasso hoặc Glide)
                    if(userAvatar != null && !userAvatar.isEmpty()){
                        Picasso.get().load(userAvatar)
                                .placeholder(R.drawable.person2) // Hình mặc định nếu chưa load được
                                .into(holder.avatar_view);
                    } else {
                        holder.avatar_view.setImageResource(R.drawable.person2);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
                Log.e("VPAdapter", "Lỗi khi lấy thông tin người dùng: " + error.getMessage());
            }
        });

        Picasso.get().load(viewPagerItem.getImgaeId()).into(holder.imageView);

        // Xử lý hiển thị text chỉ 2 từ cho tvHeading
        String originalText = viewPagerItem.Heding;
        String[] words = originalText.split("\\s+");
        if (words.length > 2) {
            // Nếu nhiều hơn 2 từ, chỉ hiển thị 2 từ đầu tiên và thêm dấu "..."
            String truncatedText = words[0] + " " + words[1] + "...";
            holder.tvHeading.setText(truncatedText);
        } else {
            holder.tvHeading.setText(originalText);
        }

        holder.tvHeading2.setText(viewPagerItem.Heding2);

        // Xử lý sự kiện cho tvNhan
        holder.tvNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Chọn phương thức nhập thông tin");

                String[] options = {"Tự nhập thông tin", "Nhập thông tin có sẵn"};

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, Activity_Form.class);
                        // Truyền extra để chỉ định chế độ hiển thị thông tin (tự nhập hay có sẵn)
                        if (which == 0) {
                            intent.putExtra("info_option", "empty");
                        } else {
                            intent.putExtra("info_option", "preset");
                        }
                        // Truyền thông tin sản phẩm từ bài đăng (thông tin này lấy từ đối tượng viewPagerItem)
                        intent.putExtra("product_name", viewPagerItem.getHeding());        // Tên sản phẩm
                        intent.putExtra("product_info", viewPagerItem.getHeding2());      // Thông tin sản phẩm (ví dụ địa chỉ)
                        intent.putExtra("product_info_more", viewPagerItem.getHeding3());
                        intent.putExtra("product_image", viewPagerItem.getImgaeId());    // URL hình ảnh sản phẩm
                        intent.putExtra("UID_personal", viewPagerItem.getUid());    // UID người đăng bài
                        context.startActivity(intent);
                    }
                });
                builder.show();
            }
        });


        holder.user_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Profile_Personal_Activity.class);
                 intent.putExtra("USER_ID", viewPagerItem.getUid());
                context.startActivity(intent);
            }
        });


        // Xử lý sự kiện cho button "Xem thêm"
        holder.xemthemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TenDonhang", viewPagerItem.Heding);
                Log.d("Diachi", viewPagerItem.Heding2);
                String thongTinChiTiet = (viewPagerItem.Heding3 != null && !viewPagerItem.Heding3.isEmpty())
                        ? viewPagerItem.Heding3
                        : "Không có thông tin chi tiết";


                String tendonhang = viewPagerItem.Heding;
                String Diachi = viewPagerItem.Heding2;
                String hisUid = viewPagerItems.get(position).getUid();
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                View dialogView = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.custom_dialog_post, null);
                ImageView imageViewDialog = dialogView.findViewById(R.id.dialog_post_img);
                TextView TendonHangDialog = dialogView.findViewById(R.id.NamDonHangPostDialog);
                TextView DiachiDialog = dialogView.findViewById(R.id.DiaChiPostDiaLog);
                TextView ThongTinChiTiet = dialogView.findViewById(R.id.ThongTinChiTietDiaLog);
                Glide.with(context).load(viewPagerItem.getImgaeId()).into(imageViewDialog);
                TendonHangDialog.setText(tendonhang);
                DiachiDialog.setText(Diachi);
                ThongTinChiTiet.setText(thongTinChiTiet);
                Log.d("TextViewValue", TendonHangDialog.getText().toString());
                AlertDialog dialog = builder.create();
                dialog.setView(dialogView);
                dialog.setCancelable(true);
                dialog.show();

                Button closeButton = dialogView.findViewById(R.id.closeButton);
                Button lienheBtn = dialogView.findViewById(R.id.lienheBtn);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                lienheBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, chat.class);
                        intent.putExtra("hisUid", hisUid);
                        context.startActivity(intent);
                    }
                });
            }
        });
    }
    @Override
    public int getItemCount() {
        return viewPagerItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView, avatar_view;
        TextView tvHeading, tvHeading2, plikeTv, tvNhan, username_view;
        Button xemthemBtn;
        LinearLayout user_info;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNhan  = itemView.findViewById(R.id.tv_nhan);
            imageView = itemView.findViewById(R.id.img_viewpager);
            tvHeading = itemView.findViewById(R.id.tvHeading);
            tvHeading2= itemView.findViewById(R.id.tv_Heading2);
            xemthemBtn= itemView.findViewById(R.id.btn_xemthem);
            avatar_view=itemView.findViewById(R.id.img_avatar_post);
            username_view=itemView.findViewById(R.id.tv_username_post);
            user_info = itemView.findViewById(R.id.user_info);
        }
    }
}
