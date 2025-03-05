package com.example.save_food.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyClusterItem implements ClusterItem {

    private final LatLng position;
    private final String imageUrl;

    public MyClusterItem(LatLng position, String imageUrl) {
        this.position = position;
        this.imageUrl = imageUrl;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Nếu cần hiển thị title hay snippet, bạn có thể bổ sung
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
