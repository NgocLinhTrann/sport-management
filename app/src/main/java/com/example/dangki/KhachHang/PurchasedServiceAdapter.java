package com.example.dangki.KhachHang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Model.PurchasedService;
import com.example.dangki.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PurchasedServiceAdapter extends RecyclerView.Adapter<PurchasedServiceAdapter.ViewHolder> {
    public interface OnDeleteItemClickListener {
        void onDeleteItem(PurchasedService purchasedService);
    }
    private List<PurchasedService> purchasedServiceList;
    String status="";
    private OnDeleteItemClickListener listener;
    public void setOnDeleteItemClickListener(OnDeleteItemClickListener listener) {
        this.listener = listener;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PurchasedServiceAdapter(List<PurchasedService> purchasedServiceList) {
        this.purchasedServiceList = purchasedServiceList;
    }

    public List<PurchasedService> getPurchasedServiceList() {
        return purchasedServiceList;
    }

    public void setPurchasedServiceList(List<PurchasedService> purchasedServiceList) {
        this.purchasedServiceList = purchasedServiceList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.khachhang_thanhtoan_item1
                                        , parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchasedService purchasedService = purchasedServiceList.get(position);
        holder.bindData(purchasedService);
    }
    @Override
    public int getItemCount() {
        return purchasedServiceList.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_tenDichVu;
        private TextView tv_sl_gioChoi;
        private TextView tv_giaTien;
        ImageView imv_hinhAnh, btn_xoa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_tenDichVu = itemView.findViewById(R.id.tv_khachhang_thanhtoan_tendouongSan);
            tv_sl_gioChoi = itemView.findViewById(R.id.tv_khachhang_thanhtoan_slGiochoi);
            tv_giaTien = itemView.findViewById(R.id.tv_khachhang_thanhtoan_giatien);
            imv_hinhAnh = itemView.findViewById(R.id.imv_khachang_thanhtoan_anh);
            btn_xoa =itemView.findViewById(R.id.btn_khachhang_thanhtoan_xoa);

            btn_xoa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Gọi phương thức xử lý sự kiện xóa item
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            PurchasedService purchasedService = purchasedServiceList.get(position);
                            listener.onDeleteItem(purchasedService);
                        }
                    }
                }
            });
        }

        public void bindData(PurchasedService purchasedService) {
            tv_tenDichVu.setText(purchasedService.getName());
            double tinhTien =0.0;
            if(purchasedService.getType().equals("Stadium")){
                tinhTien = (double) purchasedService.getQuantity()/60 * purchasedService.getPrice();
                tv_sl_gioChoi.setText(purchasedService.getQuantity()+ " phút");
            }
            else{
                tinhTien = purchasedService.getQuantity() * purchasedService.getPrice();
                tv_sl_gioChoi.setText("SL: "+purchasedService.getQuantity());
            }

            if(status.equals("Done")){
                btn_xoa.setVisibility(View.GONE);
            }
            tv_giaTien.setText(String.valueOf(tinhTien) + "VND");

            Picasso.get().load(purchasedService.getImg_url()).into(imv_hinhAnh);
        }
    }
}

