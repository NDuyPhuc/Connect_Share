package com.example.save_food.adapter;

import static java.lang.Long.parseLong;
import static java.lang.String.format;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
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
import com.example.save_food.activity_form_view;
import com.example.save_food.R;
import com.example.save_food.activity_form_view_bool;
import com.example.save_food.models.ModelChat;
import com.example.save_food.models.ThongTin_UpLoadClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import android.app.AlertDialog;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.Myholder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_TYPE_PRODUCT_LEFT = 2;
    private static final int MSG_TYPE_PRODUCT_RIGHT = 3;

    Context context;
    List<ModelChat> list;
    String imageurl;
    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<ModelChat> list, String imageurl) {
        this.context = context;
        this.list = list;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_PRODUCT_RIGHT) {
            // Layout cho tin nhắn sản phẩm bên phải (người gửi)
            view = LayoutInflater.from(context).inflate(R.layout.row_chat_product_right, parent, false);
        } else if (viewType == MSG_TYPE_PRODUCT_LEFT) {
            // Layout cho tin nhắn sản phẩm bên trái (người nhận)
            view = LayoutInflater.from(context).inflate(R.layout.row_chat_product, parent, false);
        } else if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
        } else { // MSG_TYPE_LEFT
            view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
        }
        return new Myholder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull Myholder holder, final int i) {
        ModelChat modelChat = list.get(i);
        String type = modelChat.getType();
        String timeStamp = modelChat.getTimestamp();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String timedate = DateFormat.format("hh:mm aa", calendar).toString();

        // Nếu view có TextView thời gian thì hiển thị
        if(holder.time != null) {
            holder.time.setText(timedate);
        }

        if (type.equals("product")) {
            try {
                JSONObject productObj = new JSONObject(modelChat.getMessage());
                String prodName = productObj.optString("productName");
                String prodInfo = productObj.optString("productInfo");
                String prodInfo_More = productObj.optString("productInfo_more");
                String prodImage = productObj.optString("productImage");
                if(holder.productName != null) {
                    holder.productName.setText(prodName);
                }
                if(holder.productInfo != null) {
                    holder.productInfo.setText(prodInfo);
                }
                if(holder.productInfo_More != null) {
                    holder.productInfo_More.setText(prodInfo_More);
                }
                if(holder.productImage != null) {
                    Glide.with(context).load(prodImage).into(holder.productImage);
                }
                if(holder.btnViewProduct != null) {
                    holder.btnViewProduct.setOnClickListener(v -> {
                        Intent intent = new Intent(context, activity_form_view.class);
                        // Nếu cần, truyền dữ liệu qua Intent extras
                        intent.putExtra("productName", prodName);
                        intent.putExtra("productInfo", prodInfo);
                        intent.putExtra("productInfo_more", prodInfo_More);
                        intent.putExtra("productImage", prodImage);
                        intent.putExtra("UID_sender", modelChat.getSender());

                        context.startActivity(intent);
                    });

                }
                if(holder.btnViewProduct_bool != null) {
                    holder.btnViewProduct_bool.setOnClickListener(v -> {
                        String postId = productObj.optString("postId");
                        Intent intent = new Intent(context, activity_form_view_bool.class);
                        intent.putExtra("postId", postId);
                        intent.putExtra("productName", productObj.optString("productName"));
                        intent.putExtra("productInfo", productObj.optString("productInfo"));
                        intent.putExtra("productInfo_more", productObj.optString("productInfo_more"));
                        intent.putExtra("productImage", productObj.optString("productImage"));
                        intent.putExtra("UID_sender", modelChat.getSender());
                        intent.putExtra("chatId", modelChat.getChatId());
                        context.startActivity(intent);
                    });
                }


            } catch (JSONException e) {
                e.printStackTrace();
                if(holder.productName != null) {
                    holder.productName.setText("N/A");
                }
                if(holder.productInfo != null) {
                    holder.productInfo.setText("N/A");
                }
                if(holder.productInfo_More != null) {
                    holder.productInfo_More.setText("N/A");
                }
            }
        } else {
            if (type.equals("text")) {
                if(holder.message != null) {
                    holder.message.setVisibility(View.VISIBLE);
                    holder.mimage.setVisibility(View.GONE);
                    holder.message.setText(modelChat.getMessage());
                }
            } else if (type.equals("images")) {
                if(holder.message != null) {
                    holder.message.setVisibility(View.GONE);
                }
                if(holder.mimage != null) {
                    holder.mimage.setVisibility(View.VISIBLE);
                    Glide.with(context).load(modelChat.getMessage()).into(holder.mimage);
                }
            }
        }

        if(i == list.size()-1 && holder.isSee != null){
            if(list.get(i).isDilihat()){
                holder.isSee.setText("đã xem");
            } else {
                holder.isSee.setText("đã gửi");
            }
        } else {
            if(holder.isSee != null){
                holder.isSee.setVisibility(View.GONE);
            }
        }

        if(holder.msglayput != null){
            holder.msglayput.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xóa tin nhắn");
                builder.setMessage("Bạn có chắc chắn để xóa tin nhắn này");
                builder.setPositiveButton("Delete", (dialog, which) -> deleteMsg(i));
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            });
        }

    }




    private void deleteMsg(int position) {
        final String myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String msgtimestmp = list.get(position).getTimestamp();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Chats");
        Query query = dbref.orderByChild("timestamp").equalTo(msgtimestmp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (dataSnapshot1.child("sender").getValue().equals(myuid)) {
                        dataSnapshot1.getRef().removeValue();
                    } else {
                        Toast.makeText(context, "Bạn chỉ có thể xóa tin nhắn của mình", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ModelChat chat = list.get(position);
        if(chat.getType().equals("product")){
            if(chat.getSender().equals(firebaseUser.getUid())){
                return MSG_TYPE_PRODUCT_RIGHT;
            } else {
                return MSG_TYPE_PRODUCT_LEFT;
            }
        } else if(chat.getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }


    class Myholder extends RecyclerView.ViewHolder {
        // Dành cho tin nhắn text và image
        TextView message, time, isSee;
        LinearLayout msglayput;
        ImageView mimage;
        CircleImageView image;
        // Dành cho tin nhắn sản phẩm
        TextView productName, productInfo, productInfo_More;
        ImageView productImage;
        Button btnViewProduct, btnViewProduct_bool;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            // Kiểm tra nếu itemView chứa các view của tin nhắn sản phẩm
            productName = itemView.findViewById(R.id.product_name);
            productInfo = itemView.findViewById(R.id.product_info);
            productInfo_More = itemView.findViewById(R.id.product_info_more);
            productImage = itemView.findViewById(R.id.product_image);
            btnViewProduct = itemView.findViewById(R.id.btn_view_product);
            btnViewProduct_bool = itemView.findViewById(R.id.btn_view_product_1);

            // Nếu layout tin nhắn text có, ánh xạ chúng
            message = itemView.findViewById(R.id.msgc);
            time = itemView.findViewById(R.id.timeTv);
            isSee = itemView.findViewById(R.id.isSeen);
            msglayput = itemView.findViewById(R.id.msglayout);
            mimage = itemView.findViewById(R.id.images);
            image = itemView.findViewById(R.id.profilec);
        }
    }
}






















