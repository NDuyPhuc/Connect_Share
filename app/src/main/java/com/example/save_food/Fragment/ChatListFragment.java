package com.example.save_food.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save_food.R;
import com.example.save_food.adapter.AdapterChatList;
import com.example.save_food.models.ModelChat;
import com.example.save_food.models.ModelChatList;
import com.example.save_food.models.ModelUser;
import com.example.save_food.adapter.AdapterChatList;
import com.example.save_food.models.ModelChat;
import com.example.save_food.models.ModelChatList;
import com.example.save_food.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatList> chatListList;
    List<ModelUser> usersList;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    AdapterChatList adapterChatList;
    List<ModelChat> chatList;
    private HashMap<String, Long> lastMessageTimestampMap = new HashMap<>();


    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container,false);
        firebaseAuth = FirebaseAuth.getInstance();
        // getting current user

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.chatlistrecycle);
        chatListList = new ArrayList<>();
        chatList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatListList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChatList modelChatList = ds.getValue(ModelChatList.class);
                    if (!modelChatList.getId().equals(firebaseUser.getUid())) {
                        chatListList.add(modelChatList);
                    }
                }
                loadChats();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        return view;

    }

    // loading the user chat layout using chat node
    private void loadChats() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelUser user = dataSnapshot1.getValue(ModelUser.class);
                    for (ModelChatList chatList : chatListList) {
                        if (user != null && user.getUid() != null && user.getUid().equals(chatList.getId())) {
                            usersList.add(user);
                            break;
                        }
                    }
                }
                // Cập nhật adapter với usersList đã được lấy
                adapterChatList = new AdapterChatList(getActivity(), usersList);
                recyclerView.setAdapter(adapterChatList);
                // Sau đó, load tất cả tin nhắn và sắp xếp danh sách theo thời gian tin nhắn mới nhất
                loadLastMessagesAndSort();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadLastMessagesAndSort() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        // Sử dụng listener một lần vì bạn muốn cập nhật lại toàn bộ dữ liệu và sắp xếp
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Xóa dữ liệu cũ
                lastMessageTimestampMap.clear();
                // Với mỗi người dùng trong usersList, tìm tin nhắn mới nhất
                for (ModelUser user : usersList) {
                    long maxTimestamp = 0;
                    String lastMess = "default";
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ModelChat chat = ds.getValue(ModelChat.class);
                        if (chat == null) continue;
                        // Kiểm tra điều kiện đối thoại giữa người dùng hiện tại và user
                        if ((chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(user.getUid()))
                                || (chat.getReceiver().equals(user.getUid()) && chat.getSender().equals(firebaseUser.getUid()))) {
                            try {
                                long chatTimestamp = Long.parseLong(chat.getTimestamp());
                                if (chatTimestamp > maxTimestamp) {
                                    maxTimestamp = chatTimestamp;
                                    if (chat.getType().equals("images")) {
                                        lastMess = "Đã gửi ảnh";
                                    } else if (chat.getType().equals("product")) {
                                        lastMess = "Thông tin sản phẩm";
                                    } else {
                                        lastMess = chat.getMessage();
                                    }
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // Lưu last message timestamp cho user
                    lastMessageTimestampMap.put(user.getUid(), maxTimestamp);
                    // Cập nhật last message text cho user trong adapter
                    adapterChatList.setlastMessageMap(user.getUid(), lastMess);
                }
                // Sau khi duyệt hết tin nhắn, sắp xếp usersList theo thứ tự giảm dần của timestamp
                java.util.Collections.sort(usersList, (u1, u2) -> {
                    long t1 = lastMessageTimestampMap.containsKey(u1.getUid()) ? lastMessageTimestampMap.get(u1.getUid()) : 0;
                    long t2 = lastMessageTimestampMap.containsKey(u2.getUid()) ? lastMessageTimestampMap.get(u2.getUid()) : 0;
                    // So sánh giảm dần: người có timestamp cao hơn (mới nhất) sẽ đứng đầu
                    return Long.compare(t2, t1);
                });
                adapterChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

}

