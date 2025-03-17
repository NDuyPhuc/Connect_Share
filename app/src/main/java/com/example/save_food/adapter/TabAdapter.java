package com.example.save_food.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.save_food.Fragment.PostsFragment;
import com.example.save_food.Fragment.RequestsFragment;
import com.example.save_food.Fragment.ResponsesFragment;

public class TabAdapter extends FragmentStateAdapter {

    public TabAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PostsFragment();
            case 1:
                return new RequestsFragment(); // Fragment cho "Yêu cầu nhận sản phẩm"
            case 2:
                return new ResponsesFragment(); // Fragment cho "Kết quả phản hồi"
            default:
                return new PostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Số lượng tab
    }
}