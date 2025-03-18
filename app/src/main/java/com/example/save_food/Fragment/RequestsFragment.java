package com.example.save_food.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.save_food.Profile_Personal_Activity;
import com.example.save_food.R;
import com.example.save_food.activity_form_view_bool;
import com.example.save_food.adapter.RequestsAdapter;
import com.example.save_food.models.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestsFragment extends Fragment {

    private RecyclerView rvRequests;
    private ArrayList<Request> requestList;

    private RequestsAdapter adapter;
    private DatabaseReference chatRef;
    private String currentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        rvRequests = view.findViewById(R.id.rvRequests);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        rvRequests.setLayoutManager(new LinearLayoutManager(getActivity()));
        requestList = new ArrayList<>();
        adapter = new RequestsAdapter(getActivity(), requestList);
        rvRequests.setAdapter(adapter);

        loadRequests();

        return view;
    }

    private void loadRequests() {
        chatRef.orderByChild("sender").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    if (status == null || status.equals("pending")) {
                        Request request = ds.getValue(Request.class);
                        if (request != null) {
                            request.setRequestId(ds.getKey());
                            if (!isRequestExists(requestList, request.getRequestId())) {
                                requestList.add(request); // Chỉ thêm nếu chưa tồn tại
                            }
                        }
                    }
                }
                Collections.sort(requestList, (r1, r2) -> r2.getRequestId().compareTo(r1.getRequestId()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    // Hàm kiểm tra trùng lặp
    private boolean isRequestExists(List<Request> list, String requestId) {
        for (Request req : list) {
            if (req.getRequestId().equals(requestId)) {
                return true;
            }
        }
        return false;
    }

}