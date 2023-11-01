package com.example.dangki.Admin.DatLich;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangki.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DatLichAdapter extends RecyclerView.Adapter<DatLichAdapter.ViewHolder> {
    List<DatLichModel> datLichModelList;
    private OnItemClickListener listener;


    public DatLichAdapter(List<DatLichModel> datLichModelList){
        this.datLichModelList = datLichModelList;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_datlich_item, parent, false);
        return new DatLichAdapter.ViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull DatLichAdapter.ViewHolder holder, int position) {
        DatLichModel datLichModel = datLichModelList.get(position);
        holder.bind(datLichModel);
    }

    @Override
    public int getItemCount() {
        return datLichModelList.size();
    }

    public void setDatLichModelList(List<DatLichModel> datLichModelList){
        this.datLichModelList =datLichModelList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imv_anhsan;
        TextView tv_tensan,tv_username, tv_giochoi;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imv_anhsan = itemView.findViewById(R.id.imv_admin_datlich_anhsan);
            tv_tensan = itemView.findViewById(R.id.tv_admin_datlich_tensan);
            tv_username = itemView.findViewById(R.id.tv_admin_datlich_fullname);
            tv_giochoi = itemView.findViewById(R.id.tv_admin_datlich_giochoi);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(datLichModelList.get(position));
                    }
                }
            });
        }

        public void bind(DatLichModel datLichModel){
            tv_tensan.setText(datLichModel.getTenSan());

            Date start_time = datLichModel.getStart_time();
            Date end_time = datLichModel.getEnd_time();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            String startTimeString = timeFormat.format(start_time);
            String endTimeString = timeFormat.format(end_time);
            String dateString = dateFormat.format(end_time);
            String gioChoi = startTimeString + " - " + endTimeString + " " + dateString;

            tv_giochoi.setText(gioChoi);
            tv_username.setText(datLichModel.getUserName());
            Picasso.get().load(datLichModel.getImg_url_san()).into(imv_anhsan);
        }
    }
    public List<DatLichModel> getDatLichModelList() {
        return datLichModelList;
    }
    public interface OnItemClickListener {
        void onItemClick(DatLichModel datLichModel);
    }

}
