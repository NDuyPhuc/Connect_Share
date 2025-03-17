package com.example.save_food.models;

import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ThongTin_UpLoadClass {
    private String title;
    private String imageLink;
    private String status;
    private String acceptedUserId; // Thêm trường này để lưu acceptedUserId

    private String tenDonHang;
    private String diaChi;
    private String nganhHang;
    private String thongTinChiTiet;
    private String donViHetHan;
    private String uid;
    private List<DataSnapshot> imageSnapshots = new ArrayList<>();

    // Thêm thuộc tính postId để lưu key của bài đăng
    private String postId;

    // Constructor mặc định (cần thiết cho Firebase)
    public ThongTin_UpLoadClass() {
    }

    public ThongTin_UpLoadClass(String tenDonHang, String diaChi, String nganhHang, String thongTinChiTiet) {
        this.tenDonHang = tenDonHang;
        this.diaChi = diaChi;
        this.nganhHang = nganhHang;
        this.thongTinChiTiet = thongTinChiTiet;
    }

    // Các getter và setter khác ...

    public String getAcceptedUserId() {
        return acceptedUserId;
    }

    public void setAcceptedUserId(String acceptedUserId) {
        this.acceptedUserId = acceptedUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getThongTinChiTiet() {
        return thongTinChiTiet;
    }

    public void setThongTinChiTiet(String thongTinChiTiet) {
        this.thongTinChiTiet = thongTinChiTiet;
    }

    public List<DataSnapshot> getImageSnapshots() {
        return imageSnapshots;
    }

    public void setImageSnapshots(List<DataSnapshot> snapshots) {
        this.imageSnapshots = snapshots;
    }

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
                ", thongTinChiTiet='" + thongTinChiTiet + '\'' +
                ", donViHetHan='" + donViHetHan + '\'' +
                ", status='" + status + '\'' +
                ", acceptedUserId='" + acceptedUserId + '\'' +
                '}';
    }
}
