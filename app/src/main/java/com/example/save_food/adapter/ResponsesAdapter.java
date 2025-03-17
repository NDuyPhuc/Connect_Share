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
    import com.example.save_food.models.Request;

    import org.json.JSONException;
    import org.json.JSONObject;
    import java.util.List;

    public class ResponsesAdapter extends RecyclerView.Adapter<ResponsesAdapter.ResponseViewHolder> {

        private Context context;
        private List<Request> responseList;

        public ResponsesAdapter(Context context, List<Request> responseList) {
            this.context = context;
            this.responseList = responseList;
        }

        @NonNull
        @Override
        public ResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_response, parent, false);
            return new ResponseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ResponseViewHolder holder, int position) {
            Request request = responseList.get(position);
            try {
                // Parse dữ liệu JSON từ request.getMessage()
                JSONObject productObj = new JSONObject(request.getMessage());
                String prodName = productObj.optString("productName", "N/A");
                String prodInfo = productObj.optString("productInfo", "N/A");
                String prodInfo_more = productObj.optString("productInfo_more", "N/A");
                String prodImage = productObj.optString("productImage", "");

                // Cập nhật giao diện item
                holder.tvProductName.setText(prodName);

                // Kiểm tra và cập nhật trạng thái cùng màu sắc
                if(request.getStatus() != null) {
                    if(request.getStatus().equals("accepted")) {
                        holder.tvStatus.setText("Chấp nhận");
                        // Màu xanh cho trạng thái "Chấp nhận"
                        holder.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_blue_dark));
                    } else if(request.getStatus().equals("rejected")) {
                        holder.tvStatus.setText("Từ chối");
                        // Màu đỏ cho trạng thái "Từ chối"
                        holder.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    } else {
                        holder.tvStatus.setText("Đang chờ phản hồi");
                        // Màu xanh lá cho trạng thái mặc định
                        holder.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_green_dark));
                    }
                } else {
                    holder.tvStatus.setText("Đang chờ phản hồi");
                    holder.tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, android.R.color.holo_green_dark));
                }

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
                holder.tvStatus.setText("Lỗi dữ liệu");
            }

            // Nếu có nút "Xem thêm", chuyển sang trang cá nhân của người đăng bài (người nhận)
            holder.btXemThem.setOnClickListener(v -> {
                Intent intent = new Intent(context, Profile_Personal_Activity.class);
                intent.putExtra("USER_ID", request.getReceiver());
                context.startActivity(intent);
            });
        }




        @Override
        public int getItemCount() {
            return responseList.size();
        }

        public static class ResponseViewHolder extends RecyclerView.ViewHolder {
            TextView tvProductName, tvStatus;
            Button btXemThem;

            public ResponseViewHolder(@NonNull View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btXemThem = itemView.findViewById(R.id.btnXemThem);
            }
        }
    }