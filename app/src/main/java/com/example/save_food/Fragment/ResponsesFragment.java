package com.example.save_food.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save_food.R;
import com.example.save_food.adapter.ResponsesAdapter;
import com.example.save_food.models.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class ResponsesFragment extends Fragment {

    private RecyclerView rvResponses;
    private ArrayList<Request> responseList;
    private ResponsesAdapter adapter;
    private DatabaseReference chatRef;
    private String currentUserId;

    public ResponsesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_responses, container, false);

        rvResponses = view.findViewById(R.id.rvResponses);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        rvResponses.setLayoutManager(new LinearLayoutManager(getActivity()));
        responseList = new ArrayList<>();
        adapter = new ResponsesAdapter(getActivity(), responseList);
        rvResponses.setAdapter(adapter);

        loadResponses();

        return view;
    }

    private void loadResponses() {
        chatRef.orderByChild("sender").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                responseList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    if (status != null && (status.equals("accepted") || status.equals("rejected"))) {
                        Request request = ds.getValue(Request.class);
                        if (request != null) {
                            request.setRequestId(ds.getKey());
                            responseList.add(request);
                        }
                    }
                }
                Collections.sort(responseList, (r1, r2) -> r2.getRequestId().compareTo(r1.getRequestId()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}