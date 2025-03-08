package com.example.save_food.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.save_food.R;
import com.example.save_food.chat;
import com.example.save_food.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.Myholder> {

	Context context;
	FirebaseAuth firebaseAuth;
	DatabaseReference usersRef, chatsRef;
	String uid;
	List<ModelUser> usersList;
	private final HashMap<String, String> lastMessageMap;

	public AdapterChatList(Context context, List<ModelUser> users) {
		this.context = context;
		this.usersList = users;
		this.lastMessageMap = new HashMap<>();

		firebaseAuth = FirebaseAuth.getInstance();
		uid = firebaseAuth.getUid();

		// Lấy tham chiếu tới node "Chats" trên Firebase
		chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
		loadLastMessages();
	}

	// Phương thức lắng nghe tin nhắn và cập nhật lastMessageMap
	private void loadLastMessages() {
		chatsRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				// Xóa dữ liệu cũ trước khi cập nhật
				lastMessageMap.clear();
				for (DataSnapshot ds : snapshot.getChildren()) {
					// Giả sử mỗi tin nhắn có các trường: "sender", "receiver", "message"
					String sender = ds.child("sender").getValue(String.class);
					String receiver = ds.child("receiver").getValue(String.class);
					String message = ds.child("message").getValue(String.class);

					// Cập nhật tin nhắn cuối nếu người dùng hiện tại liên quan đến cuộc trò chuyện
					if (sender != null && receiver != null && message != null) {
						if (sender.equals(uid)) {
							// Nếu bạn là người gửi, hiển thị tin nhắn cuối cho người nhận
							lastMessageMap.put(receiver, message);
						} else if (receiver.equals(uid)) {
							// Nếu bạn là người nhận, hiển thị tin nhắn cuối cho người gửi
							lastMessageMap.put(sender, message);
						}
					}
				}
				// Cập nhật giao diện sau khi có dữ liệu mới
				notifyDataSetChanged();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) { }
		});
	}

	@NonNull
	@Override
	public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
		return new Myholder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull Myholder holder, final int position) {
		final String hisuid = usersList.get(position).getUid();
		String strImage = usersList.get(position).getImage();
		String strName = usersList.get(position).getName();
		String lastmess = lastMessageMap.get(hisuid);

		// Kiểm tra nếu lastmess là JSON của sản phẩm
		if(lastmess != null && lastmess.trim().startsWith("{")){
			try {
				org.json.JSONObject jsonObject = new org.json.JSONObject(lastmess);
				if(jsonObject.has("productName")) {
					// Nếu là tin nhắn sản phẩm, hiển thị "Thông tin sản phẩm"
					lastmess = "Thông tin sản phẩm";
				}
			} catch (org.json.JSONException e) {
				e.printStackTrace();
			}
		}

		holder.name.setText(strName);
		if(usersList.get(position).getOnlineStatus().equals("online")){
			holder.status.setImageResource(R.drawable.circle_online);
		} else {
			holder.status.setImageResource(R.drawable.circle_offline);
		}
		// Nếu không có tin nhắn cuối thì ẩn view lastmessage
		if (lastmess == null || lastmess.equals("default")) {
			holder.lastmessage.setVisibility(View.GONE);
		} else {
			holder.lastmessage.setVisibility(View.VISIBLE);
			holder.lastmessage.setText(lastmess);
		}
		try {
			// Tải ảnh đại diện
			Glide.with(context).load(strImage).placeholder(R.drawable.person).into(holder.profile);
		} catch (Exception e) {
			Glide.with(context).load(R.drawable.person).into(holder.profile);
		}
		// Chuyển sang activity chat khi click vào mục chat
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, chat.class);
				intent.putExtra("hisUid", hisuid);
				context.startActivity(intent);
			}
		});
	}


	// Hàm cập nhật lastMessageMap từ bên ngoài (nếu cần)
	public void setlastMessageMap(String userId, String lastmessage) {
		lastMessageMap.put(userId, lastmessage);
	}

	@Override
	public int getItemCount() {
		return usersList.size();
	}

	static class Myholder extends RecyclerView.ViewHolder {
		CircleImageView profile;
		ImageView status, seen;
		TextView name, lastmessage;

		public Myholder(@NonNull View itemView) {
			super(itemView);
			profile = itemView.findViewById(R.id.profileimage);
			status = itemView.findViewById(R.id.onlinestatus);
			name = itemView.findViewById(R.id.nameonline);
			lastmessage = itemView.findViewById(R.id.lastmessge);
			seen = itemView.findViewById(R.id.seen);
		}
	}
}
