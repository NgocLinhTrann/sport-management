package com.example.dangki.KhachHang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.Model.ThongTinDatLichModel;
import com.example.dangki.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

public class ThongTinDatLichAdapter extends RecyclerView.Adapter<ThongTinDatLichAdapter.ViewHolder> {
    List<ThongTinDatLichModel> thongTinDatLichModelList;
    private OnItemClickListener listener;

    public ThongTinDatLichAdapter(List<ThongTinDatLichModel> thongTinDatLichModelList) {
        this.thongTinDatLichModelList = thongTinDatLichModelList;
    }


    public List<ThongTinDatLichModel> getThongTinDatLichList() {
        return thongTinDatLichModelList;
    }

    public void setThongTinDatLichList(List<ThongTinDatLichModel> thongTinDatLichModelList) {
        this.thongTinDatLichModelList = thongTinDatLichModelList;
        notifyDataSetChanged();
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.khachhang_lichsu_item,
                parent, false);
        return new ThongTinDatLichAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThongTinDatLichModel thongTinDatLichModel = thongTinDatLichModelList.get(position);
        holder.bind(thongTinDatLichModel);
    }

    @Override
    public int getItemCount() {
        return thongTinDatLichModelList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imv_anhsan;
        TextView tv_giochoi, tv_trangthai, tv_tensan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imv_anhsan = itemView.findViewById(R.id.imv_khachhang_lichsu_item_anhsan);
            tv_giochoi = itemView.findViewById(R.id.tv_khachhang_lichsu_item_giochoi);
            tv_trangthai = itemView.findViewById(R.id.tv_khachhang_lichsu_item_trangthai);
            tv_tensan = itemView.findViewById(R.id.tv_khachang_lichsu_item_tensan);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(thongTinDatLichModelList.get(position));
                    }
                }
            });

        }
        public void bind(ThongTinDatLichModel thongTinDatLichModel){
            tv_tensan.setText(thongTinDatLichModel.getName());

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String formattedStartDate = timeFormat.format(thongTinDatLichModel.getStart_time());
            String formattedEndDate = timeFormat.format(thongTinDatLichModel.getEnd_time());
            String dateString = dateFormat.format(thongTinDatLichModel.getEnd_time());

            String gioChoi = formattedStartDate + " - " + formattedEndDate + " " + dateString;

            tv_giochoi.setText(gioChoi);

            tv_trangthai.setText(thongTinDatLichModel.getStatus());

            if(!thongTinDatLichModel.getImg_url().isEmpty()){
                Picasso.get().load(thongTinDatLichModel.getImg_url()).into(imv_anhsan);
            }
        }
    }
    public interface OnItemClickListener {
        void onItemClick(ThongTinDatLichModel thongTinDatLichModel);
    }
}
