package com.example.save_food.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.save_food.models.CategoryItem;
import com.example.save_food.R;
import java.util.List;

public class CategoryAdapter extends ArrayAdapter<CategoryItem> {
    private Context context;
    private List<CategoryItem> categoryItems;

    public CategoryAdapter(@NonNull Context context, @NonNull List<CategoryItem> objects) {
        super(context, 0, objects);
        this.context = context;
        this.categoryItems = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false);
        }
        TextView tvCategory = convertView.findViewById(R.id.tvCategory);
        CategoryItem item = categoryItems.get(position);
        tvCategory.setText(item.getName() + " (" + item.getCount() + ")");
        return convertView;
    }
}
