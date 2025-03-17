package com.example.save_food.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save_food.Profile_Personal_Activity;
import com.example.save_food.R;
import com.example.save_food.activity_form_view;
import com.example.save_food.activity_form_view_bool;
import com.example.save_food.models.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private Context context;
    private List<Request> requestList;

    public RequestsAdapter(Context context, List<Request> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestList.get(position);
        try {
            // Parse dữ liệu JSON từ request.getMessage()
            JSONObject productObj = new JSONObject(request.getMessage());
            String prodName = productObj.optString("productName", "N/A");
            String prodInfo = productObj.optString("productInfo", "N/A");
            String prodInfo_more = productObj.optString("productInfo_more", "N/A");
            String prodImage = productObj.optString("productImage", "");

            // Cập nhật giao diện item
            holder.tvProductName.setText(prodName);
            holder.tvStatus.setText("Đang chờ phản hồi");

            // Khi click vào toàn bộ item, chuyển sang activity_form_view và truyền dữ liệu sản phẩm
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, activity_form_view.class);
                intent.putExtra("productName", prodName);
                intent.putExtra("productInfo", prodInfo);
                intent.putExtra("productInfo_more", prodInfo_more);
                intent.putExtra("productImage", prodImage);
                intent.putExtra("UID_sender", request.getSender());
                context.startActivity(intent);
            });
        } catch (JSONException e) {
            e.printStackTrace();
            holder.tvProductName.setText("N/A");
            holder.tvStatus.setText("Đang chờ phản hồi");
        }

        // Nếu bạn có nút riêng "Xem thêm" để chuyển sang trang cá nhân của người đăng bài, ví dụ:
        holder.btXemThem.setOnClickListener(v -> {
            Intent intent = new Intent(context, Profile_Personal_Activity.class);
            intent.putExtra("USER_ID", request.getReceiver());
            context.startActivity(intent);
        });
    }





    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvStatus;
        Button btXemThem;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btXemThem = itemView.findViewById(R.id.btnXemThem);
        }
    }
}