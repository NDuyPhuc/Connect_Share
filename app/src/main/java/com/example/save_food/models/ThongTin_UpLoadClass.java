package com.example.save_food.models;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ThongTin_UpLoadClass {
    private String tenDonHang;
    private String diaChi;
    private String nganhHang;
    private String thoiGianHetHan;
    private String donViHetHan;
    private String uid;
    private List<DataSnapshot> imageSnapshots = new ArrayList<>();

    // Thêm thuộc tính postId để lưu key của bài đăng
    private String postId;

    public ThongTin_UpLoadClass() {
    }

    public ThongTin_UpLoadClass(String tenDonHang, String diaChi, String nganhHang, String thoiGianHetHan, String donViHetHan) {
        this.tenDonHang = tenDonHang;
        this.diaChi = diaChi;
        this.nganhHang = nganhHang;
        this.thoiGianHetHan = thoiGianHetHan;
        this.donViHetHan = donViHetHan;
    }

    public String getDonViHetHan() {
        return donViHetHan;
    }

    public void setDonViHetHan(String donViHetHan) {
        this.donViHetHan = donViHetHan;
    }

    public String getTenDonHang() {
        return tenDonHang;
    }

    public void setTenDonHang(String tenDonHang) {
        this.tenDonHang = tenDonHang;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getNganhHang() {
        return nganhHang;
    }

    public void setNganhHang(String nganhHang) {
        this.nganhHang = nganhHang;
    }

    public String getThoiGianHetHan() {
        return thoiGianHetHan;
    }

    public void setThoiGianHetHan(String thoiGianHetHan) {
        this.thoiGianHetHan = thoiGianHetHan;
    }

    public List<DataSnapshot> getImageSnapshots() {
        return imageSnapshots;
    }

    public void setImageSnapshots(List<DataSnapshot> snapshots) {
        this.imageSnapshots = snapshots;
    }

    // Getter và Setter cho postId
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String toStringg() {
        return "ThongTin_UpLoadClass{" +
                "tenDonHang='" + tenDonHang + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", nganhHang='" + nganhHang + '\'' +
                ", thoiGianHetHan='" + thoiGianHetHan + '\'' +
                ", donViHetHan='" + donViHetHan + '\'' +
                '}';
    }
}
