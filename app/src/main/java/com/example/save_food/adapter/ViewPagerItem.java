package com.example.save_food.adapter;

public class ViewPagerItem {
    String ImgaeId, uid, pLikes;
    String Heding, Heding2, Heding3;

    public ViewPagerItem(String imgaeId, String heding, String heding2, String heding3,String uid) {
        ImgaeId = imgaeId;
        Heding = heding;
        Heding2 = heding2;
        Heding3 = heding3;
        this.uid = uid;
    }

    public String getHeding() {
        return Heding;
    }


    public String getHeding2() {
        return Heding2;
    }
    public String getHeding3() {
        return Heding3;
    }

    public String getImgaeId() {
        return ImgaeId;
    }
    public String getUid(){return uid;}
}
